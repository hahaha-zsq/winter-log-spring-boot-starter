package com.zsq.winter.log.util;

import cn.hutool.core.lang.Dict;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONUtil;
import com.zsq.winter.log.entity.WinterLogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import net.dreamlu.mica.ip2region.core.IpInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
@RequiredArgsConstructor
public class WinterLogUtil {

    final Ip2regionSearcher ip2regionSearcher;

    /**
     * 获取客户端IP地址
     * 该方法通过检查不同的请求头来确定客户端的IP地址，以处理通过代理服务器的情况
     *
     * @param request HTTP请求对象，用于获取请求头信息
     * @return 返回客户端的IP地址如果无法确定，则返回服务器的IP地址
     */
    public String getClientIp(HttpServletRequest request) {
        // 尝试从请求头中获取客户端IP地址
        String ip = request.getHeader("x-forwarded-for");
        // 如果IP地址为空或为"unknown"，则尝试从其他请求头中获取
        if (ObjectUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ObjectUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ObjectUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ObjectUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ObjectUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            // 如果所有请求头都没有有效IP，则使用服务器的IP地址
            ip = request.getRemoteAddr();
        }
        // 多个代理服务器时，取第一个非unknown的IP
        String comma = ",";
        String localhost = "127.0.0.1";
        if (ip.contains(comma)) {
            ip = ip.split(",")[0];
        }
        // 如果IP地址是本地地址，则获取本机真正的ip地址
        if (localhost.equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                // 日志记录IP获取错误
                log.error("ip error", e);
            }
        }
        return ip;
    }

    /**
     * 方法接收一个包含多个IP地址的字符串参数，该字符串以逗号分隔。它返回第一个IP地址（如果有多个IP地址）或者整个字符串（如果只有一个IP地址）。
     *
     * @param ipHeaderValue ip地址
     * @return {@link String}
     */
    private String getFirstIpFromHeaderValue(String ipHeaderValue) {
        int index = ipHeaderValue.indexOf(',');
        if (index != -1) {
            return ipHeaderValue.substring(0, index);
        } else {
            return ipHeaderValue;
        }
    }

    /**
     * 根据ip获取地址
     *
     * @param ip
     * @return {@link String}
     */
    public String getAddressByIp(String ip) {
        IpInfo ipInfo = ip2regionSearcher.btreeSearch(ip); // 根据ip地址获取到address
        if (!ObjectUtils.isEmpty(ipInfo)) {
            return ipInfo.getAddressAndIsp();
        } else {
            return "";
        }
    }

    public String getBrowser(HttpServletRequest request) throws Exception {
        String browser = request.getHeader("User-Agent");//获取浏览器信息
        UserAgent ua = UserAgentUtil.parse(browser);
        String browserType = ua.getBrowser().toString();
        String version = ua.getVersion();
        String engine = ua.getEngine().toString();
        String engineVersion = ua.getEngineVersion();
        String os = ua.getOs().toString();
        String platform = ua.getPlatform().toString();
        Integer mobile = ua.isMobile() ? 1 : 0;

        Dict dict = new Dict();
        dict.set("browserType", browserType).set("platform", platform).set("mobile", mobile).set("os", os).set("platform", platform).set("version", version)
                .set("engine", engine).set("engineVersion", engineVersion);
        return JSONUtil.toJsonStr(dict);
    }
}