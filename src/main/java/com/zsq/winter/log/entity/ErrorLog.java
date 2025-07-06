package com.zsq.winter.log.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ErrorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String errRequestParam;

    private String errName;

    private String errMessage;

    private String operationMethod;

    private String operationUrl;

    private String operationIp;

    private String address;

    private String createTime;
}