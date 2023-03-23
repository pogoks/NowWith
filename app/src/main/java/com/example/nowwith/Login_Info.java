package com.example.nowwith;

import android.app.Application;

// 2021531003 김범준 11/8 2차

// 로그인 한 계정 정보를 전역변수로 저장하고 사용을 위한 모델 클래스
public class Login_Info extends Application {

    private String CODE;
    private String ID;
    private String PW;
    private String Email;
    private String Gender;
    private String HP;
    private Boolean auto = false;

    public Boolean getAuto() { return auto; }
    public void setAuto(Boolean auto) { this.auto = auto; }

    // 현재 로그인 된 계정의 코드번호를 불러오거나 메소드
    public String getCODE() {
        return CODE;
    }
    public void setCODE(String CODE) {
        this.CODE = CODE;
    }

    // 현재 로그인 된 ID를 불러오거나 저장하는 메소드들
    public String getID() {
        return ID;
    }
    public void setID(String ID) {
        this.ID = ID;
    }

    // 현재 로그인 된 ID의 비밀번호를 불러오거나 저장하는 메소드
    public String getPW() {
        return PW;
    }
    public void setPW(String PW) {
        this.PW = PW;
    }

    // 현재 계정의 이메일 정보를 불러오거나 저장하는 메소드
    public String getEmail() {
        return Email;
    }
    public void setEmail(String Email) {
        this.Email = Email;
    }

    // 현재 계정의 성별 정보를 불러오거나 저장하는 메소드
    public String getGender() {
        return Gender;
    }
    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    // 현재 계정의 전화번호 정보를 불러오거나 저장하는 메소드
    public String getHP() {
        return HP;
    }
    public void setHP(String HP) {
        this.HP = HP;
    }

    // 싱글톤 방식으로 객체를 관리하기 위한 코드입니다.
    // "Login_Info.getinstance().불러올메소드" 형식을 이용해 쉽게 접근하기 위해
    private static Login_Info instance = null;
    public static synchronized Login_Info getInstance() {
        if(null == instance) {
            instance = new Login_Info();
        }
        return instance;
    }
}
