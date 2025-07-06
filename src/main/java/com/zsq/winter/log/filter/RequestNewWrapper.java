package com.zsq.winter.log.filter;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @description RequestNewWrapper类扩展了HttpServletRequestWrapper，用于修改或增强HTTP请求的处理方式
 *              它允许我们在请求处理过程中访问和操作请求体
 */
public class RequestNewWrapper extends HttpServletRequestWrapper {

    // 存储请求体的字节数组
    private final byte[] body;

    /**
     * @description 构造函数，初始化RequestNewWrapper对象
     *              它从请求中读取内容并存储到body属性中
     * @param request 原始的HttpServletRequest对象
     * @throws IOException 如果在读取请求体时发生I/O错误
     */
    public RequestNewWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = StreamUtils.copyToByteArray(request.getInputStream());
    }

    /**
     * @description 重写getInputStream方法，返回保存在属性中的body
     *              这允许多次读取请求体，克服了原始请求流只能读取一次的限制
     * @return javax.servlet.ServletInputStream
     */
    @Override
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream byteArrayInputStream  = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    /**
     * @description 重写getReader方法，基于重写的getInputStream方法返回一个BufferedReader
     *              这允许以字符流的形式读取请求体
     * @return java.io.BufferedReader
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * @description 获取请求体的字符串表示
     *              这个方法提供了一种方便的方式来获取请求体，而不需要处理字节流
     * @return java.lang.String
     */
    public String getBody() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
