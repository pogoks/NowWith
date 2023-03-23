package com.example.nowwith;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// 2021531003 김범준 12/4 6차

// 친구 목록을 읽어오기 위한 리스트뷰에 사용하는 커스텀 어댑터 클래스
// 커스텀 어댑터를 사용하기 위해 BaseAdapter를 상속받는다.
public class Friend_ListAdapter extends BaseAdapter {

    // Friend 액티비티의 context를 얻어오기 위한 선언
    Context mContext = null;
    // XML을 불러오기 위한 inflate
    LayoutInflater layoutInflater = null;

    // 연동되어있는 Firebase를 불러온다.
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    // Firebase의 루트 디렉토리("/")를 불러온다.
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    // Friend_ItemList 형식의 ArrayList를 선언한다.
    ArrayList<Friend_ItemList> friend_arrayLists;

    // 현재 사용자 ID를 불러오기 위한 변수
    String userID = null;

    SimpleDateFormat nowDate;

    AlertDialog dialog;

    // 생성자, context와 리스트뷰에 넣을 항목, 현재 사용자 ID를 받아온다.
    public Friend_ListAdapter(Context context, ArrayList<Friend_ItemList> data, String nowID) {
        mContext = context;
        friend_arrayLists = data;
        // inflate를 사용하기 위해 mContext에서 얻어온다.
        layoutInflater = LayoutInflater.from(mContext);
        userID = nowID;
    }

    // 어댑터가 관리할 데이터의 개수 설정
    @Override
    public int getCount() {
        return friend_arrayLists.size();
    }

    // 어댑터가 관리하는 데이터 item의 ID를 얻어온다.
    @Override
    public long getItemId(int itemID) {
        return itemID;
    }

    // 해당 ID가 가리키는 item을 얻어온다.
    @Override
    public Friend_ItemList getItem(int itemID) {
        return friend_arrayLists.get(itemID);
    }

    // 리스트뷰에 보여질 리스트 한 줄에 대한 화면
    @Override
    public View getView(int itemID, View view, ViewGroup viewGroup) {
        // View 형식 변수에 friend_list.xml(커스텀 리스트목록) XML 파일을 연결한다.
        View view_inflate = layoutInflater.inflate(R.layout.friend_list, null);

        ImageView friendImage = (ImageView) view_inflate.findViewById(R.id.img_friend_photo);
        TextView friendID = (TextView) view_inflate.findViewById(R.id.tv_friend_list_id);
        TextView friendEmail = (TextView) view_inflate.findViewById(R.id.tv_friend_list_email);
        TextView friendHP = (TextView) view_inflate.findViewById(R.id.tv_friend_list_hp);
        Button btn_note = (Button) view_inflate.findViewById(R.id.btn_friend_note);
        Button btn_delete = (Button) view_inflate.findViewById(R.id.btn_friend_delete);

        // 현재 날짜를 구하기 위한 부분
        // 현재 시간을 구해서 now 변수에 넣는다.
        long now = System.currentTimeMillis();
        // now 변수를 Date 형식으로 변환한다.
        Date date = new Date(now);
        // SimpleDateFormat으로 0000-00-00 00:00 형태로 변환하고 저장한다.
        nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
        String getTime = nowDate.format(date);

        // 친구 성별이 1 또는 3이면, 즉 남성이라면
        if(friend_arrayLists.get(itemID).getFriendGender().equals("1") || friend_arrayLists.get(itemID).getFriendGender().equals("3")) {
            // 남자 사진으로 설정
            friendImage.setImageResource(R.drawable.man);
        }
        // 친구 성별이 2 또는 4면, 즉 여성이라면
        else {
            // 여자 사진으로 설정
            friendImage.setImageResource(R.drawable.lady);
        }

        friendID.setText(friend_arrayLists.get(itemID).getFriendID());
        friendEmail.setText(friend_arrayLists.get(itemID).getFriendEmail());
        friendHP.setText(friend_arrayLists.get(itemID).getFriendHP());

        // 친구 목록에서 쪽지 버튼 클릭 이벤트
        btn_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 쪽지를 보낼 수 있도록 글을 쓰는 EditText를 포함한 대화상자를 만드는 과정
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                // 대화상자 안에 EditText를 구현하기 위한 edt 선언
                final EditText edt = new EditText(mContext);
                // 대화상자 안의 EditText margin을 넣어주기 위한 과정
                LinearLayout container = new LinearLayout(mContext);
                LinearLayout.LayoutParams params
                        = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 50;
                params.rightMargin = 50;
                edt.setLines(5);
                edt.setLayoutParams(params);
                container.addView(edt);

                dialog = builder.setMessage("쪽지 보내기")
                        .setView(container)
                        .setPositiveButton("전송", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext, "쪽지를 전송했습니다.", Toast.LENGTH_SHORT).show();

                                // 클릭한 친구의 ID를 얻어와 ID 변수에 넣어준다.
                                final String ID = friend_arrayLists.get(itemID).getFriendID();
                                // EditText에서 작성한 글을 main 변수애 넣어준다.
                                final String main = edt.getText().toString();
                                // 현재 사용자 ID를 writer 변수에 넣어준다.
                                final String writer = userID;

                                // Note_ItemList 모델 클래스에 친구의 ID와 입력한 글, 보낸 시간 값을 넘겨준다.
                                Note_ItemList note = new Note_ItemList(ID, main, writer, getTime);
                                // firebase의 note 디렉토리 하위의 친구의 ID = ID 변수 값의 이름을 가진 디렉토리에 접근한다.
                                // 친구 ID 하위에 push() 메소드를 이용해 랜덤 키를 생성하고 랜덤 키 하위에 값들을 넣어준다.
                                databaseReference.child("note").child(ID).push().setValue(note);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();
            }
        });

        // 친구 목록에서 삭제 버튼 클릭 이벤트
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                dialog = builder.setMessage("친구를 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // friend 디렉토리 밑의 userID(현재 사용자의 ID) 디렉토리 밑의 친구 ID 이름으로 된 디렉토리 밑에 있는 모든 값들을 삭제한다.
                                // friend_arrayLists.get(itemID).getFriendID() 부분은 친구 목록에서 선택한 친구의 ID를 받아온다.
                                // 즉, friend - 내 ID - 해당 친구 ID 밑에 있는 친구 정보 값들을 전부 삭제한다.
                                databaseReference.child("friend").child(userID).child(friend_arrayLists.get(itemID).getFriendID()).removeValue();

                                // 화면 갱신 효과를 위해 삭제 이후 액티비티를 종료하고 다시 시작한다.
                                Intent intent = new Intent(mContext, Friend.class);
                                mContext.startActivity(intent);
                                ((Activity)mContext).finish();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();
            }
        });

        return view_inflate;
    }
}
