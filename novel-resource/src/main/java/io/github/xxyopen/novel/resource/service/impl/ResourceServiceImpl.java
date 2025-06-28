package io.github.xxyopen.novel.resource.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.xxyopen.novel.common.constant.CacheConsts;
import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.common.constant.SystemConfigConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.config.exception.BusinessException;
import io.github.xxyopen.novel.resource.dto.resp.SmsVerifyCodeRespDto;
import io.github.xxyopen.novel.resource.dto.resp.ImgVerifyCodeRespDto;
import io.github.xxyopen.novel.resource.manager.redis.VerifyCodeManager;
import io.github.xxyopen.novel.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

/**
 * 资源（图片/视频/文档）相关服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final VerifyCodeManager verifyCodeManager;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${novel.file.upload.path}")
    private String fileUploadPath;

    @Override
    public RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException {
        String sessionId = IdWorker.get32UUID();
        return RestResp.ok(ImgVerifyCodeRespDto.builder()
            .sessionId(sessionId)
            .img(verifyCodeManager.genImgVerifyCode(sessionId))
            .build());
    }

    @SneakyThrows
    @Override
    public RestResp<String> uploadImage(MultipartFile file) {
        LocalDateTime now = LocalDateTime.now();
        String savePath =
            SystemConfigConsts.IMAGE_UPLOAD_DIRECTORY
                + now.format(DateTimeFormatter.ofPattern("yyyy")) + File.separator
                + now.format(DateTimeFormatter.ofPattern("MM")) + File.separator
                + now.format(DateTimeFormatter.ofPattern("dd"));
        String oriName = file.getOriginalFilename();
        assert oriName != null;
        String saveFileName = IdWorker.get32UUID() + oriName.substring(oriName.lastIndexOf("."));
        File saveFile = new File(fileUploadPath + savePath, saveFileName);
        if (!saveFile.getParentFile().exists()) {
            boolean isSuccess = saveFile.getParentFile().mkdirs();
            if (!isSuccess) {
                throw new BusinessException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR);
            }
        }
        file.transferTo(saveFile);
        if (Objects.isNull(ImageIO.read(saveFile))) {
            // 上传的文件不是图片
            Files.delete(saveFile.toPath());
            throw new BusinessException(ErrorCodeEnum.USER_UPLOAD_FILE_TYPE_NOT_MATCH);
        }
        return RestResp.ok(savePath + File.separator + saveFileName);
    }

    @Override
    public RestResp<SmsVerifyCodeRespDto> sendSmsCode(String phone) throws Exception {
        String sessionId = IdWorker.get32UUID();

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(999999));
        // 存入 Redis
        stringRedisTemplate.opsForValue().set(CacheConsts.SMS_VERIFY_CODE_CACHE_KEY + sessionId,
                code, Duration.ofMinutes(5));

            sendSms(phone, code);


        return RestResp.ok(SmsVerifyCodeRespDto.builder()
                .sessionId(sessionId)
                .build());
    }

    private void sendSms(String phone, String code) {
        String username = "buqiu"; // 短信宝用户名
        String password = "XIANGRIKUI"; // 短信宝密码

        String content = String.format("欢迎注册阅界小说网，您的验证码为：%s，5分钟内有效。", code);
        try {
            String encodedContent = java.net.URLEncoder.encode(content, "UTF-8");
            String url = String.format("http://api.smsbao.com/sms?u=%s&p=%s&m=%s&c=%s",
                    username, md5(password), phone, encodedContent);

            java.net.URL requestUrl = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) requestUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String result = reader.readLine();
                if (!"0".equals(result)) {
                    throw new RuntimeException("短信发送失败，错误码：" + result);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("发送短信失败", e);
        }
    }

    private String md5(String plainText) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(plainText.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int val = b & 0xff;
            if (val < 16) sb.append("0");
            sb.append(Integer.toHexString(val));
        }
        return sb.toString();
    }

}
