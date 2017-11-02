package com.ludi.uploadPic;

/**
 * Created by dell on 2017/10/25.
 */

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Created by dell on 2017/6/29.
 */

public class UploadMECsimple extends AppCompatActivity {
    String msg;
    long end;
    long start;
    long time;

    public String uploadPic (final File file, final String host, final int port){
        start = System.currentTimeMillis();

        Thread myThread = new Thread(new Runnable(){
            public void run(){
                try{
                    Socket socket = new Socket(host,port);
                    Log.i("socket","connect");
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());

                    FileInputStream fileInputStream = new FileInputStream(file);
                    long size = file.length();
                    String s = String.valueOf(size);

                    int bufferSize = 1024*1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = 0;
                    outputStream.write((s.getBytes()));
                    while ((length = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                        outputStream.flush();
                        Log.i("send","send done");
                    }

                    socket.shutdownOutput();
                    fileInputStream.close();

                    String a = inputStream.readLine();
                    msg = a;
                    end = System.currentTimeMillis();
                    time = end-start;

                    socket.close();
                }

                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        myThread.start();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "Name:" + msg + "   Time:" + time + "ms";

        return result;
    }
}