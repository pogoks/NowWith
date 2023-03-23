package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

// 2021531003 김범준 11/10 2차
// 2021531003 김범준 11/12 3차 : 게스트 계정의 ID인 GUEST로 회원가입을 못하도록 방지

public class Register extends AppCompatActivity {

    EditText edt_regID, edt_regPW, edt_regPW_repeat, edt_regEmail, edt_regPhone2, edt_regPhone3;
    Spinner sp_regPhone1;
    RadioGroup rg_regGender;
    RadioButton rb_regGender_male, rb_regGender_female;
    Button btn_register;
    
    // 스피너 전화번호 앞 자리를 넣기 위한 변수
    String hp1 = null;
    // 라디오 버튼으로 선택한 성별을 넣기 위한 변수
    String gender = null;

    AlertDialog dialog;

    // 단말기 뒤로가기 버튼 제어에 필요한 변수
    private long backTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        edt_regID = (EditText) findViewById(R.id.edt_regID);
        edt_regPW = (EditText) findViewById(R.id.edt_regPW);
        edt_regPW_repeat = (EditText) findViewById(R.id.edt_regPW_repeat);
        edt_regEmail = (EditText) findViewById(R.id.edt_regEmail);
        edt_regPhone2 = (EditText) findViewById(R.id.edt_regPhone2);
        edt_regPhone3 = (EditText) findViewById(R.id.edt_regPhone3);
        sp_regPhone1 = (Spinner) findViewById(R.id.sp_regPhone1);
        rg_regGender = (RadioGroup) findViewById(R.id.rg_regGender);
        rb_regGender_male = (RadioButton) findViewById(R.id.rb_regGender_male);
        rb_regGender_female = (RadioButton) findViewById(R.id.rb_regGender_female);
        btn_register = (Button) findViewById(R.id.btn_register);
        
        // 핸드폰 번호 앞 자리 선택 스피너 항목을 읽어와 hp1 변수에 저장
        sp_regPhone1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1) { // 010
                    hp1 = "010";
                } else if (position == 2){ // 011
                    hp1 = "011";
                } else if (position == 3) { // 016
                    hp1 = "016";
                } else if (position == 4) { // 017
                    hp1 = "017";
                } else {                    // 아무것도 선택하지 않으면
                    hp1 = "---";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 비밀번호를 서로 비교하기 위해 비밀번호 확인 칸의 값을 가져온 후 저장한다.
                String check = edt_regPW_repeat.getText().toString();

                // 성별 체크 칸, 남성이 체크 되어있다면 1을 저장
                if(rb_regGender_male.isChecked()) {
                    gender = "1";
                } else {   // 여성이 체크 되어있다면 2를 저장장
                   gender = "2";
                }

                // 각 입력된 값들을 저장해준다.
                final String userID = edt_regID.getText().toString();
                final String userPW = edt_regPW.getText().toString();
                final String userEmail = edt_regEmail.getText().toString();
                final String userGender = gender;
                final String userHP = (hp1) + "-" + (edt_regPhone2.getText().toString()) + "-" + (edt_regPhone3.getText().toString());

                // 각 칸의 공백 여부를 확인하는 구간
                // 여백이 있을 경우 회원가입이 이루어지지 않도록 제어한다.
                // ID 칸
                if(userID.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("아이디를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_regID.requestFocus();
                    return;
                }
                if(userID.length() < 4) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("아이디는 4글자 이상이여야 합니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_regID.requestFocus();
                    return;
                }
                if(userID.equals("GUEST")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("사용할 수 없는 아이디입니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_regID.requestFocus();
                    return;
                }
                // 비밀번호 칸
                if(userPW.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("비밀번호를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_regPW.requestFocus();
                    return;
                }
                // 이메일 칸
                if(userEmail.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("이메일 주소를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_regEmail.requestFocus();
                    return;
                }
                // 비밀번호 재입력 칸
                if(!userPW.equals(check)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("비밀번호가 서로 일치하지 않습니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_regPW_repeat.requestFocus();
                    return;
                }
                if(edt_regPW_repeat.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("비밀번호가 서로 일치하지 않습니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_regPW_repeat.requestFocus();
                    return;
                }
                // 전화번호 선택 안했을 경우
                if(hp1.equals("---")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    dialog = builder.setMessage("전화번호 앞 자리를 선택해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    sp_regPhone1.requestFocus();
                    return;
                }

                // 중복된 ID 체크를 위한 부분
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

                                // 성공했다는 대화상자를 띄운다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                                dialog = builder.setMessage("성공적으로 회원가입 되셨습니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //중복되는 ID가 없으므로 입력받은 값들을 서버에 전송하는 register 메소드 실행
                                                Register(userID, userPW, userEmail, userGender, userHP);

                                                Intent intent = new Intent(Register.this, Login.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .create();
                                dialog.show();
                            }
                            else {              // 받은 $response["success"] 값이 false 라면

                                // 실패했다는 대화상자를 띄운 후 이동하지 않는다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                                dialog = builder.setMessage("이미 등록 된 아이디입니다.")
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

                // 중복 ID 존재 여부를 위한 ID, 생성한 Response 리스너를 매개변수로 넘겨준다.
                Register_CompareID compareInfo = new Register_CompareID(userID, resListener);
                // Volley 라이브러리의 RequestQueue를 생성하고 Response 리스너와 ID 정보를 큐에 넣고 통신을 시도한다.
                RequestQueue requestQueue = Volley.newRequestQueue(Register.this);
                requestQueue.add(compareInfo);
            }
        });
    }

    // 입력받은 값들을 서버에 전송하는 register 메소드
    private void Register(String userID, String userPW, String userEmail, String userGender, String userHP) {

        // 네트워크 작업을 위한 비동기 AsyncTask 클래스 구현
        class Insert extends AsyncTask<String, Void, String> {

            // execute 할 때 진행되는 구간
            @Override
            protected String doInBackground(String... values) {
                try {
                    // 입력받은 매개변수들을 새로 변수로 옮겨준다.
                    String userID = (String) values[0];
                    String userPW = (String) values[1];
                    String userEmail = (String) values[2];
                    String userGender = (String) values[3];
                    String userHP = (String) values[4];

                    String url = "http://35.203.164.40/NW_Register.php";

                    // 새로운 변수에 있는 값들을 쭉 나열하여 저장한다.
                    String data = URLEncoder.encode("userID", "UTF-8") + "=" + URLEncoder.encode(userID, "UTF-8");
                    data += "&" + URLEncoder.encode("userPW", "UTF-8") + "=" + URLEncoder.encode(userPW, "UTF-8");
                    data += "&" + URLEncoder.encode("userEmail", "UTF-8") + "=" + URLEncoder.encode(userEmail, "UTF-8");
                    data += "&" + URLEncoder.encode("userGender", "UTF-8") + "=" + URLEncoder.encode(userGender, "UTF-8");
                    data += "&" + URLEncoder.encode("userHP", "UTF-8") + "=" + URLEncoder.encode(userHP, "UTF-8");

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
        Insert insert = new Insert();
        insert.execute(userID, userPW, userEmail, userGender, userHP);
    }
}