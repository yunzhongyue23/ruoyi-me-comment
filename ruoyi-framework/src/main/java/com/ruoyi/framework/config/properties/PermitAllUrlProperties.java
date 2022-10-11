package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.ruoyi.common.annotation.Anonymous;

/**
 * 设置Anonymous注解允许匿名访问的url
 * 
 * @author ruoyi
 */

//项目初始化的时候走这个配置
@Configuration
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware
{
//    定义了一个规则,利用正则匹配地址
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");
//设置上下文对象
    private ApplicationContext applicationContext;

//    url的arraylist,把所有的不需要鉴权 的方法放到下面的数组列表中
    private List<String> urls = new ArrayList<>();

    public String ASTERISK = "*";

    @Override
    public void afterPropertiesSet()
    {
//        将整个项目中所有的bean 对象都拿出来
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
//        获取对象的信息和全部的方法
//        RequestMappingInfo指url,HandlerMethod指方法
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
//          info代表每一个url对象
        map.keySet().forEach(info -> {
            HandlerMethod handlerMethod = map.get(info);

            //   获取方法上边的注解 替代path variable 为 *
            //   第三方的依赖,获取方法上有没有Anonymous注解
            Anonymous method = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
//            判断注解Anonymous是不是null,如果是null就停下,如果不为空就走lambda中的逻辑,拿到所有的路径,然后把路径添加到utl数组列表中
//            {POST [/monitor/job/export]} ,\{(.*?)\}
            Optional.ofNullable(method).ifPresent(anonymous -> info.getPatternsCondition().getPatterns()
                    .forEach(url -> urls.add(RegExUtils.replaceAll(url, PATTERN, ASTERISK))));

            // 获取类上边的注解, 替代path variable 为 *

            Anonymous controller = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
            Optional.ofNullable(controller).ifPresent(anonymous -> info.getPatternsCondition().getPatterns()
                    .forEach(url -> urls.add(RegExUtils.replaceAll(url, PATTERN, ASTERISK))));
        });
    }

//    设置上下文 对象
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        this.applicationContext = context;
    }

//    给对象设置可访问地址属性
    public List<String> getUrls()
    {
        return urls;
    }

    public void setUrls(List<String> urls)
    {
        this.urls = urls;
    }
}
