package com.example.aishopping.service;


import com.example.aishopping.biz.MessageRequest;
import com.example.aishopping.biz.MessageReview;
import com.example.aishopping.biz.MyMessage;
import com.example.aishopping.biz.Result;
import com.example.aishopping.biz.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.internal.ws.RealWebSocket;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("user/login")
    Call<Result> login(@Part("username") RequestBody username,
                       @Part("password") RequestBody password);

    @Multipart
    @POST("user/register")
    Call<Result> register(@Part("username") RequestBody username,
                          @Part("password") RequestBody password);

    @GET("user/userInfo")
    Call<Result<User>> getUserInfo(
            @Header("Authorization") String token
    );

    @Multipart
    @PUT("user/update")
    Call<Result> updateUser(
            @Part("id") RequestBody id,
            @Part("username") RequestBody username,
            @Part("resume") RequestBody resume,
            @Part MultipartBody.Part userPic,
            @Header("Authorization") String token
    );

    @Multipart
    @POST("user/password")
    Call<Result> updatePassword(
            @Part("password") RequestBody password,
            @Header("Authorization") String token
    );

    @POST("message/addMessage")
    Call<Result> addMessage(
            @Body MessageRequest messageRequest,
            @Header("Authorization") String token
    );

    @GET("message/allMessage")
    Call<Result<List<MessageReview>>> getAllMessage(
            @Header("Authorization") String token
    );

    @Multipart
    @POST("message/getMessage")
    Call<Result<MessageRequest>> getMessage(
            @Part("messageId") RequestBody messageId,
            @Header("Authorization") String token
    );
}
