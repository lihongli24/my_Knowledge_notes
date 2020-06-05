package com.demo.li;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

/**
 * @author lihongli
 * create：2020/2/2 3:36 下午
 */
@SpringBootApplication(scanBasePackageClasses = SpringLaner.class, exclude = {DataSourceAutoConfiguration.class, KafkaAutoConfiguration.class})
@EnableDubboConfiguration
public class SpringLaner {

//    public static void main(String[] args) throws Exception {
//        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DubboConfig.class); // #2
//        context.start(); // #3
//        ReferenceInter referenceInter = context.getBean(ReferenceInter.class);
//        String result = referenceInter.speak("apple");
//        System.out.println(result);
//
//    }

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SpringLaner.class);
        springApplication.run(args);
    }
}
