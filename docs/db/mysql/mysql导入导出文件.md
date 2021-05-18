# mysql导入导出数据

## 导出
```shell
mysqldump -h47.106.245.218 -P3306 -uskyeeTester -p --databases skyeedb > ~/dump.sql
```
## 导入
* 先连接数据库
mysql -uroot -p --default-character-set=utf8
source dump.sql