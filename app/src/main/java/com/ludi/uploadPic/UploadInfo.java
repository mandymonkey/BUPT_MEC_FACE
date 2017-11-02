package com.ludi.uploadPic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Created by dell on 2017/6/29.
 */

public class UploadInfo {
    String msg;

    public String uploadPic (final File file, final String name){

        Thread myThread = new Thread(new Runnable(){
            public void run(){
                try{
                    Socket socket = new Socket("10.108.116.53",8801);
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    outputStream.write(name.getBytes());
                    long size = file.length();
                    String s = String.valueOf(size);
                    outputStream.write(s.getBytes());

                    //outputStream.writeUTF(name);
                    FileInputStream fileInputStream = new FileInputStream(file);

//                    String[] fileEnd = file.getName().split("\\.");
//                    outputStream.writeUTF(name + "--" + fileEnd[fileEnd.length - 1].toString());
//                    System.out.println("buffer------------------" + "--"
//                           + fileEnd[fileEnd.length - 1].toString());

                    outputStream.write(name.getBytes());
                    //outputStream.writeChars(name);

                    int bufferSize = 1024*1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = 0;
                    while ((length = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                        outputStream.flush();
                    }

                    socket.shutdownOutput();
                    fileInputStream.close();

                    String a = inputStream.readLine();
                    msg = a;
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

        String result = msg;

        return result;
    }
}
