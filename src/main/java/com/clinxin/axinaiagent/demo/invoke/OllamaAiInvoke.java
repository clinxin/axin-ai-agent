package com.clinxin.axinaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.CommandLineRunner;

/**
 * Spring AI 框架调用 AI 大模型
 */
//@Component
public class OllamaAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel ollamaChatModel;

    @Override
    public void run(String... args) throws Exception {
        /*AssistantMessage output = ollamaChatModel.call(new Prompt("你好 ollama，我是 clinxin。"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());*/
    }
}
