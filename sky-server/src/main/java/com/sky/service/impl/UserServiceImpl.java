package com.sky.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String WX_OPENID_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;




    /**
     * 登录
     */
    public User login(UserLoginDTO userLoginDTO) {
        // 获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 判断openid是否为空
        if (StringUtils.isBlank(openid)) {
            // 处理openid为空的情况
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断当前用户是否已存在
        User user = userMapper.getByOpenid(openid);
        // 如果用户不存在，则注册用户
        if(user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }
        return user;
    }
    /**
     * 调用微信接口获取openid
     */
    private String getOpenid(String code) {
        String appid = weChatProperties.getAppid();
        String secret = weChatProperties.getSecret();
        if (StringUtils.isBlank(appid) || StringUtils.isBlank(secret)) {
            // 配置缺失时直接失败，避免发出 secret=null 的无效请求
            log.error("微信登录配置缺失, appid是否为空={}, secret是否为空={}",
                    StringUtils.isBlank(appid), StringUtils.isBlank(secret));
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        if (StringUtils.isBlank(code)) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("secret", secret);
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String response;
        try {
            response = HttpClientUtil.doGet(WX_OPENID_URL, params);
        } catch (IOException e) {
            // TLS/超时/DNS/代理问题会在这里带上完整原因，不再被吞掉
            log.error("调用微信jscode2session异常, appid={}", appid, e);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        log.info("微信jscode2session响应: {}", response);

        if (StringUtils.isBlank(response)) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        Integer errcode = jsonObject.getInteger("errcode");
        if (errcode != null && errcode != 0) {
            // 40029 invalid code / 40163 code been used / 40125 invalid appsecret / 41002 appid missing
            log.error("微信登录失败, errcode={}, errmsg={}", errcode, jsonObject.getString("errmsg"));
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        String openid = jsonObject.getString("openid");
        if (StringUtils.isBlank(openid)) {
            log.error("微信返回中没有openid: {}", response);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        return openid;
    }
}
