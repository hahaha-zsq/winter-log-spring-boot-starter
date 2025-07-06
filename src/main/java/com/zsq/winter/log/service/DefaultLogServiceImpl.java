package com.zsq.winter.log.service;

import com.zsq.winter.log.entity.ErrorLog;
import com.zsq.winter.log.entity.OperationLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLogServiceImpl implements LogService {
    @Override
    public void logHandler(OperationLog operationLog) {
        log.info("默认日志处理：>>>{}",operationLog);
    }

    @Override
    public void errLogHandler(ErrorLog errorLog) {
        log.error("默认错误日志处理：>>>{}",errorLog);
    }
}
