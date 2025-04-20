package com.example.aishopping;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


// 重写DrawerLayout，扩大侧边栏滑动距离
public class CustomDrawerLayout extends DrawerLayout {
    private float startX;

    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = ev.getX();
                if (currentX - startX > 100) { // 检测向右滑动
                    openDrawer(GravityCompat.START);
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
