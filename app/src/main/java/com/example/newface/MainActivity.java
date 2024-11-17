package com.example.newface;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_PICK_IMAGE = 101;

    private EditText mServerAddressEditText;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private ImageView mImageView;
    private Button mUploadButton;
    private File ResultFile;
    private SimpleDraweeView draweeView ;

    // 在创建Activity时执行的操作
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(MainActivity.this);
        setContentView(R.layout.activity_main);

        // 初始化UI控件
//        draweeView = findViewById(R.id.image_view);
        mImageView= findViewById(R.id.image_view);
        mUploadButton = findViewById(R.id.upload_button);

        // 设置上传按钮的点击事件处理逻辑
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
                System.out.println("----------获取的文件路径"+ResultFile);

            }
        });
    }

    // 选择图片
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // 上传图片
    @SuppressLint("StaticFieldLeak")
    private void uploadImage(final Uri uri) {
        // 获取相关配置信息
        final String serverAddress = "i-2.gpushare.com";
        final String username = "root";
        final String password = "gcTaYnRBFrHkR5v6c6aQ7vcke7fRq8Kx";

        // 检查相关配置信息是否为空
        if (TextUtils.isEmpty(serverAddress) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入服务器地址、用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 启动异步任务处理上传操作
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // 显示进度条
                showProgressDialog("图片处理中，请稍候...");
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected String doInBackground(Void... voids) {
                try {

                    // 连接到服务器
                    System.out.println("开始来连接服务器");
                    JSch jsch = new JSch();
                    Session session = jsch.getSession(username, serverAddress, 42060);
                    session.setPassword(password);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();
                    if (session.isConnected()) {
                        // 连接成功
                        System.out.println("连接成功，开始上传");
                    } else {
                        // 连接失败
                        System.out.println("连接失败");
                    }

                    // 创建sftp通道并上传图片文件
                    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
                    channel.connect();
                    channel.cd("/root/hy-tmp/DECA/TestSamples/examples");
                    String fileName = new File(uri.getPath()).getName();
                    if(!fileName.matches(".*\\..*")){  //如果文件名不包含扩展名
                        fileName += ".jpg";  //在文件名后添加.jpg作为扩展名
                    }
                    channel.put(getContentResolver().openInputStream(uri), fileName);
                    channel.disconnect();
                    System.out.println("上传完成");
                    System.out.println("开始执行算法");
                    // 执行算法处理
                    ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
                    String command = "/root/hy-tmp/DECA/face.sh";
                    System.out.println("-------设置指令-------");
                    channelExec.setCommand(command);
                    System.out.println("---------设置输入流--------");
                    InputStream inputStream = channelExec.getInputStream();
                    InputStream stderr = channelExec.getErrStream();
                    BufferedReader brErr = new BufferedReader(new InputStreamReader(stderr));
                    System.out.println("-----开始连接，执行算法------");
                    channelExec.connect();
                    System.out.println("------判断连接是否成功-------");
                    String line = null;
                    System.out.println("处理流的数据");
                    while ((line = brErr.readLine()) != null) {
                        // 处理错误流中的数据
                        System.out.println("错误"+line);
                    }
                    int count = 0;
//                    while (true) {
//                        count += 1;
//                        System.out.println(count +"次循环");
//                        if (channelExec.isClosed()) {
//                            if (inputStream.available() > 0) continue;
//                            String line = null;
//                            System.out.println("处理流的数据");
//                            while ((line = brErr.readLine()) != null) {
//                                // 处理错误流中的数据
//                                System.out.println("错误"+line);
//                            }
//                            System.out.println("Exit status: " + channelExec.getExitStatus());
//                            break;
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    channelExec.disconnect();
                    System.out.println("算法执行完成，开始下载图片");
                    // 下载处理后的图片文件
                    channel = (ChannelSftp) session.openChannel("sftp");
                    channel.connect();
                    channel.cd("/root/hy-tmp/DECA/TestSamples/teaser/results");
                    File resultFile = new File(getCacheDir(), "teaser.gif");
                    channel.get("teaser.gif", new FileOutputStream(resultFile));
                    System.out.println("下载图片完成，展示图片");
                    // 断开连接
                    channel.disconnect();
                    session.disconnect();
                    Glide.with(this)
                            .asGif()
                            .load(resultFile)
                            .into(mImageView);

                    // 返回处理后的图片文件路径
                    return resultFile.getAbsolutePath();

                } catch (Exception e) {
                    Log.e(TAG, "发生错误", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String resultPath) {
                super.onPostExecute(resultPath);
                // 隐藏进度条
                hideProgressDialog();

                // 检查是否处理成功并显示处理后的图片
                if (TextUtils.isEmpty(resultPath)) {
                    Toast.makeText(MainActivity.this, "处理图片失败，请重试", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("图片设置成功");
//                    Bitmap bitmap = BitmapFactory.decodeFile(resultPath);
//                    mImageView.setImageBitmap(bitmap);
                    System.out.println(resultPath);
//                    String path1 = resultPath;
//                    String path2 = "/data/data/com.example.newface/cache/teaser.gif";
//                    File file1 = new File(path1);
//                    File file2 = new File(path2);
//                    if(file1.exists()){
//                        System.out.println("--------"+path1+"存在--------");
//                    }else{
//                        System.out.println("-------"+path1+"不存在------");
//                    }
//                    if(file2.exists()){
//                        System.out.println("--------"+path2+"存在--------");
//                    }else{
//                        System.out.println("-------"+path2+"不存在------");
//                     }
                }
            }
        }.execute();
    }

    // 处理选择图片后返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            // 获取选择的图片路径并上传
            Uri selectedImageUri = data.getData();

            uploadImage(selectedImageUri);
        }
    }

    private ProgressDialog mProgressDialog;

    // 显示进度条
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    // 隐藏进度条
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
