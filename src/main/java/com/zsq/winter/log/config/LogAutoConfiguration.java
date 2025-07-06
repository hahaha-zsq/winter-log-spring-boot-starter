package com.zsq.winter.log.config;

import com.zsq.winter.log.annotation.EnableWinterLog;
import com.zsq.winter.log.aspect.LogAspect;
import com.zsq.winter.log.entity.BannerCreator;
import com.zsq.winter.log.service.DefaultLogServiceImpl;
import com.zsq.winter.log.service.LogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LogProperties.class)
//指定一个注解类，容器中必须存在至少一个带有该注解的 Bean，在注入LogAutoConfiguration
@ConditionalOnBean(annotation = EnableWinterLog.class)
public class LogAutoConfiguration {
    /**
     * 当容器中不存在LogService bean时，创建一个默认的LogService bean
     *
     * @return LogService的默认实现DefaultLogServiceImpl
     */
    @Bean
    @ConditionalOnMissingBean
    public LogService getLogService(){
        return new DefaultLogServiceImpl();
    }

    /**
     * 创建并返回一个LogAspect bean，用于处理日志切面
     *
     * @param logProperties 日志属性配置
     * @param logService 日志服务
     * @return LogAspect的实例
     */
    @Bean
    public LogAspect logAspect(LogProperties logProperties, LogService logService){
        return new LogAspect(logProperties,logService);
    }

    /**
     * 根据日志属性创建并返回一个BannerCreator bean，用于创建欢迎横幅
     *
     * @param logProperties 日志属性配置
     * @return BannerCreator的实例
     */
    @Bean
    public BannerCreator bannerCreator(LogProperties logProperties) {
        return new BannerCreator(logProperties);
    }
}

