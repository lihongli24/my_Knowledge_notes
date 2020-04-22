package com.demo.li.lombok;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * lombol测试子类
 * @author lihongli
 * create：2020/4/22 7:23 下午
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Child extends Parent{
    private int weight;
}
