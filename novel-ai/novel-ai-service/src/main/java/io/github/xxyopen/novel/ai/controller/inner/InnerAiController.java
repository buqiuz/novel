package io.github.xxyopen.novel.ai.controller.inner;

import io.github.xxyopen.novel.ai.service.AiService;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "InnerAiController", description = "内部调用-AI模块")
@RestController
@RequestMapping(ApiRouterConsts.API_INNER_AI_URL_PREFIX)
@RequiredArgsConstructor
public class InnerAiController {

    private final AiService aiService;

    /**
     * AI 续写
     */
    @PostMapping("/continue")
    public RestResp<String> continueText(
            @RequestParam("text") String text,
            @RequestParam("length") Double length) {
        return RestResp.ok(aiService.continueText(text, length));
    }


    /**
     * AI 扩写
     */
    @PostMapping("/expand")
    public RestResp<String> expandText(
            @RequestParam("text") String text,
            @RequestParam("ratio") Double ratio) {
        return RestResp.ok(aiService.expandText(text, ratio));
    }


    /**
     * AI 缩写
     */
    @PostMapping("/condense")
    public RestResp<String> condenseText(
            @RequestParam("text") String text,
            @RequestParam("ratio") Double ratio) {
        return RestResp.ok(aiService.condenseText(text, ratio));
    }


    /**
     * AI 润色
     */
    @PostMapping("/polish")
    public RestResp<String> polishText(@RequestParam("text") String text) {
        return RestResp.ok(aiService.polishText(text));
    }

}