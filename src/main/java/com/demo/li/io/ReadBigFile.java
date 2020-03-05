package com.demo.li.io;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author lihongli
 * create：2020/3/4 1:05 下午
 */
public class ReadBigFile {

    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("/Users/lihongli/Desktop/mavenDei037.txt");
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
        int bytes;
        do {
            bytes = fileChannel.read(byteBuffer);
            byte[] byteArray = new byte[bytes];
            byteBuffer.flip();
            byteBuffer.get(byteArray);
            System.out.println(new String(byteArray));

        }while (bytes > -1);

    }
}
