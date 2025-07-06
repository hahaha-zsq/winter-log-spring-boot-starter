package com.zsq.winter.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("winter-log")
public class LogProperties {
    /**
     * 方法的运行时长  当方法的运行时间> runTime，才记录。 默认为0
     */
    private Long runTime = 0L;
    /**
     * 输出结果的长度(默认为1000字符)
     */
    private Integer resultLength = 1000;
    /**
     * 是否打印banner
     */
    private Boolean isPrint = true;
    // ...... 其他的默认的信息，后期可以补充其他的
}
