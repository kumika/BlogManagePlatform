![Version 0.1-alpha](https://img.shields.io/github/tag-pre/Frodez/BlogManagePlatform.svg)
[![License](https://img.shields.io/badge/license-apache-blue.svg)](https://github.com/Frodez/BlogManagePlatform/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/Frodez/BlogManagePlatform.svg)](https://travis-ci.org/Frodez/BlogManagePlatform)
[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
# BlogManagePlatform  
## 这是一个springboot项目的脚手架,追求简洁高速可扩展。  
已完成了一些常用功能，包括:
1. https和http2支持。
2. 较简单的请求限流。
3. 较简单的日志配置。
4. 多功能的参数验证。
5. 较完备的spring security配置,使用jwt,无session。
6. 使用mybatisORM工具，采用通用mapper和pageHelper作为辅助。
7. 高性能的常用工具类，如对象复制，反射，json处理，日期处理，正则表达式处理，判空。
8. 具备良好API的通用Result。
9. 较全面的mybatis代码自动生成工具。
10. swagger支持。
11. 异常兜底处理。  

**对可能有不同应用场景的功能，均保证了尽可能良好的扩展性，方便接入不同实现。**  

## 项目结构:
1. 这个项目只包括了服务端API部分,前端页面部分可自行接入各种实现。
2. src文件夹为项目文件夹。src/test文件夹为测试文件存放处。src/main/resources文件夹为配置文件存放处，其中application.yml位于主目录，others文件夹存放了sql，settings文件夹存放了自定义的配置文件。
3. src/main/generator文件夹为代码生成器的存放处。这里存放了自行实现的mybatis-generator插件和基于eclipde.jdt的代码生成插件，可以简化开发中的重复操作。
4. src/main/java/frodez文件夹为项目代码存放处。 
    1. config文件夹存放了项目的配置，以及一些功能(AOP, spring security, mapper, swagger等)的实现。 
    2. controller文件夹存放了项目的controller。 
    3. dao文件夹存放了mapper,mapper.xml,数据库orm映射实体,以及其他与数据库相关的bean。 
    4. service文件夹存放了项目的主体业务逻辑实现。 
    5. util文件夹存放了工具类和常用常量。 

**由于使用缓存的缘故，建议不要使用springboot热部署工具,否则可能带来一些奇怪的问题。** 

## 部分性能测试

测试环境:
CPU Ryzen7 2700X 8C16T 3.7-4.3GHZ
内存 2条16G DDR42400
硬盘 EVO860 500G
系统 Windows10 1809
注意事项:Windows对tcp连接数量有限制,回收速度过慢的话会影响测试结果。一般来说必须修改默认配置。建议将tcp连接数设为最大值65535-1，将回收时间设到3s以内。

测试结果:
1.带缓存，极少量数据(content500B左右)的情况下，设置并发数200，缓存命中可达12000QPS.
2.不带缓存，少量级数据(content2KB左右)的情况下，设置并发数200，可达4400QPS.
3.设置并发数200，但只分配1C2T的CPU资源，性能大约1000QPS，对以上两种情况均适用。当CPU资源改为3C6T时，方能基本满足性能上限.
4.对比测试，纯helloworld的springboot应用，QPS大约是17000.

总结：
考虑到相比helloworld已加入诸多功能，至少在同样功能的级别下，性能不会太低下。以后也会对相关功能尽可能进行优化。
