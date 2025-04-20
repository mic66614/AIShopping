package com.example.aishopping;

public interface OnMessageReceivedListener {
    void onMessageReceived(String message,Boolean isSent,Boolean wait);
}
