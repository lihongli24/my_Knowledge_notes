package utils.base64;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import org.apache.commons.compress.utils.IOUtils;
import sun.misc.BASE64Decoder;

/**
 * Created by lihongli on 2019/11/8.
 */
public class Base64Utils {

    /**
     * base64转化成文件
     */
    public static void base64StringToFile(String base64Content, String filePath) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();

        byte[] bytes = decoder.decodeBuffer(base64Content);//base64编码内容转换为字节数组
        File file = new File(filePath);

        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
            BufferedInputStream bis = new BufferedInputStream(byteInputStream);
            FileOutputStream fos = new FileOutputStream(file);
        ) {
            File path = file.getParentFile();
            if (!path.exists()) {
                path.mkdirs();
            }

            IOUtils.copy(bis, fos);

            fos.flush();
        }
    }

    public static String encryptToBase64(String filePath) {
        if (filePath == null) {
            return null;
        }
        try {
            byte[] b = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



}
