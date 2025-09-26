package com.clinxin.axinaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class PlanAppTest {

    @Resource
    private PlanApp planApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是 clinxin";
        String answer = planApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让我的计划“国庆一日游”，更详细一点";
        answer = planApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的上一个计划叫什么来着？请帮我回忆一下";
        answer = planApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是 clinxin，我想让我的计划“国庆一日游”，更详细一点";
        PlanApp.PlanReport planReport = planApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(planReport);
    }
}

