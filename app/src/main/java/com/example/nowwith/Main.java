package com.example.nowwith;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// 2021531003 김범준 11/10 2차
// 2021531003 김범준 11/12 3차 : 메인화면 하단에 현재 날짜, 시간 실시간 갱신(스레드 이용)하여 보여주는 코드 추가,
//                              로그아웃 기능 구현, 게시판 버튼 클릭시 이동 가능(게스트, 일반 둘 다), 프로필 버튼 클릭시 이동 가능
//                  11/16     : 자동로그인, SharedPreferences에 저장되어있는 사용자 정보 불러오는 기능 추가
// 2021531003 김범준 12/5 6차 : 쪽지 기능 추가, 우측 상단 위에 편지모양 아이콘 생성 및 팝업 액티비티 구현
//                  12/7     : 설정 기능 추가

public class Main extends AppCompatActivity {

    ImageView img_photo, img_chat, img_forum, img_profile, img_friend, img_note, img_setting;
    TextView tv_hello, tv_id_info, tv_email_info, tv_login_logout, tv_today;

    // 날짜를 구하기 위한 SimpleDateFormat
    SimpleDateFormat format;

    AlertDialog dialog;
    
    // 단말기 뒤로가기 버튼 제어에 필요한 변수
    private long backTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        img_chat = (ImageView) findViewById(R.id.img_chat);
        img_forum = (ImageView) findViewById(R.id.img_forum);
        img_profile = (ImageView) findViewById(R.id.img_profile);
        img_friend = (ImageView) findViewById(R.id.img_friend);
        img_photo = (ImageView) findViewById(R.id.img_photo);
        img_note = (ImageView) findViewById(R.id.img_note);
        img_setting = (ImageView) findViewById(R.id.img_setting);
        tv_hello = (TextView) findViewById(R.id.tv_hello);
        tv_id_info = (TextView) findViewById(R.id.tv_id_info);
        tv_email_info = (TextView) findViewById(R.id.tv_email_info);
        tv_login_logout = (TextView) findViewById(R.id.tv_login_logout);
        tv_today = (TextView) findViewById(R.id.tv_today);

        // 자동로그인을 할 경우 앱이 종료되어도 정보가 남기 때문에
        // 저장해둔 SharedPreferences를 불러와준다.
        SharedPreferences auto = getSharedPreferences("auto", Context.MODE_PRIVATE);
        SharedPreferences.Editor autoEditor = auto.edit();

        // GUEST 계정 접속 상태라면
        if(Login_Info.getInstance().getID().equals("GUEST")) {
            // 게스트 이미지로 설정
            img_photo.setImageResource(R.drawable.guest);
            // 게스트 계정으로 접속중이면 쪽지 아이콘이 안보이도록 설정
            img_note.setVisibility(View.INVISIBLE);
            tv_hello.setText("GUEST 계정 접속 중");

            // 로그인 클릭 이벤트
            // 로그인 화면으로 이동한다.
            tv_login_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Login.class);
                    startActivity(intent);
                }
            });

            // 채팅 버튼 클릭 이벤트, GUEST 계정으로는 이용할 수 없다.
            img_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    dialog = builder.setMessage("로그인이 필요합니다. 로그인 하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Main.this, Login.class);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("아니요", null)
                            .create();
                    dialog.show();
                }
            });

            // 게시판 버튼 클릭 이벤트
            img_forum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Forum.class);
                    startActivity(intent);
                }
            });

            // 프로필 버튼 클릭 이벤트, GUEST 계정으로는 이용할 수 없다.
            img_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    dialog = builder.setMessage("로그인이 필요합니다. 로그인 하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Main.this, Login.class);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("아니요", null)
                            .create();
                    dialog.show();
                }
            });

            // 친구 버튼 클릭 이벤트, GUEST 계정으로는 이용할 수 없다.
            img_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    dialog = builder.setMessage("로그인이 필요합니다. 로그인 하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Main.this, Login.class);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("아니요", null)
                            .create();
                    dialog.show();
                }
            });
        }

        // 일반 계정으로 로그인 한 상태라면
        else {
            // 접속한 계정의 Gender(성별) 값이 1 또는 3이라면
            if(Login_Info.getInstance().getGender().equals("1") || Login_Info.getInstance().getGender().equals("3")) {
                img_photo.setImageResource(R.drawable.man);
            }
            else {      // 접속한 계정의 성별이 2 또는 4라면
                img_photo.setImageResource(R.drawable.lady);
            }
            
            tv_id_info.setText(Login_Info.getInstance().getID() + " 님");
            tv_email_info.setText(Login_Info.getInstance().getEmail());
            tv_login_logout.setText("로그아웃");

            // 로그아웃 클릭 이벤트
            // 모든 저장된 사용자 정보를 삭제하고 로그인 화면으로 돌아간다.
            tv_login_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    dialog = builder.setMessage("로그아웃 하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(Main.this, "성공적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

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

                                    Intent intent = new Intent(Main.this, Login.class);
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

            img_setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Setting.class);
                    startActivity(intent);
                }
            });

            // 쪽지 아이콘 클릭 이벤트
            img_note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Note.class);
                    // 액티비티를 팝업창으로 실행한다.
                    startActivityForResult(intent, 1);
                }
            });

            // 채팅 버튼 클릭 이벤트
            img_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Chat.class);
                    startActivity(intent);
                }
            });

            // 게시판 버튼 클릭 이벤트
            img_forum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Forum.class);
                    startActivity(intent);
                }
            });

            // 프로필 버튼 클릭 이벤트
            img_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Profile.class);
                    startActivity(intent);
                }
            });

            // 친구 버튼 클릭 이벤트
            img_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Main.this, Friend.class);
                    startActivity(intent);
                }
            });
        }
        
        // 현재 날짜와 시간을 구해서 화면 하단에 뿌려주기 위한 스레드
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
        new Thread(showTime).start();
    }

    // 날짜와 시간 구하고 갱신해주는 스레드
    Runnable showTime = new Runnable() {
        @Override
        public void run() {
            // do~while문, while에 true 값으로 무한 루프
            do {
                try {
                    // 1.5초씩 멈췄다가 실행, 즉 1.5초마다 갱신된다.
                    Thread.sleep(1500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 메인 스레드에서 날짜 갱신을 해준다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_today.setText(format.format(new Date()));
                    }
                });
            } while(true);
        }
    };

    // 단말기의 뒤로가기 버튼을 두 번 누르면 종료되게 만드는 메소드
    @Override
    public void onBackPressed() {
        // 마지막으로 뒤로가기 버튼을 누른 시간이 2초가 지났거나 처음 누를 경우
        if(System.currentTimeMillis() > backTime + 2000) {
            // 토스트 메세지로 알림을 띄워준다.
            backTime = System.currentTimeMillis();
            Toast.makeText(Main.this, "뒤로가기를 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
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