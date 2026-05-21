package com.qingchi.ailife.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选项视图对象
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceVO {

    /**
     * 选项ID
     **/
    private String id;

    /**
     * 选项内容
     **/
    private String content;
}
