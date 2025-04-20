package com.example.aishopping.ui.activity;

import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aishopping.R;
import com.example.aishopping.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UserCenter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_center);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        // 获取所有的 MaterialCardView
        MaterialCardView[] materialCardViews = new MaterialCardView[]{
                findViewById(R.id.user_layout),
                findViewById(R.id.account_security),
                findViewById(R.id.feedback),
                findViewById(R.id.user_agreement),
                findViewById(R.id.privacy_policy),
                findViewById(R.id.update),
                findViewById(R.id.about),
                findViewById(R.id.exit)
        };

        // 给MaterialCardView设置点击事件
        for (MaterialCardView materialCardView : materialCardViews) {
            materialCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  跳转对应界面
                    switch (v.getId()) {
                        case R.id.user_layout:
                            Intent intent = new Intent().setClass(UserCenter.this, UserInformation.class);
                            startActivity(intent);
                            break;
                        case R.id.account_security:
                            Intent intent1 = new Intent().setClass(UserCenter.this, AccountSecurity.class);
                            startActivity(intent1);
                            break;
                        case R.id.feedback:
                            Intent intent2 = new Intent().setClass(UserCenter.this, Feedback.class);
                            startActivity(intent2);
                            break;
                        case R.id.exit:
                            showLogoutConfirmationDialog();
                            break;
                    }
                }
            });
        }
    }

    // 初始化函数
    public void init(){
        // 返回按钮
        back();
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

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出", (dialog, which) -> {
                    // 执行退出登录操作
                    // 清除token
                    TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
                    tokenManager.clearTokens();
                    // 跳转主页
                    Intent intent = new Intent().setClass(UserCenter.this,MainActivity.class);
                    finishAffinity();
                    startActivity(intent);

                })
                .setNegativeButton("取消", null)
//                .setIcon(R.drawable.ic_logout) // 可以添加一个退出图标
                .show();
    }
}