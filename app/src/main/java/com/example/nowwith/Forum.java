package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;

// 2021531003 김범준 11/14 3차

public class Forum extends AppCompatActivity {

    TextView tv_forum_count;
    Button btn_forum_create;
    ListView lv_forum;

    // 밑에서 선언한 GetForum_ArrayList 형식으로 ArrayList를 선언해준다.
    ArrayList<Forum_ItemList> data;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum);

        tv_forum_count = (TextView) findViewById(R.id.tv_forum_count);
        btn_forum_create = (Button) findViewById(R.id.btn_forum_create);
        lv_forum = (ListView) findViewById(R.id.lv_forum);

        // Forum_ItemList 형태의 ArrayList 선언
        data = new ArrayList<Forum_ItemList>();

        // 로그인하며 불러왔던 게시판 정보들을 저장했던 SharedPreferences를 불러온다.
        SharedPreferences forum = getSharedPreferences("forum", MODE_PRIVATE);
        SharedPreferences.Editor putForum = forum.edit();

        // 저장해놓았던 배열 사이즈의 값을 함께 가져온다.
        int size = forum.getInt("forumSize", 0);

        // 현재 게시글 수를 세기위한 변수 count
        int count = 0;

        // 불러온 배열 사이즈만큼 반복한다.
        for(int i = 0; i < size; i++) {
            // data에 저장되어있던 SharedPreferences에 있는 값들을 불러와 저장한다.
            // index를 0으로 주면서 가장 먼저 삽입한 아이템이 밀려나게 되고 최근 아이템이 맨 위로 온다.
            // 즉, 최신 글 일수록 맨 위에 배치되게 하였다.
            data.add(0, new Forum_ItemList(
                    forum.getString("forumTitle" + i, ""),
                    forum.getString("forumWriter" + i, ""),
                    forum.getString("forumDate" + i, "")
            ));

            // 불러올 때 마다 count 변수를 증가시켜준다, 즉 게시글 수를 센다.
            count++;
        }

        // 게시글 수를 텍스트뷰에 설정해준다.
        tv_forum_count.setText("" + count);

        // 리스트뷰의 어댑터를 설정해준다.
        Forum_ListAdapter adapter = new Forum_ListAdapter(Forum.this, data);
        lv_forum.setAdapter(adapter);

        // 글 쓰기 버튼 클릭 이벤트
        btn_forum_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 로그인 한 계정이 GUEST 계정이라면 로그인 요구 대화상자를 띄운다.
                if(Login_Info.getInstance().getID().equals("GUEST")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Forum.this);
                    dialog = builder.setMessage("로그인이 필요합니다. 로그인 하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Forum.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("아니요", null)
                            .create();
                    dialog.show();
                }
                // 로그인 한 계정이 GUEST 계정이 아니라면 글 쓰기 액티비티로 이동한다.
                else {
                    Intent intent = new Intent(Forum.this, Forum_Create.class);
                    startActivity(intent);
                }
            }
        });

        // 각 리스트 뷰 아이템 클릭 이벤트
        lv_forum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 선택한 아이템이 몇 번째 아이템이라는 인덱스값을 SharedPreferences에 넣어준다.
                // 게시글 하나를 선택했을 때 그 아이템에 맞는 정보들을 찾아오기 위한 인덱스 역할을 한다.
                putForum.putInt("forumIndex", size - position - 1);
                putForum.commit();

                Intent intent = new Intent(Forum.this, Forum_Main.class);
                startActivity(intent);
            }
        });
    }
}
