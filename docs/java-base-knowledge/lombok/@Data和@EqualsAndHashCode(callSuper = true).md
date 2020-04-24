# 为什么使用lombok的时候，使用了@Data之后需要在上面添加@EqualsAndHashCode(callSuper=true)的标记

定义一个Person类
```java
@Data
public class Person {
    /**
     * 名字
     */
    private String name;
    /**
     * 年龄
     */
    private int age;
}
```
定义一个Student类
```java
@Data
public class Student extends Person {
    /**
     * 所在学校
     */
    private String school;
}
```

编写测试方法
```java
public class LombokTest {
    @Test
    public void noEqualsAnnotationTest(){
        Student student1 = new Student();
        student1.setName("小明");
        student1.setAge(11);
        student1.setSchool("小学");

        Student student2 = new Student();
        student2.setName("小红");
        student2.setAge(12);
        student2.setSchool("小学");

        System.out.println("student1 equals student2 : " + student1.equals(student2));
        System.out.println("student1.hasCode == student2.hasCode: " + (student1.hashCode() == student2.hashCode()));
    }
}

```

理论上对于两个student来说，他们只是学校相同，名字年龄都不一样，那么equals和hashCode的返回值都应该是不想等才对，但是运行的结果如下：
```java
student1 equals student2 : true
student1.hasCode == student2.hasCode: true
```


> 为什么会存在这种情况呢？ 

我们使用idea的插件,查看下lombok到底为我们生成了什么样的代码。

> 使用idea的lombok插件，***refactor***->***DeLombok***

使用上面的方法就能查看到具体的Student的具体实现如下

```java
public class Student extends Person {
    /**
     * 所在学校
     */
    private String school;

    public Student() {
    }

    public String getSchool() {
        return this.school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Student)) {
            return false;
        }
        final Student other = (Student) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$school = this.getSchool();
        final Object other$school = other.getSchool();
        if (this$school == null ? other$school != null : !this$school.equals(other$school)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Student;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $school = this.getSchool();
        result = result * PRIME + ($school == null ? 43 : $school.hashCode());
        return result;
    }

    public String toString() {
        return "Student(school=" + this.getSchool() + ")";
    }
}
```

可以看出生成的代码里面，Student中的equals和hashCode只和自己的school字段有关系，没有用上父类中的name和age属性。

当我们带上@EqualsAndHashCode之后，生成的代码如下

```java
public class StudentWithEquals extends Person {
    /**
     * 所在学校
     */
    private String school;

    public StudentWithEquals() {
    }

    public String getSchool() {
        return this.school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String toString() {
        return "StudentWithEquals(school=" + this.getSchool() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StudentWithEquals)) {
            return false;
        }
        final StudentWithEquals other = (StudentWithEquals) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Object this$school = this.getSchool();
        final Object other$school = other.getSchool();
        if (this$school == null ? other$school != null : !this$school.equals(other$school)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StudentWithEquals;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $school = this.getSchool();
        result = result * PRIME + ($school == null ? 43 : $school.hashCode());
        return result;
    }
}

```

在进行equals和hasCode的时候会带上super的方法
1. equals时候，会先判断super的equlas,如果父类的equals都不通过，直接return false
2. hashCode的时候，会把super.hasCode当成一个因数算在子类的hashCode中。


编写单元测试进行验证
```java
 @Test
    public void withEqualsAnnotationTest(){
        StudentWithEquals student1 = new StudentWithEquals();
        student1.setName("小明");
        student1.setAge(11);
        student1.setSchool("小学");

        StudentWithEquals student2 = new StudentWithEquals();
        student2.setName("小红");
        student2.setAge(12);
        student2.setSchool("小学");

        System.out.println("student1 equals student2 : " + student1.equals(student2));
        System.out.println("student1.hasCode == student2.hasCode: " + (student1.hashCode() == student2.hashCode()));
    }
```

执行结果如下
```java
student1 equals student2 : false
student1.hasCode == student2.hasCode: false

```