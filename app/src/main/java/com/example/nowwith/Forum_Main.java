package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

// 2021531003 김범준 11/16 3차

public class Forum_Main extends AppCompatActivity {

    TextView tv_title, tv_main, tv_writer, tv_date;
    Button btn_forum_back, btn_forum_delete;

    AlertDialog dialog, dialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum_main);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_main = (TextView) findViewById(R.id.tv_main);
        tv_writer = (TextView) findViewById(R.id.tv_writer);
        tv_date = (TextView) findViewById(R.id.tv_date);
        btn_forum_back = (Button) findViewById(R.id.btn_forum_back);
        btn_forum_delete = (Button) findViewById(R.id.btn_forum_delete);

        // 게시판 정보들을 저장했던 SharedPreferences를 불러온다.
        SharedPreferences forum = getSharedPreferences("forum", MODE_PRIVATE);

        // 리스트뷰 아이템을 클릭했을 때 넣은 인덱스 값을 불러와 index 변수에 넣는다.
        int index = forum.getInt("forumIndex", 0);

        // Forum_GetInfo 클래스에서 저장한 게시판 정보를 인덱스 값과 합쳐서 해당하는 값을 불러온다.
        // forumTitle0, forumTitle1... 형식으로 게시판 정보를 불러온 후 화면에 뿌린다.
        tv_title.setText(forum.getString("forumTitle" + index, ""));
        tv_main.setText(forum.getString("forumMain" + index, ""));
        tv_writer.setText(forum.getString("forumWriter" + index, ""));
        tv_date.setText(forum.getString("forumDate" + index, ""));

        // 데이터베이스에서 게시글을 검색하기 위한 변수
        String title = tv_title.getText().toString();
        // 작성자 구분을 위한 변수
        String writer = tv_writer.getText().toString();

        // 게시글 내용이 길 경우 스크롤을 이용할 수 있도록 하였다.
        tv_main.setMovementMethod(new ScrollingMovementMethod());

        // 목록 버튼 클릭 이벤트
        btn_forum_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 삭제 버튼 클릭 이벤트
        btn_forum_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 작성자와 현재 로그인한 사용자가 같은 사람이라면
                if(writer.equals(Login_Info.getInstance().getID())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Main.this);
                    dialog = builder.setMessage("게시글을 삭제하시겠습니까?")
                            // 삭제 버튼을 눌렀을 때 데이터베이스에서 게시글 정보가 삭제된다.
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Response.Listener<String> resListener = new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                // JSONObject를 이용하여 PHP파일이 실행된 결과를 forumInfo에 저장한다.
                                                JSONObject forumInfo = new JSONObject(response);

                                                // 받아온 결과인 forumInfo 부분 중 $response["success"] 부분을 받아온다.
                                                // 데이터베이스에 존재하는 게시글 제목을 현재 게시글 제목과 비교하여 일치하면 success=true 를 받게된다.
                                                boolean success = forumInfo.getBoolean("success");

                                                if(success) {       // 받은 $response["success"] 값이 true 라면
                                                    // 게시글 작성 직후 게시글들을 갱신하기 위해 Forum_GetInfo 클래스를 실행한다.
                                                    Forum_GetInfo forum_getInfo = new Forum_GetInfo(Forum_Main.this);
                                                    forum_getInfo.execute();

                                                    // 삭제되었다는 대화상자를 띄우고 예를 누르면 게시글 액티비티로 이동한다.
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Main.this);
                                                    dialog2 = builder.setMessage("성공적으로 삭제되었습니다.")
                                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(Forum_Main.this, Forum.class);
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
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Main.this);
                                                    dialog2 = builder.setMessage("삭제 과정에서 오류가 발생했습니다.")
                                                            .setPositiveButton("확인", null)
                                                            .create();
                                                    dialog2.show();
                                                }
                                            } catch(JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };

                                    // forumTitle, 생성한 Response 리스너를 매개변수로 넘겨준다.
                                    Forum_Delete delete = new Forum_Delete(title, resListener);
                                    // Volley 라이브러리의 RequestQueue를 생성하고 Response 리스너와 게시글 제목을 큐에 넣고 통신을 시도한다.
                                    RequestQueue requestQueue = Volley.newRequestQueue(Forum_Main.this);
                                    requestQueue.add(delete);
                                }
                            })
                            .setNegativeButton("취소", null)
                            .create();
                    dialog.show();
                }
                // 작성자와 현재 로그인한 사용자가 다른 사람이라면
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Forum_Main.this);
                    dialog = builder.setMessage("작성자만 삭제할 수 있습니다.")
                            .setPositiveButton("취소", null)
                            .create();
                    dialog.show();
                }
            }
        });
    }
}
