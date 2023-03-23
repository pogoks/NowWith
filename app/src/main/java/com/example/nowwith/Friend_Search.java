package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// 2021531003 김범준 12/3 6차

public class Friend_Search extends AppCompatActivity {

    ImageView img_friend;
    TextView tv_friend_id, tv_friend_email, tv_friend_hp, tv_friend_back;
    Button btn_friend_add;

    // 연동되어있는 Firebase를 불러온다.
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    // Firebase의 루트 디렉토리("/")를 불러온다.
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    // 친구의 정보를 담는 변수
    String friendID, friendEmail, friendGender, friendHP, nowID;

    // 현재 사용자의 친구 ID들을 저장하는 ArrayList
    ArrayList<String> arrayList;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_search);

        img_friend = (ImageView) findViewById(R.id.img_friend);
        tv_friend_id = (TextView) findViewById(R.id.tv_friend_id);
        tv_friend_email = (TextView) findViewById(R.id.tv_friend_email);
        tv_friend_hp = (TextView) findViewById(R.id.tv_friend_hp);
        tv_friend_back = (TextView) findViewById(R.id.tv_friend_back);
        btn_friend_add = (Button) findViewById(R.id.btn_friend_add);

        arrayList = new ArrayList<>();

        // 현재 로그인 된 ID를 nowID 변수에 넣어준다.
        nowID = Login_Info.getInstance().getID();

        // Friend.java에서 검색한 사용자의 정보를 가져와 각 변수에 넣어준다.
        friendID = Friend_ItemList.getInstance().getFriendID();
        friendEmail = Friend_ItemList.getInstance().getFriendEmail();
        friendGender = Friend_ItemList.getInstance().getFriendGender();
        friendHP = Friend_ItemList.getInstance().getFriendHP();

        // 친구의 성별이 1 또는 3이면, 즉 남성이라면
        if(friendGender.equals("1") || friendGender.equals("3")) {
            // 남자 이미지로 설정
            img_friend.setImageResource(R.drawable.man);
        }
        // 친구의 성별이 2 또는 4면, 즉 여성이라면
        else {
            // 여자 이미지로 설정
            img_friend.setImageResource(R.drawable.lady);
        }

        // 각각 친구의 정보를 TextView안에 넣어준다.
        tv_friend_id.setText(friendID);
        tv_friend_email.setText(friendEmail);
        tv_friend_hp.setText(friendHP);

        // 친구추가 버튼 클릭 이벤트
        btn_friend_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 사용자의 친구 ID값들을 가져오기 위한 firebase 접근
                // firebase의 friend 디렉토리 하위의 nowID(현재 로그인 한 ID) 값의 이름을 가진 디렉토리에 접근한다.
                // addListenerForSingleValueEvent는 호출했을 때 한 번만 실행된다.
                databaseReference.child("friend").child(nowID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // for each 문으로 firebase에 있는 값들을 dataSnapstot에 모두 넣어준다.
                        for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            // 넣어준 datasnapshot의 값을 Friend_ListItem 모델 클래스 형식으로 변환한다.
                            Friend_ItemList data = dataSnapshot.getValue(Friend_ItemList.class);
                            // 친구 ID 값 하나하나 모두 arraylist에 넣어준다. (친구가 3명이면 총 3개의 ID)
                            arrayList.add(data.getFriendID());
                        }

                        // 추가하고자 하는 사용자가 본인이 아니라면
                        if(!nowID.equals(friendID)) {
                            // arraylist 안에 이미 추가하고자 하는 친구ID가 없을 경우
                            if(!arrayList.contains(friendID)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Friend_Search.this);
                                dialog = builder.setMessage("해당 사용자를 친구로 추가하였습니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Friend_ItemList 모델 클래스에 Friend.java에서 검색한 사용자의 정보를 모두 넘겨준다.
                                                Friend_ItemList friend = new Friend_ItemList(friendID, friendGender, friendEmail, friendHP);
                                                // firebase의 friend 디렉토리 하위의 nowID(현재 로그인 한 ID) 값의 이름을 가진 디렉토리에 접근한다.
                                                // nowID 하위에 push() 메소드를 이용해 랜덤 키를 생성하고 랜덤 키 하위에 값들을 넣어준다.
                                                databaseReference.child("friend").child(nowID).child(friendID).setValue(friend);

                                                Intent intent = new Intent(Friend_Search.this, Friend.class);
                                                // 종료하면서 상위 스택 액티비티들을 모두 제거한다.
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .create();
                                dialog.show();
                            }
                            // 이미 추가하고자 있는 친구ID가 있는 경우
                            else {
                                // 이미 친구가 되어있다는 대화상자를 띄워준다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Friend_Search.this);
                                dialog = builder.setMessage("이미 친구입니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();
                            }
                        }
                        // 추가하고자 하는 사용자가 본인이라면
                        else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Friend_Search.this);
                            dialog = builder.setMessage("자신을 친구로 추가할 수 없습니다.")
                                    .setPositiveButton("확인", null)
                                    .create();
                            dialog.show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
        });

        // 목록 텍스트 클릭 이벤트
        tv_friend_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}