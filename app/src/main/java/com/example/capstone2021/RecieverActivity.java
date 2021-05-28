package com.example.capstone2021;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecieverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reciever);

        Intent intent = getIntent();
        String myData = intent.getStringExtra("data");

        TextView textview = findViewById(R.id.textview);
// patternType = "[a-zA-Z0-9]+@[a-zA-Z0-9]+.[a-zA-Z]+"; // 이메일 정규식
// patternType = "\\d{2,3}-\\d{3,4}-\\d{4}"; // 전화번호 정규식
// patternType = "\\d{6}\\-[1-4]\\d{6}"; // 주민번호 정규식
//01(?:0|1|[6-9])-(?:\d{3}|\d{4})-\d{4}// 핸드폰번호 정규식
        String regex = "01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}";
        Matcher matcher = Pattern.compile(regex).matcher(myData);

        if(matcher.find()){
            textview.setText("개인정보가 있습니다.");
//            textview.setText(myData);
        }else{
            textview.setText("개인정보가 없습니다.");
//            textview.setText(myData);
        }
    }
}