# AI CODING ASSISTANT

本文档用于约束 AI 在本工程内的代码生成与改造行为，确保输出代码的结构、命名、日志、异常与分层依赖一致，便于长期维护与统一治理。

## 0. 通用原则

- 优先修改已有代码与复用已有能力，禁止为了“更优雅”而大范围重构
- 必须遵循分层架构，禁止在 `controller` 直接写数据库逻辑
- 对外接口只返回 DTO，禁止对外暴露 Entity（DO）
- 涉及关键业务流程必须打印日志，日志内容必须使用中文
- 日志分隔符必须使用英文逗号和英文冒号，禁止使用中文逗号和中文冒号
- 禁止使用行尾注释，注释必须使用中文，且能准确体现业务意义

## 1. 数据库设计

### 1.1 字符集规范
- 字符集utf8mb4，utf8mb4_unicode_ci

### 1.2 数据库通用字段规范（适用于所有业务表）
| 字段 | 说明 | 来源 |
|----|----|----|
| created_time | 创建时间 | 自动填充 |
| updated_time | 更新时间 | 自动更新 |
| deleted | 逻辑删除标识 | MyBatis-Plus `@TableLogic` |

### 1.3 标准基础字段 DDL（字段统一版，可直接嵌入任意表）

```sql
`created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1删除'
```

## 2.工程结构规范
```text
com.qingchi.ailife
├── ai
│   ├── client
│   │   └── DeepSeekClient
│   ├── prompt
│   └── parser
│
├── controller
│   └── GameController
│
├── service
│   ├── GameService
│   └── StoryService
│
├── entity
│   └── GameRecord
│
├── dto
│   ├── StartGameRequest
│   └── NextChoiceRequest
│
├── vo
│   └── GameResponse
│
├── common
│   ├── Result
│   └── ErrorCode
│
└── config
    └── DeepSeekConfig

```

## 3.类命名规范
| 类型 | 命名规则 | 示例 | 说明 |
|----|----|----|----|
| Controller | `[业务名]Controller` | PostController | 必须以 Controller 结尾 |
| Service 接口 | `I[业务名]Service` | IPostService | 必须以 I 开头，Service 结尾 |
| Service 实现 | `[业务名]ServiceImpl` | PostServiceImpl | 必须以 ServiceImpl 结尾 |
| Mapper 接口 | `I[业务名]Mapper` | IPostMapper | 必须以 I 开头，Mapper 结尾 |
| Entity | `[业务名]` | Post / Circle | 简洁的业务实体名 |
| Request DTO | `[业务描述]Req` | PublishPostReq | 必须以 Req 结尾 |
| Response DTO | `[业务描述]Resp` | PostResp | 必须以 Resp 结尾 |

## 5.注解规范
- 依赖注入统一使用 `@Resource`，禁止使用 `@Autowired` 和构造器注入
-  @RestController 用于 Controller 类
-  @Service 用于 Service 实现类
-  @Mapper 用于 MyBatis Mapper 接口

## 6. 注释规范

### 6.1 基本要求

- 注释必须使用中文，且与业务含义一致，禁止使用无意义注释
- 禁止使用行尾注释，统一使用独立注释行或块注释
- 方法、类、成员变量在“语义不自明”时必须补充注释
- `@author` 必须根据实际作者填写，禁止固定写同一个人

### 6.2 统一注释模板

类注释模板：

```java
/**
 * 类描述
 *
 * @author 实际作者
 * @date 2025/8/25 13:46
 */
public class Circle {
}
```

方法注释模板：

```java
/**
 * 方法名
 *
 * @param {参数类型} 参数名 - 参数描述
 * @returns {返回值类型} 返回值描述
 * @author 实际作者
 * @date 2025/8/25 14:17
 */
public void methodName() {
}
```

成员变量注释模板：

```java
/**
 * 变量名
 **/
private String variableName;
```

## 7. 日志规范

### 7.1 基本要求

- 关键业务逻辑必须输出日志，日志内容必须使用中文
- 日志分隔符必须使用英文逗号和英文冒号，禁止使用中文逗号和中文冒号
- 日志打印禁止使用中文逗号分隔，必须使用英文逗号分隔

### 7.2 推荐格式

- 业务开始：`log.info("创建XXX, key: {}, key2: {}", v1, v2);`
- 业务成功：`log.info("XXX创建成功, id: {}, key: {}", id, v);`
- 参数校验或可预期异常：优先使用 `warn`
- 不可预期异常：使用 `error`，并携带异常对象

## 8. 分层与依赖规范


### 8.2 分层职责

- `controller`：仅做参数接收、校验、调用 service、返回 `Result`，禁止直接访问 `mapper`
- `service`：封装领域业务逻辑与事务边界，负责组装实体与校验业务规则
- `mapper`：仅访问本服务数据库，查询条件统一收敛在 Mapper 的 `selectPage` 等方法中
- `entity`：仅表示数据库表结构与字段，不承载对外返回
- `convertor`：DO ⇆ DTO 的转换集中在此层，推荐使用 MapStruct

## 9. 接口设计规范（HTTP Controller）

### 9.1 通用要求

- Controller 统一使用 `@RestController`，依赖注入使用 `@Resource`
- 返回值统一使用 `Result<T>`，成功场景使用 `success(...)`
- 参数校验：请求体使用 `@Valid @RequestBody`，查询参数使用 `@Valid`，路径参数使用 `@PathVariable("xxxx")`
- 分页查询：`GET /page`，请求对象继承 `PageParam`，返回 `Result<PageResult<Resp>>`
- 详情查询：`GET /{id}`
- 创建：`POST /create` 或 `POST /batchCreate`（批量修改）
- 修改：`POST /update` 或 `POST /batchUpdate`（批量修改）
- 删除：`POST /delete/{id}` 或 `POST /batchDelete`（批量删除）
-
### 9.2 接口路径规范

- `@RequestMapping` 必须体现领域模块与资源名
- 接口路径禁止使用驼峰命名, 必须使用下划线分隔, 全部小写
- `@PathVariable` 和 `@RequestParam` 的参数名必须使用下划线分隔, 全部小写

**示例:**

```java
@RestController
@RequestMapping("/api/merchant_app")
public class MerchantAppController {
    
    @PostMapping("/delete/{merchant_app_id}")
    public Result<Boolean> delete(@PathVariable("merchant_app_id") Long merchantAppId) {
        // ...业务逻辑
    }
    
    @GetMapping("/query")
    public Result<ConfigResp> query(@RequestParam("config_code") String configCode) {
        // ...业务逻辑
    }
}
```

**路径和参数命名对比:**

| ❌ 错误示例（驼峰） | ✅ 正确示例（下划线） |
|----|----|
| `/api/merchantApp` | `/api/merchant_app` |
| `/delete/{merchantAppId}` | `/delete/{merchant_app_id}` |
| `@PathVariable("merchantAppId")` | `@PathVariable("merchant_app_id")` |
| `@RequestParam("configCode")` | `@RequestParam("config_code")` |
```

## 12. Convertor 规范（MapStruct）

- Convertor 统一放在 `*-biz` 模块 `modules/**/convertor` 下
- 统一使用接口 + `@Mapper`，并提供 `INSTANCE = Mappers.getMapper(...)`
- 必须提供 DO ⇆ DTO 的转换方法
- 分页转换建议提供 `convertPage(PageResult<DO>)`，只做 list 与 total 的映射

## 13. Service 规范

- Service 接口放在 `modules/**/service`，实现放在 `modules/**/service/impl`
- **实体类相关的 Service 接口必须继承 `IService<Entity>`**，基类提供了大量通用方法（如 `save`、`getById`、`updateById`、`list`、`page` 等），可直接调用以简化代码
- 实现类通常继承 `ServiceImpl<IxxxMapper, Entity>`
- 写操作必须明确事务边界，统一使用 `@Transactional(rollbackFor = Exception.class)`
- 对外可观测性要求：写操作建议打印"开始"和"成功"两条 info 日志

### 13.1 Service 方法命名规范

Service 接口方法命名应遵循以下规范，保持简洁统一：

| 操作类型 | 方法命名 | 示例 | 说明 |
|----|----|----|----|
| 创建 | `create(Req req)` | `create(DictTypeCreateReq req)` | 统一使用 create，无需加业务前缀 |
| 更新 | `update(Req req)` | `update(DictTypeUpdateReq req)` | 统一使用 update，无需加业务前缀 |
| 启用 | `enable(Long id)` | `enable(Long id)` | 统一使用 enable，无需加业务前缀 |
| 禁用 | `disable(Long id)` | `disable(Long id)` | 统一使用 disable，无需加业务前缀 |
| 根据ID查询 | `findXxxById(Long id)` | `findDictTypeById(Long id)` | 查询方法统一使用 find 开头 |
| 根据条件查询 | `findXxxByXxx(...)` | `findDictTypeByType(String type)` | 查询方法统一使用 find 开头 |
| 查询列表 | `list()` | `list()` | 查询全部列表，无需加业务前缀 |
| 根据条件查询列表 | `listXxxByXxx(...)` | `listDictDataByType(String dictType)` | 带条件的列表查询 |
| 分页查询 | `page(PageReq req)` | `page(DictTypePageReq req)` | 统一使用 page，无需加业务前缀 |

**命名原则：**
- 基础 CRUD 操作（create、update、enable、disable、list、page）使用简洁的动词，无需加业务前缀
- 查询操作统一使用 `find` 开头，以区分查询和列表操作
- 方法名应简洁明了，避免冗余的业务前缀（如 `createDictType` 应简化为 `create`）

## 14. Mapper 规范（MyBatis）

- Mapper 命名以 `I` 开头，放在 `modules/**/mapper` 下，并使用 `@Mapper`
- 统一继承 `BaseMapperX<Entity>`
- 分页查询统一提供 `default selectPage(PageReq req)` 方法
- 查询条件构建优先使用 `LambdaQueryWrapperX` 的 `eqIfPresent`、`likeIfPresent` 等方法
- 默认排序规则需明确，例如按 `createdTime` 倒序

## 15. 错误码与异常规范

- 错误码命名使用大写下划线，例如：`OPERATE_LOG_NOT_EXISTS`
- 业务校验失败或资源不存在：统一抛出 `ServiceExceptionUtil.exception(ErrorCode)`，禁止直接 `new RuntimeException`
- 枚举类统一以 `Enum` 结尾，状态值字段建议使用 `status/code`，显示值使用 `name`

## 16. 配置与环境规范

- 业务实现模块的基础配置放在 `/src/main/resources/application.yml`

## 17. 交付检查清单
- 新增写操作：必须评估并补齐事务边界，默认使用 `@Transactional(rollbackFor = Exception.class)`
- 新增错误分支：必须定义错误码并通过 `ServiceExceptionUtil.exception(ErrorCode)` 抛出
- 关键业务变更：必须补齐必要日志，且日志内容为中文、分隔符为英文逗号与英文冒号
- 提交前自检：保证工程可编译、接口契约一致、无明显风格与分层违规
