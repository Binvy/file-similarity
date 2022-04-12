package com.example.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * desc
 *
 * @author hanbinwei
 * @date 2022/4/8 16:43
 */
@Data
public class AbstractSimilarityFile {

    private String id;

    private String fileName;

    private String checkSum;

    private String busiType;

    private String extName;

    private Date lastModified;

    private Long fileSize;

}
