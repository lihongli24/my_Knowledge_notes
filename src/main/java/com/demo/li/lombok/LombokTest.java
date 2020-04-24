package com.demo.li.lombok;

import org.testng.annotations.Test;

/**
 * @author lihongli
 * create：2020/4/23 11:04 下午
 */
public class LombokTest {


    @Test
    public void noEqualsAnnotationTest(){
        Student student1 = new Student();
        student1.setName("小明");
        student1.setAge(11);
        student1.setSchool("小学");

        Student student2 = new Student();
        student2.setName("小红");
        student2.setAge(12);
        student2.setSchool("小学");

        System.out.println("student1 equals student2 : " + student1.equals(student2));
        System.out.println("student1.hasCode == student2.hasCode: " + (student1.hashCode() == student2.hashCode()));
    }


    @Test
    public void withEqualsAnnotationTest(){
        StudentWithEquals student1 = new StudentWithEquals();
        student1.setName("小明");
        student1.setAge(11);
        student1.setSchool("小学");

        StudentWithEquals student2 = new StudentWithEquals();
        student2.setName("小红");
        student2.setAge(12);
        student2.setSchool("小学");

        System.out.println("student1 equals student2 : " + student1.equals(student2));
        System.out.println("student1.hasCode == student2.hasCode: " + (student1.hashCode() == student2.hashCode()));
    }
}
