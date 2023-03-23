package com.example.nowwith;

// 2021531003 김범준 11/14 3차

// 읽어온 값을 임시로 저장하고 불러올 수 있는 모델 클래스
public class Forum_ItemList {
    public String forumCODE;
    public String forumTitle;
    public String forumMain;
    public String forumWriter;
    public String forumDate;

    public Forum_ItemList(String forumTitle, String forumWriter, String forumDate) {
        this.forumTitle = forumTitle;
        this.forumWriter = forumWriter;
        this.forumDate = forumDate;
    }

    public String get_forumTitle() {
        return forumTitle;
    }
    public String get_forumWriter() {
        return forumWriter;
    }
    public String get_forumDate() {
        return forumDate;
    }
}