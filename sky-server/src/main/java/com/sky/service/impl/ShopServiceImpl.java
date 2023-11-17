package com.sky.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShopServiceImpl implements com.sky.service.ShopService{
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void setStatus(Integer status) {

    }
}
