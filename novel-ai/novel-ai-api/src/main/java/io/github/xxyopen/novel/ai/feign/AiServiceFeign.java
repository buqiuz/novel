package io.github.xxyopen.novel.ai.feign;

import io.github.xxyopen.novel.ai.dto.resp.AiResponse;
import io.github.xxyopen.novel.ai.dto.req.AiTextRequest;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "novel-ai-service")
public interface AiServiceFeign {

    @PostMapping(ApiRouterConsts.API_INNER_AI_URL_PREFIX + "/continue")
    RestResp<String> continueText(@RequestParam("text") String text, @RequestParam("length") Double length);

    @PostMapping(ApiRouterConsts.API_INNER_AI_URL_PREFIX + "/expand")
    RestResp<String> expandText(@RequestParam("text") String text, @RequestParam("ratio") Double ratio);

    @PostMapping(ApiRouterConsts.API_INNER_AI_URL_PREFIX + "/condense")
    RestResp<String> condenseText(@RequestParam("text") String text, @RequestParam("ratio") Double ratio);

    @PostMapping(ApiRouterConsts.API_INNER_AI_URL_PREFIX + "/polish")
    RestResp<String> polishText(@RequestParam("text") String text);
}