# DeepSeek 接入说明

> 原则：**AI 只生成剧情文本，事件抽取、状态更新、选项均由后端控制**

## 一、架构映射（设计文档 → 现有实现）

| 设计文档模块 | 本项目实现 | 说明 |
|-------------|-----------|------|
| `life_state` 表 | `game_session.life_status` (JSON) | 六维状态 + 内部叙事字段 |
| `event_pool` 表 | `story_event` 表 | 103 条事件, effect/choices JSON |
| `user_event_history` | `game_step` + `ai_prompt_log` | 步骤历史 + AI prompt/响应 |
| `LifeInitializer` | `FamilyBackgroundInitializer` + `StoryGenerator.generateOpening` | 开局状态 |
| `GameEngine` | `DefaultGameEngine` | 事件筛选、状态计算、强制里程碑 |
| `EventPool` | `story_event` + `StoryEventFilter` | 年龄/状态过滤 + 随机抽取 |
| `AIService` | `INarrativeService` / `NarrativeServiceImpl` | 调用 DeepSeek 扩写剧情 |
| 前端 Vue | `ai-life-web` | 展示 story + choices + state |

## 二、完整流程

```
用户选择
    ↓
GameServiceImpl.choice()
    ↓
DefaultGameEngine.nextStep()     ← 后端：选事件、算 state、定 options
    ↓
NarrativeService.generate()      ← DeepSeek：仅扩写 story 文本
    ↓
写入 game_step / ai_prompt_log
    ↓
返回 GameResp { story, state, choices }
    ↓
前端渲染
```

**开局** `start()` 同样在引擎产出模板后调用 `NarrativeService`。

## 三、配置

`application.yml` 或环境变量:

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| `deepseek.enabled` | `DEEPSEEK_ENABLED` | `false` | 是否启用 AI |
| `deepseek.api-key` | `DEEPSEEK_API_KEY` | 空 | DeepSeek API Key |
| `deepseek.model` | — | `deepseek-chat` | 模型 |
| `deepseek.max-tokens` | — | `400` | 单次上限 |
| `deepseek.history-limit` | — | `5` | 带入 prompt 的最近步数 |

### 本地启用示例

```bash
export DEEPSEEK_ENABLED=true
export DEEPSEEK_API_KEY=sk-xxxxxxxx
```

未启用或调用失败时, 自动回退 `story_event.event_content` 或模板文案, **不影响游戏逻辑**。

## 四、代码结构

```
com.qingchi.ailife
├── ai
│   ├── client/DeepSeekClient.java       # HTTP 调用 /chat/completions
│   ├── prompt/NarrativePromptBuilder.java
│   ├── parser/DeepSeekResponseParser.java
│   └── dto/NarrativeContext.java
├── config
│   ├── DeepSeekProperties.java
│   └── DeepSeekConfig.java
└── service
    ├── INarrativeService.java
    └── impl/NarrativeServiceImpl.java
```

## 五、Prompt 设计

- **System**: 约束 AI 只输出 150–250 字剧情, 不改数值
- **User**: 玩家状态(六维) + 用户选择 + 事件标题 + 事件模板 + 最近 5 步摘要

事件模板来自 `story_event.event_content`, AI 在其基础上扩写。

## 六、日志与存储

| 表 | 内容 |
|----|------|
| `game_step.story` | 最终展示给用户的剧情(AI 或模板) |
| `game_step.user_choice` | 用户选择 |
| `game_step.state_after` | 本步结束后状态 |
| `ai_prompt_log` | 完整 prompt、AI 响应、token_usage |

## 七、分工总结

| 模块 | 负责方 | 调用 AI |
|------|--------|---------|
| 初始化人生 | 后端引擎 | 否(状态) / 是(开局文案, 可选) |
| 用户选择处理 | 后端 | 否 |
| 事件抽取 & 状态更新 | 后端 | 否 |
| 剧情文本 | DeepSeek | 是 |
| 结局摘要 | DeepSeek | 是（`prompt_type=end`） |
| 选项列表 | 后端 `story_event.choices` | 否 |
| 存储 | 后端 | 否 |
| 前端展示 | Vue | 否 |

## 八、API 接口

前端接口**无变化**, 仍使用 `/api/game/start`、`/api/game/choice` 等, 见 `docs/FRONTEND_API.md`。

启用 DeepSeek 后, 响应中 `story` 字段为 AI 生成内容, 结构与字段不变。
