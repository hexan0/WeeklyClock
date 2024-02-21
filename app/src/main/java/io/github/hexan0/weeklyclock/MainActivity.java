package io.github.hexan0.weeklyclock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    boolean flag = false;
    String time;
    String[] preText = {"授業中","開始前","授業終了"};
    int[] nowTable;
    String nowPeriod;
    String nextPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        show();
    }

    protected void onPause(){
        flag = false;
        super.onPause();
    }

    public void show(){
        setContentView(R.layout.activity_main);
        TextView now_date = findViewById(R.id.now_date);
        TextView now_table = findViewById(R.id.now_table);
        TextView next_table = findViewById(R.id.next_table);
        TextView remain_minute = findViewById(R.id.remain_minute);
        TextView next_minute = findViewById(R.id.next_minute);

        flag = true;

        Thread thread = new Thread(){
            public void run(){
                while(flag){ //← 適当なタイミングで誰かが落とす
                    try {
                        Thread.sleep(1000); //1秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    nowTable = getNowTable();
                    nowPeriod = getNowDay() + nowTable[1];
                    if(nowTable[2] == 0) nextPeriod = getNowDay() + (nowTable[1] + 1);//授業中なら次を表示
                    if(nowTable[2] == 1) nextPeriod = nowPeriod;//開始前ならそのまま表示
                    if(nowTable[2] == 2) nowPeriod = "終了";//終了後なら表示なし
                    if(nowTable[1] == timetable.length && nowTable[2] != 0) nextPeriod = "終了";//最後の講時で開始後なら表示なし

                    if((nowTable[0] == 6) || (nowTable[0] == 7)){//土日
                        time = getNowDate() + " [" + nowPeriod + "] " + preText[nowTable[2]];
                    }else{//平日
                        time = getNowDate() + " [" + nowPeriod + "] " + preText[nowTable[2]];
                    }

                    mHandler.post(new Runnable() {
                        public void run() {
                            now_date.setText(time);
                            now_table.setText(nowPeriod);
                            next_table.setText(nextPeriod);
                            if(nowTable[2] != 2) remain_minute.setText(String.valueOf(nowTable[3]));
                            if(nowTable[2] == 2) remain_minute.setText("--");
                            if(nowTable[1] != timetable.length || nowTable[2] == 0) next_minute.setText(String.valueOf(nowTable[4]));
                            if(nowTable[1] == timetable.length && nowTable[2] != 0) next_minute.setText("--");
                        }
                    });
                }
            }
        };
        thread.start();
    }

    //現在時刻yyyy/MM/dd HH:mm:ssの取得
    public static String getNowDate(){
        final DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        return df1.format(date);
    }
    //曜日(文字列)の取得
    public static String getNowDay(){
        final DateFormat df2 = new SimpleDateFormat("E");
        final Date date = new Date(System.currentTimeMillis());
        return df2.format(date);
    }

    //時間割[開始時間,終了時間](分)
    public int[][] timetable = {{525,615},{630,720},{780,870},{885,975},{990,1080},{1095,1185}};
    //現在の講時取得
    public int[] getNowTable(){
        int day = 0, t = 0, pre = 0;//曜日、講時、授業中0開始前1終了後2
        //String preMinute;//分に変換前のHH:mm
        int nowMinute, remainMinute = 0, nextMinute = 0;//現在時刻の分変換後、次までの
        DateFormat df3 = new SimpleDateFormat("E,HH,mm");
        final Date date = new Date(System.currentTimeMillis());
        switch (df3.format(date).split(",")[0]){
            case "Mon":
            case "月":
                day = 1;
                break;
            case "Tue":
            case "火":
                day = 2;
                break;
            case "Wed":
            case "水":
                day = 3;
                break;
            case "Thu":
            case "木":
                day = 4;
                break;
            case "Fri":
            case "金":
                day = 5;
                break;
            case "Sat":
            case "土":
                day = 6;
                break;
            case "Sun":
            case "日":
                day = 7;
                break;
        }
        nowMinute = Integer.parseInt(df3.format(date).split(",")[1]) * 60 + Integer.parseInt(df3.format(date).split(",")[2]);
        int k = 1;
        for (; k <= timetable.length; k++){
            if((nowMinute >= timetable[k-1][0]) && (nowMinute < timetable[k-1][1])){
                t = k;
                pre = 0;
                remainMinute = timetable[k-1][1] - nowMinute;
                if(k != timetable.length) nextMinute = timetable[k][0] - nowMinute;
                if(k == timetable.length) nextMinute = timetable[0][0] + 1440 - nowMinute;
                break;
            }else if((nowMinute < timetable[k-1][0])){
                t = k;
                pre = 1;
                remainMinute = timetable[k-1][1] - nowMinute;
                nextMinute = timetable[k-1][0] - nowMinute;
                break;
            }
        }
        if(t == 0){
            t = k;
            pre = 2;
        }
        return new int[] {day, t, pre, remainMinute, nextMinute};
    }

}