package com.demo.li.io;

import org.testng.annotations.Test;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * @author lihongli
 * @date 2020/11/17 21:45
 */
public class ByteBufferTest {

    @Test
    public void byteBufferTest(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
        System.out.println("init" + byteBuffer);
        byteBuffer.put("123".getBytes());
        System.out.println("after put " + byteBuffer);

        byteBuffer.flip();
        System.out.println(byteBuffer.get());
        System.out.println("after get " + byteBuffer);

        byteBuffer.put("aa".getBytes());
        System.out.println("after put v2 " + byteBuffer);

        byteBuffer.flip();

        byteBuffer.compact();
        System.out.println("after compact " + byteBuffer);




    }
}
