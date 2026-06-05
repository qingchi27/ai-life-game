# 人生游戏 · 前端对接文档

> 版本：2026-06-04  
> 后端项目：`ai-life-game`  
> 基础路径：`/api/game`  
> 统一响应：`Result<T>` → `{ code, message, data }`，成功时 `code === 0`

---

## 一、本次改动摘要（Breaking Changes）

### 1. 人生状态 `state` 字段重构（必改）

| 旧字段（删除） | 新字段 | 说明 |
|----------------|--------|------|
| `money` | `wealth` | 财富，数值型，可很大（如 3000、50000） |
| `relationship` | `affection` | 感情（亲情+友情+爱情），0–100 |
| `luck` | `fame` | 名气，0–100 |
| `career` | **已移除** | 职业轨迹仅服务端叙事用，不再返回前端 |
| `health` | `health` | 身体健康，0–100（含义不变，仍 0–100） |
| — | `power` | 权力，0–100，**新增** |
| — | `lifespan` | 预期寿命（岁），**新增**，默认约 78 |
| — | `children` | 子女对象，**新增**，见下表 |

**子女对象 `children`：**

| 字段 | 类型 | 范围/说明 |
|------|------|-----------|
| `count` | number | 子女数量，0 起 |
| `ability` | number | 能力，0–100 |
| `achievement` | number | 成就，0–100 |

### 2. 开局叙事变化（文案）

- 旧：18 岁已是「程序员」
- 新：18 岁高考后进入大学计算机专业（`occupation` 在服务端为「大学生」，前端不展示）
- **新增**：开局随机抽取 7 档家庭条件，属性按档位随机初始化，见 `state.familyBackground`

| 家庭条件 | 财富 | 健康 | 名气 | 权力 | 感情 |
|----------|------|------|------|------|------|
| 普通家庭 | 0–1万 | 0–50 | 0–30 | 0–10 | 0–80 |
| 一般家庭 | 1万–10万 | 50–80 | 0–50 | 0–30 | 20–70 |
| 小康家庭 | 10万–30万 | 50–100 | 0–50 | 0–30 | 30–60 |
| 小富家庭 | 30万–80万 | 50–100 | 30–50 | 20–40 | 40–80 |
| 中富家庭 | 80万–100万 | 50–80 | 30–80 | 20–60 | 30–70 |
| 大富家庭 | 100万–300万 | 60–70 | 50–90 | 50–80 | 20–60 |
| 巨富家庭 | 500万–10亿 | 70–90 | 60–100 | 80–100 | 0–50 |

### 3. 游戏结束条件（前端提示）

除 `isEnd === true` 外，后端会在以下情况自动结束：

| 条件 | 说明 |
|------|------|
| 步数达到上限 | `step >= 30`（配置 `game.max-step`） |
| 健康耗尽 | `state.health <= 0` |
| 寿终 | **当前年龄 ≥ `state.lifespan`**（年龄由服务端推算，见下文） |
| 主动结束 | 调用 `POST /api/game/end` |

### 4. 结局文案 `summary` 格式变化

旧示例：

```text
张三的一生落下帷幕: 积蓄约50000元, 健康60, 最终职业是程序员, 已婚, 有子女。
```

新示例：

```text
张三的一生落下帷幕: 感情72, 财富85000, 子女1人(能力45/成就30), 权力55, 名气68, 健康42, 享年76岁。
```

### 5. 结局标签 `tags` 可能取值

| 标签 | 含义（大致） |
|------|----------------|
| `富足` / `稳定` | 财富高低 |
| `健康` | 身体较好 |
| `知名` | 名气较高 |
| `有后` | 有子女 |
| `冒险` / `踏实` | 是否创业向 |

### 6. 结局标题 `endingTitle` 可能取值

- `财富显赫的一生`
- `权势滔天的一生`
- `名满天下的一生`
- `透支健康的一生`
- `平凡但稳定的一生`

### 7. 接口路径与请求体

**无变化**，仍为：

- `POST /api/game/start`
- `POST /api/game/choice`
- `GET /api/game/session/{id}`
- `POST /api/game/end`
- `GET /api/game/history/{id}`

---

## 二、TypeScript 类型定义（可直接复制）

```typescript
/** 统一响应 */
export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

/** 子女状态 */
export interface ChildrenState {
  count: number;
  ability: number;
  achievement: number;
}

/** 人生六维状态（API 返回） */
export interface LifeState {
  familyBackground: string; // 开局随机家庭条件，如「小康家庭」
  affection: number;   // 感情 0-100
  wealth: number;      // 财富
  children: ChildrenState;
  power: number;       // 权力 0-100
  fame: number;        // 名气 0-100
  health: number;      // 身体 0-100
  lifespan: number;    // 预期寿命（岁）
}

export interface Choice {
  id: string;      // "A" | "B" | "C"
  content: string;
}

export interface GameEvent {
  type: string;    // career | relationship | life | family | ...
  title: string;
}

export interface GameResp {
  sessionId: number;
  step: number;           // 当前步数 1~30
  story: string;          // 当前剧情正文
  state: LifeState;
  choices: Choice[];
  event?: GameEvent;      // 部分步骤有
  isEnd?: boolean;        // true 表示本局已结束
}

export interface EndGameResp {
  endingTitle: string;
  summary: string;
  score: number;          // 0-100
  tags: string[];
}

export interface HistoryStep {
  step: number;
  story: string;
  choice: string | null;  // 用户该步选择文案，开局为 null
}

export interface HistoryResp {
  steps: HistoryStep[];
}

export interface StartGameReq {
  playerName: string;
}

export interface ChoiceReq {
  sessionId: number;
  choiceId: string;
}

export interface EndGameReq {
  sessionId: number;
}
```

---

## 三、接口明细

### 1. 开始游戏

```http
POST /api/game/start
Content-Type: application/json
```

**请求：**

```json
{
  "playerName": "张三"
}
```

**响应 `data` 示例：**

```json
{
  "sessionId": 1001,
  "step": 1,
  "story": "张三, 你出生在一个普通家庭。18岁那年高考结束, 你考入一所普通大学的计算机专业, 开启了大学生活...",
  "state": {
    "familyBackground": "小康家庭",
    "affection": 45,
    "wealth": 180000,
    "children": { "count": 0, "ability": 0, "achievement": 0 },
    "power": 15,
    "fame": 25,
    "health": 80,
    "lifespan": 78
  },
  "choices": [
    { "id": "A", "content": "努力学习" },
    { "id": "B", "content": "开始副业" },
    { "id": "C", "content": "躺平" }
  ],
  "isEnd": false
}
```

---

### 2. 做选择（推进剧情）

```http
POST /api/game/choice
Content-Type: application/json
```

**请求：**

```json
{
  "sessionId": 1001,
  "choiceId": "A"
}
```

- `choiceId` 必须为**上一步返回**的 `choices[].id`（通常 `A`/`B`/`C`）
- 传错 id → `code: 40403`，`message: 无效的选择`

**响应 `data`：** 结构同 `GameResp`，`step` 递增，`story`/`state`/`choices` 更新。

**带事件示例：**

```json
{
  "sessionId": 1001,
  "step": 3,
  "story": "课堂上那个总是坐在前排的人, 今天主动和你搭话了...",
  "state": { "...": "..." },
  "choices": [
    { "id": "A", "content": "表白试试" },
    { "id": "B", "content": "先做朋友" },
    { "id": "C", "content": "专注学业" }
  ],
  "event": {
    "type": "relationship",
    "title": "校园恋情"
  },
  "isEnd": false
}
```

**本局结束示例：**

```json
{
  "sessionId": 1001,
  "step": 30,
  "story": "...",
  "state": { "health": 0, "...": "..." },
  "choices": [],
  "isEnd": true
}
```

结束后应跳转结局页，并可用 `sessionId` 调 `end` 或展示 `summary`（若已在服务端写入，见 `session` 接口说明）。

---

### 3. 查询当前会话

```http
GET /api/game/session/{sessionId}
```

**响应 `data`：** 同 `GameResp`（用于刷新/断线恢复）。

> **注意：** 当前接口**未返回** `currentAge` 字段，仅返回 `step`。  
> 前端若需展示年龄，可：
> - 本地从开局 18 岁起按步数估算（不精确），或
> - 后续让后端在 `GameResp` 增加 `age` 字段。

---

### 4. 主动结束人生

```http
POST /api/game/end
Content-Type: application/json
```

**请求：**

```json
{
  "sessionId": 1001
}
```

**响应 `data`：**

```json
{
  "endingTitle": "平凡但稳定的一生",
  "summary": "张三的一生落下帷幕: 感情65, 财富12000, 子女0人(能力0/成就0), 权力40, 名气50, 健康55, 享年78岁。",
  "score": 72,
  "tags": ["稳定", "踏实"]
}
```

---

### 5. 人生轨迹

```http
GET /api/game/history/{sessionId}
```

**响应 `data`：**

```json
{
  "steps": [
    {
      "step": 1,
      "story": "张三, 你出生在一个普通家庭...",
      "choice": null
    },
    {
      "step": 2,
      "story": "你拿到了奖学金...",
      "choice": "努力学习"
    }
  ]
}
```

> 历史接口**不返回**每步的 `state` 快照，仅 `story` + `choice`。状态条请以最新 `session` 或 `choice` 响应为准。

---

## 四、错误码

| code | message | 前端处理建议 |
|------|---------|----------------|
| `0` | success | 正常 |
| `400` | 参数错误 | 表单校验提示 |
| `40401` | 游戏会话不存在 | 回到首页重新开局 |
| `40402` | 游戏已结束 | 禁止再 `choice`，展示结局 |
| `40403` | 无效的选择 | 刷新 choices 重选 |
| `40404` | 游戏状态异常 | 通用错误提示 |

判断成功：`response.code === 0`（不要只判断 HTTP 200）。

---

## 五、前端 UI 改造清单

### 1. 状态面板（原 5 项 → 现 6 项）

建议展示顺序与文案：

| 维度 | 绑定字段 | 展示建议 |
|------|----------|----------|
| 感情 | `state.affection` | 进度条 0–100，副标题可写「亲情·友情·爱情」 |
| 财富 | `state.wealth` | 数字 + 单位（元），大数可用千分位 |
| 子女 | `state.children` | `数量 {count} · 能力 {ability} · 成就 {achievement}` |
| 权力 | `state.power` | 进度条 0–100 |
| 名气 | `state.fame` | 进度条 0–100 |
| 健康 | `state.health` + `state.lifespan` | 身体进度条 + 文案「预期寿命 {lifespan} 岁」 |

### 2. 删除的旧 UI

- 删除「运气 / luck」
- 删除「职业 / career」展示（后端不再返回）
- 将所有 `money` 改为 `wealth`
- 将所有 `relationship` 改为 `affection`

### 3. 兼容旧存档（可选）

若本地缓存了旧 `state`，可做一次性迁移：

```typescript
function migrateLifeState(raw: Record<string, unknown>): LifeState {
  return {
    affection: (raw.affection ?? raw.relationship ?? 40) as number,
    wealth: (raw.wealth ?? raw.money ?? 0) as number,
    children: raw.children as ChildrenState ?? {
      count: raw.hasChild ? 1 : 0,
      ability: 0,
      achievement: 0,
    },
    power: (raw.power ?? 20) as number,
    fame: (raw.fame ?? raw.luck ?? 30) as number,
    health: (raw.health ?? 80) as number,
    lifespan: (raw.lifespan ?? 78) as number,
  };
}
```

服务端 JSON 存档也会自动迁移，**以接口返回为准**即可。

### 4. 游戏流程（不变）

```
start → 展示 story + choices + state
  ↓ 用户点击选项
choice(choiceId) → 更新界面
  ↓ isEnd === true
展示结局（可调 end 获取 endingTitle / summary / score / tags）
```

### 5. 结束页

- 展示 `endingTitle`、`summary`、`score`
- `tags` 渲染为标签 chips
- `summary` 已包含六维总结，可不再重复解析 `state`

---

## 六、数值范围参考（做进度条用）

| 字段 | 典型范围 | 进度条 |
|------|----------|--------|
| affection | 0–100 | 是 |
| power | 0–100 | 是 |
| fame | 0–100 | 是 |
| health | 0–100 | 是 |
| children.ability | 0–100 | 是 |
| children.achievement | 0–100 | 是 |
| wealth | 0 ~ 100000+ | 建议单独刻度或分段颜色 |
| children.count | 0–5+ | 数字展示 |
| lifespan | 约 40–120 | 数字展示，可与当前年龄对比 |

---

## 七、请求示例（axios）

```typescript
const API = '/api/game';

export async function startGame(playerName: string) {
  const { data } = await axios.post<ApiResult<GameResp>>(`${API}/start`, { playerName });
  if (data.code !== 0) throw new Error(data.message);
  return data.data;
}

export async function makeChoice(sessionId: number, choiceId: string) {
  const { data } = await axios.post<ApiResult<GameResp>>(`${API}/choice`, {
    sessionId,
    choiceId,
  });
  if (data.code !== 0) throw new Error(data.message);
  return data.data;
}

export async function getSession(sessionId: number) {
  const { data } = await axios.get<ApiResult<GameResp>>(`${API}/session/${sessionId}`);
  if (data.code !== 0) throw new Error(data.message);
  return data.data;
}

export async function endGame(sessionId: number) {
  const { data } = await axios.post<ApiResult<EndGameResp>>(`${API}/end`, { sessionId });
  if (data.code !== 0) throw new Error(data.message);
  return data.data;
}

export async function getHistory(sessionId: number) {
  const { data } = await axios.get<ApiResult<HistoryResp>>(`${API}/history/${sessionId}`);
  if (data.code !== 0) throw new Error(data.message);
  return data.data;
}
```

---

## 八、后续可让后端补充的字段（可选需求）

若前端需要更精确展示，可向后端提需求增加：

| 字段 | 说明 |
|------|------|
| `GameResp.age` | 当前年龄（服务端 `currentAge`） |
| `GameResp.playerName` | 玩家名 |
| `HistoryStepVO.stateAfter` | 每步结束后的状态快照 |

当前文档以**现有接口**为准。

---

## 九、联调检查表

- [ ] 状态栏 6 维字段绑定正确，无 `money`/`luck`/`relationship`/`career`
- [ ] `children` 空对象防御（`count/ability/achievement` 默认 0）
- [ ] `choiceId` 使用接口返回的 `id`，不是数组下标
- [ ] `isEnd === true` 时禁用选项并拉取/展示结局
- [ ] `code !== 0` 时展示 `message`
- [ ] 结局页适配新 `summary` 文案格式
- [ ] 健康条低时考虑红色预警（`health <= 30`）
- [ ] 预期寿命可展示为「还剩约 {lifespan - 当前年龄} 年」（需本地估算年龄或等后端加 `age`）

---

## 十、相关后端文件索引

| 文件 | 说明 |
|------|------|
| `vo/LifeStateVO.java` | API 状态结构 |
| `vo/ChildrenStateVO.java` | 子女结构 |
| `vo/GameResp.java` | 游戏主响应 |
| `vo/EndGameResp.java` | 结局响应 |
| `controller/GameController.java` | 路由定义 |
| `docs/FRONTEND_API.md` | 本文档 |
