package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

// 2021531003 김범준 12/1 6차

public class Friend extends AppCompatActivity {

    ListView friend_list;
    EditText edt_friend_search;
    Button btn_friend_search;

    // 연동되어있는 Firebase를 불러온다.
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    // Firebase의 루트 디렉토리("/")를 불러온다.
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    // 현재 사용자의 친구 정보들을 Friend_ItemList 모델 클래스 형식으로 저장하는 ArrayList
    ArrayList<Friend_ItemList> data;

    // 현재 로그인 중인 ID의 정보를 담는 변수
    String nowID;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend);

        friend_list = (ListView) findViewById(R.id.friend_list);
        edt_friend_search = (EditText) findViewById(R.id.edt_friend_search);
        btn_friend_search = (Button) findViewById(R.id.btn_friend_search);

        data = new ArrayList<Friend_ItemList>();

        nowID = Login_Info.getInstance().getID();

        // 친구 목록을 보여주는 FriendList() 메소드 실행
        FriendList(nowID);

        // 사용자 검색 버튼 클릭 이벤트
        btn_friend_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String friendID = edt_friend_search.getText().toString();

                // Volley 라이브러리의 RequestQueue에 적용할 Response 리스너 생성
                // RequestQueue에서 호출되고 통신에 성공하면 밑 내용을 실행한다.
                Response.Listener<String> resListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // JSONObject를 이용하여 PHP파일이 실행된 결과를 accountInfo에 저장한다.
                            JSONObject friendInfo = new JSONObject(response);

                            // 받아온 결과인 friendInfo 부분 중 $response["success"] 부분을 받아온다.
                            // 데이터베이스에 존재하는 검색하고자 하는 사용자 ID와 EditText에 입력된 값을 비교하여 일치하면 success=true 를 받게된다.
                            boolean success = friendInfo.getBoolean("success");

                            if(success) {       // 받은 $response["success"] 값이 true 라면
                                // 사용자 검색 직후 검색한 사용자 정보를 가져오는 Friend_GetInfo 클래스의 AsyncTask가 실행이 완료되기 전에
                                // 액티비티를 이동하면 값을 얻어오기 전에 이동되어 오류가 생기게 된다.
                                // AsyncTask를 모두 완료하고 화면을 이동하도록 postDelayed 0.1초를 주었다.
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(Friend.this, Friend_Search.class);
                                        startActivity(intent);
                                    }
                                }, 100);
                            }
                            else {              // 받은 $response["success"] 값이 false 라면
                                // 실패했다는 대화상자를 띄운 후 이동하지 않는다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Friend.this);
                                dialog = builder.setMessage("해당하는 사용자가 없습니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                // 검색할 ID와 생성한 Response 리스너를 매개변수로 넘겨준다.
                Friend_CompareInfo compareInfo = new Friend_CompareInfo(friendID, resListener);
                // Volley 라이브러리의 RequestQueue를 생성하고 Response 리스너와 검색할 ID 정보를 큐에 넣고 통신을 시도한다.
                RequestQueue requestQueue = Volley.newRequestQueue(Friend.this);
                requestQueue.add(compareInfo);

                // 사용자 검색 직후 검색한 사용자의 정보를 받아오기 위해 Friend_GetInfo 클래스를 AsyncTask 비동기로 실행한다.
                Friend_GetInfo getInfo = new Friend_GetInfo(Friend.this, friendID);
                // executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) = 병렬 처리, execute = 직렬 처리
                getInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    // 친구 목록을 보여주는 FriendList 메소드
    private void FriendList(String nowID) {
        // firebase의 friend 디렉토리 하위의 nowID(현재 로그인 한 ID) 값의 이름을 가진 디렉토리에 접근한다.
        // addChildEventListener는 하위 디렉토리에 이벤트가 발생했을 경우 실행된다.
        databaseReference.child("friend").child(nowID).addChildEventListener(new ChildEventListener() {
            // onChildAdded는 자료를 검색하거나 추가할 때 실행된다.
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // snapshot으로 가져온 값들을 Friend_ListItem 모델 클래스 형식으로 변환한다.
                Friend_ItemList friend = snapshot.getValue(Friend_ItemList.class);
                // Friend_ItemList 모델 클래스에서 getFriendID와 getFriendGender, getFriendEmail, getFriendHP을 통해 넘겨받은 값들을 가져와 data ArrayList에 넣어준다.
                // 즉, 친구 ID, 친구 성별정보, 친구 이메일 주소, 친구 전화번호를 가져와 ArrayList에 추가해준다.
                data.add(new Friend_ItemList(friend.getFriendID(), friend.getFriendGender(), friend.getFriendEmail(), friend.getFriendHP()));

                // 어댑터 생성, 만들어 둔 Friend_ListAdapter 커스텀 리스트 뷰 어댑터를 사용한다.
                // 매개변수로 현재 context, data ArrayList를 넘겨준다.
                Friend_ListAdapter adapter = new Friend_ListAdapter(Friend.this, data, nowID);
                // 만든 어댑터를 리스트뷰에 연결해준다.
                friend_list.setAdapter(adapter);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}