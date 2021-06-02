package com.example.capstone2021;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
public class RecieverActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reciever);

        Intent intent = getIntent();
//        String  myData = intent.getStringExtra("list");
        ArrayList<String> list = (ArrayList<String>)intent.getSerializableExtra("list");
        String regex = "";
        TextView textview = findViewById(R.id.textview);
//        ImageView imgview = findViewById(R.id.showImageView);
        String text = list.get(0);
//        String target = "uri";
//        int target_num = text.indexOf(target);
//        String result = text.substring(target_num,(text.substring(target_num).indexOf("no") + target_num));
//        String[] array = text.split("uri:");
//        text = array[0];
//        Uri uri = Uri.parse(text.);

        for(int i = 0; i < list.size(); i++) {
            textview.append(list.get(i));

        }
//        // 3. 특정단어(부분)만 자르기
//        str = "바나나 : 1000원, 사과 : 2000원, 배 : 3000원";
//        String target = "사과";
//        int target_num = str.indexOf(target); // target과 같은 value의 인덱스를 저장.
//        String result = str.substring(target_num, (str.substring(target_num).indexOf(",") + target_num));
//        // (target_num 부터 , target_num부터 끝까지 문자열 가운데 ","의 Index + target의 Index까지 출력)
//        // 잘 모르겠다면 변수 단위 별로 출력문을 사용 추천. ex)target_num /
//        // (str.substring(target_num).indexOf(",")+target_num)

        //05.29 이미지 불러오기
//        ImageView imgView = findViewById(R.id.showImageView);
//        byte[] byteArray = intent.getByteArrayExtra("image");
//        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//
//        imgView.setImageBitmap(bitmap);
////        Uri selectImage = intent.getData();
////        imgView.setImageURI(selectImage);
//
//        if (myData != null) {
//            //위치정보 탐지
//            textview.setText("위치정보가 있습니다.");
//
//            //주민 번호 탐지
//            String regex = "\\b(?:[0-9]{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[1,2][0-9]|3[0,1]))-[1-4][0-9]{6}\\b";
//            Matcher matcher = Pattern.compile(regex).matcher(myData);
//            if (matcher.find())
//                textview.setText("주민 번호 탐지");
//            //핸드폰 번호 탐지
//            regex = "\\b01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}\\b";
//            matcher = Pattern.compile(regex).matcher(myData);
//            if (matcher.find())
//                textview.setText("핸드폰 번호 탐지");
//
//            //이메일 탐지
//            regex = "\\b[a-zA-Z0-9]+@[a-zA-Z0-9]+.[a-zA-Z]+\\b";
//            matcher = Pattern.compile(regex).matcher(myData);
//            if (matcher.find())
//                textview.setText("이메일 탐지");
//
//
//        }else
//            textview.setText("위치정보가 없습니다.");
    }
}