# mysql-connector-jsl

## 版权和许可证

本项目基于 [MySQL Connector/J](https://github.com/mysql/mysql-connector-j) 开源项目进行二次开发，  
继承并遵守其 [GPL 2.0 + FOSS Exception](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html) 许可证。  

您可以在遵守该许可证条款的前提下，免费使用、修改和分发本项目的代码。  
如果您将本项目用于商业用途或发布二次作品，请务必详细阅读并遵守原项目的开源协议。

---

## 目录

- [项目简介](#项目简介)
- [主要特性](#主要特性)
- [快速开始](#快速开始)
- [使用说明](#使用说明)
- [兼容性](#兼容性)
- [贡献者](#贡献者)
- [许可证](#许可证)
- [联系方式](#联系方式)

---

## 项目简介

mysql-connector-jsl 是基于 MySQL 官方 Connector/J 驱动开发的增强版本，旨在满足以下需求：

- 支持更灵活的连接管理
- 增强日志和监控功能
- 优化批量操作性能
- 修复官方版本中的部分已知问题

本项目保留官方驱动所有核心功能，保证兼容性和稳定性。

---

## 主要特性

- 兼容官方 Connector/J 的所有功能和配置
- 新增高级连接池和自动重连机制
- 支持自定义日志级别和日志格式
- 优化批量插入和更新性能
- 增强错误诊断信息，便于排查问题
- 修复官方驱动中的若干 BUG（详细见[更新日志](CHANGELOG.md)）

---

## 快速开始

### Maven 引入

```xml
<dependency>
  <groupId>com.yourorg</groupId>
  <artifactId>mysql-connector-jsl</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 联系方式

如果你有任何问题、建议或反馈，欢迎通过 [GitHub Issues](https://github.com/你的仓库地址/issues) 联系我们。  
或者发送邮件至：myjie215@163.com