package com.clinxin.axinaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AxinManusTest {

    @Resource
    private AxinManus axinManus;

    @Test
    void run() {
        String userPrompt = """  
                我的国庆出行目的地在上海静安区，请帮我找到 5 公里内适合一家人游玩的 3 个地点，
                并结合一些网络图片，制定一份简单的国庆游玩计划，
                并以 PDF 格式输出""";
        String answer = axinManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
