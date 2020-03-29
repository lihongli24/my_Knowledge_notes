[TOC]

文章的大概内容概括：
![](https://tva1.sinaimg.cn/large/00831rSTgy1gdb6q1xky8j31yh0rfafn.jpg)


# junit 怎么注入spring创建的bean
如果要在单元测试中使用spring创建的bean的话，我们已使用注解
```java
@RunWith(SpringJUnit4ClassRunner.class)
```
![SpringJunit4ClassRunner](https://tva1.sinaimg.cn/large/00831rSTgy1gdb4vckrdnj30mq0mi0tu.jpg)



```java
ParentRunner:

 @Override
public void run(final RunNotifier notifier) {
    EachTestNotifier testNotifier = new EachTestNotifier(notifier,
            getDescription());
    try {
        Statement statement = classBlock(notifier);
        statement.evaluate();
    } catch (AssumptionViolatedException e) {
        testNotifier.addFailedAssumption(e);
    } catch (StoppedByUserException e) {
        throw e;
    } catch (Throwable e) {
        testNotifier.addFailure(e);
    }
}
```

在ParentRunner(SpringJunit4ClassRunner间接继承了它)中的，使用***classBlock***方法生成了一份Statement，然后调用了它的***evaluate()***方法，这有点像jdk8中的lambda,先构建一个方法的链，最后调用执行方法进行执行。

### classBlock

那么，***classBlock***中做了什么呢？
```java
protected Statement classBlock(final RunNotifier notifier) {
    //封装了methodBlock的Statement,后面会讲到
    Statement statement = childrenInvoker(notifier);
    //如果不是所有的方法都被ignore掉的话，将下面的classBlock相关的东西设置到当前的statement中
    if (!areAllChildrenIgnored()) {
        statement = withBeforeClasses(statement);
        statement = withAfterClasses(statement);
        statement = withClassRules(statement);
    }
    return statement;
}
```

```java
/**
* 封装@Before注解，转化出新的Statement
*/
protected Statement withBeforeClasses(Statement statement) {
    List<FrameworkMethod> befores = testClass
            .getAnnotatedMethods(BeforeClass.class);
    return befores.isEmpty() ? statement :
            new RunBefores(statement, befores, null);
}


protected Statement withAfterClasses(Statement statement) {
    List<FrameworkMethod> afters = testClass
            .getAnnotatedMethods(AfterClass.class);
    return afters.isEmpty() ? statement :
            new RunAfters(statement, afters, null);
}

private Statement withClassRules(Statement statement) {
    List<TestRule> classRules = classRules();
    return classRules.isEmpty() ? statement :
            new RunRules(statement, classRules, getDescription());
}
```

到这里，整体上可以看到，***classBlock***是将当前的***类中所有的方法***(@Test注解的），以及***@BeforeClass***和***@AfterClass***还有***@ClassRule***(这个还没用到过，后面需要看看怎么使用),都封装到一个Statement中，进行执行。


### methodBlock

上面讲了classBlock是将整个类进行了封装，转化成一个Statement。那个

```java

ParentRunner:

//封装出一个Statement,执行的方法，就是跑children---也就是里面的@Test方法
 protected Statement childrenInvoker(final RunNotifier notifier) {
    return new Statement() {
        @Override
        public void evaluate() {
            runChildren(notifier);
        }
    };
}
//具体的功能，会遍历子方法，对他们调用run方法
private void runChildren(final RunNotifier notifier) {
    final RunnerScheduler currentScheduler = scheduler;
    try {
        for (final T each : getFilteredChildren()) {
            currentScheduler.schedule(new Runnable() {
                public void run() {
                    ParentRunner.this.runChild(each, notifier);
                }
            });
        }
    } finally {
        currentScheduler.finished();
    }
}
```

具体的实现是在ParentRunner的子类***BlockJUnit4ClassRunner***中实现的.
```java
BlockJUnit4ClassRunner:

@Override
protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    if (isIgnored(method)) {
        //如果是被ignore的方法，执行忽略逻辑，比如powerMockJunitRunner的++this.ignoreCount;由子类自己实现
        notifier.fireTestIgnored(description);
    } else {
        //对子类进行封装Statement，然后运行
        runLeaf(methodBlock(method), description, notifier);
    }
}
```
里面调用了***methodBlock***方法，因为我们当前使用的是***SpringJunit4ClassRunner***，所以会使用它重写的这个方法
```java
@Override
protected Statement methodBlock(FrameworkMethod frameworkMethod) {
    Object testInstance;
    try {
        testInstance = new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                //创建这个test实例，真正核心的代码再这里面,比如bean的注入等，使用Listener来实现的功能
                return createTest();
            }
        }.run();
    }
    catch (Throwable ex) {
        return new Fail(ex);
    }

    //将当前测试类中的@Before和@After等注解的功能封装近statement中。
    Statement statement = methodInvoker(frameworkMethod, testInstance);
    statement = possiblyExpectingExceptions(frameworkMethod, testInstance, statement);
    statement = withBefores(frameworkMethod, testInstance, statement);
    statement = withAfters(frameworkMethod, testInstance, statement);
    statement = withRulesReflectively(frameworkMethod, testInstance, statement);
    statement = withPotentialRepeat(frameworkMethod, testInstance, statement);
    statement = withPotentialTimeout(frameworkMethod, testInstance, statement);
    return statement;
}


//创建test实例
@Override
protected Object createTest() throws Exception {
    Object testInstance = super.createTest();
    getTestContextManager().prepareTestInstance(testInstance);
    return testInstance;
}

//设置 testInstance中的属性
public void prepareTestInstance(Object testInstance) throws Exception {
    Assert.notNull(testInstance, "Test instance must not be null");
    if (logger.isTraceEnabled()) {
        logger.trace("prepareTestInstance(): instance [" + testInstance + "]");
    }
    getTestContext().updateState(testInstance, null, null);
    
    //核心逻辑，使用它的Listener来做特定逻辑的实现，其中包含bean的注入
    for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
        try {
            testExecutionListener.prepareTestInstance(getTestContext());
        }
        catch (Throwable ex) {
            if (logger.isErrorEnabled()) {
                logger.error("Caught exception while allowing TestExecutionListener [" + testExecutionListener +
                        "] to prepare test instance [" + testInstance + "]", ex);
            }
            ReflectionUtils.rethrowException(ex);
        }
    }
}
```

***DependencyInjectionTestExecutionListener***就是上面的***TestExecutionListener***的实现类，具体的功能就是将bean注入当前的测试类
```java
@Override
public void prepareTestInstance(TestContext testContext) throws Exception {
    if (logger.isDebugEnabled()) {
        logger.debug("Performing dependency injection for test context [" + testContext + "].");
    }
    //注入依赖
    injectDependencies(testContext);
}

//注入依赖的bean
protected void injectDependencies(TestContext testContext) throws Exception {
    Object bean = testContext.getTestInstance();
    AutowireCapableBeanFactory beanFactory = testContext.getApplicationContext().getAutowireCapableBeanFactory();
    //实现逻辑，spring bean初始化的第二步populateBean逻辑
    beanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
    //spring bean初始化逻辑的第三步，调用init-method之类的逻辑
    beanFactory.initializeBean(bean, testContext.getTestClass().getName());
    testContext.removeAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE);
}
```
