
# uml中的关系
## uml中的关系 
![](https://tva1.sinaimg.cn/large/00831rSTgy1gcnokmaq37j30kr15dgqj.jpg)

## 知识点整理
1. 泛化(generalization)：父类和子类的关系
2. 实现(Realization)：接口和实现类的关系
3. 依赖(Dependency)：对象间关联最弱的一种，代码中一般指由局部变量、函数参数、返回值建立的对于其他对象的调用关系。
4. 关联(Association) = 聚合(Aggregation)  + 组合(Composition) 
5. 聚合(Aggregation) :表示has-a的关系，是一种不稳定的包含关系。被关联的事务之间可以独立存在
6. 组合(Composition) : 表示contains-a的关系，是一种强烈的包含关系。一个人由头、手臂等组合而成的。关联的事物之间不能单独存在。

> 聚合和组合的区别：
> 关联事物之间能否独立存在，聚合可以、组合不可以

## 参考资料
[uml中的类关系](http://www.uml.org.cn/oobject/201104212.asp)