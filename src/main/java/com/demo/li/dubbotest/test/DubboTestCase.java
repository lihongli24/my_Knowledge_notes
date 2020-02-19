package com.demo.li.dubbotest.test;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.demo.li.dubbotest.AdaptiveExt2;
import org.testng.annotations.Test;

/**
 * @author lihongli
 * create：2020/1/31 9:17 上午
 */
public class DubboTestCase {
    @Test
    public void test1() {
        ExtensionLoader<AdaptiveExt2> loader = ExtensionLoader.getExtensionLoader(AdaptiveExt2.class);
        AdaptiveExt2 adaptiveExtension = loader.getAdaptiveExtension();
        URL url = URL.valueOf("test://localhost/test");
        System.out.println(adaptiveExtension.echo("d", url));
//        System.out.println(adaptiveExtension.echo("d"));
    }
}
