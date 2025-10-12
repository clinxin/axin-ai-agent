package com.clinxin.axinaiagent.controller;

import com.clinxin.axinaiagent.agent.AxinManus;
import com.clinxin.axinaiagent.app.PlanApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private PlanApp planApp;

    @Resource
    private ToolCallback[] alltools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用 AI 计划大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("plan_app/chat/sync")
    public String doChatWithPlanAppSync(String message, String chatId) {
        return planApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 计划大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "plan_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithPlanAppSSE(String message, String chatId) {
        return planApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用 AI 计划大师应用（ServerSentEvent 格式）
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "plan_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithPlanAppServerSentEvent(String message, String chatId) {
        return planApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用 AI 计划大师应用（ServerSentEvent 格式）
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "plan_app/chat/sse_emitter")
    public SseEmitter doChatWithPlanAppSseEmitter(String message, String chatId) {
        // 创建带超时时间的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时
        // 获取 Flux 响应式数据流并直接通过订阅推送给 SseEmitter
        planApp.doChatByStream(message, chatId)
                .subscribe(chuck -> {
                            // 处理消息
                            try {
                                sseEmitter.send(chuck);
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        sseEmitter::completeWithError,
                        // 处理完成
                        sseEmitter::complete);
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 智能体
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        AxinManus axinManus = new AxinManus(alltools, dashscopeChatModel);
        return axinManus.runStream(message);
    }
}
