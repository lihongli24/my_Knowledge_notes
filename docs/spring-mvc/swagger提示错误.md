# swagger 提示错误处理

## 问题：
遇到个问题，一个项目的swagger直接通过ip端口可以直接访问，
但是通过spring cloud 集成swaager的方式，`/v2/api-docs`接口可以正常返回，但是页面上显示
显示内容和下面的图差不多
![](https://img-blog.csdnimg.cn/20201222175245127.png)

> 忽略上图中的ip端口


## 解决方式：
swagger版本太高导致的，
统一将swagger关联的版本降级到 `2.5.0`
