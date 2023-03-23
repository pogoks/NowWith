package com.example.nowwith;

// 2021531003 김범준 11/19 4차
// 2021531003 김범준 11/29 5차 : 비밀번호를 저장할 수 있는 생성자 오버로딩

// 읽어온 값을 임시로 저장하고 불러올 수 있는 모델 클래스
// 입력한 메세지와 메세지를 보낸 사람의 값을 받아 반환해준다.
public class Chat_ItemList {

    private String writer;
    private String password;
    private String message;
    private String time;
    private String creater;

    public Chat_ItemList() { }

    // Chat_Room 에서 리스트 뷰 어댑터에 값을 넣을 때 사용하는 메소드
    public Chat_ItemList(String writer, String message, String time) {
        this.writer = writer;
        this.message = message;
        this.time = time;
    }

    // Chat_Room 에서 사용자가 메시지 정보들을 담아 push().setValue()를 할 때 사용하는 메소드
    public Chat_ItemList(String writer, String message, String time, String creater) {
        this.writer = writer;
        this.message = message;
        this.time = time;
        this.creater = creater;
    }

    // 비공개 방 에서 사용자가 메시지 정보(비밀번호 포함)들을 담아 push().setValue()를 할 때 사용하는 메소드
    public Chat_ItemList(String writer, String message, String time, String password, String creater) {
        this.writer = writer;
        this.message = message;
        this.time = time;
        this.password = password;
        this.creater = creater;
    }

    // 비밀번호만 저장하는 생성자 오버로딩
    public Chat_ItemList(String password) {
        this.password = password;
    }

    public String getWriter() {
        return writer;
    }
    public void setWriter(String writer) {
        this.writer = writer;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getCreater() {
        return creater;
    }
    public void setCreater(String creater) {
        this.creater = creater;
    }

    // 싱글톤 방식으로 객체를 관리하기 위한 코드입니다.
    private static Chat_ItemList instance = null;
    public static synchronized Chat_ItemList getInstance() {
        if(null == instance) {
            instance = new Chat_ItemList();
        }
        return instance;
    }
}
