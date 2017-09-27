package com.sybilandjoel.jz.touchrecord;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.Buffer;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {
    Button start, stop, txt2db, txtdel, dbdel;
    MyDatabaseHelper dbHelper;
    String timeStamp, axis, axisValue, XTimeStamp="", XAxisValue, swp;//swp中1为按下，0为抬起，2为滑动
    final String TXT_FILE = "/mnt/sdcard/TouchRecord/data/points.txt";
    final String DB_FILE = "/mnt/sdcard/TouchRecord/data/locs.db3";
    int state;//axis 的状态

//    ////////魅蓝note////////
//    final String UPDOWNAXIS ="0018",
//    AXISVALUE = "00000000";
//    ///////////////////////

    ///////中兴////////
    final String UPDOWNAXIS ="014a",
            AXISVALUE = "00000001";
    /////////////////
final String DOWN="1",UP="0",MOVE="2";//swp的值
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        final Intent intent = new Intent(this, RecordService.class);
        final Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        txt2db = (Button) findViewById(R.id.txt2db);
        txtdel = (Button) findViewById(R.id.txtdel);
        dbdel = (Button) findViewById(R.id.dbdel);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(intent);
                startActivity(home);
                Toast.makeText(getBaseContext(), "开始后台记录", Toast.LENGTH_SHORT).show();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
                Toast.makeText(getBaseContext(), "结束后台记录", Toast.LENGTH_SHORT).show();
            }
        });

        txt2db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用转换函数
                setTxt2db();
            }
        });
        txtdel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除txt文档内容
                FileDel();
            }
        });
        dbdel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除db
                DbDel(dbHelper.getReadableDatabase());
                Toast.makeText(getBaseContext(), "删除DB成功", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void init() {
        File file = new File("/mnt/sdcard/TouchRecord/data/");
        if (!file.exists()) {
            try {
                //按照指定的路径创建文件夹
                file.mkdirs();
                Log.e("file", "file done");
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("file", "file undone");
            }
        }
        File dir = new File(TXT_FILE);
        if (!dir.exists()) {
            try {
                //在指定的文件夹中创建文件
                dir.createNewFile();
                Toast.makeText(this, "text done ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("file", "text undone");
            }
        }

        dbHelper = new MyDatabaseHelper(this, DB_FILE, 1);
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void insertLoc(SQLiteDatabase db, String timeStamp, String x, String y, String swp) {
        db.execSQL("insert into locs values(null,?,?,?,?)", new String[]{timeStamp, x, y, swp});
    }

    private void unpateLoc(SQLiteDatabase db, String timeStamp,String swp){

        db.execSQL("update locs set swp = ? where timestamp = ?",new String[]{swp,timeStamp});
    }
    private void FileDel(){
        File dir = new File(TXT_FILE);
        if (dir.exists()) {
            try {
                //在指定的文件夹中创建文件
                dir.delete();
                dir.createNewFile();
                Toast.makeText(this,"删除TXT成功",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void DbDel(SQLiteDatabase db){
db.execSQL("delete from locs");
    }

    private void setTxt2db() {
        dbHelper = new MyDatabaseHelper(this, DB_FILE, 1);
        try {
            FileInputStream fis = new FileInputStream(TXT_FILE);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                timeStamp = line.substring(3, 16);
                axis = line.substring(23, 27);
                axisValue = line.substring(28, 36);
                state = axis.equals(UPDOWNAXIS) ? 1 : (axis.equals("0035") ? 2 : (axis.equals("0036") ? 3 : 0));//第一项魅蓝 note为0018，其余为014a
                switch (state) {
                    case 1:
                        if (axisValue.equals(AXISVALUE)) {//魅蓝 note为00000000，其余为00000001
                           if(!XTimeStamp.equals("")) {unpateLoc(dbHelper.getReadableDatabase(),XTimeStamp,"0");}
                            swp = DOWN;//down
                            Log.e("014a", "down");
                        } else {
                            swp = UP;//up
                            Log.e("014a", "up");

                        }

                        break;
                    case 2:
                        XTimeStamp = timeStamp;
                        XAxisValue = axisValue;
                        break;
                    case 3:
                        if (XTimeStamp.equals(timeStamp)) {

                            insertLoc(dbHelper.getReadableDatabase(), timeStamp, XAxisValue, axisValue, swp);
                            swp=MOVE;//move
                        }else{
                            XTimeStamp="";
                            XAxisValue="";
                        }
                        break;
                    default:
                        Log.e("stateerr", "stateerr");
                        break;
                }
            }
            br.close();
            Log.e( "setTxt2db: ", "转换结束");
            Toast.makeText(this,"转换结束",Toast.LENGTH_SHORT).show();
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
