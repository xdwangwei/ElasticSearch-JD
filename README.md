### ElasticSearch入门，仿京东搜索实战

#### 项目介绍

此项目是跟随[狂神ES课程](https://www.bilibili.com/video/BV17a4y1x7zq?t=85&p=12)入门所做的SpringBoot+ES+Vue实战项目，在视频的基础上，已实现**前后端分离**。功能比较简单，实现的基本的**爬取+储存+搜索+高亮**，未实现分页，若对ES已有一定的基础，可自己改进，也可发起PR，若发现问题，望及时提醒。

此项目涉及以下功能

- ES创建索引
- ES删除索引
- ES批量插入文档
- ES查询并高亮显示
- 最基础的JAVA爬虫（Jsoup）
- Vue基本操作

#### 运行环境

- [ElasticSearch 7.8.0](https://www.elastic.co/guide/en/elasticsearch/reference/current/elasticsearch-intro.html) 
- [kibana 7.8.0](https://www.elastic.co/cn/downloads/kibana)
- [IK分词器 7.8.0](https://github.com/medcl/elasticsearch-analysis-ik)（可选）
- IDEA 2020.1.3
- Maven 3.6.9
- SpringBoot 2.3.1

#### 前期准备

- 安装ElasticSearch、Kibana、ik分词器，注意这三个**版本必须保持一致**！
- 启动ElasticSearch，端口 9200，9300

#### 最终效果

同时支持中文搜索、英文搜索

![1](E:\workspace\git\project\ES-JD\images\1.png)

![1](E:\workspace\git\project\ES-JD\images\2.png)

#### 注意事项

- 由于前后端分离，`Controller`上记得加注解```@CrossOrigin(allowedHeaders = "*")```允许跨域。
- 安装`IK`分词器是为了解决中文查询时，默认分词器会将关键词其分割，导致查询不出结果。

- 此页面是我在京东搜索原网页的基础上删减得到的，所以可能有些多余的标签，可以自己试着改，由于我对前端不太熟悉，所以并未有太多的删减。

- 若`Jsoup`解析网页出错或未获取到数据，最好打开浏览器开发者模式，通过检查元素查看标签结构，在控制台用`JS`操作先试试能否获取到结果，若发现与代码中涉及的标签和属性等不一致，请自己修改。

- 京东页面所用的`css`、`js`包括一些图片都是异步加载的，其中涉及到的`css`和`js`我自己下载好了，京东logo和购物车图标的显示也是`http`请求获取到的，不是本地图标，这里我没改是因为我用的VSCode，装了插件LiveServer，所以能够正常显示，所以你能看到我的浏览器地址是 localhost:52330/itemlist.htm，而不是本地协议。

- 使用file协议也可正常运行，也就是直接选择用浏览器打开itemlist.htm，功能不受影响，只不过可能就看不到logo，大概像这样。

  ![1](E:\workspace\git\project\ES-JD\images\3.png)