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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// 2021531003 김범준 11/15 3차

public class Forum_Create extends AppCompatActivity {

    EditText edt_title, edt_main;
    Button btn_forum_write;

    SimpleDateFormat writeDate;

    AlertDialog dialog, dialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum_create);

        edt_title = (EditText) findViewById(R.id.edt_title);
        edt_main = (EditText) findViewById(R.id.edt_main);
        btn_forum_write = (Button) findViewById(R.id.btn_forum_write);

        // 현재 날짜를 구하기 위한 부분
        // 현재 시간을 구해서 now 변수에 넣는다.
        long now = System.currentTimeMillis();
        // now 변수를 Date 형식으로 변환한다.
        Date date = new Date(now);
        // SimpleDateFormat으로 0000-00-00 형태로 변환하고 저장한다.
        writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
        String getTime = writeDate.format(date);

        // 작성하기 버튼 클릭 이벤트
        btn_forum_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String forumTitle = edt_title.getText().toString();
                String forumMain = edt_main.getText().toString();
                String forumWriter = Login_Info.getInstance().getID();
                String forumDate = getTime;

                // 제목이 비어있을 경우 대화상자를 띄워준다.
                if(forumTitle.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Create.this);
                    dialog = builder.setMessage("제목을 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_title.requestFocus();
                    return;
                }

                // 내용이 비어있을 경우 대화상자를 띄워준다.
                if(forumMain.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Create.this);
                    dialog = builder.setMessage("내용을 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    edt_main.requestFocus();
                    return;
                }

                // 제목과 내용이 다 있을 경우 확인하는 대화상자를 띄워준다.
                AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Create.this);
                dialog = builder.setMessage("게시글을 저장하시겠습니까?")
                        // 확인 버튼을 눌렀을 때 데이터베이스에 게시글 정보를 저장한다.
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // CreateForum 함수로 데이터베이스에 정보를 보낸다.
                                CreateForum(forumTitle, forumMain, forumWriter, forumDate);

                                // 게시글 작성 직후 게시글들을 갱신하기 위해 Forum_GetInfo 클래스를 실행한다.
                                Forum_GetInfo forumInfo = new Forum_GetInfo(Forum_Create.this);
                                forumInfo.execute();

                                // 저장되었다는 대화상자를 띄우고 예를 누르면 게시글 액티비티로 이동한다.
                                AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Create.this);
                                dialog2 = builder.setMessage("성공적으로 저장되었습니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(Forum_Create.this, Forum.class);
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

    // 입력받은 데이터들을 서버에 전송하는 CreateForum 메소드
    private void CreateForum(String forumTitle, String forumMain, String forumWriter, String forumDate) {

        // 네트워크 작업을 위한 비동기 AsyncTask 클래스 구현
        class Create extends AsyncTask<String, Void, String> {

            // execute 할 때 진행되는 구간
            @Override
            protected String doInBackground(String... values) {
                try {
                    // 입력받은 매개변수들을 새로 변수로 옮겨준다.
                    String forumTitle = (String) values[0];
                    String forumMain = (String) values[1];
                    String forumWriter = (String) values[2];
                    String forumDate = (String) values[3];

                    String url = "http://35.203.164.40/NW_Forum_Create.php";

                    // 새로운 변수에 있는 값들을 쭉 나열하여 저장한다.
                    String data = URLEncoder.encode("forumTitle", "UTF-8") + "=" + URLEncoder.encode(forumTitle, "UTF-8");
                    data += "&" + URLEncoder.encode("forumMain", "UTF-8") + "=" + URLEncoder.encode(forumMain, "UTF-8");
                    data += "&" + URLEncoder.encode("forumWriter", "UTF-8") + "=" + URLEncoder.encode(forumWriter, "UTF-8");
                    data += "&" + URLEncoder.encode("forumDate", "UTF-8") + "=" + URLEncoder.encode(forumDate, "UTF-8");

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
        Create create = new Create();
        create.execute(forumTitle, forumMain, forumWriter, forumDate);
    }
}
