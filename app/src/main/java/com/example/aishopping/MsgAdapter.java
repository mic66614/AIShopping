package com.example.aishopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aishopping.biz.ChatMessage;

import java.util.List;

// 定义一个继承自 RecyclerView.Adapter 的适配器类，用于管理聊天消息的显示
public class MsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // 消息列表，存储所有聊天消息
    private List<ChatMessage> messages;

    // 构造函数，初始化消息列表
    public MsgAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    // 获取消息类型
    @Override
    public int getItemViewType(int position) {
        // 如果消息是用户发送的，返回 1；如果是AI发送的，返回 0
        return messages.get(position).isSent() ? 1 : 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            // 创建AI消息视图
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_left, parent, false);
            return new AIViewHolder(view);
        } else {
            // 创建用户消息视图
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_right, parent, false);
            return new UserViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if(holder instanceof UserViewHolder){
            ((UserViewHolder) holder).messageText.setText(message.getText());
        }else {
            ((AIViewHolder) holder).messageText.setText(message.getText());
        }
    }

    // 返回消息列表的大小
    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;

        public UserViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.right_msg);
        }
    }

    public static class AIViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;

        public AIViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.left_msg);
        }
    }

    // 删除最后一个消息
    public void removeLastMessage() {
        if (messages.size() > 0) {
            messages.remove(messages.size() - 1); // 删除最后一个元素
            notifyItemRemoved(messages.size()); // 通知 RecyclerView 删除了最后一个 message
        }
    }

}