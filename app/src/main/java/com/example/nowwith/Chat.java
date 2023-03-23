package com.example.nowwith;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// 2021531003 김범준 11/19 4차
// 2021531003 김범준 11/26 5차 : 공개방, 비공개방 개설 버튼 추가 및 롱클릭 시 삭제 기능 추가
//                              비공개방 입장 전 암호 체크 대화상자 추가

public class Chat extends AppCompatActivity {

    Button btn_chat_create, btn_chat_private;
    ListView chat_list, chat_list_private;
    TabHost th_chat;

    AlertDialog dialog, dialog2, dialog3;

    // 연동되어있는 Firebase를 불러온다.
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    // Firebase의 루트 디렉토리("/")를 불러온다.
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        btn_chat_create = (Button) findViewById(R.id.btn_chat_create);
        btn_chat_private = (Button) findViewById(R.id.btn_chat_private);
        chat_list = (ListView) findViewById(R.id.chat_list);
        chat_list_private = (ListView) findViewById(R.id.chat_list_private);
        th_chat = (TabHost) findViewById(R.id.th_chat);

        // 탭 호스트 초기화
        th_chat.setup();

        // 새로운 TabSpec을 생성하면서 CHAT_LIST 탭의 이름을 "공개방" 으로 변경한다.
        TabHost.TabSpec list_public = th_chat.newTabSpec("CHAT_LIST").setIndicator("공개방");
        // TabSpec에 chat_list 리스트 뷰를 연결한다.
        list_public.setContent(R.id.chat_list);
        // 탭에 추가한 TabSpec(chat_list 리스트 뷰)를 추가한다.
        th_chat.addTab(list_public);

        // 새로운 TabSpec을 생성하면서 CHAT_LIST_PRIVATE 탭의 이름을 "비공개방" 으로 변경한다.
        TabHost.TabSpec list_private = th_chat.newTabSpec("CHAT_LIST_PRIVATE").setIndicator("비공개방");
        // TabSpec에 chat_list_private 리스트 뷰를 연결한다.
        list_private.setContent(R.id.chat_list_private);
        // 탭에 추가한 TabSpec(chat_list_private 리스트 뷰)를 추가한다.
        th_chat.addTab(list_private);

        // 공개방 목록을 보여주는 ChatList(), 비공개방 ChatListPrivate() 메소드 실행
        ChatList();
        ChatListPrivate();

        // 공개방 생성 버튼 클릭 이벤트
        btn_chat_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Chat.this, Chat_CreateRoom.class);
                startActivity(intent);
            }
        });

        // 비공개방 생성 버튼 클릭 이벤트
        btn_chat_private.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Chat.this, Chat_CreatePrivate.class);
                startActivity(intent);
            }
        });

        // 공개방 탭의 리스트 뷰 목록을 클릭하면 발생하는 이벤트
        chat_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 클릭한 항목 위치의 문자열을 가져와서 title 변수에 넣고 현재 로그인 중 아이디를 writer 변수에 넣어준다.
                final String title = (String)parent.getItemAtPosition(position);
                final String writer = Login_Info.getInstance().getID();

                // creater(작성자) 값을 가져오기 위한 firebase 접근, 방 삭제 시 ID 비교를 위해 작성자 값을 불러와서 Chat_Room 액티비티로 넘겨준다.
                // firebase의 chat 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
                // addListenerForSingleValueEvent는 호출했을 때 한 번만 실행된다.
                databaseReference.child("chat").child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // for each 문으로 firebase에 있는 값들을 dataSnapstot에 모두 넣어준다.
                        for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            // 넣어준 datasnapshot의 값을 Chat_ListItem 모델 클래스 형식으로 변환한다.
                            Chat_ItemList data = dataSnapshot.getValue(Chat_ItemList.class);
                            // 변환한 값 중 creater(개설자)값을 getCreater로 가져와 setCreater로 전역변수에 넣는다.
                            Chat_ItemList.getInstance().setCreater(data.getCreater());
                        }

                        Intent intent = new Intent(Chat.this, Chat_Room.class);
                        // 방 제목과 사용자 ID, 방 개설자를 다음 액티비티에 넘겨준다.
                        intent.putExtra("title", title);
                        intent.putExtra("writer", writer);
                        // setCreater로 저장된 creater(개설자)값을 읽어서 다음 액티비티에 넘겨준다.
                        intent.putExtra("creater", Chat_ItemList.getInstance().getCreater());
                        startActivity(intent);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
        });

        // 공개방 탭의 리스트 뷰 목록을 길게 클릭하면 발생하는 이벤트
        // 자신이 개설한 방만 삭제할 수 있다.
        chat_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);

                // 방 삭제를 확인하는 대화상자를 띄운다.
                dialog = builder.setMessage("방을 삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 롱클릭한 위치에 들어있는 자료를 가져와서 String형으로 변환 후 title 변수에 넣어준다.
                                final String title = (String)parent.getItemAtPosition(position);

                                // firebase의 chat 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
                                // addListenerForSingleValueEvent는 호출했을 때 한 번만 실행된다.
                                databaseReference.child("chat").child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // for each 문으로 firebase에 있는 값들을 dataSnapstot에 모두 넣어준다.
                                        for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                            // 넣어준 datasnapshot의 값을 Chat_ListItem 모델 클래스 형식으로 변환한다.
                                            Chat_ItemList data = dataSnapshot.getValue(Chat_ItemList.class);
                                            // 변환한 값 중 creater(개설자)값을 getCreater로 가져와 setCreater로 전역변수에 넣는다.
                                            Chat_ItemList.getInstance().setCreater(data.getCreater());
                                        }

                                        // 가져온 개설자 값과 현재 로그인 중인 ID 값이 같으면
                                        if(Chat_ItemList.getInstance().getCreater().equals(Login_Info.getInstance().getID())) {
                                            // chat 디렉토리 밑의 title 이름으로 된 디렉토리 밑에 있는 모든 값들을 삭제한다.
                                            // 즉, chat - 방제목 - writer: oooo, message: oooo ... 값들을 전부 삭제한다.
                                            databaseReference.child("chat").child(title).removeValue();

                                            // 화면 갱신 효과를 위해 삭제 이후 액티비티를 종료하고 다시 시작한다.
                                            Intent intent = new Intent(Chat.this, Chat.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        // 가져온 개설자 값과 현재 로그인 중인 ID 값이 다르면
                                        else {
                                            // 방 개설자만 삭제할 수 있다는 대화상자를 생성한다.
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                                            dialog2 = builder.setMessage("방 개설자만 삭제할 수 있습니다.")
                                                    .setPositiveButton("확인", null)
                                                    .create();
                                            dialog2.show();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();

                return true;
            }
        });

        // 비공개방 탭의 리스트 뷰 목록을 클릭하면 발생하는 이벤트
        chat_list_private.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 암호를 입력하고 일치 여부를 확인하는 대화상자를 펼쳐준다.
                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);

                // 대화상자 안에 EditText를 구현하기 위한 edt 선언
                final EditText edt = new EditText(Chat.this);
                // 대화상자 안의 EditText margin을 넣어주기 위한 과정
                LinearLayout container = new LinearLayout(Chat.this);
                LinearLayout.LayoutParams params
                        = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 50;
                params.rightMargin = 50;
                edt.setLayoutParams(params);
                container.addView(edt);

                dialog = builder.setMessage("암호를 입력하세요.")
                        .setView(container)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 클릭한 위치에 들어있는 자료를 가져와서 String형으로 변환 후 title 변수에 넣어준다.
                                // 현재 ID를 writer 변수에, EditText에 작성한 값을 checkPW 변수에 넣어준다.
                                final String title = (String)parent.getItemAtPosition(position);
                                final String writer = Login_Info.getInstance().getID();
                                final String checkPW = edt.getText().toString();

                                // creater(작성자) 값과 password(방 암호) 값들을 가져오기 위한 firebase 접근,
                                // 방 삭제 시 ID 비교와 방 암호값 비교를 위해 작성자 값과 방 암호 값을 불러와서 Chat_Room 액티비티로 넘겨준다.
                                // firebase의 private 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
                                // addListenerForSingleValueEvent는 호출했을 때 한 번만 실행된다.
                                databaseReference.child("private").child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // for each 문으로 firebase에 있는 값들을 dataSnapstot에 모두 넣어준다.
                                        for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                            // 넣어준 datasnapshot의 값을 Chat_ListItem 모델 클래스 형식으로 변환한다.
                                            Chat_ItemList data = dataSnapshot.getValue(Chat_ItemList.class);
                                            // 변환한 값 중 creater값과 password 값을 getCreater, getPassword로 가져와 전역변수에 넣는다.
                                            Chat_ItemList.getInstance().setPassword(data.getPassword());
                                            Chat_ItemList.getInstance().setCreater(data.getCreater());
                                        }

                                        // 입력한 대화상자의 EditText와 방 암호 값이 일치하면 인텐트 값으로 넘겨주고 액티비티를 이동한다.
                                        if(checkPW.equals(Chat_ItemList.getInstance().getPassword())) {
                                            Intent intent = new Intent(Chat.this, Chat_Room.class);
                                            // 방 제목과 방 암호, 사용자 ID, 개설자 값을 다음 액티비티에 넘겨준다.
                                            intent.putExtra("title", title);
                                            intent.putExtra("password", Chat_ItemList.getInstance().getPassword());
                                            intent.putExtra("writer", writer);
                                            intent.putExtra("creater", Chat_ItemList.getInstance().getCreater());
                                            startActivity(intent);
                                        }
                                        // 입력한 EditText와 암호 값이 일치하지 않으면 일치하지 않다는 대화상자를 띄운다.
                                        else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                                            dialog2 = builder.setMessage("방 암호가 일치하지 않습니다.")
                                                    .setPositiveButton("확인", null)
                                                    .create();
                                            dialog2.show();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });
                            }
                        })
                        // 취소 버튼을 누르면 대화상자를 닫는다.
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();
            }
        });

        // 비공개방 탭의 리스트 뷰 목록을 길게 클릭하면 발생하는 이벤트
        // 자신이 개설한 방만 삭제할 수 있고 암호가 일치해야 삭제할 수 있다.
        chat_list_private.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 암호를 입력하고 일치 여부를 확인하는 대화상자를 펼쳐준다.
                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);

                // 방 삭제를 확인하는 대화상자를 띄운다.
                dialog = builder.setMessage("방을 삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 대화상자 안에 EditText를 구현하기 위한 edt 선언
                                final EditText edt = new EditText(Chat.this);
                                // 대화상자 안의 EditText margin을 넣어주기 위한 과정
                                LinearLayout container = new LinearLayout(Chat.this);
                                LinearLayout.LayoutParams params
                                        = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.leftMargin = 50;
                                params.rightMargin = 50;
                                edt.setLayoutParams(params);
                                container.addView(edt);

                                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                                dialog2 = builder.setMessage("암호를 입력하세요")
                                        .setView(container)
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // 롱클릭한 위치에 들어있는 자료를 가져와서 String형으로 변환 후 title 변수에 넣어준다.
                                                // EditText에 작성한 값을 checkPW 변수에 넣어준다.
                                                final String title = (String)parent.getItemAtPosition(position);
                                                final String checkPW = edt.getText().toString();

                                                // firebase의 private 디렉토리 하위의 title 변수 값의 이름을 가진 디렉토리에 접근한다.
                                                // addListenerForSingleValueEvent는 호출했을 때 한 번만 실행된다.
                                                databaseReference.child("private").child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        // for each 문으로 firebase에 있는 값들을 dataSnapstot에 모두 넣어준다.
                                                        for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                                            // 넣어준 datasnapshot의 값을 Chat_ListItem 모델 클래스 형식으로 변환한다.
                                                            Chat_ItemList data = dataSnapshot.getValue(Chat_ItemList.class);
                                                            // 변환한 값 중 creater값과 password 값을 getCreater, getPassword로 가져와 전역변수에 넣는다.
                                                            Chat_ItemList.getInstance().setPassword(data.getPassword());
                                                            Chat_ItemList.getInstance().setCreater(data.getCreater());
                                                        }

                                                        // 방 개설자이며 암호가 일치하면 방을 삭제한다.
                                                        // 입력한 대화상자의 EditText와 방 암호 값이 일치하면 다음 조건문을 체크한다.
                                                        if(checkPW.equals(Chat_ItemList.getInstance().getPassword())) {
                                                            // 가져온 개설자 값과 현재 로그인 중인 ID 값이 같으면
                                                            if(Chat_ItemList.getInstance().getCreater().equals(Login_Info.getInstance().getID())) {
                                                                // private 디렉토리 밑의 title 이름으로 된 디렉토리 밑에 있는 모든 값들을 삭제한다.
                                                                // 즉, private - 방제목 - writer: oooo, message: oooo ... 값들을 전부 삭제한다.
                                                                databaseReference.child("private").child(title).removeValue();

                                                                // 화면 갱신 효과를 위해 삭제 이후 액티비티를 종료하고 다시 시작한다.
                                                                Intent intent = new Intent(Chat.this, Chat.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                            // 가져온 개설자 값과 현재 로그인 중인 ID 값이 다르면
                                                            else {
                                                                // 방 개설자만 삭제할 수 있다는 대화상자를 생성한다.
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                                                                dialog2 = builder.setMessage("방 개설자만 삭제할 수 있습니다.")
                                                                        .setPositiveButton("확인", null)
                                                                        .create();
                                                                dialog2.show();
                                                            }
                                                        }
                                                        // 입력한 EditText와 암호 값이 일치하지 않으면 일치하지 않다는 대화상자를 띄운다.
                                                        else {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                                                            dialog3 = builder.setMessage("방 암호가 일치하지 않습니다.")
                                                                    .setPositiveButton("확인", null)
                                                                    .create();
                                                            dialog3.show();
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) { }
                                                });

                                            }
                                        })
                                        .setNegativeButton("취소", null)
                                        .create();
                                dialog2.show();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();

                return true;
            }
        });
    }

    // 공개방 목록을 보여주는 메소드
    private void ChatList() {
        // 어댑터 생성, 안드로이드에서 기본적으로 제공해주는 모양의 형식을 사용한다.
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Chat.this, android.R.layout.simple_list_item_1, android.R.id.text1);
        // 만든 어댑터를 리스트뷰에 연결해준다.
        chat_list.setAdapter(adapter);

        // firebase의 chat 디렉토리에 접근한다.
        // addChildEventListener는 하위 디렉토리에 이벤트가 발생했을 경우 실행된다.
        databaseReference.child("chat").addChildEventListener(new ChildEventListener() {
            // onChildAdded는 자료를 검색하거나 추가할 때 실행된다.
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // chat 디렉토리 하위에 있는 값들의 키 값, 즉 방 제목인 title값을 가져와서 어댑터에 추가한다.
                // 방 제목들을 chat_list 리스트 뷰에 쭉 나열하게 된다.
                adapter.add(snapshot.getKey());
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // 비공개방 목록을 보여주는 메소드
    private void ChatListPrivate() {
        // 어댑터 생성, 안드로이드에서 기본적으로 제공해주는 모양의 형식을 사용한다.
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Chat.this, android.R.layout.simple_list_item_1, android.R.id.text1);
        // 만든 어댑터를 리스트뷰에 연결해준다.
        chat_list_private.setAdapter(adapter);

        // firebase의 private 디렉토리에 접근한다.
        // addChildEventListener는 하위 디렉토리에 이벤트가 발생했을 경우 실행된다.
        databaseReference.child("private").addChildEventListener(new ChildEventListener() {
            // onChildAdded는 자료를 검색하거나 추가할 때 실행된다.
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // private 디렉토리 하위에 있는 값들의 키 값, 즉 방 제목인 title값을 가져와서 어댑터에 추가한다.
                // 방 제목들을 chat_list_private 리스트 뷰에 쭉 나열하게 된다.
                adapter.add(snapshot.getKey());
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
