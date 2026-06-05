package com.qingchi.ailife.engine;

import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.entity.StoryEvent;

import java.util.List;
import java.util.Set;

/**
 * 剧情事件连贯性过滤
 *
 * @author hengji-chen
 * @date 2026/6/4
 */
public final class StoryEventFilter {

    private static final Set<String> STUDENT_ONLY = Set.of(
            "GRADUATE_COLLEGE", "CET6_PASS", "CAMPUS_LOVE", "LIBRARY_STUDY",
            "ROOMATE_CONFLICT", "DROPOUT_THOUGHT", "SCHOLARSHIP"
    );

    private static final Set<String> REQUIRES_NOT_GRADUATED = Set.of(
            "GRADUATE_COLLEGE"
    );

    private static final Set<String> REQUIRES_GRADUATED_OR_WORKER = Set.of(
            "FIRST_JOB", "PROBATION_PASS", "LAYOFF_RUMOR", "ACTUALLY_LAYOFF"
    );

    private static final Set<String> REQUIRES_PARTNER = Set.of(
            "MARRIAGE_PROPOSE", "WEDDING", "BREAKUP", "LONG_DISTANCE", "MARRIAGE_CRISIS"
    );

    private static final Set<String> REQUIRES_MARRIED = Set.of(
            "MARRIAGE_CRISIS", "DIVORCE", "CHILD_BORN"
    );

    private static final Set<String> SINGLE_ROMANCE = Set.of(
            "LOVE_MEET", "CAMPUS_LOVE", "OFFICE_ROMANCE", "BLIND_DATE"
    );

    private static final Set<String> REQUIRES_CHILD = Set.of(
            "CHILD_EDUCATION", "CHILD_COLLEGE", "GRANDCHILD"
    );

    private static final Set<String> REQUIRES_ENTREPRENEUR = Set.of(
            "STARTUP_FAIL", "STARTUP_SUCCESS", "COFOUNDER_LEAVE", "ACQUIRE_OFFER"
    );

    private StoryEventFilter() {
    }

    /**
     * 判断事件是否符合当前人生状态
     *
     * @param {StoryEvent} event - 候选事件
     * @param {LifeState} state - 当前状态
     * @param {int} age - 当前年龄
     * @returns {boolean} 是否可触发
     */
    public static boolean canTrigger(StoryEvent event, LifeState state, int age) {
        if (event == null || event.getEventCode() == null) {
            return false;
        }
        String code = event.getEventCode();
        if (isRecentDuplicate(state, code)) {
            return false;
        }
        if (STUDENT_ONLY.contains(code) && !inStudentPhase(state)) {
            return false;
        }
        if (REQUIRES_NOT_GRADUATED.contains(code) && Boolean.TRUE.equals(state.getGraduated())) {
            return false;
        }
        if (REQUIRES_GRADUATED_OR_WORKER.contains(code) && inStudentPhase(state)) {
            return false;
        }
        if (REQUIRES_PARTNER.contains(code) && !Boolean.TRUE.equals(state.getHasPartner())) {
            return false;
        }
        if (REQUIRES_MARRIED.contains(code) && !Boolean.TRUE.equals(state.getMarried())) {
            return false;
        }
        if (("MARRIAGE_PROPOSE".equals(code) || "WEDDING".equals(code))
                && Boolean.TRUE.equals(state.getMarried())) {
            return false;
        }
        if (SINGLE_ROMANCE.contains(code) && Boolean.TRUE.equals(state.getMarried())) {
            return false;
        }
        if (REQUIRES_CHILD.contains(code) && state.childCount() <= 0) {
            return false;
        }
        if ("GRANDCHILD".equals(code) && age < 48) {
            return false;
        }
        if ("FAMILY_PRESSURE".equals(code) && (Boolean.TRUE.equals(state.getMarried()) || age < 26)) {
            return false;
        }
        if (REQUIRES_ENTREPRENEUR.contains(code) && !isEntrepreneur(state)) {
            return false;
        }
        if ("DIVORCE".equals(code) && !Boolean.TRUE.equals(state.getMarried())) {
            return false;
        }
        if ("BREAKUP".equals(code) && !Boolean.TRUE.equals(state.getHasPartner())) {
            return false;
        }
        return true;
    }

    private static boolean inStudentPhase(LifeState state) {
        if (Boolean.TRUE.equals(state.getGraduated())) {
            return false;
        }
        String occupation = state.getOccupation();
        return occupation == null || occupation.contains("大学") || "大学生".equals(occupation);
    }

    private static boolean isEntrepreneur(LifeState state) {
        String occupation = state.getOccupation();
        if (occupation != null && (occupation.contains("创业") || "创业者".equals(occupation))) {
            return true;
        }
        return state.getPower() != null && state.getPower() >= 70;
    }

    private static boolean isRecentDuplicate(LifeState state, String code) {
        List<String> recent = state.getRecentEventCodes();
        if (recent == null || recent.isEmpty()) {
            return false;
        }
        return recent.contains(code);
    }
}
