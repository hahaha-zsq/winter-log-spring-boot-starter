package com.zsq.winter.log.service;

import com.zsq.winter.log.entity.ErrorLog;
import com.zsq.winter.log.entity.OperationLog;

public interface LogService {


    /**
     * 日志处理程序
     *
     * @param operationLog 操作日志
     */
    public void logHandler(OperationLog operationLog);

    /**
     * 日志处理程序
     *
     * @param errorLog 错误日志
     */
    public void errLogHandler(ErrorLog errorLog);

}
