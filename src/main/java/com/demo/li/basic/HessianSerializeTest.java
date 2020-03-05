package com.demo.li.basic;

import com.alibaba.com.caucho.hessian.io.HessianInput;
import com.alibaba.com.caucho.hessian.io.HessianOutput;
import com.demo.li.basic.seriaze.Employee001;
import com.demo.li.basic.seriaze.Employee002;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author lihongli
 * create：2020/2/17 8:27 下午
 */
public class HessianSerializeTest {
    /**
     * Hessian实现序列化
     * @param employee
     * @return
     * @throws IOException
     */
    public static byte[] serialize(Employee001 employee){
        ByteArrayOutputStream byteArrayOutputStream = null;
        HessianOutput hessianOutput = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            // Hessian的序列化输出
            hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(employee);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                hessianOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Hessian实现反序列化
     * @param employeeArray
     * @return
     */
    public static Employee002 deserialize(byte[] employeeArray) {
        ByteArrayInputStream byteArrayInputStream = null;
        HessianInput hessianInput = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(employeeArray);
            // Hessian的反序列化读取对象
            hessianInput = new HessianInput(byteArrayInputStream);
            return (Employee002) hessianInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                hessianInput.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String [] args) {

       Employee001 employee001 = new Employee001();
       employee001.setName("aa");
       employee001.setAge(1);
        // 序列化
        byte[] serialize = serialize(employee001);
        System.out.println(serialize);
        // 反序列化
        Employee002 employee002 = deserialize(serialize);
        System.out.println(employee002);

    }

}
