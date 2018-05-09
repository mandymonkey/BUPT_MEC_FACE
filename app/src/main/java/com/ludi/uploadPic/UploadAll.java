package com.ludi.uploadPic;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by dell on 2018/4/18.
 */

public class UploadAll {
    String ip1 = "10.108.121.239";
    String ip2 = "10.108.117.152";
    String ip = ip1;
    int flag = 0;
    int port = 9999;
    int i = 0;
    ArrayList<String> imagePath = new ArrayList<String>();

    //获取全部图片list
    public ArrayList<String> refreshFileList(String strPath){
        String filename;//文件名
        String suf; //文件后缀
        File dir = new File(strPath);
        File[ ] files = dir.listFiles();

        if(files == null)
            return null;

        for (int i = 0; i<files.length; i++){
            if (files[i].isDirectory()){
                System.out.println("---" + files[i].getAbsolutePath());
                refreshFileList(files[i].getAbsolutePath());//递归文件夹
            }else {
                filename = files[i].getName();
                int j = filename.lastIndexOf(".");
                suf = filename.substring(j+1);//得到文件后缀

                if (suf.equalsIgnoreCase("jpg")){
                    String strFileName = files[i].getAbsolutePath().toLowerCase();
                    imagePath.add(files[i].getAbsolutePath());
                }
            }
        }
        return imagePath;
    }

    public class ipChange extends Thread{
        public void run()
        {
            while(true)
            {
                switch (flag)
                {
                    case 0:
                        ip = ip1;
                        flag = 1;
                        break;
                    case 1:
                        ip = ip2;
                        flag = 0;
                        break;
                }
                try
                {
                    this.sleep(15000);
                }catch(InterruptedException e){

                }
            }
        }
    }

    public String uploadAllImage(final String path){
            ipChange ipThread = new ipChange();
            ipThread.start();
            Thread threadIp1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket(ip, port);
                        Log.i("socket", "connect");
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        DataInputStream inputStream = new DataInputStream(socket.getInputStream());

                        FileInputStream fileInputStream = new FileInputStream(path);
                        long size = path.length();
                        //String s = String.valueOf(size);

                        int bufferSize = 1024 * 1024;
                        byte[] buffer = new byte[bufferSize];
                        int length = 0;
                        //outputStream.write((s.getBytes()));
                        while ((length = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                            outputStream.flush();
                            Log.i("send", "send done");
                        }

                        socket.shutdownOutput();
                        fileInputStream.close();

                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threadIp1.start();
            i++;
        return "done";
    }
}
