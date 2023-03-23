package com.example.nowwith;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// 2021531003 김범준 11/9 2차
// 2021531003 김범준 11/14 3차 : 로그인 후 게시판의 게시글들 불러오는 기능 추가
//                  11/16     : 자동로그인, SharedPreferences에 저장되어있는 사용자 정보 불러오는 기능 추가
// 2021531003 김범준 11/23 4차 : 자동로그인 오류 수정

public class Login extends AppCompatActivity {

    EditText edt_logID, edt_logPW;
    Button btn_login;
    TextView tv_to_register, tv_guestlogin;
    CheckBox cb_autologin;

    AlertDialog dialog;

    // 단말기 뒤로가기 버튼 제어에 필요한 변수
    private long backTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        edt_logID = (EditText) findViewById(R.id.edt_logID);
        edt_logPW = (EditText) findViewById(R.id.edt_logPW);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_to_register = (TextView) findViewById(R.id.tv_to_register);
        tv_guestlogin = (TextView) findViewById(R.id.tv_guestlogin);
        cb_autologin = (CheckBox) findViewById(R.id.cb_autologin);

        // 자동로그인을 할 경우 앱이 종료되어도 정보가 남기 때문에
        // 저장해둔 SharedPreferences를 불러와준다.
        SharedPreferences auto = getSharedPreferences("auto", Context.MODE_PRIVATE);
        SharedPreferences.Editor autoEditor = auto.edit();

        // 자동로그인 활성화 체크 여부를 저장하는 SharedPreferences
        // 마지막 로그인 때 자동로그인 활성화 체크를 하였다면 true 값이 들어가있다.
        boolean check = auto.getBoolean("check", false);

        // 자동로그인 활성화(check 값)가 체크되어 있고, 아이디랑 비밀번호가 SharedPreferences에 저장되어 있다면
        // 로그인 하지 않고 바로 기존 사용자 정보를 가지고 메인화면으로 넘어감.
        // 즉, 자동 로그인 활성화
        if(check && auto.contains("userID") && auto.contains("userPW")) {
            Login_Info.getInstance().setID(auto.getString("userID", ""));
            Login_Info.getInstance().setPW(auto.getString("userPW", ""));
            // 추가적으로 auto SharedPreferences에 저장된 값들을 전부 불러옴, 3차 때 정상적으로 넣어주지 않아 오류 발생
            Login_Info.getInstance().setEmail(auto.getString("userEmail", ""));
            Login_Info.getInstance().setGender(auto.getString("userGender", ""));
            Login_Info.getInstance().setHP(auto.getString("userHP", ""));

            // 로그인 직후 게시판에 있는 게시글들을 받아오기 위해 Forum_GetInfo 클래스를 AsyncTask 비동기로 실행한다.
            Forum_GetInfo forumInfo = new Forum_GetInfo(Login.this);
            forumInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            Intent intent = new Intent(Login.this, Main.class);
            startActivity(intent);
        }

        // 로그인 버튼 클릭 이벤트
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String logID = edt_logID.getText().toString();
                final String logPW = edt_logPW.getText().toString();

                // 로그인 하려는 ID를 전역변수에 저장한다.
                // 계정 정보를 불러오기 위해서 저장
                Login_Info.getInstance().setID(logID);

                // Volley 라이브러리의 RequestQueue에 적용할 Response 리스너 생성
                // RequestQueue에서 호출되고 통신에 성공하면 밑 내용을 실행한다.
                Response.Listener<String> resListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // JSONObject를 이용하여 PHP파일이 실행된 결과를 accountInfo에 저장한다.
                            JSONObject accountInfo = new JSONObject(response);

                            // 받아온 결과인 accountInfo 부분 중 $response["success"] 부분을 받아온다.
                            // 데이터베이스에 존재하는 ID와 PW를 EditText에 입력된 값들과 비교하여 일치하면 success=true 를 받게된다.
                            boolean success = accountInfo.getBoolean("success");

                            if(success) {       // 받은 $response["success"] 값이 true 라면
                                // 자동로그인 활성화 체크가 되어있다면
                                if(cb_autologin.isChecked()) {
                                    // check라는 Bool 형식의 SharedPreferences에 true를 넣어준다.
                                    autoEditor.putBoolean("check", true);
                                    autoEditor.commit();
                                }
                                else {  // 자동로그인 활성화 체크가 되어있지 않다면
                                    // check라는 Bool 형식의 SharedPreferences에 false를 넣어준다.
                                    autoEditor.putBoolean("check", false);
                                    autoEditor.commit();
                                }

                                // 로그인 직후 사용자 정보(Login_GetInfo)와 게시판 정보(Forum_GetInfo)를 가져오는 클래스의 AsyncTask가 실행이 완료되기 전에
                                // 액티비티를 이동하면 값을 얻어오기 전에 이동되어 오류가 생기게 된다.
                                // AsyncTask를 모두 완료하고 화면을 이동하도록 postDelayed 0.1초를 주었다.
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 성공했다는 토스트메세지와 함께 메인화면으로 이동한다.
                                        Toast.makeText(getApplicationContext(), "성공적으로 로그인되었습니다.", Toast.LENGTH_SHORT).show();

                                        // 메인화면으로 이동한다.
                                        Intent intent = new Intent(Login.this, Main.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 100);
                            }
                            else {              // 받은 $response["success"] 값이 false 라면
                                // 실패했다는 대화상자를 띄운 후 이동하지 않는다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                dialog = builder.setMessage("계정이 존재하지 않거나 비밀번호가 일치하지 않습니다.")
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

                // ID와 PW, 생성한 Response 리스너를 매개변수로 넘겨준다.
                Login_CompareInfo compareInfo = new Login_CompareInfo(logID, logPW, resListener);
                // Volley 라이브러리의 RequestQueue를 생성하고 Response 리스너와 ID, PW 정보를 큐에 넣고 통신을 시도한다.
                RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
                requestQueue.add(compareInfo);

                // 로그인 직후 사용자의 정보를 받아오기 위해 Login_GetInfo 클래스를 AsyncTask 비동기로 실행한다.
                Login_GetInfo getInfo = new Login_GetInfo(Login.this);
                // executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) = 병렬 처리, execute = 직렬 처리
                getInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                // 로그인 직후 게시판에 있는 게시글들을 받아오기 위해 Forum_GetInfo 클래스를 AsyncTask 비동기로 실행한다.
                Forum_GetInfo forumInfo = new Forum_GetInfo(Login.this);
                // executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) = 병렬 처리, execute = 직렬 처리
                forumInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        // 회원가입 클릭 이벤트
        tv_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        // GUEST 계정 접속 클릭 이벤트
        tv_guestlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 ID와 PW 전역변수 값을 "GUEST"로 설정한다.
                Login_Info.getInstance().setID("GUEST");
                Login_Info.getInstance().setPW("GUEST");

                Intent intent = new Intent(Login.this, Main.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // 단말기의 뒤로가기 버튼을 두 번 누르면 종료되게 만드는 메소드
    @Override
    public void onBackPressed() {
        // 마지막으로 뒤로가기 버튼을 누른 시간이 2초가 지났거나 처음 누를 경우
        if(System.currentTimeMillis() > backTime + 2000) {
            // 토스트 메세지로 알림을 띄워준다.
            backTime = System.currentTimeMillis();
            Toast.makeText(Login.this, "뒤로가기를 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 마지막으로 뒤로가기 버튼을 누른 시간 이후 2초가 지나지 않았으면
        if(System.currentTimeMillis() <= backTime + 2000) {
            // 프로그램 종료
            finishAffinity();
            System.runFinalization();
            System.exit(0);
        }
    }
}
