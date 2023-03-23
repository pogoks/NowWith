package com.example.nowwith;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

// 2021531003 김범준 11/10 2차
// 2021531003 김범준 11/16 3차 : 자동로그인, SharedPreferences(auto)에 사용자 정보를 저장하는 기능 추가
// 2021531003 김범준 11/23 4차 : 자동로그인 오류 수정

// 로그인 직후 사용자의 정보를 읽어오기 위한 클래스
// 네트워크 작업을 위한 비동기 AsyncTask 클래스 구현
public class Login_GetInfo extends AsyncTask<String, Void, String> {

    // 로그인 액티비티의 context를 얻어오기 위한 선언
    Context mContext = null;

    // 서버에서 얻어온 result 값을 저장할 String 변수
    private String jsonString;
    // 받아온 정보를 담을 ArrayList
    ArrayList<GetInfo_ArrayList> infoArrayList;

    // 생성자, 매개변수로 로그인 액티비티의 context를 얻어온다.
    public Login_GetInfo(Context context) {
        mContext = context;
    }

    // execute 할 때 진행되는 구간
    @Override
    protected String doInBackground(String... strings) {
        // 사용할 PHP 파일의 주소를 url 변수에 넣어준다.
        String url = "http://35.203.164.40/NW_Login_GetInfo.php";

        try {
            // selectData 변수에 현재 로그인 한 ID 정보를 불러와 넣는다.
            String selectData = "userID=" + Login_Info.getInstance().getID();

            // url에 담긴 주소를 실제 URL로 만들어준다.
            URL URL = new URL(url);
            HttpURLConnection http = (HttpURLConnection) URL.openConnection();

            // POST 방식으로 설정한 후 통신
            http.setRequestMethod("POST");
            http.connect();

            // 데이터를 쓸 수 있는 OutputStream 객체 생성
            OutputStream output = http.getOutputStream();
            // selectData 변수에 있는 정보를 output이 가리키고 있는 대상에 써준다.
            output.write(selectData.getBytes("UTF-8"));
            output.flush();
            output.close();

            // 통신 상태에 대한 정보를 담는 statusCode 변수를 선언해준다.
            int statusCode = http.getResponseCode();

            InputStream input;

            if(statusCode == HttpURLConnection.HTTP_OK) {     // 통신 상태가 OK라면
                // 입력 스트림을 얻는다.
                input = http.getInputStream();
            }
            else {     // 통신 상태가 OK가 아니면
                // 에러 스트림을 얻는다.
                input = http.getErrorStream();
            }

            // 입력 스트림을 읽어오는 inputStreamReader 선언
            InputStreamReader inputStreamReader = new InputStreamReader(input, "UTF-8");
            // 읽은 것들을 버퍼에 넣고 버퍼를 읽어오는 객체 선언
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // StringBuilder 객체를 생성한다.
            StringBuilder stringBuilder = new StringBuilder();
            String lines;

            // 버퍼에서 읽어와 lines 변수에 넣는 값이 null 값이 아니라면
            while((lines = bufferedReader.readLine()) != null) {
                // .append()로 연결하고자 하는 문자열들을 차례대로 넣는다.
                stringBuilder.append(lines);
            }

            bufferedReader.close();

            // 마지막으로 stringBuilder에 있는 내용을 String으로 변환하고 공백없이 반환한다.
            // 즉 PHP파일을 실행하고 결과를 받아와 반환한다.
            return stringBuilder.toString().trim();
        } catch(Exception e) {
            return null;
        }
    }

    // AsyncTask가 끝난 후 실행되는 구간
    // doInBackground의 결과 값을 result에다가 받아온다.
    @Override
    protected void onPostExecute(String result) {
        if(result == null) {        // 받은 result 값이 없다면?
            System.out.println("에러가 발생하였습니다.");
        }
        else {                      // 받은 result 값이 있다면
            // result 값을 미리 선언한 jsonString에 넣어준다.
            jsonString = result;
            // 받아온 result를 정리하여 담아주는 putValue() 메소드의 결과를 infoArrayList에 넣어준다.
            infoArrayList = putValue();

            // 자동로그인을 할 경우 앱이 종료되어도 정보가 남기 때문에 로그인할 때 auto SharedPreferences에 사용자 정보를 담아둔다.
            SharedPreferences auto = mContext.getApplicationContext().getSharedPreferences("auto", Context.MODE_PRIVATE);
            SharedPreferences.Editor autoEditor = auto.edit();

            // 저장하기 전 한 번 초기화 시켜준다.
            autoEditor.clear();

            autoEditor.putString("userID", infoArrayList.get(0).get_userID());
            autoEditor.putString("userPW", infoArrayList.get(0).get_userPW());
            autoEditor.putString("userEmail", infoArrayList.get(0).get_userEmail());
            autoEditor.putString("userGender", infoArrayList.get(0).get_userGender());
            autoEditor.putString("userHP", infoArrayList.get(0).get_userHP());

            // infoArrayList에 있는 값들을 전역변수로 전부 넣어준다.
            // 즉, 서버에서 사용자 정보를 가져와 전역변수로 넣는 과정
            Login_Info.getInstance().setCODE(infoArrayList.get(0).get_userCODE());
            Login_Info.getInstance().setID(infoArrayList.get(0).get_userID());
            Login_Info.getInstance().setPW(infoArrayList.get(0).get_userPW());
            Login_Info.getInstance().setEmail(infoArrayList.get(0).get_userEmail());
            Login_Info.getInstance().setGender(infoArrayList.get(0).get_userGender());
            Login_Info.getInstance().setHP(infoArrayList.get(0).get_userHP());

            // SharedPreferences에 저장된 값들을 커밋한다.
            autoEditor.commit();
        }
    }

    // 받은 result의 각각 정보를 추출하여 임시 배열에 넣고 반환해주는 메소드
    private ArrayList<GetInfo_ArrayList> putValue() {
        // 동일한 형태의 임시저장 배열 tempArray 선언
        ArrayList<GetInfo_ArrayList> tempArray = new ArrayList<GetInfo_ArrayList>();

        try {
            // JSONObject를 이용하여 PHP의 결과가 저장된 변수인 result를 jsonObject에 저장한다.
            JSONObject jsonObject = new JSONObject(jsonString);
            // result를 JSONArray로 담을 jsonArray 선언
            JSONArray jsonArray = jsonObject.getJSONArray("result");

            for(int i = 0; i < jsonArray.length(); i++) {       // jsonArray의 크기만큼 반복
                // 읽어온 값을 GetInfo_ArrayList 안에 있는 변수에 임시로 저장하기 위한 객체 선언
                GetInfo_ArrayList tempInfo = new GetInfo_ArrayList();
                // 해당 i값의 jsonArray[i] 값을 읽어와 value 변수에 저장한다.
                JSONObject value = jsonArray.getJSONObject(i);

                // 얻어온 value를 GetInfo_ArrayList 안에 있는 변수에다가 임시로 저장한다.
                tempInfo.set_userCODE(value.getString("userCODE"));
                tempInfo.set_userID(value.getString("userID"));
                tempInfo.set_userPW(value.getString("userPW"));
                tempInfo.set_userEmail(value.getString("userEmail"));
                tempInfo.set_userGender(value.getString("userGender"));
                tempInfo.set_userHP(value.getString("userHP"));

                // 모두 임시저장 배열인 tempArray에다가 추가해준다.
                tempArray.add(tempInfo);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        // 저장된 임시저장 배열을 반환한다.
        return tempArray;
    }

    // 읽어온 값을 임시로 저장하고 불러올 수 있는 모델 클래스
    public class GetInfo_ArrayList {
        public String userCODE;
        public String userID;
        public String userPW;
        public String userEmail;
        public String userGender;
        public String userHP;

        public String get_userCODE() {
            return userCODE;
        }
        public void set_userCODE(String userCODE) {
            this.userCODE = userCODE;
        }
        public String get_userID() {
            return userID;
        }
        public void set_userID(String userID) {
            this.userID = userID;
        }
        public String get_userPW() {
            return userPW;
        }
        public void set_userPW(String userPW) {
            this.userPW = userPW;
        }
        public String get_userEmail() {
            return userEmail;
        }
        public void set_userEmail(String userEmail) {
            this.userEmail = userEmail;
        }
        public String get_userGender() {
            return userGender;
        }
        public void set_userGender(String userGender) {
            this.userGender = userGender;
        }
        public String get_userHP() {
            return userHP;
        }
        public void set_userHP(String userHP) {
            this.userHP = userHP;
        }
    }
}