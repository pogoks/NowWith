package com.example.nowwith;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

// 2021531003 김범준 11/8 2차

public class Intro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        Handler handler = new Handler();

        // 3000 동안 인트로 화면 보여준 후 로그인 화면으로 넘어간다.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        },  3000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }
}
