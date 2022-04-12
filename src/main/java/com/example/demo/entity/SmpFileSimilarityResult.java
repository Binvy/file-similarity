package com.example.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * smp文件相似度分析结果信息
 * @author hanbinwei
 * @date 2022/4/8 15:43
 */
@Data
public class SmpFileSimilarityResult {

    private Long id;
    private String srcFileId;
    private String srcFileName;
    private String srcFileCheckSum;
    private String srcFileBusiType;
    private String srcFileExtName;
    private String srcFileUri;
    private String dstFileId;
    private String dstFileName;
    private String dstFileCheckSum;
    private String dstFileBusiType;
    private String dstFileExtName;
    private String dstFileUri;
    private Double similarityScore;
    private Date createTime;

}
