package com.ludi.uploadPic;

import android.support.v7.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Created by dell on 2017/6/29.
 */

public class UploadMCCSimple extends AppCompatActivity {
    String msg;
    long end;
    long start;
    long time;

    public String uploadPic (final File file){
        start = System.currentTimeMillis();

        Thread myThread = new Thread(new Runnable(){
            public void run(){
                try{
                    Socket socket = new Socket("47.92.85.186",8802);
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());

                    FileInputStream fileInputStream = new FileInputStream(file);

//                    String[] fileEnd = file.getName().split("\\.");
//                    outputStream.writeUTF("--" + fileEnd[fileEnd.length - 1].toString());
//                    System.out.println("buffer------------------" + "--"
//                           + fileEnd[fileEnd.length - 1].toString());

                    int bufferSize = 1024*1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = 0;
                    while ((length = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                        outputStream.flush();
                    }

                    socket.shutdownOutput();
                    fileInputStream.close();

                    msg = inputStream.readUTF();
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
