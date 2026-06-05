package com.qingchi.ailife.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 人生状态核心对象
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class LifeState {

    /**
     * 感情, 含亲情友情爱情, 0-100
     **/
    private Integer affection;

    /**
     * 财富
     **/
    private Integer wealth;

    /**
     * 子女状态
     **/
    private ChildrenState children;

    /**
     * 权力, 0-100
     **/
    private Integer power;

    /**
     * 名气, 0-100
     **/
    private Integer fame;

    /**
     * 身体健康, 0-100
     **/
    private Integer health;

    /**
     * 预期寿命
     **/
    private Integer lifespan;

    /**
     * 家庭条件
     **/
    private String familyBackground;

    /**
     * 职业轨迹, 叙事与事件过滤用
     **/
    private String occupation;

    /**
     * 是否已毕业
     **/
    private Boolean graduated;

    /**
     * 是否有伴侣
     **/
    private Boolean hasPartner;

    /**
     * 是否已婚
     **/
    private Boolean married;

    /**
     * 是否有孩子, 与子女数量联动
     **/
    private Boolean hasChild;

    /**
     * 近期已触发的事件编码, 用于去重
     **/
    private List<String> recentEventCodes;

    /**
     * 记录近期事件编码
     *
     * @param {String} eventCode - 事件编码
     */
    public void recordRecentEvent(String eventCode) {
        if (eventCode == null || eventCode.isBlank()) {
            return;
        }
        if (recentEventCodes == null) {
            recentEventCodes = new ArrayList<>();
        }
        recentEventCodes.remove(eventCode);
        recentEventCodes.add(eventCode);
        while (recentEventCodes.size() > 8) {
            recentEventCodes.removeFirst();
        }
    }

    /**
     * 获取子女数量
     *
     * @returns {int} 子女数量
     */
    public int childCount() {
        if (children != null && children.getCount() != null) {
            return children.getCount();
        }
        return Boolean.TRUE.equals(hasChild) ? 1 : 0;
    }

    /**
     * 确保子女对象已初始化
     *
     * @returns {ChildrenState} 子女状态
     */
    public ChildrenState ensureChildren() {
        if (children == null) {
            children = new ChildrenState();
            children.setCount(0);
            children.setAbility(0);
            children.setAchievement(0);
        }
        return children;
    }
}
