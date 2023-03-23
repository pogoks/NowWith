package com.example.nowwith;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

// 2021531003 김범준 12/5 6차

public class Note extends Activity {

    TextView tv_note_count;
    ListView note_list;
    Button btn_note_close;

    // 연동되어있는 Firebase를 불러온다.
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    // Firebase의 루트 디렉토리("/")를 불러온다.
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    // 현재 사용자가 받은 쪽지들을 Note_ItemList 모델 클래스 형식으로 저장하는 ArrayList
    ArrayList<Note_ItemList> data;

    // 현재 로그인 중인 ID의 정보를 담는 변수
    String nowID;

    // 사용자가 받은 쪽지 개수를 저장하기 위한 변수
    Integer count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 팝업 액티비티의 타이틀 바를 없애주는 코드, setContentView 위에 적어줘야 한다.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.note);

        tv_note_count = (TextView) findViewById(R.id.tv_note_count);
        note_list = (ListView) findViewById(R.id.note_list);
        btn_note_close = (Button) findViewById(R.id.btn_note_close);

        data = new ArrayList<Note_ItemList>();

        nowID = Login_Info.getInstance().getID();

        // 받은 쪽지들의 목록을 보여주는 NoteList 메소드
        NoteList(nowID);

        // 쪽지를 한 번 보고 닫기 버튼을 누르면 받은 쪽지 내용이 모두 사라진다.
        // 닫기 버튼 클릭 이벤트
        btn_note_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // note 디렉토리 밑의 nowID(현재 사용자의 ID) 디렉토리 밑에 있는 모든 값들을 삭제한다.
                // 즉, note - 내 ID 밑에 있는 쪽지 정보 값들을 전부 삭제한다.
                databaseReference.child("note").child(nowID).removeValue();

                // 팝업 액티비티를 끝낸다.
                finish();
            }
        });
    }

    // 받은 쪽지들의 목록을 보여주는 NoteList 메소드
    private void NoteList(String nowID) {
        // firebase의 note 디렉토리 하위의 nowID(현재 로그인 한 ID) 값의 이름을 가진 디렉토리에 접근한다.
        // addChildEventListener는 하위 디렉토리에 이벤트가 발생했을 경우 실행된다.
        databaseReference.child("note").child(nowID).addChildEventListener(new ChildEventListener() {
            // onChildAdded는 자료를 검색하거나 추가할 때 실행된다.
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // snapshot으로 가져온 값들을 Note_ListItem 모델 클래스 형식으로 변환한다.
                Note_ItemList note = snapshot.getValue(Note_ItemList.class);
                // Note_ItemList 모델 클래스에서 getWriter와 getNoteMain, getTime을 통해 넘겨받은 값들을 가져와 data ArrayList에 넣어준다.
                // 즉, 쪽지 보낸 사람과 쪽지 내용, 쪽지 보낸 시간 정보를 가져와 ArrayList에 추가해준다.
                data.add(new Note_ItemList(note.getWriter(), note.getNoteMain(), note.getTime()));

                // 추가된 ArrayList의 크기만큼 count 변수를 설정해준다.
                count = data.size();

                // 개수를 표시해준다. 쪽지가 4개가 왔다면 4로 표시가 된다.
                tv_note_count.setText("" + count);

                // 어댑터 생성, 만들어 둔 Note_ListAdapter 커스텀 리스트 뷰 어댑터를 사용한다.
                // 매개변수로 현재 context, data ArrayList를 넘겨준다.
                Note_ListAdapter adapter = new Note_ListAdapter(Note.this, data);
                // 만든 어댑터를 리스트뷰에 연결해준다.
                note_list.setAdapter(adapter);
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

    // 팝업 액티비티의 바깥창을 클릭해도 종료가 되지 않도록 하는 부분
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    // 팝업 액티비티에서 뒤로가기 키를 사용하지 못하도록 하는 부분
    @Override
    public void onBackPressed() {
        return;
    }
}
