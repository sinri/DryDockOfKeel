# DryDock

![Maven Central](https://img.shields.io/maven-central/v/io.github.sinri/DryDock)
![GitHub](https://img.shields.io/github/license/sinri/DryDockOfKeel)

以原型快速构建一个JAVA项目！

```xml

<dependency>
    <groupId>io.github.sinri</groupId>
    <artifactId>DryDock</artifactId>
    <version>1.0.4</version>
</dependency>
```

## Naval

以 Keel 框架为基础，利用海军近战单位系列原型快速构建一个JAVA项目！

#### Galley

桨帆船：能航行。

提供最基本的Keel-Based项目框架：框架配置定义、日志支持（默认使用标准输出）、初始化错误处理、初始化后续业务处理。

#### Caravel

轻快帆船：能借助风力航行。

在桨帆船的基础上，使用阿里云日志服务作为默认日志支持，提供了基本的健康检查模块。

#### Ironclad

铁甲舰：利用燃煤动力航行。

在轻快帆船的基础上，提供KeelHttpServer插口支持HTTP服务。

#### Destroyer

驱逐舰：利用燃油动力航行。

在铁甲舰的基础上，提供Sundial和Funnel支持。