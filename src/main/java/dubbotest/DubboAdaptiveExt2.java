package dubbotest;

import com.alibaba.dubbo.common.URL;

/**
 * @author lihongli
 * create：2020/1/31 9:11 上午
 */
public class DubboAdaptiveExt2 implements AdaptiveExt2 {

    @Override
    public String echo(String msg, URL url) {
        return "dubbotest";
    }
}
