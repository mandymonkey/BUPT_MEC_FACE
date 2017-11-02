package com.ludi.uploadPic;

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

    public String uploadPic (final File file){
        Log.i("tag","jinlai");
        start = System.currentTimeMillis();

        Thread myThread = new Thread(new Runnable(){
            public void run(){
                try{
//                    Socket socket = new Socket("10.108.116.181",8802);
                    Socket socket = new Socket("10.108.117.14",8886);
                    Log.i("socket","connect");
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());

                    FileInputStream fileInputStream = new FileInputStream(file);
                    long size = file.length();
                    String s = String.valueOf(size);

//                    String[] fileEnd = file.getName().split("\\.");
//                    outputStream.writeUTF("--" + fileEnd[fileEnd.length - 1].toString());
//                    System.out.println("buffer------------------" + "--"
//                           + fileEnd[fileEnd.length - 1].toString());
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

 //                   Byte a = inputStream.readByte();
                    String a = inputStream.readLine();
                    // finputStream.readUTF();
                    msg = a;
                    //msg = a.toString();
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
