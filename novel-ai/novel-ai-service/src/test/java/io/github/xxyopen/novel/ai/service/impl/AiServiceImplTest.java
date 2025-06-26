package io.github.xxyopen.novel.ai.service.impl;

import io.github.xxyopen.novel.ai.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AiServiceImplTest {

    @Autowired
    private AiService aiService;
    @Test
    void testContinueText() {
        String input = "从前有一座山";
        Double length = 200.0;
        String result = aiService.continueText(input, length);

        System.out.println("续写结果：" + result);
        assertThat(result).isNotBlank();
    }
    @Test
    void testExpandText() {
        String input = "这本书很有趣，它讲述了...";
        Double ratio = 120.0;
        String result = aiService.expandText(input, ratio);

        System.out.println("扩写结果：" + result);
        assertThat(result).isNotBlank();
    }
    @Test
    void testCondenseText() {
        String input = "这是一个关于春天的故事，阳光明媚，万物复苏，花开满园，鸟语花香，微风拂面，令人心旷神怡。";
        Double ratio = 120.0;
        String result = aiService.expandText(input, ratio);

        System.out.println("扩写结果：" + result);
        assertThat(result).isNotBlank();
    }
    @Test
    void testPolishText() {
        String input = "这本书很好看，故事讲得很好，我很喜欢。";
        String result = aiService.polishText(input);

        System.out.println("润色结果：" + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void testCallDashScopeModel() {
        String input = "请帮我写一首关于春天的诗。";
        String result = aiService.polishText(input);

        System.out.println("AI 返回结果：" + result);
        assertThat(result).isNotBlank();
    }
}


