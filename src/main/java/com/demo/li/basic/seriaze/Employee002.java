package com.demo.li.basic.seriaze;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lihongli
 * create：2020/2/17 8:29 下午
 */
@Data
public class Employee002 implements Serializable {

    private String name;

    private Integer age;
}
