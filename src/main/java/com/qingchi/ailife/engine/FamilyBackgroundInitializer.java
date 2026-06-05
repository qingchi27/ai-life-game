package com.qingchi.ailife.engine;

import com.qingchi.ailife.domain.ChildrenState;
import com.qingchi.ailife.domain.FamilyBackgroundType;
import com.qingchi.ailife.domain.LifeState;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 家庭条件随机初始化
 *
 * @author hengji-chen
 * @date 2026/6/4
 */
public final class FamilyBackgroundInitializer {

    private FamilyBackgroundInitializer() {
    }

    /**
     * 随机抽取家庭条件并写入人生状态
     *
     * @param {LifeState} state - 待初始化状态
     * @returns {FamilyBackgroundType} 抽中的家庭条件
     */
    public static FamilyBackgroundType applyRandom(LifeState state) {
        FamilyBackgroundType background = pickRandom();
        apply(state, background);
        return background;
    }

    /**
     * 按指定家庭条件初始化属性
     *
     * @param {LifeState} state - 待初始化状态
     * @param {FamilyBackgroundType} background - 家庭条件
     */
    public static void apply(LifeState state, FamilyBackgroundType background) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        state.setFamilyBackground(background.getLabel());
        state.setWealth(randomInt(random, background.getWealthMin(), background.getWealthMax()));
        state.setHealth(clamp(randomInt(random, background.getHealthMin(), background.getHealthMax()), 0, 100));
        state.setFame(clamp(randomInt(random, background.getFameMin(), background.getFameMax()), 0, 100));
        state.setPower(clamp(randomInt(random, background.getPowerMin(), background.getPowerMax()), 0, 100));
        state.setAffection(clamp(randomInt(random, background.getAffectionMin(), background.getAffectionMax()), 0, 100));
        state.setLifespan(resolveLifespan(state.getHealth()));
        state.setOccupation("大学生");
        state.setGraduated(false);
        state.setHasPartner(false);
        state.setMarried(false);
        state.setHasChild(false);
        ChildrenState children = state.ensureChildren();
        children.setCount(0);
        children.setAbility(0);
        children.setAchievement(0);
    }

    private static FamilyBackgroundType pickRandom() {
        FamilyBackgroundType[] values = FamilyBackgroundType.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private static int randomInt(ThreadLocalRandom random, int min, int max) {
        if (min >= max) {
            return min;
        }
        return random.nextInt(min, max + 1);
    }

    private static int resolveLifespan(int health) {
        return clamp(68 + health / 5, 60, 95);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
