
#### 项目描述

* 这是一个使用Java代码，通过多线程，爬取新闻首页中的新闻内容，
 然后将数据在数据库中持久化的爬虫项目。
 
 #### 设计流程
 * 使用Docker启动数据库并使用Flyway对数据库进行初始化，接着从数据库中拿出网址，使用HttpClient发起请求，拿到HTML页面内容。接着使用Jsoup解析HTML，拿到主页面中的其他链接并发起请求，拿到新闻的标题、内容等，存储在数据库中。初次试验成功后，将H2数据库替换为MySQL，将Dao层的实现由接口方式替换为MyBatis。当数据量达到上百万级的时候，对查找非文本内容，采用建立索引的方式，将查找时间下降了近百倍。对文本查找，通过ElasticSearch搜索引擎，控制文本搜索的时间在毫秒级。
 
 #### 如何使用
 将代码拷贝下来后，启动步骤如下：
 * 1.启动本地数据库，这里用的是MySQL，其他数据库也可
 ```
docker run --name mysql -p 3306:3306 -v `pwd`/database:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=password -d mysql:5.7
```
 * 2.在确保安装了Maven的情况下，使用`mvn flyway:migrate`命令运行数据库初始化命令。建议使用之前先`mvn flyway:clean`清除缓存。
 * 3.本项目在Java8环境下开发的，请确保运行版本高于Java8，否则可能出现部分API不兼容问题。
 #### 运行环境
 * JDK环境在8以上，并且保证本机中下载了Docker引擎，以及Maven管理工具。如果是采用虚拟机，请确保本机的3306端口与宿主机之间的3306端口是打通的。
 #### 联系方式
 邮箱：mryuyan@gmail.com
 
