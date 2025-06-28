package io.github.xxyopen.novel.resource.service;

import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.resource.dto.req.ImgVerifyCodeReqDto;
import io.github.xxyopen.novel.resource.dto.resp.ImgVerifyCodeRespDto;
import io.github.xxyopen.novel.resource.dto.resp.SmsVerifyCodeRespDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 资源（图片/视频/文档）相关服务类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
public interface ResourceService {

    /**
     * 获取图片验证码
     *
     * @throws IOException 验证码图片生成失败
     * @return Base64编码的图片
     */
    RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException;

    /**
     * 图片上传
     * @param file 需要上传的图片
     * @return 图片访问路径
     * */
    RestResp<String> uploadImage(MultipartFile file);

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 短信验证码响应DTO
     */
    RestResp<SmsVerifyCodeRespDto> sendSmsCode(String phone) ;

    /**
     * 验证图片验证码
     *
     * @param dto 请求DTO
     * @return void
     */
    RestResp<Void> verifyImgCode(ImgVerifyCodeReqDto dto);
}
