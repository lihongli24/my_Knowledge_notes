# dubbo的线程模型

最近在线上的项目偶尔会出现`Thread pool is EXHAUSTED`这个异常，意思是线程池耗尽。所以在这里研究下dubbo的这个线程池是什么地方使用的，能不能解决下这个线程池的这个耗尽的问题。



要说dubbo怎么提供服务和怎么调用别人提供的服务，直接用了官网上的两张介绍图片。

![/dev-guide/images/dubbo-framework.jpg](http://dubbo.apache.org/docs/zh-cn/dev/sources/images/dubbo-framework.jpg)

<img src="http://dubbo.apache.org/docs/zh-cn/dev/sources/images/dubbo-extension.jpg" alt="/dev-guide/images/dubbo-extension.jpg" />

