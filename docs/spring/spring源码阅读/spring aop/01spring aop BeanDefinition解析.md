# spring aop beanDefinition解析

[toc]

## 前言
需要让spring在创建bean的过程中能使用到aop的内容，就得在beanDefinition解析的时候，将aop的内容生成到beanFactory中

## 解析步骤

### 1. 找到具体的地方用来解析aop的注解
@import "01-寻找解析器.puml"

### 2. 开始解析