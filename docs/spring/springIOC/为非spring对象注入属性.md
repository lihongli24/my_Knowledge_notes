domain层的Repository
```java
public abstract class DomainRepository<T extends Entity> {

    @Resource
    protected AutowireCapableBeanFactory spring;

    protected T autowireEntity(T t) {
        spring.autowireBean(t);
        return t;
    }
```

domain层的entity，将一个领域实体内该干的事情内聚到这个entity里面。

```java
@Setter
@Getter
@Accessors(chain = true)
@ToString(callSuper = true)
public class SimpleEntity extends Entity<Long> {
    @Autowired
    private  SimpleEntityRepository entityRepository;

    private String name;

    private Integer age;

    @Override
    public void persist() {
        entityRepository.persist(this);
    }
}
```

在使用的时候可以使用
```java
@Repository
@AllArgsConstructor
@Slf4j
public class SimpleEntityRepository extends DomainRepository<SimpleEntity> {

    public SimpleEntity getEntity(){
        //使用什么方式获取一个entity
        SimpleEntity entity = new SimpleEntity();
        this.autowireEntity(entity);
    }
}

```

上面的使用方式属于DDD的domain层的使用编写方式，将实体的操作内容写在entity自己的代码里面，所以需要对new出来的entity里面注入一些属性，那些属性又是在spring里面维护了单例，所以可以使用AutowireCapableBeanFactory来往一个非spring的对象里面注入spring对象。