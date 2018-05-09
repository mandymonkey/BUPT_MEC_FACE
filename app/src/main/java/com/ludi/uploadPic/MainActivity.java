package com.ludi.uploadPic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PICK_IMAGE = 1; //相册选取
    private static final int REQUEST_CAPTURE = 2;  //拍照
    private static final int REQUEST_PERMISSION = 4;  //权限请求
    private ImageView iv, iv_receive;
    private Button take_btn, album_btn,simpleUpload,automaticUpload;
    private PermissionsChecker mPermissionsChecker; // 权限检测器
    static final String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private Uri imageUri,imageUriGray;//原图保存地址
    private boolean isClickCamera;
    private String imagePath, imagePathTake;
    private File file;

    private final Handler mHandler = new Handler();
    private TextView textViewsimple, textIp, textProcess,textReceive;

    private String result;
    private String host,port;
    private EditText mPortView,mHostView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListener();
        init();
    }


    private void initViews() {
        take_btn = (Button) findViewById(R.id.take_btn);
        album_btn = (Button) findViewById(R.id.album_btn);
        simpleUpload = (Button)findViewById(R.id.simpleUpload);
        iv = (ImageView) findViewById(R.id.iv);
        //iv_receive = (ImageView)findViewById(R.id.iv_receive);
        textViewsimple = (TextView)findViewById(R.id.textViewsimple);
        textIp = (TextView)findViewById(R.id.ip);
        //textProcess = (TextView)findViewById(R.id.process);
        //textReceive = (TextView)findViewById(R.id.receive);
        mHostView = (EditText)findViewById(R.id.host);
        mPortView = (EditText)findViewById(R.id.port);
        //automaticUpload = (Button)findViewById(R.id.automaticUpload);
    }

    private void initListener() {
        take_btn.setOnClickListener(this);
        album_btn.setOnClickListener(this);
        simpleUpload.setOnClickListener(this);
       // automaticUpload.setOnClickListener(this);
    }

    private void init() {
        mPermissionsChecker = new PermissionsChecker(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_btn:
                //检查权限(6.0以上做权限判断)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
                        startPermissionsActivity();
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
                isClickCamera = true;
                break;
            case R.id.album_btn:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
                        startPermissionsActivity();
                    } else {
                        selectFromAlbum();
                    }
                } else {
                    selectFromAlbum();
                }
                isClickCamera = false;
                break;
            case R.id.simpleUpload:
                String host = mHostView.getText().toString();
                String port  = mPortView.getText().toString();
                if(file != null){
                    final UploadMECsimple uploadMECsimple = new UploadMECsimple();
                    result = uploadMECsimple.uploadPic(file,host,Integer.parseInt(port));
                    updateSimple();
                }
                break;
/*            case R.id.automaticUpload:
                ExecutorService exec = Executors.newFixedThreadPool(2); // i为自定义的线程数量。
                new Upload().executeOnExecutor(exec);
                new Download().executeOnExecutor(exec);
                break;*/
        }
    }

    private void updateSimple(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                textViewsimple.setText(result);
            }
        });
    }

    class Download extends AsyncTask<String, Bitmap, String> {
        int k = 0;

        protected String doInBackground(String... params) {
            try{
                ServerSocket serverSocket = new ServerSocket(10000);
                while (true){
                    Socket socket = serverSocket.accept();
                    DataInputStream is = new DataInputStream(socket.getInputStream());
                    String fileName = System.currentTimeMillis() + "." + "JPG";
                    File file = new File("/storage/emulated/0/Download/" + fileName);
                    FileOutputStream bo = new FileOutputStream(file);
                    int bytesRead = 0;
                    byte[] buffer = new byte[1024*1024];

                    while ((bytesRead = is.read(buffer,0,buffer.length)) != -1){
                        bo.write(buffer,0,bytesRead);
                    }
                    bo.flush();
                    bo.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    k++;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    publishProgress(bitmap);
                }
            }catch(IOException e){
                e.printStackTrace();
            }

            return "over";
        }

        protected void onProgressUpdate(Bitmap... bitmap) {

            iv_receive.setImageBitmap(bitmap[0]);
            textReceive.setText("Receive: " + k);
        }
    }
    class Upload extends AsyncTask<String, Bitmap, String>{
        UploadAll uploadAll;

        protected String doInBackground(String... params)
        {

                    uploadAll = new UploadAll();
                    String ipath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
                    ArrayList<String> imagePath = uploadAll.refreshFileList(ipath);

                    for(String path : imagePath) {
                        uploadAll.uploadAllImage(path);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {

                        }

                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        publishProgress(bitmap);
                    }

            return  "over";
        }



        protected void onProgressUpdate(Bitmap... bitmap)
        {
                textIp.setText("Current IP: " + uploadAll.ip);
                iv.setImageBitmap(bitmap[0]);
                textProcess.setText("Upload: " + uploadAll.i + "/" + uploadAll.imagePath.size());
        }

    }

    /**
     * 打开系统相机
     */
    private void openCamera() {
        File file = new FileStorage().createIconFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(MainActivity.this, "ludi.bupt.edu.facerecdocker.fileprovider", file);//通过FileProvider创建一个content类型的Uri
        } else {
            imageUri = Uri.fromFile(file);
        }
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
        startActivityForResult(intent, REQUEST_CAPTURE);
    }

    /**
     * 从相册选择
     */
    private void selectFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION,
                PERMISSIONS);
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        imagePath = null;
        imageUri = data.getData();
        if (DocumentsContract.isDocumentUri(this, imageUri)) {
            //如果是document类型的uri,则通过document id处理
            String docId = DocumentsContract.getDocumentId(imageUri);
            if ("com.android.providers.media.documents".equals(imageUri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.downloads.documents".equals(imageUri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            //如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(imageUri, null);
        } else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            //如果是file类型的Uri,直接获取图片路径即可
            imagePath = imageUri.getPath();
        }

        //cropPhoto();
    }

    private void handleImageBeforeKitKat(Intent intent) {
        imageUri = intent.getData();
        imagePath = getImagePath(imageUri, null);
        //cropPhoto();
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection老获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void showPic(){
        Bitmap bitmap;
        try {
            if (isClickCamera) {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                imagePathTake = imageUri.getPath();
                file = new File(String.valueOf(imagePathTake));
            } else {
                bitmap = BitmapFactory.decodeFile(imagePath);
                file = new File(imagePath);
            }
            iv.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE://从相册选择
                if (Build.VERSION.SDK_INT >= 19) {
                    handleImageOnKitKat(data);
                } else {
                    handleImageBeforeKitKat(data);
                }
                showPic();
                break;
            case REQUEST_CAPTURE://拍照
                if (resultCode == RESULT_OK) {
                    //cropPhoto();
                    showPic();
                }
                break;
            case REQUEST_PERMISSION://权限请求
                if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
                    finish();
                } else {
                    if (isClickCamera) {
                        openCamera();
                    } else {
                        selectFromAlbum();
                    }
                }
                break;
        }
    }
}

