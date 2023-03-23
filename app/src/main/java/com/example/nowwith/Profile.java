package com.example.nowwith;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

// 2021531003 김범준 11/16 3차

public class Profile extends AppCompatActivity {

    ImageView img_profile_profile;
    TextView tv_profile_id, tv_profile_email, tv_profile_hp, tv_logout;
    Button btn_profile_change, btn_profile_pw, btn_profile_delete;

    AlertDialog dialog, dialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        img_profile_profile = (ImageView) findViewById(R.id.img_profile_profile);
        tv_profile_id = (TextView) findViewById(R.id.tv_profile_id);
        tv_profile_email = (TextView) findViewById(R.id.tv_profile_email);
        tv_profile_hp = (TextView) findViewById(R.id.tv_profile_hp);
        tv_logout = (TextView) findViewById(R.id.tv_logout);
        btn_profile_change = (Button) findViewById(R.id.btn_profile_change);
        btn_profile_pw = (Button) findViewById(R.id.btn_profile_pw);
        btn_profile_delete = (Button) findViewById(R.id.btn_profile_delete);

        // 로그아웃, 회원탈퇴 할 때 자동로그인 정보를 지워주기 위해 불러온다.
        SharedPreferences auto = getSharedPreferences("auto", Context.MODE_PRIVATE);
        SharedPreferences.Editor autoEditor = auto.edit();

        // 탈퇴를 진행할 때 삭제할 아이디를 변수에 담아준다.
        final String removeID = Login_Info.getInstance().getID();

        // 접속한 계정의 Gender(성별) 값이 1 또는 3이라면
        if(Login_Info.getInstance().getGender().equals("1") || Login_Info.getInstance().getGender().equals("3")) {
            img_profile_profile.setImageResource(R.drawable.man);
        }
        else {      // 접속한 계정의 성별이 2 또는 4라면
            img_profile_profile.setImageResource(R.drawable.lady);
        }

        // 프로필 화면을 전역변수에서 받아온다.
        tv_profile_id.setText(Login_Info.getInstance().getID());
        tv_profile_email.setText(Login_Info.getInstance().getEmail());
        tv_profile_hp.setText(Login_Info.getInstance().getHP());
        
        // 프로필 편집 버튼 클릭 이벤트
        btn_profile_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, Profile_ChangeInfo.class);
                startActivity(intent);
            }
        });

        // 비밀번호 변경 버튼 클릭 이벤트
        btn_profile_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, Profile_ChangePW.class);
                startActivity(intent);
            }
        });

        // 회원 탈퇴 버튼 클릭 이벤트
        btn_profile_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                dialog = builder.setMessage("정말 탈퇴하시겠습니까? 계정이 삭제됩니다.")
                        .setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Response.Listener<String> resListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            // JSONObject를 이용하여 PHP파일이 실행된 결과를 accountInfo에 저장한다.
                                            JSONObject deleteInfo = new JSONObject(response);

                                            // 받아온 결과인 accountinfo 부분 중 $response["success"] 부분을 받아온다.
                                            // 데이터베이스에 존재하는 게시글 제목을 현재 게시글 제목과 비교하여 일치하면 success=true 를 받게된다.
                                            boolean success = deleteInfo.getBoolean("success");

                                            if(success) {       // 받은 $response["success"] 값이 true 라면
                                                // 탈퇴되었다는 대화상자를 띄우고 확인을 누르면 로그인 액티비티로 이동한다.
                                                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                                                dialog2 = builder.setMessage("성공적으로 탈퇴되었습니다.")
                                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                // 탈퇴와 동시에 저장되어있던 모든 계정 정보를 삭제한다.
                                                                Login_Info.getInstance().setCODE(null);
                                                                Login_Info.getInstance().setID(null);
                                                                Login_Info.getInstance().setPW(null);
                                                                Login_Info.getInstance().setEmail(null);
                                                                Login_Info.getInstance().setGender(null);
                                                                Login_Info.getInstance().setHP(null);

                                                                // 회원탈퇴와 동시에 저장되어있는 자동로그인 정보들도 삭제해준다.
                                                                autoEditor.clear();
                                                                autoEditor.commit();
                                                                
                                                                Intent intent = new Intent(Profile.this, Login.class);
                                                                // 종료하면서 상위 스택 액티비티들을 모두 제거한다.
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        })
                                                        .create();
                                                dialog2.show();
                                            }
                                            else {      // 받은 $response["success"] 값이 false 라면
                                                // 오류 대화상자를 띄워준다.
                                                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                                                dialog2 = builder.setMessage("탈퇴 과정에서 오류가 발생했습니다.")
                                                        .setPositiveButton("확인", null)
                                                        .create();
                                                dialog2.show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                // 삭제할 ID, 생성한 Response 리스너를 매개변수로 넘겨준다.
                                Profile_Delete profile_delete = new Profile_Delete(removeID, resListener);
                                // Volley 라이브러리의 RequestQueue를 생성하고 Response 리스너와 삭제할 ID를 큐에 넣고 통신을 시도한다.
                                RequestQueue requestQueue = Volley.newRequestQueue(Profile.this);
                                requestQueue.add(profile_delete);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();
            }
        });

        // 로그아웃 클릭 이벤트
        // 모든 저장된 사용자 정보를 삭제하고 로그인 화면으로 돌아간다.
        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                dialog = builder.setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Profile.this, "성공적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                                // 로그아웃과 동시에 저장되어있던 모든 계정 정보를 삭제한다.
                                Login_Info.getInstance().setCODE(null);
                                Login_Info.getInstance().setID(null);
                                Login_Info.getInstance().setPW(null);
                                Login_Info.getInstance().setEmail(null);
                                Login_Info.getInstance().setGender(null);
                                Login_Info.getInstance().setHP(null);

                                // 로그아웃과 동시에 저장되어있는 자동로그인 정보들도 삭제해준다.
                                autoEditor.clear();
                                autoEditor.commit();

                                Intent intent = new Intent(Profile.this, Login.class);
                                // 종료하면서 상위 스택 액티비티들을 모두 제거한다.
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("아니요", null)
                        .create();
                dialog.show();
            }
        });
    }
}