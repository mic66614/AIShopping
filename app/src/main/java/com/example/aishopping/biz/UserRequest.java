package com.example.aishopping.biz;

import java.io.File;

public class UserRequest {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String resume;
    private File userPic;

    public UserRequest(int id, File userPic, String resume, String salt, String password, String username) {
        this.id = id;
        this.userPic = userPic;
        this.resume = resume;
        this.salt = salt;
        this.password = password;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public File getUserPic() {
        return userPic;
    }

    public void setUserPic(File userPic) {
        this.userPic = userPic;
    }
}
