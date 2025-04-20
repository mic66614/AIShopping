package com.example.aishopping.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountSecurity extends AppCompatActivity {
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_security);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化
        init();
    }

    public void init(){
        // 初始化Retrofit
        apiService = RetrofitClient.getClient();
        // 返回功能实现
        back();
        // 修改密码功能实现
        updatePassword();
    }

    // 左上角退返回
    public void back(){
        MaterialButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void updatePassword(){
        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取密码
                TextInputEditText etPassword = findViewById(R.id.etPassword);
                String password = Objects.requireNonNull(etPassword.getText()).toString();
                TextInputEditText etConfPassword = findViewById(R.id.etConfPassword);
                String confPassword = Objects.requireNonNull(etConfPassword.getText()).toString();

                // 合规性判断
                if(password.isEmpty()) {
                    Toast.makeText(AccountSecurity.this, "密码为空！", Toast.LENGTH_SHORT).show();
                } else {
                    if(password.length() <5 || password.length()>20){
                        Toast.makeText(AccountSecurity.this, "密码应为5-20位字母或数字！", Toast.LENGTH_SHORT).show();
                    } else if (!password.equals(confPassword)) {
                        Toast.makeText(AccountSecurity.this, "两次输入的密码不相同！", Toast.LENGTH_SHORT).show();
                    } else {
                        // 显示加载对话框
                        ProgressDialog progressDialog = new ProgressDialog(AccountSecurity.this);
                        progressDialog.setMessage("正在修改...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        // 创建RequestBody对象
                        RequestBody passwordBody = RequestBody.create(
                                MediaType.parse("text/plain"),
                                password
                        );

                        // 获取token
                        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
                        String token = tokenManager.getLoginToken();

                        // 发起网络请求
                        apiService.updatePassword(passwordBody,token).enqueue(new Callback<Result>() {
                            @Override
                            public void onResponse(Call<Result> call, Response<Result> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Result registerResponse = response.body();
                                    if (registerResponse.getCode() == 1) { // 1表示成功
                                        // 修改成功
                                        Toast.makeText(AccountSecurity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                                        // 修改完成后，退出当前页面
                                        finish();
                                    } else {
                                        // 注册失败，显示错误信息
                                        Toast.makeText(AccountSecurity.this, registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // 处理网络错误
                                    Toast.makeText(AccountSecurity.this, "密码修改失败，请检查网络", Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<Result> call, Throwable t) {
                                Toast.makeText(AccountSecurity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            }
        });
    }
}