#!/usr/bin/env python3
"""生成 story_event 初始数据 SQL"""

import json
from collections import Counter
from pathlib import Path

EVENTS = [
    ("GRADUATE_COLLEGE", "大学毕业", "education", 1, 22, 25, "你拿到了毕业证书, 站在校门口感慨万千, 未来充满未知...", '["考研深造", "直接就业", "gap一年旅行"]', {"money": -2000, "health": 0, "luck": 5}),
    ("INTERNSHIP_OFFER", "实习offer", "career", 1, 20, 24, "一家互联网公司向你发来实习邀请, 薪资不高但能学到东西...", '["接受实习", "继续找更好的", "先考证书"]', {"money": 500, "health": -2, "luck": 3}),
    ("CET6_PASS", "六级通过", "education", 1, 19, 26, "英语六级成绩出来了, 你终于通过了...", '["准备考研", "投外企简历", "庆祝一下"]', {"money": -200, "luck": 5, "health": 0}),
    ("PART_TIME_JOB", "兼职打工", "career", 1, 18, 26, "生活费不够, 你在校外找了一份周末兼职...", '["继续做", "影响学习就停", "换成线上兼职"]', {"money": 1500, "health": -5, "luck": 2}),
    ("CAMPUS_LOVE", "校园恋情", "relationship", 2, 18, 25, "课堂上那个总是坐在前排的人, 今天主动和你搭话了...", '["表白试试", "先做朋友", "专注学业"]', {"money": -500, "relationship": 20, "luck": 5}),
    ("SCHOLARSHIP", "奖学金到账", "education", 2, 19, 28, "你凭借优异成绩拿到了一笔奖学金...", '["存起来", "买设备学习", "请朋友吃饭"]', {"money": 3000, "luck": 10, "health": 2}),
    ("HACKATHON_WIN", "黑客松获奖", "career", 2, 20, 30, "你和队友熬夜48小时, 项目在黑客松上拿了奖...", '["继续深耕技术", "趁机找工作", "休息一周"]', {"money": 2000, "luck": 15, "health": -6}),
    ("FIRST_JOB", "第一份工作", "career", 1, 22, 28, "HR打来电话, 你通过了终面, 即将成为职场新人...", '["开心入职", "再比比价", "negotiate薪资"]', {"money": 1000, "luck": 5, "health": 0}),
    ("PROBATION_PASS", "转正通过", "career", 1, 23, 32, "三个月试用期结束, 领导找你谈话...", '["请同事吃饭", "低调做人", "要求加薪"]', {"money": 500, "relationship": 5, "luck": 3}),
    ("OVERTIME_MONTH", "连续加班", "career", 1, 23, 45, "项目上线前, 整个月都在公司过夜...", '["坚持扛过去", "请假休息", "提离职"]', {"money": 2000, "health": -12, "luck": 0}),
    ("BOSS_PRAISE", "领导表扬", "career", 1, 24, 50, "周会上领导点名表扬了你的方案...", '["谦虚回应", "争取晋升", "请团队喝奶茶"]', {"money": -300, "relationship": 10, "luck": 8}),
    ("JOB_HOPPING", "跳槽机会", "career", 2, 24, 40, "猎头推荐了一个薪资涨幅30%的岗位...", '["果断跳槽", "再观望", "内部转岗"]', {"money": 3000, "luck": 8, "health": -3}),
    ("LAYOFF_RUMOR", "裁员风声", "career", 2, 26, 55, "公司群里流传下个月要优化20%...", '["更新简历", "找领导谈心", "躺平等赔偿"]', {"money": 0, "health": -8, "luck": -5}),
    ("ACTUALLY_LAYOFF", "被裁员", "career", 3, 26, 55, "周一早上, HR把你叫进小会议室...", '["拿赔偿休息", "立刻找工作", "创业试试"]', {"money": 5000, "health": -10, "luck": -8}),
    ("PROMOTION", "升职加薪", "career", 2, 26, 50, "年度考评优秀, 你获得了晋升和调薪...", '["庆祝一下", "继续卷", "带新人"]', {"money": 4000, "luck": 12, "relationship": 5}),
    ("START_OUTSOURCE", "进入外包公司", "career", 1, 18, 30, "你进入一家外包公司, 每天加班到深夜, 工资却涨得很慢...", '["接副业", "继续上班", "学习AI"]', {"money": 0, "health": -5, "luck": 0}),
    ("SIDE_INCOME", "第一次副业收入", "career", 2, 20, 45, "你开始接私活, 第一笔订单到账了, 虽然不多但很有成就感...", '["扩大副业", "继续稳定上班", "辞职创业"]', {"money": 2000, "health": -3, "luck": 5}),
    ("FREELANCE_FULL", "全职自由职业", "career", 2, 25, 45, "副业收入超过了主业, 你在考虑是否全职...", '["辞职全职", "保持双线", "注册公司"]', {"money": 1500, "health": -5, "luck": 10, "career": "自由职业者"}),
    ("AI_COURSE", "报名AI课程", "education", 2, 22, 40, "AI浪潮来袭, 你花了几千块报了线上训练营...", '["认真学习", "学完就投简历", "浅尝辄止"]', {"money": -3000, "luck": 12, "health": -2}),
    ("STARTUP", "创业机会", "career", 3, 25, 50, "一个老同学邀请你一起创业, 需要投入积蓄...", '["全力投入", "小额试水", "婉拒邀请"]', {"money": -3000, "luck": 10, "career": "创业者"}),
    ("STARTUP_FAIL", "创业失败", "career", 3, 26, 55, "公司账上只剩三个月现金流, 合伙人意见不合...", '["及时止损", "再融一轮", "转型做咨询"]', {"money": -8000, "health": -15, "luck": -10}),
    ("STARTUP_SUCCESS", "创业融资", "career", 3, 27, 50, "投资人看中了你们的商业模式, 愿意领投...", '["接受条款", "继续谈判", "保持独立"]', {"money": 20000, "luck": 25, "health": -5}),
    ("BIG_TECH_OFFER", "大厂offer", "career", 3, 24, 40, "你收到了一线互联网大厂的录用通知...", '["欣然接受", "用来谈薪", "留在现公司"]', {"money": 5000, "luck": 20, "career": "大厂员工"}),
    ("BURNOUT", "职业倦怠", "career", 2, 28, 50, "每天早上醒来都不想上班, 对代码失去了热情...", '["请假旅行", "换赛道", "硬撑"]', {"money": -2000, "health": -12, "luck": -5}),
    ("MANAGER_ROLE", "升任管理", "career", 2, 30, 50, "你被提拔为团队负责人, 要管人了...", '["接受挑战", "婉拒升职", "先学管理"]', {"money": 3000, "relationship": -5, "luck": 8}),
    ("STOCK_OPTION", "期权兑现", "finance", 3, 28, 50, "公司上市, 你的期权终于可以变现了...", '["全部卖出", "留一部分", "继续持股"]', {"money": 50000, "luck": 20, "health": 5}),
    ("WORKPLACE_BULLY", "职场PUA", "career", 2, 23, 45, "直属领导总是否定你的工作, 让你怀疑自己...", '["正面刚", "越级汇报", "默默忍受"]', {"money": 0, "health": -10, "luck": -8}),
    ("INDUSTRY_CHANGE", "行业巨变", "career", 2, 25, 55, "AI取代了很多岗位, 你所在的业务线被砍了...", '["转型AI", "考公上岸", "出国深造"]', {"money": -1000, "luck": -5, "health": -5}),
    ("GOVERNMENT_JOB", "考公上岸", "career", 3, 23, 35, "经过一年备考, 你终于通过了公务员考试...", '["去报到", "再考虑", "放弃体制内"]', {"money": 2000, "luck": 15, "health": 5, "career": "公务员"}),
    ("HEALTH_WARNING", "健康预警", "life", 1, 25, 60, "长期熬夜让你的身体发出警告, 体检报告有几项指标偏高...", '["拼命加班", "开始副业", "躺平休息"]', {"money": -500, "health": -10, "luck": 0}),
    ("GYM_START", "开始健身", "life", 1, 22, 50, "你办了健身卡, 决心改掉久坐的毛病...", '["请私教", "自己练", "三天打鱼"]', {"money": -2000, "health": 12, "luck": 3}),
    ("INSOMNIA", "失眠困扰", "life", 1, 25, 55, "连续几周睡不着, 白天精神恍惚...", '["看医生", "冥想放松", "加班累到睡"]', {"money": -800, "health": -8, "luck": -3}),
    ("BUY_HOUSE", "买房决策", "finance", 3, 26, 45, "看了半年房, 终于有一套心仪的, 首付够但月供压力大...", '["咬牙上车", "再等等", "租更好的"]', {"money": -50000, "luck": 10, "health": -3}),
    ("RENT_RISE", "房租上涨", "life", 1, 22, 40, "房东通知下个月房租涨20%...", '["接受涨价", "搬家", "和房东砍价"]', {"money": -1200, "health": -2, "luck": 0}),
    ("PET_ADOPT", "领养宠物", "life", 2, 24, 45, "路过宠物店, 一只小猫望着你...", '["带回家", "云养就好", "捐款给救助站"]', {"money": -2000, "health": 5, "luck": 8, "relationship": 5}),
    ("TRAVEL_SOLO", "独自旅行", "life", 2, 22, 50, "攒够了年假和预算, 你想一个人去远方...", '["说走就走", "等朋友一起", "周边游"]', {"money": -5000, "health": 10, "luck": 12}),
    ("INVEST_LOSS", "投资亏损", "finance", 2, 25, 55, "基金连续下跌, 账户缩水30%...", '["割肉离场", "加仓摊平", "再也不碰"]', {"money": -10000, "health": -8, "luck": -12}),
    ("STOCK_GAIN", "股票盈利", "finance", 2, 26, 55, "持仓的股票突然涨停, 心情大好...", '["止盈离场", "继续持有", "加仓"]', {"money": 8000, "luck": 10, "health": 3}),
    ("DEBT_PRESSURE", "还贷压力", "finance", 2, 28, 50, "房贷车贷信用卡, 每月工资到账就没了...", '["接更多活", "削减开支", "向家人求助"]', {"money": -2000, "health": -6, "luck": -5}),
    ("TAX_REFUND", "退税到账", "finance", 1, 22, 55, "个税汇算多缴的税款退回来了...", '["存起来", "犒劳自己", "还信用卡"]', {"money": 2000, "luck": 5, "health": 2}),
    ("LOVE_MEET", "偶遇心动的人", "relationship", 2, 20, 40, "在一次朋友聚会上, 你遇到了一个聊得来的人...", '["主动追求", "保持观望", "专注事业"]', {"money": -300, "relationship": 15, "luck": 3}),
    ("BLIND_DATE", "相亲安排", "relationship", 1, 25, 40, "父母/朋友给你安排了一场相亲...", '["认真见面", "敷衍应付", "拒绝相亲"]', {"money": -500, "relationship": 5, "luck": 0}),
    ("BREAKUP", "分手", "relationship", 2, 20, 45, "感情走到尽头, 对方提出了分手...", '["挽回试试", "潇洒放手", "做朋友"]', {"money": 0, "health": -12, "relationship": -25, "luck": -5}),
    ("MARRIAGE_PROPOSE", "求婚", "relationship", 3, 26, 45, "交往多年, 你觉得是时候给对方一个承诺了...", '["浪漫求婚", "简单领证", "再等等"]', {"money": -10000, "relationship": 30, "luck": 15}),
    ("WEDDING", "举办婚礼", "relationship", 2, 26, 45, "婚礼筹备比想象中费钱费力...", '["盛大婚礼", "旅行结婚", "只摆几桌"]', {"money": -50000, "relationship": 20, "health": -8}),
    ("MARRIAGE_CRISIS", "婚姻危机", "relationship", 3, 30, 55, "结婚几年后, 你们频繁为琐事争吵...", '["婚姻咨询", "冷静分居", "为了孩子忍"]', {"money": -3000, "health": -10, "relationship": -15}),
    ("OLD_FRIEND", "老友重逢", "social", 1, 25, 60, "十年未见的大学同学突然联系你...", '["热情聚会", "线上聊聊", "婉拒"]', {"money": -800, "relationship": 12, "luck": 5}),
    ("FRIEND_BORROW", "朋友借钱", "social", 2, 25, 55, "好朋友开口借一笔不小的钱...", '["借给他", "婉拒", "少借一点"]', {"money": -5000, "relationship": 10, "luck": -5}),
    ("FAMILY_PRESSURE", "催婚催生", "family", 1, 28, 45, "过年回家, 亲戚轮番轰炸式关心...", '["据理力争", "敷衍答应", "减少回家"]', {"money": -2000, "health": -5, "relationship": -8}),
    ("PARENT_ILL", "父母生病", "family", 2, 30, 60, "母亲突然住院, 需要人照顾...", '["请假陪护", "请护工", "接来同住"]', {"money": -8000, "health": -8, "relationship": 10}),
    ("CHILD_BORN", "孩子出生", "family", 3, 28, 45, "家里迎来了新生命, 喜悦与压力并存...", '["全职带娃", "请月嫂", "父母帮忙"]', {"money": -15000, "health": -10, "relationship": 25, "luck": 10}),
    ("DIVORCE", "离婚", "relationship", 3, 32, 55, "经过深思熟虑, 你们决定结束婚姻...", '["协议离婚", "打官司", "再试试"]', {"money": -30000, "health": -15, "relationship": -30, "luck": -10}),
    ("OFFICE_ROMANCE", "办公室恋情", "relationship", 2, 23, 40, "你和同事之间产生了微妙的感觉...", '["低调发展", "公开关系", "保持距离"]', {"money": 0, "relationship": 15, "luck": -3, "health": -2}),
    ("MENTOR_MEET", "遇见贵人", "career", 3, 24, 50, "行业前辈欣赏你的潜力, 愿意指点你...", '["虚心请教", "送礼感谢", "保持距离"]', {"money": -500, "luck": 20, "relationship": 15}),
    ("LOTTERY_WIN", "彩票小奖", "luck", 3, 20, 60, "随手买的彩票中了小奖, 够吃顿好的...", '["请朋友吃饭", "继续买", "存起来"]', {"money": 500, "luck": 20, "health": 2}),
    ("VIRAL_VIDEO", "视频爆火", "career", 3, 20, 45, "你随手发的短视频获得了百万播放...", '["做自媒体", "低调处理", "接广告"]', {"money": 5000, "luck": 20, "health": -3}),
    ("INHERITANCE", "意外遗产", "finance", 3, 30, 60, "远房亲戚去世留给你一笔遗产...", '["捐一部分", "全部收下", "拒绝继承"]', {"money": 100000, "luck": 15, "health": 0}),
    ("MID_LIFE_CRISIS", "中年危机", "life", 2, 38, 55, "四十岁将至, 你开始怀疑人生意义...", '["换活法", "接受现实", "心理咨询"]', {"money": -5000, "health": -8, "luck": -5}),
    ("HAIR_LOSS", "脱发焦虑", "life", 1, 28, 50, "洗头时掉发越来越多, 发际线后移...", '["植发", "假发", "顺其自然"]', {"money": -15000, "health": 0, "luck": -3}),
    ("BACK_PAIN", "腰肌劳损", "life", 1, 26, 55, "久坐导致腰疼, 弯腰都困难...", '["理疗康复", "换人体工学椅", "硬扛"]', {"money": -2000, "health": -6, "luck": 0}),
    ("CHILD_COLLEGE", "孩子高考", "family", 2, 42, 55, "孩子即将高考, 全家如临大敌...", '["全程陪考", "请家教", "顺其自然"]', {"money": -20000, "health": -10, "relationship": 5}),
    ("RETIRE_PLAN", "规划退休", "life", 2, 50, 60, "离退休不远了, 你开始盘算养老金够不够...", '["加大储蓄", "投资理财", "继续工作"]', {"money": -5000, "luck": 5, "health": 3}),
    ("VOLUNTEER", "志愿服务", "social", 1, 25, 60, "社区组织公益活动, 招募志愿者...", '["报名参加", "捐款代替", "没时间"]', {"money": -200, "relationship": 10, "luck": 8, "health": 3}),
    ("ONLINE_HARASS", "网络暴力", "life", 2, 20, 45, "你发的内容引来大量恶意评论...", '["正面回应", "删帖退网", "法律维权"]', {"money": -1000, "health": -10, "luck": -8}),
    ("PATENT_APPROVE", "专利获批", "career", 3, 26, 55, "你申请的发明专利终于获批了...", '["技术转化", "挂名就好", "继续申请"]', {"money": 5000, "luck": 15, "health": 2}),
    ("LAWSUIT", "卷入官司", "life", 3, 25, 60, "因为一个合同纠纷, 你收到了律师函...", '["应诉", "和解", "逃避"]', {"money": -20000, "health": -15, "luck": -15}),
    ("RELOCATION", "城市迁徙", "life", 2, 24, 45, "机会来了, 另一个城市有更好的发展...", '["搬家", "远程通勤", "放弃机会"]', {"money": -8000, "luck": 10, "health": -5, "relationship": -5}),
    ("PANDEMIC_LOCK", "疫情封控", "life", 2, 20, 55, "突发公共卫生事件, 居家隔离数周...", '["远程工作", "学新技能", "焦虑躺平"]', {"money": -2000, "health": -6, "luck": 0}),
    ("CRYPTO_CRASH", "币圈暴跌", "finance", 3, 22, 40, "你跟风买的币一夜腰斩...", '["割肉", "抄底", "装死"]', {"money": -15000, "luck": -15, "health": -10}),
    ("ANGEL_INVEST", "天使投资", "finance", 3, 28, 55, "有人想让你投资他的早期项目...", '["投一笔", "要股份", "拒绝"]', {"money": -10000, "luck": 12}),
    ("INSURANCE_CLAIM", "保险理赔", "finance", 2, 25, 60, "之前买的保险终于派上用场了...", '["申请理赔", "嫌麻烦放弃", "加保"]', {"money": 8000, "luck": 5, "health": 5}),
    ("SCAM_CALL", "诈骗电话", "life", 1, 25, 70, "接到冒充公检法的电话, 差点转账...", '["挂断举报", "吓出一身汗", "差点被骗"]', {"money": 0, "luck": 8, "health": -3}),
    ("DREAM_JOB_FAIL", "梦企拒信", "career", 2, 22, 40, "你心仪已久的公司发了拒信...", '["再战", "降低预期", "转行"]', {"money": 0, "health": -5, "luck": -5}),
    ("CONFERENCE_SPEAK", "大会演讲", "career", 3, 28, 55, "受邀在行业大会上做主题演讲...", '["精心准备", "紧张应付", "请人代讲"]', {"money": 2000, "luck": 18, "relationship": 12}),
    ("MBA_APPLY", "申请MBA", "education", 2, 26, 40, "商学院MBA项目开始招生, 学费不菲...", '["申请全日制", "在职读", "不读"]', {"money": -50000, "luck": 15, "health": -3}),
    ("PHD_OFFER", "博士录取", "education", 3, 24, 32, "你收到了博士项目的录取通知...", '["去读博", "放弃读博", "defer一年"]', {"money": -10000, "luck": 12, "health": -5}),
    ("ACQUIRE_OFFER", "收购要约", "career", 3, 30, 55, "大公司提出收购你们的小团队...", '["接受收购", "拒绝", "抬价谈判"]', {"money": 30000, "luck": 20, "health": 0}),
    ("SABBATICAL", "带薪休假", "career", 2, 28, 50, "公司推行 Sabbatical, 你可以休三个月...", '["去旅行", "进修", "换现金"]', {"money": 0, "health": 15, "luck": 10}),
    ("DAILY_COMMUTE", "通勤地狱", "life", 1, 22, 45, "每天单程两小时地铁, 身心俱疲...", '["搬家近点", "换工作", "忍"]', {"money": -3000, "health": -6, "luck": 0}),
    ("THERAPY", "心理咨询", "life", 2, 22, 55, "压力大到影响生活, 朋友建议看心理咨询...", '["预约咨询", "线上倾诉", "自己扛"]', {"money": -2000, "health": 10, "luck": 5}),
    ("MARATHON", "跑马拉松", "life", 2, 25, 55, "你报名了人生第一次半程马拉松...", '["认真训练", "走完全程", "转让名额"]', {"money": -800, "health": 10, "luck": 8}),
    ("REMOTE_WORK", "远程办公", "career", 1, 24, 45, "公司宣布可以每周两天居家办公...", '["回老家办公", "租更好的房子", "效率至上"]', {"money": -1000, "health": 5, "luck": 3}),
    ("OPEN_SOURCE", "开源贡献", "career", 2, 22, 40, "你给知名开源项目提的PR被合并了...", '["继续贡献", "写技术博客", "低调就好"]', {"money": 0, "luck": 15, "relationship": 5}),
    ("TECH_CONFERENCE", "技术大会", "career", 2, 24, 45, "公司报销让你参加了一场行业技术大会...", '["疯狂社交", "专注听课", "顺便旅游"]', {"money": 500, "relationship": 8, "luck": 6}),
    ("SIDE_BUSINESS_FAIL", "副业翻车", "career", 2, 24, 45, "你接的一个大项目甲方跑路了, 尾款要不回...", '["打官司", "认栽", "转行"]', {"money": -5000, "health": -5, "luck": -8}),
    ("COLLEAGUE_RIVAL", "职场竞争", "career", 2, 25, 45, "同期入职的同事升得比你快, 心里不是滋味...", '["加倍努力", "跳槽走人", "调整心态"]', {"money": 0, "health": -5, "luck": -2}),
    ("TEAM_BUILDING", "团建活动", "social", 1, 22, 50, "公司组织了一次户外团建, 要爬山...", '["积极参与", "找借口不去", "当摄影师"]', {"money": 0, "health": 3, "relationship": 8}),
    ("NETWORKING", "行业社交", "social", 1, 24, 50, "朋友拉你参加了一个高端行业酒会...", '["积极社交", "早退", "只做听众"]', {"money": -1000, "relationship": 15, "luck": 8}),
    ("BETRAYED_TRUST", "被朋友背叛", "social", 3, 25, 50, "你发现好朋友在背后说你坏话...", '["对质", "默默疏远", "原谅"]', {"money": 0, "health": -8, "relationship": -20, "luck": -10}),
    ("LONG_DISTANCE", "异地恋考验", "relationship", 2, 22, 35, "对方拿到了外地offer, 要分开两年...", '["坚持异地", "一起去", "分手"]', {"money": -2000, "relationship": -10, "luck": 0}),
    ("CHILD_EDUCATION", "子女教育", "family", 2, 32, 55, "孩子上学问题, 学区房还是国际学校...", '["买学区房", "普通公立", "homeschool"]', {"money": -100000, "luck": 5, "health": -5}),
    ("GRANDCHILD", "孙辈出生", "family", 2, 50, 60, "儿女有了孩子, 你升级当爷爷奶奶了...", '["帮忙带", "给钱", "享受自由"]', {"money": -10000, "relationship": 25, "health": -5}),
    ("RENOVATION", "装修新家", "life", 2, 28, 50, "买了房开始装修, 预算每天都在超...", '["豪华装", "简约实用", "自己盯工地"]', {"money": -30000, "health": -5, "luck": 5}),
    ("CAR_ACCIDENT", "小车祸", "life", 2, 22, 55, "下班路上被追尾, 人没事但车要修...", '["走保险", "私了", "顺便换车"]', {"money": -5000, "health": -5, "luck": -8}),
    ("WALLET_LOST", "钱包丢失", "life", 1, 18, 55, "地铁上发现钱包和手机不见了...", '["挂失补办", "报警", "自认倒霉"]', {"money": -3000, "luck": -10, "health": -3}),
    ("FIND_WALLET", "捡到钱包", "luck", 1, 18, 60, "路边捡到一个钱包, 里面有现金和证件...", '["交给警察", "联系失主", "据为己有"]', {"money": 500, "luck": 10, "relationship": 5}),
    ("BOOK_PUBLISH", "出版书籍", "career", 3, 28, 55, "你写的技术书被出版社看中...", '["签约出版", "自费出版", "网上免费"]', {"money": 10000, "luck": 18, "health": -5}),
    ("PODCAST_HIT", "播客走红", "career", 2, 25, 50, "你业余做的播客突然上了平台推荐...", '["加大投入", "保持更新", "停更"]', {"money": 3000, "luck": 15, "relationship": 10}),
    ("COFOUNDER_LEAVE", "合伙人离开", "career", 3, 27, 55, "创业公司的联合创始人提出退出...", '["好聚好散", "打官司", "回购股份"]', {"money": -5000, "health": -10, "luck": -8}),
    ("DATA_LEAK_COMPANY", "公司数据泄露", "career", 2, 24, 50, "公司数据库被拖库, 你作为负责人被问责...", '["连夜修复", "引咎辞职", "推给外包"]', {"money": -3000, "health": -12, "luck": -10}),
    ("LIBRARY_STUDY", "泡馆备考", "education", 1, 19, 28, "考试季, 你每天泡在图书馆直到闭馆...", '["坚持到底", "适当放松", "找研友组队"]', {"money": -300, "health": -4, "luck": 8}),
    ("ROOMATE_CONFLICT", "室友矛盾", "life", 1, 18, 24, "宿舍卫生和作息问题让你和室友吵了一架...", '["主动沟通", "申请换宿舍", "忍一忍"]', {"money": 0, "health": -3, "relationship": -5}),
    ("DROPOUT_THOUGHT", "想退学", "life", 2, 19, 24, "学业压力让你一度想放弃, 坐在天台发呆...", '["咬牙坚持", "休学调整", "转专业试试"]', {"money": 0, "health": -8, "luck": -3}),
    ("MINIMALISM", "极简生活", "life", 2, 26, 50, "房间堆满东西, 你决定断舍离...", '["大清理", "慢慢整理", "买买买"]', {"money": 1000, "health": 5, "luck": 5}),
    ("MEDITATION_RETREAT", "禅修闭关", "life", 2, 28, 55, "朋友推荐了一个山间禅修营, 断网一周...", '["报名参加", "周末版", "不感兴趣"]', {"money": -3000, "health": 15, "luck": 8}),
    ("POACH_TEAM", "被挖角", "career", 2, 28, 48, "竞争对手开出双倍薪资想挖你整个组...", '["带团队跳槽", "只自己去", "效忠现公司"]', {"money": 6000, "luck": 5, "relationship": -10}),
    ("CONTRACT_END", "合同到期", "career", 1, 24, 45, "一年期合同即将到期, 公司还没提续约...", '["主动谈续约", "开始面试", "休息两个月"]', {"money": 0, "luck": 0, "health": -3}),
    ("NFT_HYPE", "NFT热潮", "finance", 2, 22, 35, "朋友拉你买数字藏品, 说能翻倍...", '["跟风买入", "自己发行", "不参与"]', {"money": -3000, "luck": -5}),
    ("DIVIDEND", "分红到账", "finance", 2, 30, 60, "持有的理财产品到期分红...", '["再投资", "消费", "还贷款"]', {"money": 5000, "luck": 6, "health": 2}),
    ("NEIGHBOR_HELP", "邻居互助", "social", 1, 25, 60, "邻居出差请你帮忙浇花喂猫...", '["热心帮忙", "有偿服务", "婉拒"]', {"money": 200, "relationship": 12, "luck": 5}),
    ("GLASSES_NEEDED", "近视加深", "life", 1, 25, 55, "看屏幕越来越模糊, 验光度数又涨了...", '["做手术", "换眼镜", "少看手机"]', {"money": -3000, "health": 3, "luck": 0}),
    ("COOKING_CLASS", "学做饭", "life", 1, 22, 50, "外卖吃腻了, 你决定学几道拿手菜...", '["认真学", "买半成品", "继续点外卖"]', {"money": -500, "health": 8, "luck": 2}),
    ("KARAOKE_NIGHT", "KTV之夜", "social", 1, 20, 45, "部门聚餐后去KTV, 你五音不全但气氛热烈...", '["麦霸", "只听不说", "提前溜走"]', {"money": -500, "relationship": 8, "health": -4}),
    ("YOGA_CLASS", "瑜伽课", "life", 1, 22, 50, "朋友安利瑜伽改善体态和睡眠...", '["办卡", "跟视频练", "不感兴趣"]', {"money": -1500, "health": 8, "luck": 3}),
    ("COMPLIANCE_AUDIT", "合规审查", "career", 2, 26, 55, "公司面临严格合规审查, 加班写材料...", '["积极配合", "跳槽", "内部举报"]', {"money": 1000, "health": -8, "luck": -3}),
    ("MEDIA_INTERVIEW", "媒体采访", "career", 2, 28, 55, "记者想采访你关于行业的观点...", '["接受采访", "拒绝", "匿名发表"]', {"money": 0, "luck": 12, "relationship": 8}),
    ("CLIMATE_DISASTER", "极端天气", "life", 2, 20, 60, "暴雨/高温导致出行困难, 工作受影响...", '["居家办公", "硬出门", "请假"]', {"money": -1000, "health": -4, "luck": -3}),
    ("FLOOD_HOME", "家里漏水", "life", 1, 25, 55, "出差回来发现家里水管爆了, 地板泡了...", '["紧急维修", "保险理赔", "顺便翻新"]', {"money": -8000, "health": -5, "luck": -5}),
    ("ID_THEFT", "信息泄露", "life", 2, 22, 50, "收到陌生短信, 你的个人信息可能在暗网流通...", '["改密码", "报警", "无视"]', {"money": -1000, "luck": -8, "health": -2}),
    ("LUCKY_CHARM", "转运符", "luck", 1, 20, 60, "寺庙里求了一个转运符, 心理安慰不少...", '["虔诚供奉", "图个心安", "不信这些"]', {"money": -100, "luck": 8, "health": 2}),
    ("SUNRISE_HIKE", "看日出", "life", 1, 18, 60, "朋友约你凌晨爬山看日出...", '["出发", "放鸽子", "改看日落"]', {"money": -200, "health": 5, "luck": 6}),
]


def esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def main() -> None:
    events = EVENTS[:100]
    codes = [e[0] for e in events]
    assert len(events) == 100, len(events)
    assert len(codes) == len(set(codes)), "duplicate event_code"

    rows = []
    for code, title, etype, rarity, min_a, max_a, content, choices, effect in events:
        effect_json = json.dumps(effect, ensure_ascii=False)
        rows.append(
            f"('{code}', '{esc(title)}', '{etype}', {rarity}, {min_a}, {max_a},\n"
            f" '{esc(content)}',\n"
            f" '{esc(choices)}',\n"
            f" '{esc(effect_json)}')"
        )

    sql = (
        "-- 剧情事件池初始数据(100条)\n"
        "-- 执行前可先清空: DELETE FROM story_event WHERE deleted = 0;\n\n"
        "INSERT INTO `story_event` (`event_code`, `title`, `event_type`, `rarity`, "
        "`min_age`, `max_age`, `event_content`, `choices`, `effect`)\n"
        "VALUES\n" + ",\n".join(rows) + ";\n"
    )

    out = Path(__file__).resolve().parents[1] / "src/main/resources/sql/story_event_data.sql"
    out.write_text(sql, encoding="utf-8")
    print(f"generated {len(events)} events -> {out}")
    print("types:", dict(Counter(e[2] for e in events)))
    print("rarity:", dict(Counter(e[3] for e in events)))


if __name__ == "__main__":
    main()
