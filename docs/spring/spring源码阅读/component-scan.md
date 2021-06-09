# component-scan 标签的作用和实现原理啊
## 现象
使用xml配置方式下，当需要让spring自动扫描某一个目录下的bean的时候，我们会使用下面的方式进行实现

```xml
<context:component-scan base-package="xxxx"></context:component-scan>
```

那么他是怎么实现的呢，为什么加入了这个标签之后，文件夹下面的bean就能被自动注入

## 

## 思路

在spring中想让一个类的实例的对象被spring自动管理，那么就需要让这个类的信息以`BeanDefinition`的方式写入spring容器里面，那之后spring才会对它进行实例化、初始化、和后续的管理。



## component-scan的实现

### spring 容器的启动

AbstractApplicationContext 容器的启动

```java
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			。。。。。。。。

			// Tell the subclass to refresh the internal bean factory.
			// 创建容器对象：DefaultListableBeanFactory
			// 加载xml配置文件的属性值到当前工厂中，最重要的就是BeanDefinition
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
			。。。。。。。。
		}
	}

```

在启动spring容器的过程中，需要创建一个BeanFactory来管理对象，



```java
// 创建beanFactory
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		// 初始化BeanFactory,并进行XML文件读取，并将得到的BeanFactory记录在当前实体的属性中
		refreshBeanFactory();
		// 返回当前实体的beanFactory属性
		return getBeanFactory();
	}
```

在创建beanfactory的过程中，需要对指定的xml进行读取，






AbstractRefreshableApplicationContext

```java
@Override
	protected final void refreshBeanFactory() throws BeansException {
		// 如果存在beanFactory，则销毁beanFactory
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			// 创建DefaultListableBeanFactory对象
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			// 为了序列化指定id，可以从id反序列化到beanFactory对象
			beanFactory.setSerializationId(getId());
			// 定制beanFactory，设置相关属性，包括是否允许覆盖同名称的不同定义的对象以及循环依赖
			customizeBeanFactory(beanFactory);
			// 初始化documentReader,并进行XML文件读取及解析,默认命名空间的解析，自定义标签的解析
			loadBeanDefinitions(beanFactory);
			this.beanFactory = beanFactory;
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}
```



**里面最重要的代码为：loadBeanDefinitions(beanFactory); ** 加载BeanDefinition,就是把bean的定义信息写入beanFactory，之后就能通过这些信息进行bean的实例化，初始化，依赖注入这一系列的操作。



### loadBeanDefinitions
* 在spring中使用xml的方式加载bean的方式如下

```java
@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		// 创建一个xml的beanDefinitionReader，并通过回调设置到beanFactory中
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		// 给reader对象设置环境对象
		beanDefinitionReader.setEnvironment(this.getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		//  初始化beanDefinitionReader对象，此处设置配置文件是否要进行验证
		initBeanDefinitionReader(beanDefinitionReader);
		// 开始完成beanDefinition的加载
		loadBeanDefinitions(beanDefinitionReader);
	}
```
创建一个**XmlBeanDefinitionReader**对象，进行beanDefinition进行读取。

* 后面就是获取资源文件，对文件进行读取

```java
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		// 以Resource的方式获得配置文件的资源位置
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		// 以String的形式获得配置文件的位置
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			reader.loadBeanDefinitions(configLocations);
		}
	}
```

* XmlBeanDefinitionReader 使用xml的方式读取文件，解析xml里面的节点

```java
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
		// 对xml的beanDefinition进行解析
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
		int countBefore = getRegistry().getBeanDefinitionCount();
		// 完成具体的解析过程
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}
```



### 解析xml标签

* DefaultBeanDefinitionDocumentReader 解析xml中的beanDefinition

```java
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					if (delegate.isDefaultNamespace(ele)) {
						parseDefaultElement(ele, delegate);
					}
					else {
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			delegate.parseCustomElement(root);
		}
	}
```



delegate.isDefaultNamespace 判断内容如下：

>  判断element节点是不是 "http://www.springframework.org/schema/beans"下定义的
>
> 如果不是，就认为是自定义节点



```java
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
  // 解析<import>标签
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
  // 解析 <alias>标签
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
  // 解析 <bean>标签
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
  // 解析 <beans>标签
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// recurse
			doRegisterBeanDefinitions(ele);
		}
	}
```

### 解析自定义xml标签

对于自定义标签的接下

```java
@Nullable
	public BeanDefinition parseCustomElement(Element ele, @Nullable BeanDefinition containingBd) {
		// 获取对应的命名空间
		String namespaceUri = getNamespaceURI(ele);
		if (namespaceUri == null) {
			return null;
		}
		// 根据命名空间找到对应的NamespaceHandlerspring
		NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
		if (handler == null) {
			error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
			return null;
		}
		// 调用自定义的NamespaceHandler进行解析
		return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
	}
```



![image-20210606235639795](https://tva1.sinaimg.cn/large/008i3skNgy1gr8zkf7sgjj310s0u0naw.jpg)



在readerContext中维护了已对xml标签对应的解析处理类。我们的```<context:component-scan/>``` 标签对应的处理类为**ComponentScanBeanDefinitionParser**



### ComponentScanBeanDefinitionParser的内容解析

```java
@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		// 获取<context:component-scan>节点的base-package属性值
		String basePackage = element.getAttribute(BASE_PACKAGE_ATTRIBUTE);
		// 解析占位符
		basePackage = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(basePackage);
		// 解析base-package(允许通过,;\t\n中的任一符号填写多个)
		String[] basePackages = StringUtils.tokenizeToStringArray(basePackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);

		// Actually scan for bean definitions and register them.
		// 构建和配置ClassPathBeanDefinitionScanner
		ClassPathBeanDefinitionScanner scanner = configureScanner(parserContext, element);
		// 使用scanner在执行的basePackages包中执行扫描，返回已注册的bean定义
		Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan(basePackages);
		// 组件注册（包括注册一些内部的注解后置处理器，触发注册事件）
		registerComponents(parserContext.getReaderContext(), beanDefinitions, element);

		return null;
	}
```

### ClassPathBeanDefinitionScanner.doscan

```java
protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
		// 遍历basePackages
		for (String basePackage : basePackages) {
			// 扫描basePackage,将符合要求的bean定义全部找出来
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
			// 遍历所有候选的bean定义
			for (BeanDefinition candidate : candidates) {
				// 解析@Scope注解，包括scopeName和proxyMode
				ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
				candidate.setScope(scopeMetadata.getScopeName());
				// 使用beanName生成器来生成beanName
				String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
				if (candidate instanceof AbstractBeanDefinition) {
					// 处理beanDefinition对象，例如，此bean是否可以自动装配到其他bean中
					postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
				}
				if (candidate instanceof AnnotatedBeanDefinition) {
					// 处理定义在目标类上的通用注解，包括@Lazy，@Primary，@DependsOn，@Role，@Description
					AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
				}
				// 检查beanName是否已经注册过，如果注册过，检查是否兼容
				if (checkCandidate(beanName, candidate)) {
					// 将当前遍历bean的bean定义和beanName封装成BeanDefinitionHolder
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
					// 根据proxyMode的值，选择是否创建作用域代理
					definitionHolder =
							AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
					beanDefinitions.add(definitionHolder);
					// 注册beanDefinition
					registerBeanDefinition(definitionHolder, this.registry);
				}
			}
		}
		return beanDefinitions;
	}
```



```java
private Set<BeanDefinition> scanCandidateComponents(String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					resolveBasePackage(basePackage) + '/' + this.resourcePattern;
			Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			for (Resource resource : resources) {
				if (traceEnabled) {
					logger.trace("Scanning " + resource);
				}
				if (resource.isReadable()) {
					try {
						MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
						if (isCandidateComponent(metadataReader)) {
							ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
							sbd.setSource(resource);
							if (isCandidateComponent(sbd)) {
								if (debugEnabled) {
									logger.debug("Identified candidate component class: " + resource);
								}
								candidates.add(sbd);
							}
							.....
		return candidates;
	}
```



解析basePackage下的资源文件，
