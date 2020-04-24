package com.demo.li.lombok;

import lombok.Data;

/**
 * lombol测试子类
 *
 * @author lihongli
 * create：2020/4/22 7:23 下午
 */
@Data
public class Student extends Person {
    /**
     * 所在学校
     */
    private String school;
}
