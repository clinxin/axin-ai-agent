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

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "马上国庆放假了，但是我还没有做好国庆七天的计划，怎么办？";
        String answer = planApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案 + 测试网页抓取：恋爱案例分析
        testMessage("最近和对象吵架了，看看百度网站（baidu.com）的其他情侣是怎么解决矛盾的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘国庆七天旅游计划’PDF，包含行程安排、旅游景点和特产清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = planApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
        String message = "我的国庆出行目的地在上海静安区，请帮我找到 5 公里内适合一家人游玩的地点";
        String answer = planApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithImageSearchMcp() {
        // 测试图片搜索 MCP
        String chatId = UUID.randomUUID().toString();
        String message = "帮我搜索一些迪士尼乐园的图片";
        String answer =  planApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }

}

