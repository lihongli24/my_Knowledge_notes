package com.demo.li.dubbotest.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.demo.li.dubbotest.service.ServiceInter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author lihongli
 * create：2020/2/2 3:27 下午
 */
@Service
public class ServiceInterImpl implements ServiceInter {
    @Override
    public String say(String param) {
        return "hello " + (StringUtils.isBlank(param) ? "null" : param);
    }
}
