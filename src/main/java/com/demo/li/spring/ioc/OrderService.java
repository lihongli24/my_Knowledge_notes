package com.demo.li.spring.ioc;

/**
 * 测试用的订单服务
 *
 * @author lihongli
 * create：2020/4/18 11:15 下午
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param productId 产品Id
     * @param userId    用户Id
     * @param num       购买数量
     */
    void createOrder(Long productId, Long userId, Integer num);
}
