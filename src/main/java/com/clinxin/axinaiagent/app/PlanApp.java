package com.clinxin.axinaiagent.app;

import com.clinxin.axinaiagent.advisor.MyLoggerAdvisor;
import com.clinxin.axinaiagent.chatmemory.FileBasedChatMemory;
import com.clinxin.axinaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class PlanApp {

    @Resource
    private VectorStore planAppVectorStore;

    @Resource
    private Advisor planAppRagCloudAdvisor;

//    @Resource
//    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕计划制定与时间管理领域的专家。开场向用户表明身份，" +
            "告知用户可咨询任何计划相关的难题，如目标设定、时间安排、任务优先级等。围绕日常计划、项目计划、长期目标" +
            "三种类型提问：日常计划询问时间分配、效率提升及习惯养成的困扰；项目计划询问资源协调、进度控制及团队协作" +
            "的矛盾；长期目标询问方向明确、动力维持及里程碑设定的问题。引导用户详述计划内容、当前进展、遇到障碍及" +
            "自身想法，以便给出专属解决方案。";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public PlanApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        // 初始基于内存的对话记忆
//        InMemoryChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，打印 INFO 级别日志
                        new MyLoggerAdvisor()
                        // 自定义推理增强 Advisor，用于增强模型推理能力
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础会话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record PlanReport(String title, List<String> suggestions) {
    }

    /**
     * AI 计划报告功能（实战结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public PlanReport doChatWithReport(String message, String chatId) {
        PlanReport planReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成计划报告，标题为{用户名}的计划报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(PlanReport.class);
        log.info("doChatWithReport.planReport: {}", planReport);
        return planReport;
    }

    /**
     * RAG 知识库问答功能
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用知识库问答
                .advisors(new QuestionAnswerAdvisor(planAppVectorStore))
                // 应用 RAG 检索增强服务（基于云知识库）
//                .advisors(planAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        PlanAppRagCustomAdvisorFactory.createPlanAppRagCustomAdvisor(
//                                planAppVectorStore, "计划之后")
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
