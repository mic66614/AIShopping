package com.example.aishopping;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

// 自定义流式布局

public class FlowLayout extends ViewGroup {
    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalWidth = 0; // 总宽度
        int totalHeight = 0; // 总高度
        int lineWidth = 0; // 当前行宽度
        int lineHeight = 0; // 当前行高度

        measureChildren(widthMeasureSpec, heightMeasureSpec); // 测量子视图

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果当前行宽度加上子视图宽度超过父视图宽度，则换行
            if (lineWidth + childWidth > MeasureSpec.getSize(widthMeasureSpec)) {
                totalWidth = Math.max(totalWidth, lineWidth); // 更新总宽度
                totalHeight += lineHeight; // 更新总高度
                lineWidth = childWidth; // 重置当前行宽度
                lineHeight = childHeight; // 重置当前行高度
            } else {
                lineWidth += childWidth; // 累加当前行宽度
                lineHeight = Math.max(lineHeight, childHeight); // 更新当前行高度
            }
        }

        // 处理最后一行
        totalWidth = Math.max(totalWidth, lineWidth);
        totalHeight += lineHeight;

        // 设置最终的测量尺寸
        setMeasuredDimension(resolveSize(totalWidth, widthMeasureSpec), resolveSize(totalHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0; // 子视图的左边界
        int childTop = 0; // 子视图的上边界
        int lineWidth = 0; // 当前行宽度
        int lineHeight = 0; // 当前行高度

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                // 如果当前行宽度加上子视图宽度超过父视图宽度，则换行
                if (lineWidth + childWidth > getWidth()) {
                    childLeft = 0; // 重置左边界
                    childTop += lineHeight; // 更新上边界
                    lineWidth = 0; // 重置当前行宽度
                    lineHeight = 0; // 重置当前行高度
                }

                // 布局子视图
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);

                // 更新当前行宽度和高度
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);

                // 更新子视图的左边界
                childLeft += childWidth;
            }
        }
    }
}