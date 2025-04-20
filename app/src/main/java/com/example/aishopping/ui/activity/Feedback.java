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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class Feedback extends AppCompatActivity {
    // PICK_IMAGE_REQUEST是一个int常量，用于在onActivityResult中识别这个请求
    private static final int REQUEST_CODE_PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化
        init();
    }

    // 初始化函数
    public void init(){
        // 返回按钮
        back();
        // 添加图片功能实现
        addPicture();
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

    public void addPicture(){
        // 添加图片功能实现
        MaterialCardView addPicture = findViewById(R.id.add_picture);
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //用户选择图片功能实现
                Intent intent = new Intent();
                // 显示所有图片类型的intent
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);

                // 启动图片选择器
                startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_PICK_IMAGE);
            }
        });
    }
}