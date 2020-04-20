package com.demo.li.spring.ioc.impl;

import com.demo.li.spring.ioc.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lihongli
 * create：2020/4/18 11:17 下午
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public void createOrder(Long productId, Long userId, Integer num) {
        log.info("user {} has by product {} num {}", userId, productId, num);
    }
}
