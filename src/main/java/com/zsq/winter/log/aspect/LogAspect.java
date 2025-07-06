package com.zsq.winter.log.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.zsq.winter.log.annotation.SystemLog;
import com.zsq.winter.log.config.LogProperties;
import com.zsq.winter.log.entity.ErrorLog;
import com.zsq.winter.log.entity.OperationLog;
import com.zsq.winter.log.service.LogService;
import com.zsq.winter.log.util.WinterLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@Aspect
@Slf4j
public class LogAspect {
    private LogService logService;
    private LogProperties logProperties;

    @Resource
    private WinterLogUtil browserUtil;

    public LogAspect(LogProperties logProperties, LogService logService) {
        this.logService = logService;
        this.logProperties = logProperties;
    }


    // 这个切点表达式的作用是匹配所有被 @SystemLog 注解直接标记的方法。这意味着只有那些显式使用 @SystemLog 注解的方法会被视为切点。使用logPointCut()方法进行增强。
    @Pointcut("@annotation(com.zsq.winter.log.annotation.SystemLog)")
    //@Pointcut("@annotation(com.zsq.winter.log.annotation.SystemLog)||(execution(public * *..controller.*.*(..))))")
    public void logPointCut() {
    }

    //
    @Pointcut("execution(public * *..controller.*.*(..)))")
    public void exceptionLogPointCut() {
    }

    // 在执行匹配logPointCut()切点的方法之前执行
    @Before(value = "logPointCut()")
    public void deBeforeOperationLog(JoinPoint joinPoint) throws Exception {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(requestAttributes)) {
            return;
        }
        //请求日志的操作
        HttpServletRequest request = requestAttributes.getRequest();

        // 处理请求头
        Map<String, Object> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerKey = headerNames.nextElement();
            String headerValue = request.getHeader(headerKey);
            if (!ObjectUtils.isEmpty(headerKey) && !ObjectUtils.isEmpty(headerValue)) {
                headerMap.put(headerKey, headerValue);
            }
        }
        // 请求头
        String header = JSONUtil.toJsonStr(headerMap);
        // 获取ip,ip所在地，浏览器信息
        String clientIp = browserUtil.getClientIp(request);
        String browserInfo = browserUtil.getBrowser(request);
        String address = browserUtil.getAddressByIp(clientIp);
        // 获取请求方法
        String method = request.getMethod();
        //请求url
        String url = request.getRequestURL().toString();
        SystemLog systemLog = getSystemLog((ProceedingJoinPoint) joinPoint);

        //操作模块
        String operationModule = systemLog.operationModule();
        //操作类型
        String operationType = systemLog.operationType();
        //操作描述
        String operationDesc = systemLog.operationDesc();
        // 请求发送的时间
        String requestTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 接口全路径
        String interfaceFullPath = joinPoint.getSignature().getDeclaringTypeName() + "." + ((MethodSignature) joinPoint.getSignature()).getName();
        //请求body参数
        //问题：aop 中使用 request.getInputStream()、request.getReader()获取post参数，因为此时存放post参数的流已经关闭，所以报汇报IO异常
        //CustomHttpServletRequestWrapper wrapper = (CustomHttpServletRequestWrapper) request;
        ServletInputStream inputStream = request.getInputStream();
        //IoUtil.readUtf8将输入流中的内容按照UTF-8编码进行解码，并将解码后的字符串返回,如果body的json格式带有空格或者换行，解析出来的也会有，所以要替换掉
        String str = filterJson(IoUtil.readUtf8(inputStream));
        String contentType = request.getContentType();
        String requestBody = "";
        if (!ObjectUtils.isEmpty(contentType) && request.getContentType().contains(ContentType.MULTIPART.getValue())) {
            requestBody = "文件类型";
        }
        if (!ObjectUtils.isEmpty(contentType) && request.getContentType().contains(ContentType.JSON.getValue())) {
            requestBody = str;
        }
        // 请求参数
        String requestParams = getRequestParams(request, joinPoint);
        request.setAttribute("startTime", System.currentTimeMillis()); //设置请求开始的时间
        String uuid = IdUtil.simpleUUID();
        request.setAttribute("uuid", uuid);
        OperationLog requestInfo = OperationLog.builder().uuid(uuid).operationModule(operationModule).operationDesc(operationDesc).operationType(operationType)
                .browserInfo(browserInfo).ip(clientIp).address(address).url(url).method(method).interfaceFullPath(interfaceFullPath)
                .requestTime(requestTime).requestHeader(header).requestParamArgs(requestParams).requestBodyArgs(requestBody)
                .build();
        // 设置前置通知的请求信息
        request.setAttribute("requestInfo", requestInfo);
    }


    // 过滤和处理JSON字符串中的特殊信息
    private static String filterJson(String json) {
        if(ObjectUtils.isEmpty(json)){
            return "";
        }
        String requestBody = "";
        // 去除空格
        requestBody = json.replaceAll("\\s+", "");
        // 过滤密码和空格,找到password字段的值，使用(?i)开启正则表达式的不区分大小写模式,并用***替换
        requestBody = requestBody.replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]*", "$1***");
        // 去除所有的空格
        return requestBody;
    }

    /**
     * 打印操作日志
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行. 返回后通知，在目标方法执行后执行，如果出现异常不会执行
     * 使用returning属性指定了把目标方法返回值赋值给下面方法的参数keys
     *
     * @param joinPoint 切入点
     * @param result    后果
     * @throws Exception 例外
     */
    @AfterReturning(value = "logPointCut()", returning = "result")
    public void saveOperationLog(JoinPoint joinPoint, Object result) throws Exception {
        // 获取RequestAttributes
        // RequestContextHolder：持有上下文的Request容器,通过RequestContextHolder的静态方法可以随时随地取到当前请求的request对象
        // RequestContextHolder.getRequestAttributes() 获取相关对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(requestAttributes)) {
            return;
        }
//
       /*
       * httpServletRequest对象对http请求进行了封装，我们使用这个对象就可以方便快捷的获取http中的信息。
       * HttpServletRequest 对象代表客户端的请求，在里面就包含了所有的客服端信息，我们通过这个对象的方法便可以获取所有的客服端信息，
       * 包括请求头和请求体中的所有数据。
       * getRequestURL()：得到请求的URI
         getRequestURL()：得到请求的URL
         getHeader(String s)：getHeader表示得到请求头，参数s表示想要获取请求头中的什么数据
         getHeader("Host")：获取请求的主机
         getHeader("Referer")：获取请求来自于哪里，一般用来做防盗链
         getRemotetAddr()：得到请求的ip
         getParameter(String s)：得到请求参数的值
         getParameterValues(String s)：得到请求参数的值，用于参数有多个值，返回一个数组
       * */
        HttpServletRequest request = requestAttributes.getRequest();
        Long startTime = Convert.toLong(request.getAttribute("startTime"));
        Long spendTime = System.currentTimeMillis() - startTime;
        String uuid = Convert.toStr(request.getAttribute("uuid"));
        OperationLog requestInfo = (OperationLog) (request.getAttribute("requestInfo"));

        String responseTime = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN);
        // 过滤特殊字符，并截取响应内容，避免太长
        String responseStr = result instanceof String ? Convert.toStr(result) : filterJson(StrUtil.sub(JSONUtil.toJsonStr(result), 0, logProperties.getResultLength()));

        OperationLog responseInfo = OperationLog.builder().responseTime(responseTime).spendTime(spendTime).response(responseStr)
                .build();
        // 最终的数据
        OperationLog operationLog = requestInfo.setResponseTime(responseTime)
                .setSpendTime(spendTime)
                .setResponse(responseStr);
        //方法的运行时长  当方法的运行时间> 设置的值时，才记录。 默认为0
        if (logProperties.getRunTime() <= spendTime) {
            logService.logHandler(operationLog);
        }
    }
//

    /**
     * 异常返回通知，用于拦截异常日志信息 连接点抛出异常后执行
     *
     * @param joinPoint 切入点
     * @param e         异常信息
     */
    @AfterThrowing(pointcut = "exceptionLogPointCut()", throwing = "e")
    public void saveExceptionLog(JoinPoint joinPoint, Throwable e) {
        // 获取RequestAttributes
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
//
        ErrorLog errorLog = new ErrorLog();
        String userName = "";
        String operationUserId = "";
        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String method = request.getMethod();
            String clientIp = browserUtil.getClientIp(request);
            String address = browserUtil.getAddressByIp(clientIp);

            errorLog
                    .setErrRequestParam(getRequestParams(request, joinPoint))
                    .setOperationMethod(method)
                    .setErrName(e.getClass().getName())
                    .setErrMessage(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace()))
                    .setOperationUrl(request.getRequestURI())
                    .setOperationIp(clientIp)
                    .setAddress(address)
                    .setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            logService.errLogHandler(errorLog);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }


    /**
     * 获取请求参数
     *
     * @param request:   request
     * @param joinPoint: joinPoint
     * @return {@link String}
     */
    private String getRequestParams(HttpServletRequest request, JoinPoint joinPoint) {
        String httpMethod = request.getMethod();
        Object[] paramsArray = joinPoint.getArgs();
        StringBuilder params = new StringBuilder();
        return argsToString(paramsArray, joinPoint);
    }

    /**
     * 参数处理
     */
    private String argsToString(Object[] args, JoinPoint joinPoint) {
        // 检查参数是否包含servletRequest，ServletResponse，MultipartFile等参数，其不能被序列化
        boolean parameterExclude = Arrays.stream(args).anyMatch(arg -> arg instanceof MultipartFile || arg instanceof ServletResponse || arg instanceof ServletRequest);
        if (parameterExclude) {
            StringBuilder fileInfo = new StringBuilder();
            Arrays.stream(args).filter(arg -> arg instanceof MultipartFile).forEach(file -> {
                MultipartFile multipartFile = (MultipartFile) file;
                String fileName = multipartFile.getOriginalFilename();
                long fileSize = multipartFile.getSize();
                fileInfo.append("【文件名:").append(fileName).append(" ,文件大小: ").append(fileSize).append("】");
            });
            return filterJson(fileInfo.toString());
        } else {
            StringBuilder params = new StringBuilder();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            // 请求的方法参数名称(LocalVariableTableParameterNameDiscoverer只能用于获取普通方法的参数名，对于构造方法、静态方法等特殊方法可能无法获取参数名。)
            LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
            // 获取方法参数名数组
            String[] paramNames = u.getParameterNames(method);
            if (!ObjectUtils.isEmpty(args) && !ObjectUtils.isEmpty(paramNames)) {
                for (int i = 0; i < args.length; i++) {
                    params.append("  ").append(paramNames[i]).append(": ").append(args[i]);
                }
            }
            return filterJson(params.toString());
        }
    }


    /**
     * 转换异常信息为字符串
     *
     * @param exceptionName    异常名称
     * @param exceptionMessage 异常信息
     * @param elements         堆栈信息
     */
    public String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuilder str = new StringBuilder();
        for (StackTraceElement stet : elements) {
            str.append(stet).append("\n");
        }
        return exceptionName + ":" + exceptionMessage + "\n\t" + str;
    }

    private SystemLog getSystemLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getAnnotation(SystemLog.class);
    }
}
