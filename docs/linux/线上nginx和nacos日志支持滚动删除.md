# 线上nginx和nacos日志滚动功能

## 问题背景
线上nginx和nacos的访问过程中会输出，而且他们的日志不支持滚动覆盖的方式，所以日志文件会一直增加，直到把磁盘占用完
我们希望的情况应该是这样的:
1. 能查看最近一段时间内的nacos或者nginx访问日志
2. 磁盘使用量上不能受这两个日志影响太大
所以现在使用下面的方法进行日志滚动的操作


## 网上的脚本
网上找到一个不错的帖子，按照定时任务的方式自动切割日志文件
[网上地址](https://www.cnblogs.com/wushuaishuai/p/9336454.html#_label1_2)


## 我们的实现
使用了帖子里面的脚本3
* 创建脚本文件
```shell
vim /home/skyee-mw/shell/logs-rotate.sh
```
* 输入内容
```shell
#!/bin/bash

function rotate() {
logs_path=$1

echo Rotating Log: $1
cp ${logs_path} ${logs_path}.$(date -d "yesterday" +"%Y%m%d")
> ${logs_path}
    rm -f ${logs_path}.$(date -d "7 days ago" +"%Y%m%d")
}

for i in $*
do
        rotate $i
done
```

## 脚本设置权限&&手动执行确认没问题
```shell
chmod +x /home/skyee-mw/shell/logs-rotate.sh
```

```shell
find /home/skyee/nginx/logs/ -size +0 -name 'access.log' | xargs /home/skyee-mw/shell/logs-rotate.sh
```

## 设置到定时任务的列表中去
* 1. 往/var/spool/cron/root中写入，以eof结尾
```shell
cat >> /var/spool/cron/root << eof
```

* 2. 输入内容如下
```shell
30 0 * * * find /home/skyee/nginx/logs/ -size +0 -name 'access.log' | xargs /home/skyee-mw/shell/logs-rotate.sh
```

* 3. 输入结束
```shell
eof
```

## 结束
上面的设置是nginx的，nacos也可以按照这个方式处理
