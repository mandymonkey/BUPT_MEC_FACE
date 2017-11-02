package com.ludi.uploadPic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

import java.io.File;

;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PICK_IMAGE = 1; //相册选取
    private static final int REQUEST_CAPTURE = 2;  //拍照
    //private static final int REQUEST_PICTURE_CUT = 3;  //剪裁图片
    private static final int REQUEST_PERMISSION = 4;  //权限请求
    private ImageView iv;
    private Button take_btn, album_btn,simpleUpload,glspUpload,upload,cloudUpload,cloudPUpload,trans,signup;
    private PermissionsChecker mPermissionsChecker; // 权限检测器
    static final String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private Uri imageUri,imageUriGray;//原图保存地址
    private boolean isClickCamera;
    private String imagePath, imagePathTake;
    private File file;

    private final Handler mHandler = new Handler();
    private TextView textViewsimple,textViewglsp,textView,textViewcloud,textViewCloudP,textresult;

    private String result;
    private EditText editText;
    String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLoadOpenCVLibs();
        initViews();
        initListener();
        init();
    }

    private String TAG = "CV";
    private void initLoadOpenCVLibs() {
        Boolean success = OpenCVLoader.initDebug();
        if(success){
            Log.i(TAG,"load CVlibs successfully");
            //trans.setText("click");
        }
    }

    private void initViews() {
        take_btn = (Button) findViewById(R.id.take_btn);
        album_btn = (Button) findViewById(R.id.album_btn);
        //trans = (Button)findViewById(R.id.trans);

        simpleUpload = (Button)findViewById(R.id.simpleUpload);
        glspUpload = (Button)findViewById(R.id.glspUpload);
        //upload = (Button)findViewById(R.id.upload);
        //cloudPUpload = (Button)findViewById(R.id.uploadcloudP);
        //cloudUpload = (Button)findViewById(R.id.uploadcloud);
        signup = (Button)findViewById(R.id.signup);

        editText = (EditText)findViewById(R.id.name);

        iv = (ImageView) findViewById(R.id.iv);

        textViewsimple = (TextView)findViewById(R.id.textViewsimple);
        textViewglsp = (TextView)findViewById(R.id.textViewglsp);
        //textView = (TextView)findViewById(R.id.textView);
        //textViewcloud = (TextView)findViewById(R.id.textViewcloud);
        //textViewCloudP = (TextView)findViewById(R.id.textViewcloudP);
        textresult = (TextView)findViewById(R.id.textresult);

    }

    private void initListener() {
        take_btn.setOnClickListener(this);
        album_btn.setOnClickListener(this);
        simpleUpload.setOnClickListener(this);
        glspUpload.setOnClickListener(this);
        //upload.setOnClickListener(this);
        //cloudUpload.setOnClickListener(this);
        //cloudPUpload.setOnClickListener(this);
        signup.setOnClickListener(this);
        //trans.setOnClickListener(this);
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
                if(file != null){
                    final UploadMECsimple uploadMECsimple = new UploadMECsimple();
                    result = uploadMECsimple.uploadPic(file);
                    updateSimple();
                }
                break;
            case R.id.glspUpload:
                if(file != null){
                    UploadMECglsp uploadMECglsp = new UploadMECglsp();
                    result = uploadMECglsp.uploadPic(file);
                    updateGLSP();
                }
                break;
//            case R.id.upload:
//                if(file != null){
//                    UploadmyScheme uploadmyScheme = new UploadmyScheme();
//                    result = uploadmyScheme.uploadPic(file);
//                    updateMY();
//                    break;
//                }
//            case R.id.uploadcloud:
//                if(file != null){
//                    UploadMCCSimple uploadMCCSimple = new UploadMCCSimple();
//                    result = uploadMCCSimple.uploadPic(file);
//                    updateMCCsimple();
//                    break;
//                }
//            case R.id.uploadcloudP:
//                if(file != null){
//                    UploadMCCglsp uploadMCCglsp = new UploadMCCglsp();
//                    result = uploadMCCglsp.uploadPic(file);
//                    updateMCCglsp();
//                    break;
//                }
            case R.id.signup:
                if(file != null && editText != null){
                    name = editText.getText().toString();
                    UploadInfo uploadInfo = new UploadInfo();
                    result = uploadInfo.uploadPic(file,name);
                    updateInfo();
                    break;
                }

//            case R.id.trans:
//                iv.setDrawingCacheEnabled(true);
//                Bitmap bitmap = Bitmap.createBitmap(iv.getDrawingCache());
//
//                Mat src = new Mat();
//                Mat gray = new Mat();
//                Utils.bitmapToMat(bitmap,src);
//
//                CascadeClassifier mJavaDetector = new CascadeClassifier("/storage/FaceDetect/haarcascade_frontalface_alt2.xml");
//                MatOfRect faceDetections = new MatOfRect();
//                mJavaDetector.detectMultiScale(src,faceDetections);
//                System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));
//                for (Rect rect : faceDetections.toArray()) {
//                    Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
//                    Mat mat = src.submat(rect);
//                    Mat face = new Mat();
//                    Size size = new Size(100,100);
//                    Imgproc.resize(mat,face,size);
//                    Imgproc.cvtColor(face,gray,Imgproc.COLOR_BGRA2GRAY);
//                    Utils.matToBitmap(gray,bitmap);
//                }

                //iv.setImageBitmap(bitmap);
                //iv.setDrawingCacheEnabled(false);
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

    private void updateGLSP(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                textViewglsp.setText(result);
            }
        });
    }

//    private void updateMY(){
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                textView.setText(result);
//            }
//        });
//    }
//
//    private void updateMCCsimple(){
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                textViewcloud.setText(result);
//            }
//        });
//    }
//
//    private void updateMCCglsp(){
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                textViewCloudP.setText(result);
//            }
//        });
//    }
//
    private void updateInfo(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                textresult.setText(result);
            }
        });
    }


    /**
     * 打开系统相机
     */
    private void openCamera() {
        File file = new FileStorage().createIconFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(MainActivity.this, "com.bugull.cameratakedemo.fileprovider", file);//通过FileProvider创建一个content类型的Uri
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

    /**
     * 裁剪
     */
//private void cropPhoto() {
//        File file = new FileStorage().createCropFile();
//        Uri outputUri = Uri.fromFile(file);//缩略图保存地址
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        }
//        intent.setDataAndType(imageUri, "image/*");
//        intent.putExtra("crop", "true");
//        intent.putExtra("aspectX", 2);
//        intent.putExtra("aspectY", 3);
//        intent.putExtra("scale", true);
//        intent.putExtra("return-data", false);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        intent.putExtra("noFaceDetection", true);
//        startActivityForResult(intent, REQUEST_PICTURE_CUT);
//    }

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
//            case REQUEST_PICTURE_CUT://裁剪完成
//                Bitmap bitmap;
//                try {
//                    if (isClickCamera) {
//                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                        imagePathTake = imageUri.getPath();
//                        file = new File(String.valueOf(imagePathTake));
//                    } else {
//                        bitmap = BitmapFactory.decodeFile(imagePath);
//                        file = new File(imagePath);
//                    }
//                    iv.setImageBitmap(bitmap);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
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
