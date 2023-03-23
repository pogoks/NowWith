package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

// 2021531003 김범준 11/16 3차

public class Profile_ChangeInfo extends AppCompatActivity {

    EditText edt_change_email, edt_change_hp;
    Button btn_change;

    AlertDialog dialog, dialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_changeinfo);

        edt_change_email = (EditText) findViewById(R.id.edt_change_email);
        edt_change_hp = (EditText) findViewById(R.id.edt_change_hp);
        btn_change = (Button) findViewById(R.id.btn_change);

        // 변경하기 버튼 클릭 이벤트
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 입력한 변경할 이메일과 전화번호를 변수에 넣어준다.
                final String updateEmail = edt_change_email.getText().toString();
                final String updateHP = edt_change_hp.getText().toString();
                // 데이터베이스 select에 사용할 ID 변수를 넣어준다.
                final String checkID = Login_Info.getInstance().getID();

                // 변경할 이메일의 형식이 xxxx@xxx.xxx 형식이 아니면
                if(!updateEmail.matches(".+(@).+[.].+")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangeInfo.this);
                    dialog = builder.setMessage("이메일 형식에 맞게 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_change_email.requestFocus();
                    return;
                }

                // 변경할 전화번호 형식이 010-0000-0000 형식이 아니면
                if(!updateHP.matches("...(-)....(-)....")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangeInfo.this);
                    dialog = builder.setMessage("전화번호 형식에 맞게 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_change_hp.requestFocus();
                    return;
                }

                // 모든 형식에 만족한다면 변경을 묻는 대화상자를 띄운다.
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangeInfo.this);
                dialog = builder.setMessage("프로필을 변경하시겠습니까?")
                        .setPositiveButton("변경", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 변경을 누르면 서버 데이터베이스에서 사용자 정보를 변경하는 Update 메소드를 실행한다.
                                Update(updateEmail, updateHP, checkID);  // 112줄

                                // 프로필 변경 후 사용자의 정보를 다시 받아오기 위해 Login_GetInfo 클래스를 실행한다.
                                Login_GetInfo getInfo = new Login_GetInfo(Profile_ChangeInfo.this);
                                getInfo.execute();

                                // 변경되었다는 대화상자를 띄우고 확인을 누르면 메인 액티비티로 이동한다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangeInfo.this);
                                dialog2 = builder.setMessage("성공적으로 변경되었습니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(Profile_ChangeInfo.this, Main.class);
                                                // 종료하면서 상위 스택 액티비티들을 모두 제거한다.
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .create();
                                dialog2.show();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();
            }
        });
    }

    // 입력받은 값들을 서버에 전송하여 변경하는 Update 메소드
    private void Update(String userEmail, String userHP, String userID) {

        // 네트워크 작업을 위한 비동기 AsyncTask 클래스 구현
        class Update extends AsyncTask<String, Void, String> {

            // execute 할 때 진행되는 구간
            @Override
            protected String doInBackground(String... values) {
                try {
                    // 입력받은 매개변수들을 새로 변수로 옮겨준다.
                    String userEmail = (String) values[0];
                    String userHP = (String) values[1];
                    String userID = (String) values[2];

                    String url = "http://35.203.164.40/NW_Profile_ChangeInfo.php";

                    // 새로운 변수에 있는 값들을 쭉 나열하여 저장한다.
                    String data = URLEncoder.encode("userEmail", "UTF-8") + "=" + URLEncoder.encode(userEmail, "UTF-8");
                    data += "&" + URLEncoder.encode("userHP", "UTF-8") + "=" + URLEncoder.encode(userHP, "UTF-8");
                    data += "&" + URLEncoder.encode("userID", "UTF-8") + "=" + URLEncoder.encode(userID, "UTF-8");

                    // url에 담긴 주소를 실제 URL로 만들어주고 접속한다.
                    URL URL = new URL(url);
                    URLConnection conn = URL.openConnection();

                    // 데이터를 쓸 수 있는 OutputStreamWriter 객체 생성
                    conn.setDoOutput(true);
                    OutputStreamWriter output = new OutputStreamWriter(conn.getOutputStream());

                    // data 에 있는 정보를 output이 가리키고 있는 대상에 써준다.
                    output.write(data);
                    output.flush();

                    // 읽은 것들을 버퍼에 넣고 버퍼를 읽어오는 객체 선언
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    // StringBuilder 객체를 생성한다.
                    StringBuilder stringBuilder = new StringBuilder();
                    String lines = null;

                    // 버퍼에서 읽어와 lines 변수에 넣는 값이 null 값이 아니라면
                    while((lines = bufferedReader.readLine()) != null) {
                        // .append()로 연결하고자 하는 문자열들을 차례대로 넣는다.
                        stringBuilder.append(lines);
                        break;
                    }

                    // 마지막으로 stringBuilder에 있는 내용을 String으로 변환하고 공백없이 반환한다.
                    // 즉 PHP파일을 실행하고 결과를 받아와 반환한다.
                    return stringBuilder.toString().trim();
                } catch(Exception e) {
                    return new String("Exception : " + e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }

        // 위에서 정의한 AsyncTask 클래스를 실행한다.
        Update update = new Update();
        update.execute(userEmail, userHP, userID);
    }
}