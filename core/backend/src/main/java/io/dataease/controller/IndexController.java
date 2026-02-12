package io.dataease.controller;

import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.service.panel.PanelLinkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

@Controller
@RequestMapping
public class IndexController {

    private static final int FOR_EVER = 3600 * 24 * 30 * 12 * 10; // 10 years in second

    // 内网IP正则表达式
    private static final Pattern INTRANET_IP_PATTERN = Pattern.compile(
            "^10\\..*|" +                           // 10.0.0.0/8
            "^172\\.(1[6-9]|2[0-9]|3[01])\\..*|" + // 172.16.0.0/12
            "^192\\.168\\..*|" +                    // 192.168.0.0/16
            "^127\\..*|" +                          // 127.0.0.0/8
            "^localhost$|" +                        // localhost
            "^::1$|" +                              // IPv6 localhost
            "^0:0:0:0:0:0:0:1$"                     // IPv6 localhost另一种格式
    );

    @Resource
    private DefaultLicenseService defaultLicenseService;

    @Resource
    private PanelLinkService panelLinkService;

    @Value("${asdbi.external-url:}")
    private String externalUrl;

    /**
     * 获取外网访问的重定向URL
     * @param serverName 服务器名称（可能是IP或域名）
     * @param originalUrl 原始URL
     * @return 内网访问返回null，外网访问返回带有externalUrl前缀的完整URL
     */
    private String getExternalRedirectUrl(String serverName, String originalUrl) {
        if (StringUtils.isBlank(serverName)) {
            return null;
        }
        // 判断是否为内网访问
        boolean isIntranet = INTRANET_IP_PATTERN.matcher(serverName).matches();
        if (isIntranet) {
            // 内网访问返回null
            return null;
        }
        // 外网访问：构建带有externalUrl前缀的URL
        String externalUrl = this.externalUrl;
        if (StringUtils.isBlank(externalUrl)) {
            return null;
        }

        // 确保externalUrl以/结尾
        if (!externalUrl.endsWith("/")) {
            externalUrl = externalUrl + "/";
        }
        // 去掉originalUrl开头的/避免双斜杠
        return originalUrl.startsWith("/") ? externalUrl + originalUrl.substring(1) : externalUrl + originalUrl;
    }


    @GetMapping(value = "/")
    public String index() {
        return "index.html";
    }

    @GetMapping(value = "/login")
    public String login() {
        return "index.html";
    }

    @GetMapping("/link/{index}")
    public void link(@PathVariable(value = "index", required = true) String index) {
        String url;
        if (CodingUtil.isNumeric(index)) {
            url = panelLinkService.getUrlByIndex(Long.parseLong(index));
        } else {
            url = panelLinkService.getUrlByUuid(index);
        }
//        String contextPath = ServletUtils.getContextPath();
//        if (StringUtils.isNotBlank(contextPath)) {
//            url = contextPath + url;
//        }
        HttpServletResponse response = ServletUtils.response();
        try {
            // TODO 增加仪表板外部参数
            HttpServletRequest request = ServletUtils.request();

            String attachParams = request.getParameter("attachParams");
            if (StringUtils.isNotEmpty(attachParams)) {
                url = url + "&attachParams=" + attachParams;
            }

            String fromLink = request.getParameter("fromLink");
            if (StringUtils.isNotEmpty(fromLink)) {
                url = url + "&fromLink=" + fromLink;
            }
            String ticket = request.getParameter("ticket");
            if (StringUtils.isNotEmpty(ticket)) {
                url = url + "&ticket=" + ticket;
            }


            // 判断内外网访问并获取重定向URL
            String serverName = request.getServerName();
            String redirectUrl = getExternalRedirectUrl(serverName, url);

            if (redirectUrl == null) {
                // 内网访问或配置未设置，使用原始URL
                response.sendRedirect(url);
            } else {
                // 外网访问，使用带externalUrl前缀的URL
                response.sendRedirect(redirectUrl);
            }

        } catch (IOException e) {
            LogUtil.error(e.getMessage());
            DataEaseException.throwException(e);
        }
    }

    @GetMapping("/tempMobileLink/{id}/{token}")
    public void tempMobileLink(@PathVariable("id") String id, @PathVariable("token") String token) {
        String url = "/#preview/" + id;
//        String contextPath = ServletUtils.getContextPath();
//        if (StringUtils.isNotBlank(contextPath)) {
//            url = contextPath + url;
//        }
        HttpServletResponse response = ServletUtils.response();

        // 判断内外网访问
        HttpServletRequest request = ServletUtils.request();

        Cookie cookie = new Cookie("Authorization", token);
        cookie.setPath("/");
        cookie.setMaxAge(FOR_EVER);
        response.addCookie(cookie);
        try {
            // 判断内外网访问并获取重定向URL
            String serverName = request.getServerName();
            String redirectUrl = getExternalRedirectUrl(serverName, url);

            if (redirectUrl == null) {
                // 内网访问或配置未设置，使用原始URL
                response.sendRedirect(url);
            } else {
                // 外网访问，使用带externalUrl前缀的URL
                response.sendRedirect(redirectUrl);
            }
        } catch (IOException e) {
            LogUtil.error(e.getMessage());
            DataEaseException.throwException(e);
        }
    }

}
