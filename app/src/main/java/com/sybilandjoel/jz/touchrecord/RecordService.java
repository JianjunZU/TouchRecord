package com.sybilandjoel.jz.touchrecord;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import static android.R.attr.logo;
import static android.R.attr.process;


/**
 * Created by JZ on 2017/9/5.
 */

public class RecordService extends Service {
    /////////魅蓝 note
  //  final String UPDOWNAXIS = "\"0003 0018\"";
    ///////////
    /////////中兴
    final String UPDOWNAXIS = "0001 014a";
    ///////////
    boolean run;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("sercive", "onCreate: ");
        run = true;
        Thread th = new Thread(new Runnable() {
            private Process process = null;
            DataOutputStream os = null;
            InputStreamReader is;
            String s;
            BufferedReader br;

            @Override
            public void run() {
                Looper.prepare();

                try {
                    File file = new File("/mnt/sdcard/TouchRecord/data/points.txt");
                    FileOutputStream fos = new FileOutputStream(file, true);
                    PrintStream ps = new PrintStream(fos);
                    Log.e("yes", "su前");
                    process = Runtime.getRuntime().exec("su");

                    os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("chmod 666 /dev/input/event2\n");//中兴为2，魅蓝 note为4，pro6 为3
                    os.writeBytes("getevent -t /dev/input/event2 | grep -e \"0003 0035\" -e \"0003 0036\" -e "+UPDOWNAXIS+"\n");//note最后一项为0003 0018，其余为0001 014a

                    is = new InputStreamReader(
                            process.getInputStream());

                    br = new BufferedReader(is);

                    while (((s = br.readLine()) != null && run)) {

                        Log.e("yes", s);

                        ps.println(s);


                    }
                    is.close();
                    ps.close();

                    os.writeBytes("exit\n");
                    os.flush();
                    return;

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        process.destroy();
                    } catch (Exception e) {
                    }
                }
                Looper.loop();
            }
        });
        th.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        run = false;
        //Toast.makeText(getBaseContext(), "onDestroy", Toast.LENGTH_SHORT).show();

    }


}