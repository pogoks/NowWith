package com.example.nowwith;

// 2021531003 김범준 12/1 6차

// 쪽지 정보를 전역변수로 저장하고 사용을 위한 모델 클래스
public class Note_ItemList {

    private String friendID;
    private String noteMain;
    private String writer;
    private String time;

    public Note_ItemList() { }

    // 쪽지를 받는 사용자가 받은 쪽지 정보를 가져올 때 firebase에 넣을 때 사용하는 생성자
    public Note_ItemList(String writer, String noteMain, String time) {
        this.writer = writer;
        this.noteMain = noteMain;
        this.time = time;
    }

    // 쪽지를 보내는 사용자의 정보와 친구 ID 값을 firebase에 넣을 때 사용하는 생성자
    public Note_ItemList(String friendID, String noteMain, String writer, String time) {
        this.friendID = friendID;
        this.noteMain = noteMain;
        this.writer = writer;
        this.time = time;
    }

    public String getFriendID() {
        return friendID;
    }
    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }
    public String getNoteMain() {
        return noteMain;
    }
    public void setNoteMain(String noteMain) {
        this.noteMain = noteMain;
    }
    public String getWriter() {
        return writer;
    }
    public void setWriter(String writer) {
        this.writer = writer;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    // 싱글톤 방식으로 객체를 관리하기 위한 코드입니다.
    private static Note_ItemList instance = null;
    public static synchronized Note_ItemList getInstance() {
        if(null == instance) {
            instance = new Note_ItemList();
        }
        return instance;
    }
}
