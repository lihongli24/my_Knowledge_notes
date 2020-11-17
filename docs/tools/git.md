# git

## 使用ssh的方式关联多个git平台

### 创建工作文件夹

在用户的个人目录~下，创建一个文件夹.ssh

```shell
mkdir -p ~/.ssh/git_rsa_keys
```

### 生成密钥对
```shell
 ssh-keygen -t rsa -C "XXXX@qq.com"
```

####设置需要保存的密钥对名称，其他的直接回车就行

```shell
Generating public/private rsa key pair.
Enter file in which to save the key (/Users/lihongli/.ssh/id_rsa): tencent
Enter passphrase (empty for no passphrase):
Enter same passphrase again:
Your identification has been saved in tencent.
Your public key has been saved in tencent.pub.
```

上面的tencent是为了方便区分git平台所设置的密钥对名称，tencent对应腾讯开发者平台

试了下，貌似不能修改路径，不知道是不是姿势不对

执行完成之后得到

```shell
ls | grep tencent
tencent
tencent.pub
```

### 配置config文件

因为上面的操作密钥对生成在~/.ssh目录下面，为了统一起见，我们将他们全都挪到上面生成的git_rsa_keys目录下

在~/.ssh目录下创建配置文件

```shell
# tencent coding
Host dev.tencent.com
HostName dev.tencent.com
User git
IdentityFile ~/.ssh/git_rsa_keys/tencent_rsa

# github
Host github.com
HostName github.com
User git
IdentityFile ~/.ssh/git_rsa_keys/github_rsa
```

### 设置远程的公钥

将上面生成的tencent.pub中的内容复制到平台的设置ssh的位置，每个平台都差不多，找一下就行了

### 测试是否可用



