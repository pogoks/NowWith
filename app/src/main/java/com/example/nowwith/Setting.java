package com.example.nowwith;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

// 2021531003 김범준 12/7 6차

public class Setting extends Activity {

    SwitchCompat switch_auto;
    Button btn_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 팝업 액티비티의 타이틀 바를 없애주는 코드, setContentView 위에 적어줘야 한다.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting);

        switch_auto = (SwitchCompat) findViewById(R.id.switch_auto);
        btn_setting = (Button) findViewById(R.id.btn_setting);

        // 자동로그인을 할 경우 앱이 종료되어도 정보가 남기 때문에
        // 저장해둔 SharedPreferences를 불러와준다.
        SharedPreferences auto = getSharedPreferences("auto", MODE_PRIVATE);
        SharedPreferences.Editor autoEditor = auto.edit();

        // 자동로그인 활성화 체크 여부를 저장하는 SharedPreferences
        // 마지막 로그인 때 자동로그인 활성화 체크를 하였다면 true 값이 들어가있다.
        boolean check = auto.getBoolean("check", false);

        // 자동로그인 활성화가 되어있다면
        if(check) {
            // 스위치 버튼이 체크 된 것으로 표시한다.
            switch_auto.setChecked(true);
        }
        // 자동로그인을 활성화하지 않았다면
        else {
            // 스위치 버튼이 체크 안 된 상태로 표시한다.
            switch_auto.setChecked(false);
        }

        // 완료 버튼 클릭 이벤트
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 스위치 버튼이 체크되어 있다면
                if(switch_auto.isChecked()) {
                    // 자동로그인 활성화 체크 값(check)을 true로 넣는다.
                    // 체크 값을 통해 Login.java에서 이 값을 가지고 자동로그인 여부를 판단한다.
                    autoEditor.putBoolean("check", true);
                    autoEditor.commit();

                    // 팝업 액티비티 종료
                    finish();
                }
                // 스위치 버튼이 체크 해제 되어있다면
                else {
                    // 자동로그인 활성화 체크 값(check)을 false로 넣는다.
                    // 체크 값을 통해 Login.java에서 이 값을 가지고 자동로그인 여부를 판단한다.
                    autoEditor.putBoolean("check", false);
                    autoEditor.commit();

                    // 팝업 액티비티 종료
                    finish();
                }
            }
        });
    }

    // 팝업 액티비티의 바깥창을 클릭해도 종료가 되지 않도록 하는 부분
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    // 팝업 액티비티에서 뒤로가기 키를 사용하지 못하도록 하는 부분
    @Override
    public void onBackPressed() {
        return;
    }
}
