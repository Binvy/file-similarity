package com.example.demo.entity;

import lombok.Data;

/**
 * smp文件相似度分析结果信息
 * @author hanbinwei
 * @date 2022/4/8 15:43
 */
@Data
public class SmpFileSimilarityResult {

    private Long id;
    private String srcFileId;
    private String dstFileId;
    private Double similarityScore;

}
