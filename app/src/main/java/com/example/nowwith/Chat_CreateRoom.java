package com.example.nowwith;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// 2021531003 김범준 11/26 5차

public class Chat_CreateRoom extends AppCompatActivity {

    EditText edt_chat_create;
    Button btn_create;
    TextView tv_chat_ID;

    String title, writer, nowID, creater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_createroom);

        edt_chat_create = (EditText) findViewById(R.id.edt_chat_create);
        btn_create = (Button) findViewById(R.id.btn_create);
        tv_chat_ID = (TextView) findViewById(R.id.tv_chat_ID);

        nowID = Login_Info.getInstance().getID();

        tv_chat_ID.setText(nowID);

        // 개설하기 버튼 클릭 이벤트
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = edt_chat_create.getText().toString();
                writer = Login_Info.getInstance().getID();
                creater = Login_Info.getInstance().getID();

                // 공개방이기 때문에 암호는 따로 전달하지 않는다.
                // 방을 만들 때 방 제목, 작성자, 방 개설자 값을 다음 액티비티에 넘겨준다.
                Intent intent = new Intent(Chat_CreateRoom.this, Chat_Room.class);
                intent.putExtra("title", title);
                intent.putExtra("writer", writer);
                intent.putExtra("creater", creater);
                startActivity(intent);
                finish();
            }
        });
    }
}
