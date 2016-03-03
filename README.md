# EasyDao
用注解和反射实现的orm

## 近期更新


### 16.03.03
* 增加泛型支持
* 增加外键关联（只支持一对多，如学生-老师）


# 如何使用

## 1.定义实体类
```
@Table("bean")
public class TestBean
{
    @ID
    private long id;

    @Column(Name = "name")
    private String name;

    @Column(Name = "age")
    private int age;
}
```
> @Table 定义表名

>  @Column 定义列名

> @ID 定义对象id,这是区别对象在数据库中的唯一标准

> 三者不可缺一

## 对象关联
```
@Table(name="stu")
public class student
{
    @ID
    private long id;

    @Column(Name = "name")
    private String name;

    @Column(Name = "age")
    private int age;

    @Foreign(TableName="school")
    private School mSchool;
}

@Table("school")
public class School
{
    @ID
    private long id;

    @Column(Name = "name")
     String Name;

    @Column(Name = "location")
    String Location;
}
```

## 获取dao对象
```
//一个类全局只有一个dao
EasyDAO<TestBean> dao = EasyDAO.getInstance(TestBean.class, context);
```

## 操作接口
```
TestBean b = new TestBean();

dao.insertNew(b);
dao.save(b);
dao.delete(b);
dao.deleteAll();
dao.qureybyId();
dao.qureyFirst("name=?", new String[]{"dzy"});
List<TestBean> list = dao.qureyWhere("name=?", new String[]{"dzy"});
```
## TODO
优化代码
