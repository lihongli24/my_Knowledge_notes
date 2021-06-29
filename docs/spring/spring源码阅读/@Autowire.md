# @Autowire的实现原理

## 流程图
@import "@Autowire流程图.puml"

## 归纳总结
这个过程发生在bean创建的过程中，spring最开始是支持xml的方式注入属性的，所以有autowireByName或者autowireByType这种直接写在BeanFactory中的代码。
@Autowire或者@Value这种是后续增加的支持，spring对于新功能的拓展上做的很好。

spring 在bean的创建过程中有两个步骤
1. applyMergedBeanDefinitionPostProcessors
2. ibp.postProcessProperties


applyMergedBeanDefinitionPostProcessors 方法 内容如下：
```shell
protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof MergedBeanDefinitionPostProcessor) {
				MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
				bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
			}
		}
	}
```

这个方法

