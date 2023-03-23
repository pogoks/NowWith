package com.example.nowwith;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

// 2021531003 김범준 11/15 3차

// 쪽지를 읽어오기 위한 리스트뷰에 사용하는 커스텀 어댑터 클래스
// 커스텀 어댑터를 사용하기 위해 BaseAdapter를 상속받는다.
public class Note_ListAdapter extends BaseAdapter {

    // Note 액티비티의 context를 얻어오기 위한 선언
    Context mContext = null;
    // XML을 불러오기 위한 inflate
    LayoutInflater layoutInflater = null;

    // Note.Java의 게시판 임시 저장 ArrayList를 불러온다.
    ArrayList<Note_ItemList> getNote_arrayLists;

    // 생성자, context와 리스트뷰에 넣을 항목을 받아온다.
    public Note_ListAdapter(Context context, ArrayList<Note_ItemList> data) {
        mContext = context;
        getNote_arrayLists = data;
        // inflate를 사용하기 위해 mContext에서 얻어온다.
        layoutInflater = LayoutInflater.from(mContext);
    }

    // 어댑터가 관리할 데이터의 개수 설정
    @Override
    public int getCount() {
        return getNote_arrayLists.size();
    }

    // 어댑터가 관리하는 데이터 item의 ID를 얻어온다.
    @Override
    public long getItemId(int itemID) {
        return itemID;
    }

    // 해당 ID가 가리키는 item을 얻어온다.
    @Override
    public Note_ItemList getItem(int itemID) {
        return getNote_arrayLists.get(itemID);
    }

    // 리스트뷰에 보여질 리스트 한 줄에 대한 화면
    @Override
    public View getView(int itemID, View view, ViewGroup viewGroup) {
        // View 형식 변수에 Note_list.xml(커스텀 리스트목록) XML 파일을 연결한다.
        View view_inflate = layoutInflater.inflate(R.layout.note_list, null);

        TextView noteWriter = (TextView) view_inflate.findViewById(R.id.tv_note_writer);
        TextView noteMain = (TextView) view_inflate.findViewById(R.id.tv_note_main);
        TextView noteDate = (TextView) view_inflate.findViewById(R.id.tv_note_date);

        noteWriter.setText(getNote_arrayLists.get(itemID).getWriter());
        noteMain.setText(getNote_arrayLists.get(itemID).getNoteMain());
        noteDate.setText(getNote_arrayLists.get(itemID).getTime());

        return view_inflate;
    }
}
