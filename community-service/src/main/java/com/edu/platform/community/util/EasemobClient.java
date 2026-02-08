package com.edu.platform.community.util;

import com.edu.platform.community.config.EasemobConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 环信IM客户端工具类
 */
@Slf4j
@Component
public class EasemobClient {
    
    private final EasemobConfig config;
    private final OkHttpClient httpClient;
    private String accessToken;
    private long tokenExpireTime;
    
    public EasemobClient(EasemobConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getRestApi().getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getRestApi().getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getRestApi().getTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * 获取访问令牌(自动刷新)
     */
    public String getAccessToken() throws IOException {
        // 如果token未过期,直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        
        // 获取新token
        String url = String.format("%s/%s/%s/token",
                config.getRestApi().getBaseUrl(),
                config.getOrgName(),
                config.getAppName());
        
        JSONObject body = new JSONObject();
        body.put("grant_type", "client_credentials");
        body.put("client_id", config.getClientId());
        body.put("client_secret", config.getClientSecret());
        
        RequestBody requestBody = RequestBody.create(
                body.toString(),
                MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取token失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            
            accessToken = json.getString("access_token");
            int expiresIn = json.getInt("expires_in");
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L; // 提前5分钟刷新
            
            log.info("环信token获取成功, 有效期: {}秒", expiresIn);
            return accessToken;
        }
    }
    
    /**
     * 发送REST API请求
     */
    public String sendRequest(String method, String path, String body) throws IOException {
        String token = getAccessToken();
        
        String url = String.format("%s/%s/%s%s",
                config.getRestApi().getBaseUrl(),
                config.getOrgName(),
                config.getAppName(),
                path);
        
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json");
        
        if ("POST".equalsIgnoreCase(method)) {
            RequestBody requestBody = body != null ? 
                    RequestBody.create(body, MediaType.parse("application/json")) :
                    RequestBody.create("", MediaType.parse("application/json"));
            builder.post(requestBody);
        } else if ("PUT".equalsIgnoreCase(method)) {
            RequestBody requestBody = body != null ? 
                    RequestBody.create(body, MediaType.parse("application/json")) :
                    RequestBody.create("", MediaType.parse("application/json"));
            builder.put(requestBody);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.delete();
        } else {
            builder.get();
        }
        
        try (Response response = httpClient.newCall(builder.build()).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                log.error("环信API请求失败: {} {}, 响应: {}", method, path, responseBody);
                throw new IOException("API请求失败: " + response.code() + ", " + responseBody);
            }
            
            return responseBody;
        }
    }
}
