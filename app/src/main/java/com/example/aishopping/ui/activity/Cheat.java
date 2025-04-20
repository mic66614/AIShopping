package com.example.aishopping.ui.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.dashscope.common.Message;
import com.example.aishopping.MsgAdapter;
import com.example.aishopping.OnMessageReceivedListener;
import com.example.aishopping.R;
import com.example.aishopping.biz.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cheat extends Fragment {
    private RecyclerView recyclerView;
    private MsgAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private OnMessageReceivedListener listener; // 消息传递接口
    private GestureDetector gestureDetector; // 手势检测

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_cheat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new MsgAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        Bundle bundle = getArguments();
        if(bundle != null){
            // 使用正则表达式匹配每个 Message 对象
            String message = bundle.getString("message");
            Pattern pattern = Pattern.compile("MyMessage\\{role='(.*?)', content='(.*?)'\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(message);

            for (int i = 0; matcher.find(); i++) {
                if(i>1){
                    String role = matcher.group(1);
                    String content = matcher.group(2);

                    if(role.equals("user")) {
                        messages.add(new ChatMessage(content, true));
                    }else {
                        messages.add(new ChatMessage(content, false));
                    }
                    adapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            }
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMessageReceivedListener) {
            listener = (OnMessageReceivedListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnMessageListener");
        }
    }

    // 接受Activity传递进来的数据
    public void receiveMessage(String message,Boolean isSent,Boolean wait) {
        if(!wait && !isSent){
            // 如果不是等待消息，且不是用户消息需要先将上一条等待消息删除
            adapter.removeLastMessage();
        }
        messages.add(new ChatMessage(message, isSent));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }


    private void showLeft(){
        //设置侧边栏全局滑动
//        // 初始化手势检测
//        gestureDetector = new GestureDetector(this,
//                new DrawerGestureListener(drawerLayout, GravityCompat.START));
//
//        // 设置根布局触摸监听
//        findViewById(R.id.main).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//        });
    }
}