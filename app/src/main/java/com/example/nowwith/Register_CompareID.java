package com.example.nowwith;

// 2021531003 김범준 11/10 2차

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

// 서버 데이터베이스에서 ID와 비밀번호를 비교하기 위해 가져오는 클래스
public class Register_CompareID extends StringRequest {
    // 웹서버에 있는 PHP문에 접근하기 위한 주소를 URL변수에 저장
    final static private String URL = "http://35.203.164.40/NW_Register_CheckID.php";
    // String 두개로 구성되어 값들을 받아올 Map 생성
    private Map<String, String> parameters;

    // 생성자, 요청이 들어오면 ID와 PW를 받아서 POST 방식으로 해당 주소로 전송한다.
    public Register_CompareID(String userID, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        // HashMap에 있는 put 함수를 통해 데이터베이스에 있는 userID와 userPW를 읽어와서 저장한다.
        parameters = new HashMap<>();
        parameters.put("userID", userID);
    }

    // PHP에서 반환된 값들을 받아온다.
    @Override
    protected Map<String, String> getParams() {
        return parameters;
    }
}