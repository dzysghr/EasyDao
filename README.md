# EasyDao
用注解和反射实现的orm

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

## 获取dao对象
```
//一个类全局只有一个dao
EasyDAO dao = EasyDAO.getInstance(TestBean.class, context);
```

## 操作接口
```
TestBean b = new TestBean();

dao.insertNew(b);
dao.save(b);
dao.delete(b);
dao.deleteAll();
dao.qureyFirst("name=?", new String[]{"dzy"});
List<TestBean> list = dao.qureyWhere("name=?", new String[]{"dzy"});
```

## TODO
多表外键联接

