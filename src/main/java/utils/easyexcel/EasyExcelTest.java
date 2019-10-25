package utils.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import java.io.InputStream;
import java.util.List;
import lombok.Data;
import org.testng.annotations.Test;

/**
 * Created by lihongli on 2019/10/25.
 */
public class EasyExcelTest {

    @Test
    public void testRead() {
        String path = "easyExcel.xlsx";
        InputStream inputStream =
            this.getClass().getClassLoader().getResourceAsStream(path);
        List<UserInExcel> userInExcels = EasyExcelUtils.doRead(inputStream, UserInExcel.class);
        System.out.print("aa");
    }

    @Data
    public static class UserInExcel extends BaseRowModel {

        @ExcelProperty(value = "姓名", index = 0)
        private String userName;

        @ExcelProperty(value = "年龄", index = 1)
        private Integer age;
    }

}
