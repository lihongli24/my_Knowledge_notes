# SPI

文件：ExtensionLoader

路径：org.apache.dubbo.common.extension

作用：使用SPI来实现接口的定义和实现的解耦，做到面向接口编程

使用实例：org.apache.dubbo.remoting.transport.dispatcher.ChannelHandlers

```java
protected ChannelHandler wrapInternal(ChannelHandler handler, URL url) {
  return new MultiMessageHandler(new 				HeartbeatHandler(ExtensionLoader.getExtensionLoader(Dispatcher.class)                                      .getAdaptiveExtension().dispatch(handler, url)));
}
```



调用到了

1. ExtensionLoader.getExtensionLoader
2. getAdaptiveExtension

最后异步.dispatch方法就是在调用Dispatcher的方法了



```java
 
ExtensionLoader：
  
public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
	//非空判断
   if (type == null) {
     throw new IllegalArgumentException("Extension type == null");
   }
   //传入的必须是一个接口
   if (!type.isInterface()) {
     throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
   }
   //该接口上必须有@SPI注解
   if (!withExtensionAnnotation(type)) {
     throw new IllegalArgumentException("Extension type (" + type +
                                        ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
   }
   
   //使用map来存放，防止重复加载
   ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
   if (loader == null) {
     EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
     loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
   }
   return loader;
 }

ExtensionLoader：

//ExtensionLoader的构造函数：设置了type
private ExtensionLoader(Class<?> type) {
	//设置变量type
  this.type = type;
  //用同样的方式加载ExtensionFactory的实现类
  objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
}
```

上面的方法不管是加载Dispatcher还是ExtensionFactory，最后都调用了getAdaptiveExtension方法获取到对应的实现类

```java
ExtensionLoader：
  
public T getAdaptiveExtension() {
  //使用单例二次确认的方式获取最终的返回值
   Object instance = cachedAdaptiveInstance.get();
   if (instance == null) {
     if (createAdaptiveInstanceError != null) {
       throw new IllegalStateException("Failed to create adaptive instance: " +
                                       createAdaptiveInstanceError.toString(),
                                       createAdaptiveInstanceError);
     }

     synchronized (cachedAdaptiveInstance) {
       instance = cachedAdaptiveInstance.get();
       if (instance == null) {
         try {
           //最终调用获取实例的方法,然后放入这个map中
           instance = createAdaptiveExtension();
           cachedAdaptiveInstance.set(instance);
         } catch (Throwable t) {
           createAdaptiveInstanceError = t;
           throw new IllegalStateException("Failed to create adaptive instance: " + t.toString(), t);
         }
       }
     }
   }

   return (T) instance;
 }


//创建适配器
private T createAdaptiveExtension() {
  try {
    return injectExtension((T) getAdaptiveExtensionClass().newInstance());
  } catch (Exception e) {
    throw new IllegalStateException("Can't create adaptive extension " + type + ", cause: " + e.getMessage(), e);
  }
}

//载入对应的class
private Class<?> getAdaptiveExtensionClass() {
  //从配置文件加载指定的类
  getExtensionClasses();
  if (cachedAdaptiveClass != null) {
    return cachedAdaptiveClass;
  }
  return cachedAdaptiveClass = createAdaptiveExtensionClass();
}
```


## getExtensionClasses 加载配置的类
```java
ExtensionLoader： 
  
//单例的二重校验获取
private Map<String, Class<?>> getExtensionClasses() {
  Map<String, Class<?>> classes = cachedClasses.get();
  if (classes == null) {
    synchronized (cachedClasses) {
      classes = cachedClasses.get();
      if (classes == null) {
        classes = loadExtensionClasses();
        cachedClasses.set(classes);
      }
    }
  }
  return classes;
}

//加载配置
private Map<String, Class<?>> loadExtensionClasses() {
  cacheDefaultExtensionName();

  Map<String, Class<?>> extensionClasses = new HashMap<>();

  for (LoadingStrategy strategy : strategies) {
    loadDirectory(extensionClasses, strategy.directory(), type.getName(), strategy.preferExtensionClassLoader(), strategy.excludedPackages());
    loadDirectory(extensionClasses, strategy.directory(), type.getName().replace("org.apache", "com.alibaba"), strategy.preferExtensionClassLoader(), strategy.excludedPackages());
  }

  return extensionClasses;
}
```
上面的代码里面涉及到了一个策略strategy类，目前会从下面的三个目录中加载类
```java
private static final String SERVICES_DIRECTORY = "META-INF/services/";
private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";
private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";
```

### loadFile

```java
ExtensionLoader:

private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir, String type,
                           boolean extensionLoaderClassLoaderFirst, String... excludedPackages) {
  //目录名+接口名=需要寻找的文件名字
  String fileName = dir + type;
  try {
    Enumeration<java.net.URL> urls = null;
    ClassLoader classLoader = findClassLoader();

    // try to load from ExtensionLoader's ClassLoader first
    if (extensionLoaderClassLoaderFirst) {
      ClassLoader extensionLoaderClassLoader = ExtensionLoader.class.getClassLoader();
      if (ClassLoader.getSystemClassLoader() != extensionLoaderClassLoader) {
        urls = extensionLoaderClassLoader.getResources(fileName);
      }
    }

    //这个位置没太看懂这个urls是什么概念，可能是说这个文件不止在一个路径上出现
    if(urls == null || !urls.hasMoreElements()) {
      if (classLoader != null) {
        urls = classLoader.getResources(fileName);
      } else {
        urls = ClassLoader.getSystemResources(fileName);
      }
    }

    if (urls != null) {
      while (urls.hasMoreElements()) {
        java.net.URL resourceURL = urls.nextElement();
        //真正的加载类
        loadResource(extensionClasses, classLoader, resourceURL, excludedPackages);
      }
    }
  } catch (Throwable t) {
    logger.error("Exception occurred when loading extension class (interface: " +
                 type + ", description file: " + fileName + ").", t);
  }
}

//从文件中加载类
private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader,
                          java.net.URL resourceURL, String... excludedPackages) {
  try {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
      String line;
      //一行一行的读取，对#=等特殊位置做判断，取出名字和实现的类
      while ((line = reader.readLine()) != null) {
        final int ci = line.indexOf('#');
        if (ci >= 0) {
          line = line.substring(0, ci);
        }
        line = line.trim();
        if (line.length() > 0) {
          try {
            String name = null;
            int i = line.indexOf('=');
            if (i > 0) {
              name = line.substring(0, i).trim();
              line = line.substring(i + 1).trim();
            }
            if (line.length() > 0 && !isExcluded(line, excludedPackages)) {
              //真正去执行加载类,使用Class.forName的方式加载类 Class.forName(line, true, classLoader), name
              loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
            }
          } catch (Throwable t) {
            IllegalStateException e = new IllegalStateException("Failed to load extension class (interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
            exceptions.put(line, e);
          }
        }
      }
    }
  } catch (Throwable t) {
    logger.error("Exception occurred when loading extension class (interface: " +
                 type + ", class file: " + resourceURL + ") in " + resourceURL, t);
  }
}



private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {
  //判断类型是否匹配
  if (!type.isAssignableFrom(clazz)) {
    throw new IllegalStateException("Error occurred when loading extension class (interface: " +
                                    type + ", class line: " + clazz.getName() + "), class "
                                    + clazz.getName() + " is not subtype of interface.");
  }
  
  //如果当前类是一个带有@Adaptive注解的类， 使用cachedAdaptiveClass指向它
  if (clazz.isAnnotationPresent(Adaptive.class)) {
    cacheAdaptiveClass(clazz);
    //如果这个类是一个封装类，构造函数中用type指定的类型，放入缓存set cachedWrapperClasses中
  } else if (isWrapperClass(clazz)) {
    cacheWrapperClass(clazz);
  } else {
    //对@Extension注解的情况做判断，放入cachedActivates
    clazz.getConstructor();
    if (StringUtils.isEmpty(name)) {
      name = findAnnotationName(clazz);
      if (name.length() == 0) {
        throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
      }
    }

    String[] names = NAME_SEPARATOR.split(name);
    if (ArrayUtils.isNotEmpty(names)) {
      cacheActivateClass(clazz, names[0]);
      for (String n : names) {
        cacheName(clazz, n);
        saveInExtensionClass(extensionClasses, clazz, n);
      }
    }
  }
}
```

到这里我们已经载入了对应的类，`getAdaptiveExtensionClass`方法已经执行完成了，它返回的是一个class对象。

回到我们的最上面的代码

```java
private Class<?> getAdaptiveExtensionClass() {
  //从资源里面加载类，----------已完成
  getExtensionClasses();
  //判断缓存cachedAdaptiveClass是否已经被设置了，按照上面的代码，只有类上注释了@dAdaptive的才会被设置
  if (cachedAdaptiveClass != null) {
    return cachedAdaptiveClass;
  }
  //对那些方法上写了@dAdaptive的类进行dubbo的动态生成适配器操作
  return cachedAdaptiveClass = createAdaptiveExtensionClass();
}
```

到这里总结下：

1. 类上注释了@Adaptive的属于用户自己实现的适配器
2. 方法上加了@Adaptive注解的，需要dubbo动态生成适配器

```java
//使用dubbo的方式动态生成适配器
private Class<?> createAdaptiveExtensionClass() {
  String code = new AdaptiveClassCodeGenerator(type, cachedDefaultName).generate();
  ClassLoader classLoader = findClassLoader();
  org.apache.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(org.apache.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
  return compiler.compile(code, classLoader);
}
```

这种动态生成适配器的方式，下面举个例子：

```java
@SPI(FailoverCluster.NAME)
public interface Cluster {

    /**
     * Merge the directory invokers to a virtual invoker.
     *
     * @param <T>
     * @param directory
     * @return cluster invoker
     * @throws RpcException
     */
    @Adaptive
    <T> Invoker<T> join(Directory<T> directory) throws RpcException;
}
```

生成的动态适配器为

```java
package com.alibaba.dubbo.rpc.cluster;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
public class Cluster$Adpative implements com.alibaba.dubbo.rpc.cluster.Cluster {
|   public com.alibaba.dubbo.rpc.Invoker join(com.alibaba.dubbo.rpc.cluster.Directory arg0) throws com.alibaba.dubbo.rpc.RpcException {
|   |   if (arg0 == null) throw new IllegalArgumentException("com.alibaba.dubbo.rpc.cluster.Directory argument == null");
|   |   if (arg0.getUrl() == null) throw new IllegalArgumentException("com.alibaba.dubbo.rpc.cluster.Directory argument getUrl() == null");
|   |   com.alibaba.dubbo.common.URL url = arg0.getUrl();
|   |   String extName = url.getParameter("cluster", "failover");
|   |   if(extName == null) throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.cluster.Cluster) name from url(" + url.toString() + ") use keys([cluster])");
|   |   com.alibaba.dubbo.rpc.cluster.Cluster extension = (com.alibaba.dubbo.rpc.cluster.Cluster)ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.cluster.Cluster.class).getExtension(extName);
|   |   return extension.join(arg0);
|   }
 }
```





后续会被调用它的构造函数，创建实例` getAdaptiveExtensionClass().newInstance()`

```java
//创建适配器
private T createAdaptiveExtension() {
  try {
    return injectExtension((T) getAdaptiveExtensionClass().newInstance());
  } catch (Exception e) {
    throw new IllegalStateException("Can't create adaptive extension " + type + ", cause: " + e.getMessage(), e);
  }
}
```

下面执行`injectExtension`，这个方法在看spring源码的时候有遇到过，就是对内部的属性设置。

```java
//给对象内的属性设值
private T injectExtension(T instance) {
  if (objectFactory == null) {
    return instance;
  }

  try {
    //遍历方法
    for (Method method : instance.getClass().getMethods()) {
      //不是set方法忽略，所以在自己实现dubbo的spi类的时候，如果想注入对象，需要实现它的set方法，比如filter
      if (!isSetter(method)) {
        continue;
      }
     //忽略不需要注入的
      if (method.getAnnotation(DisableInject.class) != null) {
        continue;
      }
      Class<?> pt = method.getParameterTypes()[0];
      //如果属性是原语的，忽略
      if (ReflectUtils.isPrimitives(pt)) {
        continue;
      }

      try {
        //获取属性值，使用set方法设置进去
        String property = getSetterProperty(method);
        Object object = objectFactory.getExtension(pt, property);
        if (object != null) {
          method.invoke(instance, object);
        }
      } catch (Exception e) {
        logger.error("Failed to inject via method " + method.getName()
                     + " of interface " + type.getName() + ": " + e.getMessage(), e);
      }

    }
  } catch (Exception e) {
    logger.error(e.getMessage(), e);
  }
  return instance;
}

```




















## 参考

[源码讲解](https://blog.csdn.net/xiaoxufox/article/details/75117992)

