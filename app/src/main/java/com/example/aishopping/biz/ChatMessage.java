package com.example.aishopping.biz;

public class ChatMessage {
    private String text;
    private boolean isSent;

    public ChatMessage(String text, boolean isSent) {
        this.text = text;
        this.isSent = isSent;
    }

    public String getText() {
        return text;
    }

    public boolean isSent() {
        return isSent;
    }
}
