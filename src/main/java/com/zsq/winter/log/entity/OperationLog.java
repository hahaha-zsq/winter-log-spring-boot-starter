package com.zsq.winter.log.entity;



import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 操作日志
 *
 * @author zero
 * @date 2024/05/14
 */
@Data
@Accessors(chain = true)
@Builder
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String uuid;
    private String operationType;
    private String operationDesc;
    private String operationModule;
    private String browserInfo;
    private String url;
    private String requestHeader;
    private String method;
    private String interfaceFullPath;
    private String ip;
    private String address;
    private String requestParamArgs;
    private String requestBodyArgs;
    private String requestTime;
    private String responseTime;
    private Long spendTime;
    private String response;
}