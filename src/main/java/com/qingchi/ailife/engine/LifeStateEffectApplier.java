package com.qingchi.ailife.engine;

import com.qingchi.ailife.domain.ChildrenState;
import com.qingchi.ailife.domain.LifeState;

import java.util.Map;

/**
 * 剧情数值效果应用器
 *
 * @author hengji-chen
 * @date 2026/6/4
 */
public final class LifeStateEffectApplier {

    private LifeStateEffectApplier() {
    }

    /**
     * 应用事件或选项的效果 JSON
     *
     * @param {LifeState} state - 目标状态
     * @param {Map} effect - 效果键值
     */
    public static void apply(LifeState state, Map<String, Object> effect) {
        if (state == null || effect == null || effect.isEmpty()) {
            return;
        }
        applyMappedDelta(state, effect, "wealth", "money", false);
        applyMappedDelta(state, effect, "affection", "relationship", true);
        applyMappedDelta(state, effect, "fame", "luck", true);
        applyMappedDelta(state, effect, "power", "power", true);
        applyMappedDelta(state, effect, "health", "health", true);
        applyMappedDelta(state, effect, "lifespan", "lifespan", false);

        applyMappedDelta(state, effect, "childCount", "childCount", false);
        applyMappedDelta(state, effect, "childAbility", "childAbility", true);
        applyMappedDelta(state, effect, "childAchievement", "childAchievement", true);

        if (effect.get("occupation") != null) {
            state.setOccupation(String.valueOf(effect.get("occupation")));
            state.setPower(clamp(safeAdd(state.getPower(), 5), 0, 100));
        }
        if (effect.get("career") != null) {
            state.setOccupation(String.valueOf(effect.get("career")));
            state.setPower(clamp(safeAdd(state.getPower(), 8), 0, 100));
        }
        syncChildFlags(state);
    }

    private static void applyMappedDelta(LifeState state, Map<String, Object> effect,
                                         String newKey, String legacyKey, boolean clamp0To100) {
        Object raw = effect.containsKey(newKey) ? effect.get(newKey) : effect.get(legacyKey);
        if (raw == null) {
            return;
        }
        int delta = Integer.parseInt(String.valueOf(raw));
        switch (newKey) {
            case "wealth" -> state.setWealth(safeAdd(state.getWealth(), delta));
            case "affection" -> state.setAffection(clamp(safeAdd(state.getAffection(), delta), 0, 100));
            case "fame" -> state.setFame(clamp(safeAdd(state.getFame(), delta), 0, 100));
            case "power" -> state.setPower(clamp(safeAdd(state.getPower(), delta), 0, 100));
            case "health" -> {
                state.setHealth(clamp(safeAdd(state.getHealth(), delta), 0, 100));
                if (delta < -5) {
                    state.setLifespan(safeAdd(state.getLifespan(), delta / 5));
                } else if (delta > 5) {
                    state.setLifespan(safeAdd(state.getLifespan(), 1));
                }
            }
            case "lifespan" -> state.setLifespan(clamp(safeAdd(state.getLifespan(), delta), 40, 120));
            case "childCount" -> {
                ChildrenState children = state.ensureChildren();
                children.setCount(Math.max(0, safeAdd(children.getCount(), delta)));
            }
            case "childAbility" -> {
                ChildrenState children = state.ensureChildren();
                children.setAbility(clamp(safeAdd(children.getAbility(), delta), 0, 100));
            }
            case "childAchievement" -> {
                ChildrenState children = state.ensureChildren();
                children.setAchievement(clamp(safeAdd(children.getAchievement(), delta), 0, 100));
            }
            default -> {
            }
        }
    }

    private static void syncChildFlags(LifeState state) {
        if (state.childCount() > 0) {
            state.setHasChild(true);
        }
    }

    private static int safeAdd(Integer value, int delta) {
        int base = value == null ? 0 : value;
        return base + delta;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
