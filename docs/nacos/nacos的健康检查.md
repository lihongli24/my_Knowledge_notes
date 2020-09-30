# nacos的心跳机制

在nacos的依赖包中，有一个专门的目录`beat`
![](https://tva1.sinaimg.cn/large/007S8ZIlly1gj8ij63wtkj30kh0bgq51.jpg)

里面有两个类，`BeatInfo`是心跳的元数据信息
```java
public class BeatInfo {

    private int port;
    private String ip;
    private double weight;
    private String serviceName;
    private String cluster;
    private Map<String, String> metadata;
    private volatile boolean scheduled;
    private volatile long period;
    private volatile boolean stopped;
    ......
}
```

`BeatReactor` 才是心跳的触发类


## 心跳的注册
![](https://tva1.sinaimg.cn/large/007S8ZIlly1gj8ip9zk86j30vz0g6420.jpg)
使用NacosNamingService去注册服务的时候，如果当前的服务节点为临时节点的话，会构件出一个`BeatInfo`记录，调用 `beatReactor.addBeatInfo`方法。

```java
public void addBeatInfo(String serviceName, BeatInfo beatInfo) {
        NAMING_LOGGER.info("[BEAT] adding beat: {} to beat map.", beatInfo);
        dom2Beat.put(buildKey(serviceName, beatInfo.getIp(), beatInfo.getPort()), beatInfo);
        executorService.schedule(new BeatTask(beatInfo), 0, TimeUnit.MILLISECONDS);
        MetricsMonitor.getDom2BeatSizeMonitor().set(dom2Beat.size());
    }
```

这里面使用了`executorService`线程池，异步执行BeatTask任务.
```java
class BeatTask implements Runnable {

    BeatInfo beatInfo;

    public BeatTask(BeatInfo beatInfo) {
        this.beatInfo = beatInfo;
    }

    @Override
    public void run() {
        if (beatInfo.isStopped()) {
            return;
        }
        long result = serverProxy.sendBeat(beatInfo);
        long nextTime = result > 0 ? result : beatInfo.getPeriod();
        executorService.schedule(new BeatTask(beatInfo), nextTime, TimeUnit.MILLISECONDS);
    }
}
```
BeatTask 会向nacos集群发送心跳，然后按照返回的间隔时间做延迟。


## 心跳的取消
在NacosNameService中注销一个应用实例的时候，会调用他`deregisterInstance`方法.
里面除了从通知nacos集群取消节点之外，还会去删除掉心跳记录
```java
@Override
    public void deregisterInstance(String serviceName, String groupName, Instance instance) throws NacosException {
        if (instance.isEphemeral()) {
            beatReactor.removeBeatInfo(NamingUtils.getGroupedName(serviceName, groupName), instance.getIp(), instance.getPort());
        }
        serverProxy.deregisterService(NamingUtils.getGroupedName(serviceName, groupName), instance);
    }
```

```java
 public void removeBeatInfo(String serviceName, String ip, int port) {
    NAMING_LOGGER.info("[BEAT] removing beat: {}:{}:{} from beat map.", serviceName, ip, port);
    BeatInfo beatInfo = dom2Beat.remove(buildKey(serviceName, ip, port));
    if (beatInfo == null) {
        return;
    }
    beatInfo.setStopped(true);
    MetricsMonitor.getDom2BeatSizeMonitor().set(dom2Beat.size());
}
```

这里将beatInfo的stop字段设置成了true, 按照前面`BeatTask`中的代码，当beatInfo的stop字段为true的时候，就会停止发送心跳请求。

