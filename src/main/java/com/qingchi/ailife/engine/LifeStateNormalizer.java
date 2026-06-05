package com.qingchi.ailife.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingchi.ailife.domain.ChildrenState;
import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.util.JsonUtil;

/**
 * 人生状态归一化, 兼容旧存档字段
 *
 * @author hengji-chen
 * @date 2026/6/4
 */
public final class LifeStateNormalizer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LifeStateNormalizer() {
    }

    /**
     * 解析并归一化人生状态 JSON
     *
     * @param {String} json - 存档 JSON
     * @returns {LifeState} 归一化后状态
     */
    public static LifeState parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(json);
            LifeState state = JsonUtil.fromJson(json, LifeState.class);
            if (state == null) {
                return null;
            }
            migrateLegacyFields(state, node);
            fillDefaults(state);
            return state;
        } catch (Exception ex) {
            LifeState state = JsonUtil.fromJson(json, LifeState.class);
            fillDefaults(state);
            return state;
        }
    }

    private static void migrateLegacyFields(LifeState state, JsonNode node) {
        if (state.getWealth() == null && node.has("money")) {
            state.setWealth(node.get("money").asInt());
        }
        if (state.getAffection() == null && node.has("relationship")) {
            state.setAffection(node.get("relationship").asInt());
        }
        if (state.getFame() == null && node.has("luck")) {
            state.setFame(node.get("luck").asInt());
        }
        if (state.getOccupation() == null && node.has("career")) {
            state.setOccupation(node.get("career").asText());
        }
        if (state.getChildren() == null && node.has("childCount")) {
            ChildrenState children = state.ensureChildren();
            children.setCount(node.get("childCount").asInt());
        }
        if (Boolean.TRUE.equals(state.getHasChild()) && state.childCount() == 0) {
            ChildrenState children = state.ensureChildren();
            children.setCount(1);
        }
    }

    private static void fillDefaults(LifeState state) {
        if (state == null) {
            return;
        }
        if (state.getAffection() == null) {
            state.setAffection(40);
        }
        if (state.getWealth() == null) {
            state.setWealth(3000);
        }
        if (state.getPower() == null) {
            state.setPower(20);
        }
        if (state.getFame() == null) {
            state.setFame(30);
        }
        if (state.getHealth() == null) {
            state.setHealth(80);
        }
        if (state.getLifespan() == null) {
            state.setLifespan(78);
        }
        if (state.getOccupation() == null) {
            state.setOccupation("大学生");
        }
        ChildrenState children = state.ensureChildren();
        if (children.getCount() == null) {
            children.setCount(state.childCount());
        }
        if (children.getAbility() == null) {
            children.setAbility(0);
        }
        if (children.getAchievement() == null) {
            children.setAchievement(0);
        }
    }
}
