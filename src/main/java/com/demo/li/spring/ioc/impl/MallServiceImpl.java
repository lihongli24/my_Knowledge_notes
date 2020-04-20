package com.demo.li.spring.ioc.impl;

import com.demo.li.spring.ioc.MallService;
import com.demo.li.spring.ioc.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lihongli
 * create：2020/4/19 12:02 上午
 */
@Service
public class MallServiceImpl implements MallService {
    @Autowired
    private OrderService orderService;

    @Override
    public void buySomeThing(Long productId, Long userId, Integer num) {
        orderService.createOrder(productId, userId, num);
    }
}
