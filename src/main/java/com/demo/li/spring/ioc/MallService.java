package com.demo.li.spring.ioc;

/**
 * 商城服务类
 *
 * @author lihongli
 * create：2020/4/19 12:00 上午
 */
public interface MallService {

    /**
     * 执行东西
     *
     * @param productId
     * @param userId
     * @param num
     */
    void buySomeThing(Long productId, Long userId, Integer num);
}
