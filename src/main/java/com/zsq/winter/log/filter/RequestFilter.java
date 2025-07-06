package com.zsq.winter.log.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@Order(1) // 优先级数字越小优先级越高
//该类的作用是将请求包装为可重复读取的Request对象，解决HTTP请求体流只能被读取一次的问题
//通过继承OncePerRequestFilter确保每个请求仅处理一次，创建HttpServletRequest的包装器（RequestNewWrapper或StandardServletMultipartResolver处理的multipart请求），使得后续处理（如日志记录、参数审计）可多次读取请求体
//获取请求中的流，将取出来的，再次转换成流，然后把它放入到新request对象中
//必须保证在所有过滤器之前执行,如果有另一个过滤器先读取了流，那么当RequestFilter尝试包装请求时，流已经是空的了，导致后续处理（如日志记录或文件上传）无法获取数据，出现错误
public class RequestFilter extends OncePerRequestFilter {

    /**
     *  将自定义的ServletRequest替换进filterChain中，使request可以重复读取
     *
     * @param request  要求
     * @param response 回答
     * @param chain    链条
     * @throws ServletException servlet异常
     * @throws IOException      IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {
        // 初始化自定义的HttpServletRequest
        HttpServletRequest warpper = null;
        // 判断请求的内容类型，如果是multipart/form-data，则使用StandardServletMultipartResolver进行解析
        if (request.getContentType() != null && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            warpper = new StandardServletMultipartResolver().resolveMultipart(request);
        } else {
            // 否则，使用RequestNewWrapper包装request，以支持重复读取
            warpper = new RequestNewWrapper(request);
        }
        // 使用自定义的request继续执行filterChain
        chain.doFilter(warpper, response);
    }
}
