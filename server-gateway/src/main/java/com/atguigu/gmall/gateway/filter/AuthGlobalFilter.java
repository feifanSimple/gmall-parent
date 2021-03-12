package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import com.netflix.ribbon.proxy.annotation.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${authUrls.url}")
    private String authUrlsUrl;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        //  判断当前的路径是否符合我们判断的标准
        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //判断是否盗用token
        String userId = this.getUserId(request);
        if ("-1".equals(userId)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //  前提是用户在未登录的时候访问/api/**/auth/**这样的控制器,那么提示你需要登录
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
        String[] split = authUrlsUrl.split(",");
        //  authUrlsUrl=trade.html,myOrder.html,list.html 需要跳转到登录页面，进行登录
        if (split != null && split.length > 0) {
            for (String url : split) {
                if (path.indexOf(url) != -1 && StringUtils.isEmpty(userId)) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + request.getURI());
                    //重定向到登录页面
                    return response.setComplete();
                }
            }
        }
        //把用户Id传递给后端各个微服务
         String userTempId  =   this.getUserTempId(request);
        if (!StringUtils.isEmpty(userTempId) || !StringUtils.isEmpty(userId)) {
            if (!StringUtils.isEmpty(userId)) {
                request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)) {
                request.mutate().header("userTempId", userTempId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }

    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        List<String> list = request.getHeaders().get("userTempId");
        if (list != null) {
            userTempId = list.get(0);
        }else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap  = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("userTempId");
            if (cookie != null) {
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }

    private String getUserId(ServerHttpRequest request) {

        String token = "";
        List<String> list = request.getHeaders().get("token");
        if (list != null) {
            token = list.get(0);
        } else {
            HttpCookie cookie = request.getCookies().getFirst("token");
            if (cookie != null) {
                token = cookie.getValue();
            }
        }
        if (!StringUtils.isEmpty(token)) {
            String userKey = "user:login:" + token;
            String userObjet = (String) redisTemplate.opsForValue().get(userKey);
            JSONObject jsonObject = JSONObject.parseObject(userObjet);
            String ip = jsonObject.getString("ip");
            String ipAddress = IpUtil.getGatwayIpAddress(request);
            if (ip.equals(ipAddress)) {
                String userId = jsonObject.getString("userId");
                return userId;
            } else {
                return "-1";
            }
        }
        return null;
    }

    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum permission) {
        Result<Object> result = Result.build(null, permission);
        String str = JSONObject.toJSONString(result);
        //  准备输入页面的响应
        DataBuffer dataBuffer = response.bufferFactory().wrap(str.getBytes());
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        Mono<DataBuffer> just = Mono.just(dataBuffer);
        return response.writeWith(just);
    }
}
