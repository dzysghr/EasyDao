# EasyDao
 使数据库操作简单化

## 近期更新

#### 16.04.10
* bug fix
* 可自定义数据库名
* 表名和列名默认取类名和成员名
* 自动检测成员变量的变化，调整表结构

#### 16.03.08
* 去除实体类需无参数构造函数的限制
* 性能优化，大幅度提升插入速度

#### 16.03.03
* 增加泛型支持
* 增加外键关联（只支持一对多，如学院-学生）,实验性功能


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
> @Table 定义表名，默认为类名

>  @Column 定义列名，默认为成员名

> @ID 定义对象id,必须为long,这是区别对象在数据库中的唯一标准

> *三者不可缺一*

## 对象关联
```
@Table("stu")
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

## 自定义app并设置Context
```
public class app extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        EasyDAO.init(this,DBName,version);
        
    }
}
```

## 获取dao对象
```
//一个类全局只有一个dao
EasyDAO<TestBean> dao = EasyDAO.getInstance(TestBean.class);
```
> 数据库打开后不会自动关闭，如果确定不再访问数据库，需手动调用EasyDao.CloseDB()来关闭连接

## 操作接口
```
TestBean b = new TestBean();

dao.insertNew(b);
dao.save(b);
dao.delete(b);
dao.deleteAll();
dao.qureybyId(long id);
dao.qureyFirst("name=?", new String[]{"dzy"});
List<TestBean> list = dao.qureyWhere("name=?", new String[]{"dzy"});
```
## TODO
优化代码
