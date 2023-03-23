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

// 2021531003 김범준 11/28 5차

public class Chat_CreatePrivate extends AppCompatActivity {

    EditText edt_chat_private, edt_chat_password;
    Button btn_create_private;
    TextView tv_chat_private;

    String title, password, writer, nowID, creater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_createroom_private);

        edt_chat_private = (EditText) findViewById(R.id.edt_chat_private);
        edt_chat_password = (EditText) findViewById(R.id.edt_chat_password);
        btn_create_private = (Button) findViewById(R.id.btn_create_private);
        tv_chat_private = (TextView) findViewById(R.id.tv_chat_private);

        nowID = Login_Info.getInstance().getID();

        tv_chat_private.setText(nowID);

        // 개설하기 버튼 클릭 이벤트
        btn_create_private.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = edt_chat_private.getText().toString();
                password = edt_chat_password.getText().toString();
                writer = Login_Info.getInstance().getID();
                creater = Login_Info.getInstance().getID();

                // 비공개방이기 때문에 암호를 입력받고 함께 넘겨준다.
                // 방을 만들 때 방 제목, 방 암호, 작성자, 방 개설자 값을 다음 액티비티에 넘겨준다.
                Intent intent = new Intent(Chat_CreatePrivate.this, Chat_Room.class);
                intent.putExtra("title", title);
                intent.putExtra("password", password);
                intent.putExtra("writer", writer);
                intent.putExtra("creater", creater);
                startActivity(intent);
                finish();
            }
        });
    }
}
