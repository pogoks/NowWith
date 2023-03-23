package com.example.nowwith;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// 2021531003 김범준 11/23 4차

// 실시간 채팅에 사용되는 리스트뷰에 사용하는 커스텀 어댑터 클래스
// 커스텀 어댑터를 사용하기 위해 BaseAdapter를 상속받는다.
public class Chat_ListAdapter extends BaseAdapter {

    // Chat_Room의 context를 얻어오기 위한 선언
    Context mContext = null;
    // XML을 불러오기 위한 inflate
    LayoutInflater layoutInflater = null;
    // 작성자 ID를 불러오기 위한 변수
    String userID = null;

    // Chat_ItemList 형식의 ArrayList를 선언한다.
    ArrayList<Chat_ItemList> chat;

    // 생성자, context와 리스트뷰에 넣을 항목을 받아온다.
    public Chat_ListAdapter(Context context, ArrayList<Chat_ItemList> data, String writer) {
        mContext = context;
        chat = data;
        // inflate를 사용하기 위해 mContext에서 얻어온다.
        layoutInflater = LayoutInflater.from(mContext);
        userID = writer;
    }

    // 어댑터가 관리할 데이터의 개수 설정
    @Override
    public int getCount() {
        return chat.size();
    }

    // 어댑터가 관리하는 데이터 item의 ID를 얻어온다.
    @Override
    public long getItemId(int itemID) {
        return itemID;
    }

    // 해당 ID가 가리키는 item을 얻어온다.
    @Override
    public Chat_ItemList getItem(int itemID) {
        return chat.get(itemID);
    }

    // 리스트뷰에 보여질 리스트 한 줄에 대한 화면
    @Override
    public View getView(int itemID, View view, ViewGroup viewGroup) {
        // View 형식 변수에 chat_list.xml(커스텀 리스트목록) XML 파일을 연결한다.
        View view_inflate = layoutInflater.inflate(R.layout.chat_list, null);

        LinearLayout ll_id = (LinearLayout) view_inflate.findViewById(R.id.ll_id);
        LinearLayout ll_message = (LinearLayout) view_inflate.findViewById(R.id.ll_message);
        LinearLayout ll_time = (LinearLayout) view_inflate.findViewById(R.id.ll_time); 

        TextView chatWriter = (TextView) view_inflate.findViewById(R.id.tv_chat_writer);
        TextView chatMessage = (TextView) view_inflate.findViewById(R.id.tv_chat_message);
        TextView chatTime = (TextView) view_inflate.findViewById(R.id.tv_chat_time);

        // 작성자와 메세지를 연결한 chat에서 받아와서 설정한다.
        chatWriter.setText(chat.get(itemID).getWriter());
        chatMessage.setText(chat.get(itemID).getMessage());
        // 현재 시간을 설정한다.
        chatTime.setText(chat.get(itemID).getTime());

        // 현재 작성자가 작성한 메세지라면
        if(userID.equals(chatWriter.getText().toString())) {
            // ID 표시하는 TextView를 안보이게 설정한다.
            ll_id.setVisibility(View.INVISIBLE);
            // 메세지를 표시하는 TextView의 배경을 초록색으로 변경한다.
            chatMessage.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_textbackground));
            // 메세지 위치와 시간을 오른쪽 끝으로 설정한다.
            ll_message.setGravity(Gravity.END);
            ll_time.setGravity(Gravity.END);
        } else {       // 다른 사람이 작성한 메세지라면
            // ID 표시하는 TextView를 보이게 설정한다.
            ll_id.setVisibility(View.VISIBLE);
            // 메세지를 표시하는 TextView의 배경을 회색으로 변경한다.
            chatMessage.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_textbackground2));
            // ID 위치와 메세지 위치, 시간을 왼쪽 끝으로 설정한다.
            ll_id.setGravity(Gravity.START);
            ll_message.setGravity(Gravity.START);
            ll_time.setGravity(Gravity.START);
        }

        return view_inflate;
    }
}
