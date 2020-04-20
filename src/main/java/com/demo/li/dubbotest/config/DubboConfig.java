package com.demo.li.dubbotest.config;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.demo.li.dubbotest.reference.impl.ReferenceInterImpl;
import com.demo.li.dubbotest.service.impl.ServiceInterImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author lihongli
 * create：2020/2/2 3:28 下午
 */
//@Configuration
//@EnableDubbo(scanBasePackageClasses = {ServiceInterImpl.class, ReferenceInterImpl.class})
//@ComponentScan(basePackageClasses = ReferenceInterImpl.class)
public class DubboConfig {
    @Bean // #1
    public ProviderConfig providerConfig() {
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setTimeout(1000);
        providerConfig.setScope(Constants.SCOPE_LOCAL);
        return providerConfig;
    }

    @Bean // #2
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("dubbo-annotation-provider");
        return applicationConfig;
    }

    @Bean // #3
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("N/A");
        return registryConfig;
    }

    @Bean // #4
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        return protocolConfig;
    }

    @Bean // #2
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setTimeout(3000);
        return consumerConfig;
    }
}
