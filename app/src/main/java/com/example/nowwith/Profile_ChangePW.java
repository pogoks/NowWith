package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

// 2021531003 김범준 11/16 3차

public class Profile_ChangePW extends AppCompatActivity {

    EditText edt_now_pw, edt_change_pw, edt_change_pw_repeat;
    Button btn_change_pw;

    AlertDialog dialog, dialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_changepw);

        edt_now_pw = (EditText) findViewById(R.id.edt_now_pw);
        edt_change_pw = (EditText) findViewById(R.id.edt_change_pw);
        edt_change_pw_repeat = (EditText) findViewById(R.id.edt_change_pw_repeat);
        btn_change_pw = (Button) findViewById(R.id.btn_change_pw);

        btn_change_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 입력한 변경할 현재 비밀번호, 바꿀 비밀번호, 바꿀 비밀번호 재입력을 각 변수에 넣어준다.
                final String checkPW = edt_now_pw.getText().toString();
                final String updatePW = edt_change_pw.getText().toString();
                final String updatePW_repeat = edt_change_pw_repeat.getText().toString();
                // 데이터베이스 select에 사용할 ID 변수를 넣어준다.
                final String checkID = Login_Info.getInstance().getID();

                // 현재 비밀번호가 로그인 된 비밀번호가 아니라면
                if(!checkPW.equals(Login_Info.getInstance().getPW())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangePW.this);
                    dialog = builder.setMessage("현재 비밀번호를 확인해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_change_pw.requestFocus();
                    return;
                }

                // 변경할 비밀번호가 재입력 칸과 일치하지 않는다면
                if(!updatePW_repeat.equals(updatePW)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangePW.this);
                    dialog = builder.setMessage("변경할 비밀번호가 서로 일치하지 않습니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_change_pw_repeat.requestFocus();
                    return;
                }

                // 모든 조건에 만족한다면 변경을 묻는 대화상자를 띄운다.
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangePW.this);
                dialog = builder.setMessage("비밀번호를 변경하시겠습니까?")
                        .setPositiveButton("변경", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 변경을 누르면 서버 데이터베이스에서 비밀번호를 변경하는 UpdatePW 메소드를 실행한다.
                                UpdatePW(updatePW, checkID);  // 105줄

                                // 변경되었다는 대화상자를 띄우고 확인을 누르면 로그인 액티비티로 이동한다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Profile_ChangePW.this);
                                dialog2 = builder.setMessage("변경되었습니다. 다시 로그인 해주세요.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(Profile_ChangePW.this, Login.class);
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

    // 입력받은 값들을 서버에 전송하여 변경하는 UpdatePW 메소드
    private void UpdatePW(String userPW, String userID) {

        // 네트워크 작업을 위한 비동기 AsyncTask 클래스 구현
        class UpdatePW extends AsyncTask<String, Void, String> {

            // execute 할 때 진행되는 구간
            @Override
            protected String doInBackground(String... values) {
                try {
                    // 입력받은 매개변수들을 새로 변수로 옮겨준다.
                    String userPW = (String) values[0];
                    String userID = (String) values[1];

                    String url = "http://35.203.164.40/NW_Profile_ChangePW.php";

                    // 새로운 변수에 있는 값들을 쭉 나열하여 저장한다.
                    String data = URLEncoder.encode("userPW", "UTF-8") + "=" + URLEncoder.encode(userPW, "UTF-8");
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
        UpdatePW updatePW = new UpdatePW();
        updatePW.execute(userPW, userID);
    }
}