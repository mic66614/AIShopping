package com.example.aishopping;

import com.example.aishopping.biz.Result;
import com.example.aishopping.service.ApiService;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository() {
        apiService = RetrofitClient.getClient();
    }

    public Call<Result> register(String username, String password) {
        // 创建RequestBody对象
        RequestBody usernameBody = RequestBody.create(
                MediaType.parse("text/plain"),
                username
        );
        RequestBody passwordBody = RequestBody.create(
                MediaType.parse("text/plain"),
                password
        );
        return apiService.register(usernameBody,passwordBody);
    }

    public Call<Result> login(String username, String password) {
        // 创建RequestBody对象
        RequestBody usernameBody = RequestBody.create(
                MediaType.parse("text/plain"),
                username
        );
        RequestBody passwordBody = RequestBody.create(
                MediaType.parse("text/plain"),
                password
        );
        return apiService.login(usernameBody,passwordBody);
    }
}
