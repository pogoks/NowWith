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

// 2021531003 김범준 11/13 3차

// 로그인 직후 데이터베이스에 저장된 게시판들을 읽어오기 위한 클래스
// 네트워크 작업을 위한 비동기 AsyncTask 클래스 구현
public class Forum_GetInfo extends AsyncTask<String, Void, String> {

    // 로그인 액티비티의 context를 얻어오기 위한 선언
    Context mContext = null;

    // 서버에서 얻어온 result 값을 저장할 String 변수
    private String jsonString;
    // 받아온 정보를 담을 ArrayList
    ArrayList<ForumInfo_ArrayList> forumArrayList;

    // 생성자, 매개변수로 로그인 액티비티의 context를 얻어온다.
    public Forum_GetInfo(Context context) {
        mContext = context;
    }

    // execute 할 때 진행되는 구간
    @Override
    protected String doInBackground(String... strings) {
        // 사용할 PHP 파일의 주소를 url 변수에 넣어준다.
        String url = "http://35.203.164.40/NW_Forum_GetInfo.php";

        try {
            // url에 담긴 주소를 실제 URL로 만들어준다.
            URL URL = new URL(url);
            HttpURLConnection http = (HttpURLConnection) URL.openConnection();

            // POST 방식으로 설정한 후 통신
            http.setRequestMethod("POST");
            http.connect();

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
            // 받아온 result를 정리하여 담아주는 putValue() 메소드의 결과를 forumArrayList에 넣어준다.
            forumArrayList = putValue();

            // 데이터베이스에서 게시판 정보들을 불러온 후 SharedPreferences라는 간편한 데이터베이스에 넣어준다.
            // 즉, 서버 DB에서 가져온 정보를 임시로 담아두고 필요할 때 마다 사용한다.
            SharedPreferences forum = mContext.getApplicationContext().getSharedPreferences("forum", Context.MODE_PRIVATE);
            SharedPreferences.Editor putForum = forum.edit();

            // 게시판 목록을 만들 때 사이즈를 측정하기 위해 가져온 배열 크기를 같이 int로 넣어준다.
            putForum.putInt("forumSize", forumArrayList.size());

            // 임시로 담아두기 전에 초기화 시켜준다.
            putForum.clear();

            // infoArrayList에 있는 값들을 SharedPreferences 에 전부 넣어준다.
            // 사용자 정보는 로그인 된 계정의 정보만 가져오면 되지만, 게시판은 모두가 작성한 글을 불러와야 하기 때문에
            // 반복문으로 넣어준다. i 값으로 작성된 게시글마다 구분할 수 있도록 Key 옆에 i를 붙여서 구분해준다.
            for (int i = 0; i < forumArrayList.size(); i++) {
                putForum.putString("forumCODE" + i, forumArrayList.get(i).get_forumCODE());
                putForum.putString("forumTitle" + i, forumArrayList.get(i).get_forumTitle());
                putForum.putString("forumMain" + i, forumArrayList.get(i).get_forumMain());
                putForum.putString("forumWriter" + i, forumArrayList.get(i).get_forumWriter());
                putForum.putString("forumDate" + i, forumArrayList.get(i).get_forumDate());
            }

            // 불러온 게시글들을 모두 커밋해준다.
            putForum.commit();
        }
    }

    // 받은 result의 각각 정보를 추출하여 임시 배열에 넣고 반환해주는 메소드
    private ArrayList<ForumInfo_ArrayList> putValue() {
        // 동일한 형태의 임시저장 배열 tempArray 선언
        ArrayList<ForumInfo_ArrayList> tempArray = new ArrayList<ForumInfo_ArrayList>();

        try {
            // JSONObject를 이용하여 PHP의 결과가 저장된 변수인 result를 jsonObject에 저장한다.
            JSONObject jsonObject = new JSONObject(jsonString);
            // result를 JSONArray로 담을 jsonArray 선언
            JSONArray jsonArray = jsonObject.getJSONArray("result");

            for(int i = 0; i < jsonArray.length(); i++) {       // jsonArray의 크기만큼 반복
                // 읽어온 값을 GetInfo_ArrayList 안에 있는 변수에 임시로 저장하기 위한 객체 선언
                ForumInfo_ArrayList tempInfo = new ForumInfo_ArrayList();
                // 해당 i값의 jsonArray[i] 값을 읽어와 value 변수에 저장한다.
                JSONObject value = jsonArray.getJSONObject(i);

                // 얻어온 value를 ForumInfo_ArrayList 안에 있는 변수에다가 임시로 저장한다.
                tempInfo.set_forumCODE(value.getString("forumCODE"));
                tempInfo.set_forumTitle(value.getString("forumTitle"));
                tempInfo.set_forumMain(value.getString("forumMain"));
                tempInfo.set_forumWriter(value.getString("forumWriter"));
                tempInfo.set_forumDate(value.getString("forumDate"));

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
    public class ForumInfo_ArrayList {
        public String forumCODE;
        public String forumTitle;
        public String forumMain;
        public String forumWriter;
        public String forumDate;

        public String get_forumCODE() {
            return forumCODE;
        }
        public void set_forumCODE(String forumCODE) {
            this.forumCODE = forumCODE;
        }
        public String get_forumTitle() {
            return forumTitle;
        }
        public void set_forumTitle(String forumTitle) {
            this.forumTitle = forumTitle;
        }
        public String get_forumMain() {
            return forumMain;
        }
        public void set_forumMain(String forumMain) {
            this.forumMain = forumMain;
        }
        public String get_forumWriter() {
            return forumWriter;
        }
        public void set_forumWriter(String forumWriter) {
            this.forumWriter = forumWriter;
        }
        public String get_forumDate() {
            return forumDate;
        }
        public void set_forumDate(String forumDate) {
            this.forumDate = forumDate;
        }
    }
}