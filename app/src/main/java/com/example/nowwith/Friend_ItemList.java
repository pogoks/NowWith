package com.example.nowwith;

// 2021531003 김범준 12/1 6차

// 검색한 계정 정보를 전역변수로 저장하고 사용을 위한 모델 클래스
public class Friend_ItemList {

    private String friendCODE;
    private String friendID;
    private String friendPW;
    private String friendEmail;
    private String friendGender;
    private String friendHP;

    public Friend_ItemList() { }

    // 친구 목록을 불러올 때와 친구 추가로 친구에 대한 값을 firebase에 넣을 때 사용하는 생성자
    public Friend_ItemList(String friendID, String friendGender, String friendEmail, String friendHP) {
        this.friendID = friendID;
        this.friendGender = friendGender;
        this.friendEmail = friendEmail;
        this.friendHP = friendHP;
    }

    public String getFriendCODE() {
        return friendCODE;
    }
    public void setFriendCODE(String friendCODE) {
        this.friendCODE = friendCODE;
    }
    public String getFriendID() {
        return friendID;
    }
    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }
    public String getFriendPW() {
        return friendPW;
    }
    public void setFriendPW(String friendPW) {
        this.friendPW = friendPW;
    }
    public String getFriendEmail() {
        return friendEmail;
    }
    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }
    public String getFriendGender() {
        return friendGender;
    }
    public void setFriendGender(String friendGender) {
        this.friendGender = friendGender;
    }
    public String getFriendHP() {
        return friendHP;
    }
    public void setFriendHP(String friendHP) {
        this.friendHP = friendHP;
    }

    // 싱글톤 방식으로 객체를 관리하기 위한 코드입니다.
    private static Friend_ItemList instance = null;
    public static synchronized Friend_ItemList getInstance() {
        if(null == instance) {
            instance = new Friend_ItemList();
        }
        return instance;
    }
}
