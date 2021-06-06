# shareding-jdbc-demo
shareding jdbc can auto create tables

基于shareding jdbc 4.11做的例子

实现了自动根据模板表创建表

通过org.apache.shardingsphere.underlying.route.hook.RoutingHook的SPI实现SQL路由之后，创建尚不存在相应子表。

通过启动和定时任务检查数据库是否有新的子表，一定程度上保障广播执行的SQL不会漏掉新创建的子表。

```yaml
spring:  
  shardingsphere:
    sharding: 
      auto-create-table:
        enabled: true #是否开启自动建表
        tables: #以下对应逻辑表开启自动建表，值为从数据库查找子表的正则
          user: user_.*
        refresh-time-span: 2000 #多少毫秒从数据库查找子表变化，-1为不主动查找
```
