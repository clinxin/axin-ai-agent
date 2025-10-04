package com.clinxin.axinaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebScrapingToolTest {

    @Test
    void scrapWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.baidu.com";
        String result = webScrapingTool.scrapWebPage(url);
        Assertions.assertNotNull(result);
    }
}
