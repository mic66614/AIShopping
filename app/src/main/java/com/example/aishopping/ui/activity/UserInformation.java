package com.example.aishopping.ui.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.aishopping.R;
import com.example.aishopping.RetrofitClient;
import com.example.aishopping.TokenManager;
import com.example.aishopping.biz.Result;
import com.example.aishopping.biz.User;
import com.example.aishopping.biz.UserRequest;
import com.example.aishopping.service.ApiService;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Url;

@Slf4j
public class UserInformation extends AppCompatActivity {
    // PICK_IMAGE_REQUEST是一个int常量，用于在onActivityResult中识别这个请求
    private static final int REQUEST_PICK_IMAGE = 101;
    private static final int REQUEST_CROP_IMAGE = 102;
    private ApiService apiService; // apiService实体
    User user; // user实体
    UserRequest userRequest; // userRequest实体
    private ImageView userImage;
    EditText edUsername;
    EditText edUserResume;
    File imageFile;
    MultipartBody.Part userPicPart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化
        init();
        userImage = findViewById(R.id.user_image);
        edUsername = findViewById(R.id.edUsername);
        edUserResume = findViewById(R.id.edUserResume);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // 获取图片
            Uri imageUri = data.getData();

            uriToFile(imageUri); // 转换为 File

            Glide.with(this)
                    .load(imageFile) // 图片资源
                    .transform(new RoundedCorners(16)) // 设置圆角半径（单位：像素）
                    .skipMemoryCache(true)  // 跳过内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 跳过磁盘缓存
                    .into(userImage);
        }
    }

    // 初始化函数
    public void init(){
        // 初始化Retrofit
        apiService = RetrofitClient.getClient();
        // 返回按钮
        back();
        // 修改头像功能实现
        choiceImage();
        // 修改用户信息实现
        uploadUserInfo();
        // 初始化
        loadDataAndUpdateUI();
    }

    // 返回按钮
    public void back(){
        MaterialButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void choiceImage(){
        // 添加图片功能实现
        ImageView userImage = findViewById(R.id.user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //用户选择图片功能实现
                Intent intent = new Intent();
                // 显示所有图片类型的intent
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                // 启动图片选择器
                startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_PICK_IMAGE);
            }
        });
    }

    // 获取用户信息
    public void getUserInfo(){
        // 显示加载对话框
        ProgressDialog progressDialog = new ProgressDialog(UserInformation.this);
        progressDialog.setMessage("正在加载...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 获取token
        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
        String token = tokenManager.getLoginToken();

        // 发起网络请求
        apiService.getUserInfo(token).enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result userInfoResponse = response.body();
                    if (userInfoResponse.getCode() == 1) { // 1表示成功
                        // 用户数据获取成功
                        user = (User)userInfoResponse.getData();
                        // 设置用户信息
                        setUser(user);
                    } else {
                        // 用户数据获取失败，显示错误信息
                        Toast.makeText(UserInformation.this, userInfoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 401) {
                        // 处理401未授权情况
                        Toast.makeText(UserInformation.this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                        // 跳转到登录页面
                        Intent intent = new Intent(UserInformation.this,Login.class);
                        startActivity(intent);

                    }else {
                        // 处理网络错误
                        Toast.makeText(UserInformation.this, "数据加载失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                }
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<Result<User>> call, Throwable t) {
                Toast.makeText(UserInformation.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    // 设置用户信息
    public void setUser(User user){
        // 加载用户头像
        Glide.with(this)
                .load(user.getUserPic()) // 图片资源
                .transform(new RoundedCorners(16)) // 设置圆角半径（单位：像素）
                .skipMemoryCache(true)  // 跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 跳过磁盘缓存
                .into(userImage);
        // 设置用户名
        edUsername.setText(user.getUsername());
        // 设置用户简介
        edUserResume.setText(user.getResume());
    }

    // 更新数据与UI
    public void loadDataAndUpdateUI(){
        getUserInfo();
    }

    public void uploadUserInfo(){
        MaterialButton save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = user.getId();
                String username = edUsername.getText().toString();
                String userResume = edUserResume.getText().toString();

                // 创建RequestBody对象
                RequestBody userIdBody = RequestBody.create(
                        MediaType.parse("text/plain"),
                        String.valueOf(id)
                );
                RequestBody usernameBody = RequestBody.create(
                        MediaType.parse("text/plain"),
                        username
                );
                RequestBody userResumeBody = RequestBody.create(
                        MediaType.parse("text/plain"),
                        userResume
                );
                if(imageFile != null){
                    RequestBody userPicBody = RequestBody.create(
                            MediaType.parse("image/*"),
                            imageFile
                    );
                    userPicPart = MultipartBody.Part.createFormData("userPic", imageFile.getName(), userPicBody);
                }


                // 显示加载对话框
                ProgressDialog progressDialog = new ProgressDialog(UserInformation.this);
                progressDialog.setMessage("正在加载...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // 获取token
                TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
                String token = tokenManager.getLoginToken();

                // 发起网络请求
                apiService.updateUser(userIdBody,usernameBody,userResumeBody,userPicPart,token).enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Result userInfoResponse = response.body();
                            if (userInfoResponse.getCode() == 1) { // 1表示成功
                                // 用户数据更新成功
                                Toast.makeText(UserInformation.this, "信息更改成功,请重新登录", Toast.LENGTH_SHORT).show();

                                // 清除原有token
                                TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
                                tokenManager.clearTokens();

                                // 回到主页
                                Intent intent = new Intent().setClass(UserInformation.this,Login.class);
                                finishAffinity();
                                startActivity(intent);
                            } else {
                                // 信息更改失败
                                Toast.makeText(UserInformation.this, userInfoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                edUsername.setText(userInfoResponse.getMessage());
                            }
                        } else {
                            if (response.code() == 401) {
                                // 处理401未授权情况
                                Toast.makeText(UserInformation.this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                                // 跳转到登录页面
                                Intent intent = new Intent(UserInformation.this,Login.class);
                                startActivity(intent);
                            }else {
                                // 处理网络错误
                                Toast.makeText(UserInformation.this, "数据加载失败，请检查网络", Toast.LENGTH_SHORT).show();
                            }
                        }
                        progressDialog.dismiss();
                    }
                    @Override
                    public void onFailure(Call<Result> call, Throwable t) {
                        Toast.makeText(UserInformation.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    // 将Uri转成File
    public void uriToFile(Uri uri){
        imageFile = new File(getCacheDir(), "temp_image.jpg");
        if (imageFile.exists()) {
            imageFile.delete(); // 删除旧文件
        }
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(imageFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}