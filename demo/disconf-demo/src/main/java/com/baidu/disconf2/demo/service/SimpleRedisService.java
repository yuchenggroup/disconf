package com.baidu.disconf2.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import com.baidu.disconf2.demo.config.JedisConfig;
import com.baidu.disconf2.demo.utils.JedisUtil;


/**
 * 一个简单的Redis服务
 * 
 * @author liaoqiqi
 * @version 2014-6-17
 */
@Service
public class SimpleRedisService implements InitializingBean, DisposableBean {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(SimpleRedisService.class);

    // 代表
    private Jedis jedis = null;

    @Autowired
    private JedisConfig jedisConfig;

    /**
     * 关闭
     */
    @Override
    public void destroy() throws Exception {

        if (jedis != null) {
            jedis.disconnect();
        }
    }

    /**
     * 进行连接
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        jedis = JedisUtil.createJedis(jedisConfig.getHost(),
                jedisConfig.getPort());
    }

    /**
     * 获取一个值
     * 
     * @param key
     * @return
     */
    public String getKey(String key) {
        if (jedis != null) {
            return jedis.get(key);
        }

        return null;
    }

    /**
     * 更改Jedis
     */
    public void changeJedis() {

        LOGGER.info("start to change jedis hosts to: " + jedisConfig.getHost()
                + " : " + jedisConfig.getPort());

        jedis = JedisUtil.createJedis(jedisConfig.getHost(),
                jedisConfig.getPort());

        LOGGER.info("change ok.");
    }

    public Jedis getJedis() {
        return jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }
}