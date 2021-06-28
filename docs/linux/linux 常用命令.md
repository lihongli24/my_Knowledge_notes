# linux 常用命令

[toc]

## 查看磁盘使用情况

* 列出某个目录下，每个文件夹占用空间大小

  ```shell
  du --max-depth=1 -h /   
  ```

* ls 的时候显示文件大小的单位

  ```shell
  ls -h
  ```

## 往文件中输入数据

```shell
cat >> /var/spool/cron/root << eof
```

往/var/spool/cron/root文件中输入内容，当输入 **eof ** 的时候表示结束



## 创建linux定时任务

1. 往/var/spool/cron/root输入文件，并且eof为结束

```shell
cat >> /var/spool/cron/root << eof
```

2. 输入任务

```shell
0 0 * * * sh 可执行的脚本
```

3. 输入eof结束输入





 