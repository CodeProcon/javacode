package com.hazer.filter;


import com.hazer.shiro.JwtToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author hazer
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    /**
     * 是否允许访问，如果带有 token，则对 token 进行检查，否则直接通过
     * @param request request
     * @param response response
     * @param mappedValue mappedValue
     * @return boolean
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        //判断请求的请求头是否带上 "Token"
        if (isLoginAttempt(request, response)){
            //如果存在，则进入 executeLogin 方法执行登入，检查 token 是否正确
            try {
                executeLogin(request, response);
                return true;
            }catch (Exception e){
                //token 错误
                responseError(response,e.getMessage());
                return false;
            }
        }
        //如果请求头不存在 Token，则可能是执行登陆操作或者是游客状态访问，无需检查 token，直接返回 true
        return true;
    }

    /**
     * 判断用户是否想要登入。
     * 检测 header 里面是否包含 Token 字段
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req= (HttpServletRequest) request;
        String token=req.getHeader("Authorization");
        return token!=null;
    }


    /**  executeLogin实际上就是先调用createToken来获取token，这里我们重写了这个方法，就不会自动去调用createToken来获取token
     * 然后调用getSubject方法来获取当前用户再调用login方法来实现登录
     * 这也解释了我们为什么要自定义jwtToken，因为我们不再使用Shiro默认的UsernamePasswordToken了。
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        log.info("executeLogin");
        HttpServletRequest req= (HttpServletRequest) request;
        String token=req.getHeader("Authorization");
        JwtToken jwt=new JwtToken(token);
        //交给自定义的realm对象去登录，如果错误他会抛出异常并被捕获
        getSubject(request, response).login(jwt);
        return true;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        log.info("preHandle");
        HttpServletRequest req= (HttpServletRequest) request;
        HttpServletResponse res= (HttpServletResponse) response;
        res.setHeader("Access-control-Allow-Origin","*");
        res.setHeader("Access-control-Allow-Methods","GET,POST,OPTIONS,PUT,DELETE");
        res.setHeader("Access-control-Allow-Headers","*");
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            res.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * 将非法请求跳转到 /unauthorized/**
     */
    private void responseError(ServletResponse response, String message) {
        log.info("responseError");
        try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            //设置编码，否则中文字符在重定向时会变为空字符串
            message = URLEncoder.encode(message, "UTF-8");
            httpServletResponse.sendRedirect("/unauthorized/" + message);
        } catch (IOException e) {
           log.error(e.getMessage());
        }
    }
}
