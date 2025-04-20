package com.example.aishopping;

import java.util.ArrayList;
import java.lang.System;
import java.util.List;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

public class DeepSeek {
    private List<Message> messageList = new ArrayList<>();
    private final String API_KEY = "sk-0e1f47a439034e4aa365f839a918e845";

    // 无参构造方法(初次创建)
    public DeepSeek(){
            // 初始化AI角色设定
            Message message = creatMessage(1,"现在给你一个新的身份，我的智能购物助手，你的职责是根据我的要求帮我推荐商品。\n" +
                    "例如：\n" +
                    "我说我想要一款手机，你为了给我推荐商品，会像导购员那样，不断询问我相关信息。\n" +
                    "如：你想要大概什么价位的手机，我回答：3000-5000元。\n" +
                    "这些信息还不足以让你准确了解我想要什么，所以你可以接着问。\n" +
                    "如：那你平时是打游戏多，摄影多，还是有其他需求呢。\n" +
                    "我回答：打游戏多。\n" +
                    "你可以接着问：那你对品牌有什么要求吗\n" +
                    "我回答：国产品牌都可以\n" +
                    "正如上面的例子，你通过像导购员或者超市推销员之类的不断询问我相关的问题，但是注意，不要一下子问一堆东西，比如：“你对手机的型号，颜色，尺寸有什么要求，你更喜欢国产品牌还是国外品牌...”    \n" +
                    "你要一个问题一个问题循序渐进的询问，直到获取到足够的信息，开始给用户推荐商品，如果足够多，可以推荐多个商品，以供用户选择。\n" +
                    "\n" +
                    "推荐商品的格式为：\n" +
                    "商品名称：xxx\n" +
                    "商品相关信息：\n" +
                    "推荐理由：\n" +
                    "商品链接：淘宝，京东等平台该商品的链接，或者是其他购买链接。\n" +
                    "此外，你的回答请使用纯文本，不要带有表情符号");

            Message message2 = creatMessage(0,"好的，我明白了。作为你的智能购物助手，我会一步一步地询问你需要的信息，以便为你推荐最适合的商品。那我们开始吧！");

            // 将消息体加入到历史消息集合中
            messageList.add(message);
            // 将AI消息体加入到历史消息集合中
            messageList.add(message2);
    }

    public DeepSeek(List<Message> messageList) {
        this.messageList = messageList;
    }

    public GenerationResult callWithMessage(List messageList) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(API_KEY)
                .model("qwen-plus")
                .messages(messageList)
                // 不可以设置为"text"
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        return gen.call(param);
    }

    //构建消息体
    public Message creatMessage(int role,String message){
        // 0代表AI 1代表用户
        if(role == 0){
            return Message.builder()
                    .role(Role.ASSISTANT.getValue())
                    .content(message)
                    .build();
        }else {
            return Message.builder()
                    .role(Role.USER.getValue())
                    .content(message)
                    .build();
        }
    }

    public String run(String userMessage) {
        try {
            // 接收用户传递进来的信息，并创建消息体
            Message message = creatMessage(1,userMessage);
            // 将消息体加入到历史消息集合中
            messageList.add(message);
            // 开始请求响应
            GenerationResult result = callWithMessage(messageList);
//            System.out.println("思考过程：");
//            System.out.println(result.getOutput().getChoices().get(0).getMessage().getReasoningContent());
//            System.out.println("回复内容：");
            // 接收AI返回回复数据
            String AIMessage = result.getOutput().getChoices().get(0).getMessage().getContent();
            // 创建AI消息体
            message = creatMessage(0,AIMessage);
            // 将AI消息体加入到历史消息集合中
            messageList.add(message);

            // 将AI返回回复数据返回给主页面
            return AIMessage;
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            // 使用日志框架记录异常信息
            System.err.println("An error occurred while calling the generation service: " + e.getMessage());
            return e.getMessage();
        }
    }
    public List getMessageList() {
        return messageList;
    }
}