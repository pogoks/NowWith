package com.example.nowwith;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// 2021531003 김범준 11/19 4차
// 2021531003 김범준 11/29 5차 : ChatJoin 메소드를 오버로딩을 이용해 공개방, 비공개방 작동 분리
//                              onCreate 부분에서 암호를 넘겨받았는지에 대한 체크를 통해 공개방, 비공개방 리스트를 보여준다.
//                              메세지 전송을 할 때도 암호를 넘겨받았는지에 대한 체크를 통해 Firebase의 chat디렉토리 / private디렉토리를 구분해서 값을 넣는다.

public class Chat_Room extends AppCompatActivity {

    EditText edt_message;
    Button btn_send;
    ListView chat_message;

    SimpleDateFormat nowDate;

    // Firebase에서 작성자 정보와 메세지 정보를 받아와 어댑터에 넘겨줄 ArrayList
    ArrayList<Chat_ItemList> data;

    // 연동되어있는 Firebase를 불러온다.
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    // Firebase의 루트 디렉토리("/")를 불러온다.
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    String title, password, writer, creater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        edt_message = (EditText) findViewById(R.id.edt_message);
        btn_send = (Button) findViewById(R.id.btn_send);
        chat_message = (ListView) findViewById(R.id.chat_message);

        // data ArrayList를 Chat_ItemList 형식으로 지정해준다.
        data = new ArrayList<Chat_ItemList>();

        // 현재 날짜를 구하기 위한 부분
        // 현재 시간을 구해서 now 변수에 넣는다.
        long now = System.currentTimeMillis();
        // now 변수를 Date 형식으로 변환한다.
        Date date = new Date(now);
        // SimpleDateFormat으로 00-00 00:00 형태로 변환하고 저장한다.
        nowDate = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA);
        String getTime = nowDate.format(date);

        // Chat 액티비티에서 넘겨준 값들을 받는다.
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        password = intent.getStringExtra("password");
        writer = intent.getStringExtra("writer");
        creater = intent.getStringExtra("creater");

        // Firebase에 저장되어있는 메세지 정보들을 가져오는 메소드
        // 방 암호 여부를 체크하는 조건문
        // 방 암호가 없으면 ChatJoin(), 방 암호가 있다면 ChatJoinPrivate()
        if(password == null) {
            ChatJoin(title);
        }
        else {
            ChatJoinPrivate(title);
        }

        // 전송 버튼 클릭 이벤트
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password == null) {
                    // Chat_ItemList 모델 클래스에 작성자와 입력한 메세지, 보낸 시간, 방 개설자 값을 넘겨준다.
                    Chat_ItemList chat = new Chat_ItemList(writer, edt_message.getText().toString(), getTime, creater);
                    // firebase의 chat 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
                    // title 하위에 push() 메소드를 이용해 랜덤 키를 생성하고 랜덤 키 하위에 값들을 넣어준다.
                    databaseReference.child("chat").child(title).push().setValue(chat);
                    // 전송이 완료되면 입력 내용을 비워준다.
                    edt_message.setText("");
                }
                else {
                    // Chat_ItemList 모델 클래스에 작성자와 입력한 메세지, 보낸 시간, 방 암호, 방 개설자 값을 넘겨준다.
                    Chat_ItemList chat = new Chat_ItemList(writer, edt_message.getText().toString(), getTime, password, creater);
                    // firebase의 private 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
                    // title 하위에 push() 메소드를 이용해 랜덤 키를 생성하고 랜덤 키 하위에 값들을 넣어준다.
                    databaseReference.child("private").child(title).push().setValue(chat);
                    // 전송이 완료되면 입력 내용을 비워준다.
                    edt_message.setText("");
                }

            }
        });
    }

    // 공개방 Firebase에 저장되어있는 메세지 정보들을 가져오는 메소드
    private void ChatJoin(String title) {
        // firebase의 chat 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
        // addChildEventListener는 하위 디렉토리에 이벤트가 발생했을 경우 실행된다.
        databaseReference.child("chat").child(title).addChildEventListener(new ChildEventListener() {
            // onChildAdded는 자료를 검색하거나 추가할 때 실행된다.
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // snapshot으로 가져온 값들을 Chat_ListItem 모델 클래스 형식으로 변환한다.
                Chat_ItemList chat = snapshot.getValue(Chat_ItemList.class);
                // Chat_ItemList 모델 클래스에서 getWriter와 getMessage, getTime을 통해 넘겨받은 값들을 가져와 data ArrayList에 넣어준다.
                // 즉, 메시지 작성자, 메시지 내용, 메시지 보낸 시간을 가져와 ArrayList에 추가해준다.
                data.add(new Chat_ItemList(chat.getWriter(), chat.getMessage(), chat.getTime()));

                // 어댑터 생성, 만들어 둔 Chat_ListAdapter 커스텀 리스트 뷰 어댑터를 사용한다.
                // 매개변수로 현재 context, data ArrayList, 작성자 ID를 넘겨준다.
                Chat_ListAdapter adapter = new Chat_ListAdapter(Chat_Room.this, data, writer);
                // 만든 어댑터를 리스트뷰에 연결해준다.
                chat_message.setAdapter(adapter);
                // 새로운 메세지가 뜰 때 마다 맨 아래로 자동으로 이동된다.
                chat_message.setSelection(adapter.getCount() - 1);
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
    
    // 비공개방 Firebase에 저장되어있는 메세지 정보들을 가져오는 메소드
    private void ChatJoinPrivate(String title) {
        // firebase의 private 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
        // addChildEventListener는 하위 디렉토리에 이벤트가 발생했을 경우 실행된다.
        databaseReference.child("private").child(title).addChildEventListener(new ChildEventListener() {
            // 하위에 무언가 추가될 때 실행되는 구간
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // snapshot으로 가져온 값들을 Chat_ListItem 모델 클래스 형식으로 변환한다.
                Chat_ItemList chat = snapshot.getValue(Chat_ItemList.class);
                // Chat_ItemList 모델 클래스에서 getWriter와 getMessage, getTime을 통해 넘겨받은 값들을 가져와 data ArrayList에 넣어준다.
                // 즉, 메시지 작성자, 메시지 내용, 메시지 보낸 시간을 가져와 ArrayList에 추가해준다.
                data.add(new Chat_ItemList(chat.getWriter(), chat.getMessage(), chat.getTime()));

                // 어댑터 생성, 만들어 둔 Chat_ListAdapter 커스텀 리스트 뷰 어댑터를 사용한다.
                // 매개변수로 현재 context, data ArrayList, 작성자 ID를 넘겨준다.
                Chat_ListAdapter adapter = new Chat_ListAdapter(Chat_Room.this, data, writer);
                // 만든 어댑터를 리스트뷰에 연결해준다.
                chat_message.setAdapter(adapter);
                // 새로운 메세지가 뜰 때 마다 맨 아래로 자동으로 이동된다.
                chat_message.setSelection(adapter.getCount() - 1);
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
