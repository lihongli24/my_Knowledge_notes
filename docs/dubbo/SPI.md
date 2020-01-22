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
              //真正去执行加载类
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
  
  if (clazz.isAnnotationPresent(Adaptive.class)) {
    cacheAdaptiveClass(clazz);
  } else if (isWrapperClass(clazz)) {
    cacheWrapperClass(clazz);
  } else {
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




```java
//加载指定的类
private Class<?> createAdaptiveExtensionClass() {
  String code = new AdaptiveClassCodeGenerator(type, cachedDefaultName).generate();
  ClassLoader classLoader = findClassLoader();
  org.apache.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(org.apache.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
  return compiler.compile(code, classLoader);
}
```





## 参考

[源码讲解](https://blog.csdn.net/xiaoxufox/article/details/75117992)

