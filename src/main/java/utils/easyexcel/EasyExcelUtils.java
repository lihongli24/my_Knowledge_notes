package utils.easyexcel;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import org.testng.collections.Lists;

/**
 * 需要apache-pio在3.17之上，否则会因为excel版本出问题
 * Created by lihongli on 2019/10/25.
 */
public class EasyExcelUtils {

    public static <T extends BaseRowModel> List<T> doRead(InputStream inputStream, Class<T> clazz) {
        MyExcelReadListener<T> readListener = new MyExcelReadListener<>();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ExcelReader excelReader = new ExcelReader(bufferedInputStream, null, readListener);
        excelReader.read(new Sheet(1, 1, clazz));
        return readListener.getData();
    }

    public static class MyExcelReadListener<R extends BaseRowModel> extends AnalysisEventListener<R> {

        private List<R> data = Lists.newArrayList();

        /**
         * when analysis one row trigger invoke function
         *
         * @param object one row data
         * @param context analysis context
         */
        @Override
        public void invoke(R object, AnalysisContext context) {
            data.add(object);
        }

        /**
         * if have something to do after all  analysis
         */
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
//            throw new RuntimeException("aa");
        }

        public List<R> getData() {
            return data;
        }
    }
}
