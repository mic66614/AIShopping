package com.example.aishopping.biz;

public class MyMessage {
    private String role;
    private String content;

    public MyMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public MyMessage() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MyMessage{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
