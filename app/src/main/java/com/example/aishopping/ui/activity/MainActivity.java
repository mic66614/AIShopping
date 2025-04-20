package com.example.aishopping.ui.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.dashscope.common.Message;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.aishopping.CustomDrawerLayout;
import com.example.aishopping.DeepSeek;
import com.example.aishopping.HistoryAdapter;
import com.example.aishopping.OnMessageReceivedListener;
import com.example.aishopping.R;
import com.example.aishopping.RetrofitClient;
import com.example.aishopping.TokenManager;
import com.example.aishopping.biz.MessageRequest;
import com.example.aishopping.biz.MessageReview;
import com.example.aishopping.biz.MyMessage;
import com.example.aishopping.biz.Result;
import com.example.aishopping.biz.User;
import com.example.aishopping.service.ApiService;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@Slf4j
public class MainActivity extends AppCompatActivity implements OnMessageReceivedListener, HistoryAdapter.OnItemClickListener {
    private CustomDrawerLayout drawerLayout;
    private GestureDetector gestureDetector; // 手势检测
    private View bottomLayout;
    private View bodyLayout;
    private View bodyLinear;
    private View topLayout;
    private View bodyContainerLayout;
    private ImageView userImage;
    private MaterialButton up;
    private int keyboardHeight = 0; //键盘高度
    private static final long DOUBLE_CLICK_INTERVAL = 2000; // 两次点击的时间间隔阈值（2秒）
    private long lastClickTime = 0; // 上一次点击返回键的时间
    private boolean flag = false; // 标志，记录当前是否是聊天界面
    private ApiService apiService; // apiService实体
    private User user;
    // 创建DeepSeek实例
    private DeepSeek deepSeek;
    private TokenManager tokenManager;
    private String token;
    private int messageId; // 消息id
    private MessageRequest messageRequest;
    private String title;
    private List<Message> messageList;
    private NetworkRequest networkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 获取传递进来的数据
        Intent intent = getIntent();
        if(intent != null){
            messageId = intent.getIntExtra("messageId",0);
            title = intent.getStringExtra("title");
        }



        // 获取相关控件
        topLayout = findViewById(R.id.top);
        bottomLayout = findViewById(R.id.bottom);
        bodyLinear = findViewById(R.id.bodyLinear);
        bodyContainerLayout = findViewById(R.id.bodyContainer);
        userImage = findViewById(R.id.user_image);
        up = findViewById(R.id.up);

        // 获取token
        tokenManager = TokenManager.getInstance(getApplicationContext());
        token = tokenManager.getLoginToken();

        // 初始化
        init();
    }

    // Activity停止时，保存对话
    @Override
    protected void onStop() {
        super.onStop();
        List<MyMessage> myMessageList = convertToMyMessageList(deepSeek.getMessageList());
        
        if(myMessageList.size() > 2) {
            messageRequest = new MessageRequest();
            messageRequest.setId(messageId);
            messageRequest.setMessage(myMessageList.toString());

            // 如果没有标题 创建标题
            if (title == null){
                String titleMessage = myMessageList.get(2).toString();
                if(titleMessage.length() > 52){
                    title = titleMessage.substring(32,53);
                } else {
                    if(titleMessage.length() > 32)
                        title = titleMessage.substring(32,titleMessage.length()-2);
                }
            }
            messageRequest.setTitle(title);

            // 发起网络请求
            apiService.addMessage(messageRequest,token).enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Result userInfoResponse = response.body();
                        if (userInfoResponse.getCode() == 1) { // 1表示成功
                            // 上传成功
                        } else {
                            // 上传失败，显示错误信息
                            Log.d("错误",userInfoResponse.getMessage());
                            Toast.makeText(MainActivity.this, userInfoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (response.code() == 401) {
                            // 处理401未授权情况
                            Toast.makeText(MainActivity.this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                            // 跳转到登录页面
                            Intent intent = new Intent(MainActivity.this,Login.class);
                            startActivity(intent);
                        }else {
                            // 处理网络错误
                            Toast.makeText(MainActivity.this, "数据上传失败，请检查网络", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<Result> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void init(){
        // 初始化Retrofit
        apiService = RetrofitClient.getClient();

        // 设置键盘显示在EditText下方
        setupKeyboardListener();

        // 点击内容区时隐藏键盘
        bodyContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(MainActivity.this);
            }
        });
        bodyLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(MainActivity.this);
            }
        });

        // 侧边栏显示
        showLeft();

        // 发送消息功能
        send();

        // 新建对话功能
        newCommunicate();

        // 点击侧边栏左上角进入设置页面
        LinearLayout top = findViewById(R.id.drawer_top);
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent().setClass(MainActivity.this, UserCenter.class);
//                Intent intent = new Intent().setClass(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

        // 更新数据与UI
        loadDataAndUpdateUI();

        if (messageId == 0){
            deepSeek = new DeepSeek();
        }else {
            // 获取消息内容
            getMessage();
        }
    }

    //设置键盘显示在EditText下方
    private void setupKeyboardListener() {
        // 获取当前Activity的根视图（内容视图）
        View rootView = findViewById(android.R.id.content);
        // 为根视图的视图树观察者添加全局布局监听器
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // 创建Rect对象用于存储窗口可见区域坐标
            Rect rect = new Rect();
            // 获取当前窗口可见区域的坐标（排除状态栏和导航栏）
            rootView.getWindowVisibleDisplayFrame(rect);

            // 获取根视图的完整高度（包含不可见区域）
            int screenHeight = rootView.getHeight();
            // 计算当前键盘高度 = 屏幕总高度 - 可见区域底部坐标
            int currentKeyboardHeight = screenHeight - rect.bottom;

            if (currentKeyboardHeight > screenHeight * 0.15) { // 键盘弹出
                if (keyboardHeight != currentKeyboardHeight) {

                    // 获取相关控件高度
                    keyboardHeight = currentKeyboardHeight;
                    int bottomHeight = bottomLayout.getHeight();
                    int bodyLinearHeight = bodyLinear.getHeight();
                    int bodyLinearBottom = bodyLinear.getBottom();
                    int bodyContainerLayoutHeight = bodyContainerLayout.getHeight();

                    // 上移底部布局
                    move("Y",150,-keyboardHeight,bottomLayout);
                    //上移中间内容区
                    //如果中间的  消息区高度 高于 容器可显示高度，则上移同键盘的高度
                    if(bodyLinearBottom > (bodyContainerLayoutHeight - (keyboardHeight + bottomHeight))){
                        move("Y",150,-((keyboardHeight+bodyLinearHeight)-bodyContainerLayoutHeight),bodyLinear);
                    }
                }
            } else { // 键盘隐藏
                if (keyboardHeight > 0) {
                    keyboardHeight = 0;
                    // 底部布局复位
                    move("Y",150,keyboardHeight,bottomLayout);
                    // 中间内容区复位
                    move("Y",150,0,bodyLinear);
                }
            }
        });
    }

    // 侧边栏显示
    private void showLeft(){
        //点击左侧按钮显示侧边栏
        drawerLayout = findViewById(R.id.main);
        MaterialButton more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示侧边栏
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    //布局移动
    private void move(String direction,int speed,int distance,View view){
        // 创建平移动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view, // 目标视图
                "translation"+direction, // 移动方向（轴平移）X(左右)/Y（上下）
                distance); // 目标值 向上为distance为负值，向左distance为负值

        animator.setDuration(speed); // 动画持续时间（毫秒） 速度越快时间越短
        animator.setInterpolator(new AccelerateDecelerateInterpolator()); // 使用加速减速插值器
        animator.start(); // 开始动画

    }

    // 关闭当前焦点的键盘
    public void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity); // 备用视图
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 关闭指定 View 的键盘（如 EditText）
    public void hideKeyboardFromView(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 两次退出退出程序
    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis(); // 获取当前时间
        if (currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL) {
            // 如果两次点击的时间间隔小于阈值，则退出程序
            super.onBackPressed();
        } else {
            // 否则提示用户再次点击
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            lastClickTime = currentTime; // 更新上一次点击的时间
        }
    }

    // 新建对话
    public void newCommunicate(){
        MaterialButton new_con = findViewById(R.id.new_con);
        new_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    Toast.makeText(MainActivity.this, "已开启新对话", Toast.LENGTH_SHORT).show();
//                    recreate(); // 调用 recreate 方法重启 Activity
//                } else {
//                    // 对于低于 API 16 的设备，可以使用其他方式重启
//                    Intent intent = getIntent();
//                    Toast.makeText(MainActivity.this, "已开启新对话", Toast.LENGTH_SHORT).show();
//                    finish();
//                    startActivity(intent);
//                }
                if(networkRequest == null){
                    Intent intent = new Intent().setClass(MainActivity.this,MainActivity.class);
                    finish();
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "已开启新对话", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    // 获取用户信息
    public void getUserInfo(){
        // 显示加载对话框
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("正在加载...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 发起网络请求
        apiService.getUserInfo(token).enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result userInfoResponse = response.body();
                    if (userInfoResponse.getCode() == 1) { // 1表示成功
                        // 用户数据获取成功
                        user = (User)userInfoResponse.getData();
                        // 设置用户信息
                        setUser(user);
                    } else {
                        // 用户数据获取失败，显示错误信息
                        Toast.makeText(MainActivity.this, userInfoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 401) {
                        // 处理401未授权情况
                        Toast.makeText(MainActivity.this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                        // 跳转到登录页面
                        Intent intent = new Intent(MainActivity.this,Login.class);
                        startActivity(intent);
                    }else {
                        // 处理网络错误
                        Toast.makeText(MainActivity.this, "数据加载失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                }
                // 加载对话框关闭
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<Result<User>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // 加载对话框关闭
                progressDialog.dismiss();
            }
        });
    }

    // 获取所有的消息
    public void getAllMessage(){
        // 发起网络请求
        apiService.getAllMessage(token).enqueue(new Callback<Result<List<MessageReview>>>() {
            @Override
            public void onResponse(Call<Result<List<MessageReview>>> call, Response<Result<List<MessageReview>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result allMessageResponse = response.body();
                    if (allMessageResponse.getCode() == 1) { // 1表示成功
                        // 消息记录获取成功
                        setMessageHistory((List<MessageReview>)allMessageResponse.getData());
                    } else {
                        // 用户数据获取失败，显示错误信息
                        Toast.makeText(MainActivity.this, allMessageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 401) {
                        // 处理401未授权情况
                        Toast.makeText(MainActivity.this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                        // 跳转到登录页面
                        Intent intent = new Intent(MainActivity.this,Login.class);
                        startActivity(intent);

                    }else {
                        // 处理网络错误
                        Toast.makeText(MainActivity.this, "数据加载失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Result<List<MessageReview>>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getMessage(){
        // 创建RequestBody对象
        RequestBody messageIdBody = RequestBody.create(
                MediaType.parse("text/plain"),
                String.valueOf(messageId)
        );

        // 发起网络请求
        apiService.getMessage(messageIdBody,token).enqueue(new Callback<Result<MessageRequest>>() {
            @Override
            public void onResponse(Call<Result<MessageRequest>> call, Response<Result<MessageRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result messageResponse = response.body();
                    if (messageResponse.getCode() == 1) { // 1表示成功
                        // 消息获取成功
                        MessageRequest myMessageRequest = (MessageRequest)messageResponse.getData();
                        // 使用正则表达式匹配每个 Message 对象
                        Pattern pattern = Pattern.compile("MyMessage\\{role='(.*?)', content='(.*?)'\\}", Pattern.DOTALL);
                        Matcher matcher = pattern.matcher(myMessageRequest.getMessage());
                        messageList = new ArrayList<>();
                        while (matcher.find()) {
                            String role = matcher.group(1);
                            Log.d("别这样",role);
                            String content = matcher.group(2);

                            // 创建 Message 实体并添加到列表中
                            Message message = Message.builder()
                                    .role(role)
                                    .content(content)
                                    .build();
                            messageList.add(message);
                        }
                        // 创建实体
                        deepSeek = new DeepSeek(messageList);

                        // 设置消息
                        setMessage(myMessageRequest.getMessage());

                    } else {
                        // 用户数据获取失败，显示错误信息
                        Toast.makeText(MainActivity.this, messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 401) {
                        // 处理401未授权情况
                        Toast.makeText(MainActivity.this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                        // 跳转到登录页面
                        Intent intent = new Intent(MainActivity.this,Login.class);
                        startActivity(intent);

                    }else {
                        // 处理网络错误
                        Toast.makeText(MainActivity.this, "数据加载失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Result<MessageRequest>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 设置用户信息
    public void setUser(User user){
        // 加载用户头像
        Glide.with(this)
                .load(user.getUserPic()) // 图片资源
                .transform(new RoundedCorners(16)) // 设置圆角半径（单位：像素）
                .skipMemoryCache(true)  // 跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 跳过磁盘缓存
                .into(userImage);

        // 设置用户名
        TextView tvUsername = findViewById(R.id.tvUsername);
        tvUsername.setText(user.getUsername());
    }

    public void setMessageHistory(List<MessageReview> messageReviewList) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        HistoryAdapter historyAdapter = new HistoryAdapter(messageReviewList, this);
        recyclerView.setAdapter(historyAdapter);
        // 隐藏滚轮
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(false);
    }
    public void setMessage(String message){
        // 更改flag
        flag =true;

        // 清空中间内容区原有控件
        LinearLayout layout = findViewById(R.id.bodyLinear);
        layout.removeAllViews();

        // 获取子控件的父布局的布局参数
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();

        // 获取 FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // 修改 layout_gravity 属性
        layoutParams.gravity = Gravity.NO_GRAVITY; // 移除对齐方式

        // 将修改后的布局参数重新设置给子控件
        layout.setLayoutParams(layoutParams);

        // 创建一个新的 Fragment 实例 并将消息传递给它
        Cheat cheat = new Cheat();
        Bundle bundle = new Bundle();
        bundle.putString("message",message);
        cheat.setArguments(bundle);

        // 将 Fragment 添加到 FrameLayout 中
        fragmentTransaction.add(R.id.bodyLinear, cheat);

        // 提交事务
        fragmentTransaction.commitNow();
    }

    // 更新数据与UI
    public void loadDataAndUpdateUI(){
        // 获取用户数据
        getUserInfo();
        // 获取历史消息记录
        getAllMessage();
    }

    // 通信功能实现
    public void send(){
        // 获取按钮、编辑框
        EditText editText = findViewById(R.id.edit_text);

        // 给按钮绑定点击事件
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取编辑框文本
                String InputMessage = editText.getText().toString();
                // 获取 introduction2控件对象
                TextView introduction2 = (TextView) findViewById(R.id.introduction2);
                if(InputMessage.isEmpty()){
                    Toast.makeText(MainActivity.this, "你还没有输入内容哦！", Toast.LENGTH_SHORT).show();
                }else {
                    // 编辑框清空
                    editText.getText().clear();

                    // 隐藏键盘
                    hideKeyboard(MainActivity.this);

                    // 编辑框清除焦点
                    editText.clearFocus();

                    if (!flag){
                        // 更改flag
                        flag =true;

                        // 清空中间内容区原有控件
                        LinearLayout layout = findViewById(R.id.bodyLinear);
                        layout.removeAllViews();

                        // 获取子控件的父布局的布局参数
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();

                        // 获取 FragmentManager
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        // 修改 layout_gravity 属性
                        layoutParams.gravity = Gravity.NO_GRAVITY; // 移除对齐方式

                        // 将修改后的布局参数重新设置给子控件
                        layout.setLayoutParams(layoutParams);

                        // 创建一个新的 Fragment 实例
                        Cheat cheat = new Cheat();

                        // 将 Fragment 添加到 FrameLayout 中
                        fragmentTransaction.add(R.id.bodyLinear, cheat);

                        // 提交事务
                        fragmentTransaction.commitNow();

                        // 传递用户消息给Fragment
                        onMessageReceived(InputMessage,true,false);
                        // 传递等待消息，提示用户AI正在思考
                        onMessageReceived("AI正在思考中...",false,true);

                        // 调用AI回复
                        new Thread(){
                            @Override
                            public void run() {
                                String message = deepSeek.run(InputMessage);
                                runOnUiThread(()->{
                                    onMessageReceived(message,false,false);
                                });
                            }
                        }.start();

                    }else {
                        // 传递用户消息给Fragment
                        onMessageReceived(InputMessage,true,false);
                        // 传递等待消息，提示用户AI正在思考
                        onMessageReceived("AI正在思考中...",false,true);
                        // 调用AI回复
                        new Thread(){
                            @Override
                            public void run() {
                                String message = deepSeek.run(InputMessage);
                                runOnUiThread(()->{
                                    onMessageReceived(message,false,false);
                                });
                            }
                        }.start();
                    }

                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editText.getText().toString().isEmpty()){ // 如果编辑框为空，发送图标设置为未发送状态
                    // 发送键恢复
                    up.setIconTint(ColorStateList.valueOf(Color.BLACK));
                }else {
                    // 如果编辑框内有字，发送图标设置为未发送状态(蓝色)
                    int color = ContextCompat.getColor(getApplicationContext(), R.color.blue);
                    up.setIconTint(ColorStateList.valueOf(color));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    // 消息类型转换
    public List<MyMessage> convertToMyMessageList(List<Message> messageList) {
        List<MyMessage> myMessageList = new ArrayList<>();
        for (Message message : messageList) {
            MyMessage myMessage = new MyMessage();
            myMessage.setRole(message.getRole());
            myMessage.setContent(message.getContent());
            myMessageList.add(myMessage);
        }
        return myMessageList;
    }

    public List<Message> convertToMessageList(List<MyMessage> MymessageList) {
        List<Message> MessageList = new ArrayList<>();
        for (MyMessage myMessage : MymessageList) {
            Message message = new Message();
            message.setRole(myMessage.getRole());
            message.setContent(myMessage.getContent());
            MessageList.add(message);
        }
        return MessageList;
    }

    // 与Fragment通信
    @Override
    public void onMessageReceived(String message, Boolean isSent, Boolean wait) {
        Cheat cheat = (Cheat) getSupportFragmentManager().findFragmentById(R.id.bodyLinear);
        if (cheat != null) {
            cheat.receiveMessage(message,isSent,wait);
        }
    }

    @Override
    public void onItemClick(MessageReview item) {
        Intent intent = new Intent().setClass(MainActivity.this,MainActivity.class);
        intent.putExtra("messageId",item.getId());
        intent.putExtra("title",item.getTitle());
        finish();
        startActivity(intent);
    }
}