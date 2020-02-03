package com.demo.li.dubbotest.reference.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.demo.li.dubbotest.reference.ReferenceInter;
import com.demo.li.dubbotest.service.ServiceInter;
import org.springframework.stereotype.Component;

/**
 * @author lihongli
 * create：2020/2/2 4:00 下午
 */
@Component
public class ReferenceInterImpl implements ReferenceInter {
    @Reference
    private ServiceInter serviceInter;


    @Override
    public String speak(String param) {
        return serviceInter.say(param);
    }
}
