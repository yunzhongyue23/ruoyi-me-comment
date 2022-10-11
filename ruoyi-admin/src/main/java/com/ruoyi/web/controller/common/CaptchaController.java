package com.ruoyi.web.controller.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.Anonymous;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.code.kaptcha.Producer;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.sign.Base64;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 验证码操作处理
 * 
 * @author ruoyi
 */
@RestController
public class CaptchaController
{
//  创建字符验证码
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;
// 创建数字验证码
    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;
// 验证码存入redis
    @Autowired
    private RedisCache redisCache;
//    一些配置
    @Autowired
    private ISysConfigService configService;
    /**
     * 生成验证码
     */
//    @Anonymous
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) throws IOException
    {
//       创建返回给前端的结果
        AjaxResult ajax = AjaxResult.success();
//        获取验证码是否开启,从ISysConfigService获取,ISysConfigService从数据库中获取.
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled)
        {
            return ajax;
        }

        // 保存验证码信息,
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;

        // 判断验证类型
        String captchaType = RuoYiConfig.getCaptchaType();
        if ("math".equals(captchaType))
        {
            String capText = captchaProducerMath.createText();
//            生成验证码
            capStr = capText.substring(0, capText.lastIndexOf("@"));
//            拿到答案,放到redis里面.
            code = capText.substring(capText.lastIndexOf("@") + 1);
//            创建图片
            image = captchaProducerMath.createImage(capStr);
        }
        else if ("char".equals(captchaType))
        {
            capStr = code = captchaProducer.createText();
//            创建图片
            image = captchaProducer.createImage(capStr);
        }

//        验证码存入redis,
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
//            将image对象写入到os中,以jpg格式结尾
            ImageIO.write(image, "jpg", os);
        }
        catch (IOException e)
        {
            return AjaxResult.error(e.getMessage());
        }
//  根据uuid获取存在redis中的答案.
        ajax.put("uuid", uuid);
//        将图片的二进制数据转换成base64格式
        ajax.put("img", Base64.encode(os.toByteArray()));
        return ajax;
    }
}
