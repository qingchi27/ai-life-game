package com.qingchi.ailife.domain;

import lombok.Getter;

/**
 * 家庭条件类型
 *
 * @author hengji-chen
 * @date 2026/6/4
 */
@Getter
public enum FamilyBackgroundType {

    ORDINARY("普通家庭", 0, 10_000, 0, 50, 0, 30, 0, 10, 0, 80),
    AVERAGE("一般家庭", 10_000, 100_000, 50, 80, 0, 50, 0, 30, 20, 70),
    COMFORTABLE("小康家庭", 100_000, 300_000, 50, 100, 0, 50, 0, 30, 30, 60),
    AFFLUENT("小富家庭", 300_000, 800_000, 50, 100, 30, 50, 20, 40, 40, 80),
    UPPER_MIDDLE("中富家庭", 800_000, 1_000_000, 50, 80, 30, 80, 20, 60, 30, 70),
    RICH("大富家庭", 1_000_000, 3_000_000, 60, 70, 50, 90, 50, 80, 20, 60),
    SUPER_RICH("巨富家庭", 5_000_000, 1_000_000_000, 70, 90, 60, 100, 80, 100, 0, 50);

    /**
     * 展示名称
     **/
    private final String label;

    /**
     * 财富下限
     **/
    private final int wealthMin;

    /**
     * 财富上限
     **/
    private final int wealthMax;

    /**
     * 健康下限
     **/
    private final int healthMin;

    /**
     * 健康上限
     **/
    private final int healthMax;

    /**
     * 名气下限
     **/
    private final int fameMin;

    /**
     * 名气上限
     **/
    private final int fameMax;

    /**
     * 权力下限
     **/
    private final int powerMin;

    /**
     * 权力上限
     **/
    private final int powerMax;

    /**
     * 感情下限
     **/
    private final int affectionMin;

    /**
     * 感情上限
     **/
    private final int affectionMax;

    FamilyBackgroundType(String label, int wealthMin, int wealthMax,
                         int healthMin, int healthMax,
                         int fameMin, int fameMax,
                         int powerMin, int powerMax,
                         int affectionMin, int affectionMax) {
        this.label = label;
        this.wealthMin = wealthMin;
        this.wealthMax = wealthMax;
        this.healthMin = healthMin;
        this.healthMax = healthMax;
        this.fameMin = fameMin;
        this.fameMax = fameMax;
        this.powerMin = powerMin;
        this.powerMax = powerMax;
        this.affectionMin = affectionMin;
        this.affectionMax = affectionMax;
    }
}
