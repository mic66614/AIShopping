package com.example.aishopping.ui.activity;

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
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {
    private ApiService apiService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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
        // 跳转登录实现
        toLogin();
        // 注册功能实现
        register();
    }

    // 注册功能实现
    public void register(){
        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户名密码
                TextInputEditText etUsername = findViewById(R.id.etUsername);
                String username = Objects.requireNonNull(etUsername.getText()).toString();
                TextInputEditText etPassword = findViewById(R.id.etPassword);
                String password = Objects.requireNonNull(etPassword.getText()).toString();
                TextInputEditText etConfPassword = findViewById(R.id.etConfPassword);
                String confPassword = Objects.requireNonNull(etConfPassword.getText()).toString();

                // 合规性判断
                if(username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Register.this, "用户名或密码为空！", Toast.LENGTH_SHORT).show();
                } else {
                    if((username.length() <5 || username.length()>20) || (password.length() <5 || password.length()>20)){
                        Toast.makeText(Register.this, "用户名和密码应为5-20位字母或数字！", Toast.LENGTH_SHORT).show();
                    } else if (!password.equals(confPassword)) {
                        Toast.makeText(Register.this, "两次输入的密码不相同！", Toast.LENGTH_SHORT).show();
                    } else {
                        // 显示加载对话框
                        ProgressDialog progressDialog = new ProgressDialog(Register.this);
                        progressDialog.setMessage("正在注册...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

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
                        apiService.register(usernameBody,passwordBody).enqueue(new Callback<Result>() {
                            @Override
                            public void onResponse(Call<Result> call, Response<Result> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Result registerResponse = response.body();
                                    if (registerResponse.getCode() == 1) { // 1表示成功
                                        // 注册成功
                                        // 保存token
                                        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
                                        tokenManager.saveTokens(registerResponse.getData().toString());

                                        // 注册完成后，直接登录 跳转主页面
                                        Intent intent = new Intent().setClass(Register.this, MainActivity.class);
                                        finish();
                                        startActivity(intent);

                                    } else {
                                        // 注册失败，显示错误信息
                                        Toast.makeText(Register.this, registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // 处理网络错误
                                    Toast.makeText(Register.this, "注册失败，请检查网络", Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<Result> call, Throwable t) {
                                Toast.makeText(Register.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            }
        });
    }

    // 跳转登录
    public void toLogin(){
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent().setClass(Register.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}