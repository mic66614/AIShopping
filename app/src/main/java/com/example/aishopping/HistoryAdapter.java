package com.example.aishopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aishopping.biz.MessageReview;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<MessageReview> historyList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MessageReview item);
    }

    public HistoryAdapter(List<MessageReview> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }


    // 将数据绑定到 ViewHolder 中的视图
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        // 获取当前位置的历史记录项
        MessageReview item = historyList.get(position);
        // 设置历史记录项的标题到 TextView
        holder.tvHistoryText.setText(item.getTitle());
        // 为整个历史记录项设置点击事件
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    // 获取历史记录列表的大小
    @Override
    public int getItemCount() {
        return historyList.size();
    }


    // 更新历史记录数据的方法
    public void updateData(List<MessageReview> newData) {
        this.historyList = newData;
        notifyDataSetChanged();
    }

    // 定义静态内部类 HistoryViewHolder，用于缓存视图
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryText;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryText = itemView.findViewById(R.id.tv_history_text);
        }
    }
}