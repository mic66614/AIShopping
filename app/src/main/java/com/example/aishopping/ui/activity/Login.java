package com.example.aishopping.ui.activity;

import android.app.MediaRouteButton;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aishopping.R;
import com.example.aishopping.RetrofitClient;
import com.example.aishopping.TokenManager;
import com.example.aishopping.biz.Result;
import com.example.aishopping.service.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    // 使用ViewBinding替代findViewById（推荐）
    TextInputEditText etUsername;
    TextInputEditText etPassword;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化
        init();
    }

    // 初始化方法
    public void init(){
        // 初始化Retrofit
        apiService = RetrofitClient.getClient();

        // 跳转注册实现
        toRegister();

        // 登录功能实现
        login();
    }

    // 跳转注册
    public void toRegister(){
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent().setClass(Login.this, Register.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void login(){
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示加载对话框
                ProgressDialog progressDialog = new ProgressDialog(Login.this);
                progressDialog.setMessage("正在登录...");
                progressDialog.setCancelable(false);
                progressDialog.show();


                // 获取用户名密码
                TextInputEditText etUsername = findViewById(R.id.etUsername);
                String username = etUsername.getText().toString();
                TextInputEditText etPassword = findViewById(R.id.etPassword);
                String password = etPassword.getText().toString();

                // 创建RequestBody对象
                RequestBody usernameBody = RequestBody.create(
                        MediaType.parse("text/plain"),
                        username
                );
                RequestBody passwordBody = RequestBody.create(
                        MediaType.parse("text/plain"),
                        password
                );

                // 发起网络请求
                apiService.login(usernameBody,passwordBody).enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Result loginResponse = response.body();
                            if (loginResponse.getCode() == 1) { // 1表示成功
                                // 登录成功
                                // 保存token
                                TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
                                tokenManager.saveTokens(loginResponse.getData().toString());

                                // 跳转主页面
                                Intent intent = new Intent().setClass(Login.this, MainActivity.class);
                                finish();
                                startActivity(intent);

                            } else {
                                // 登录失败，显示错误信息
                                Toast.makeText(Login.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 处理网络错误
                            Toast.makeText(Login.this, "登录失败，请检查网络", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable t) {
                        Toast.makeText(Login.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }
}