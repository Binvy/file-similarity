package com.example.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * 文件记录表or全文检索索引结构
 *
 * @author hanbinwei
 * @date 2022/4/8 15:43
 */

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SmpFileRecord extends AbstractSimilarityFile {

    /**
     * 新增主键id，由解析器生成，类型为varchar。
     */
    private String id;

    /**
     * 文件名称
     * 例：190612010093_0a753cc61259329c488f1c6d6d2a634b.txt
     */
    private String fileName;

    /**
     * 文件类型
     * 例：txt
     */
    private String fileType;

    /**
     * 文件大小
     * 数值类型，以字节(B)为单位
     * 例：15531
     */
    private Long fileSize;

    /**
     * 文件内容
     * ES中存储全量内容，MySQL存前1024字符内容
     */
    private String fileContent;
    /**
     * 校验码
     * md5值
     */
    private String fileCheckSum;
    /**
     * 文件相对位置
     * <p>
     * 文件存储在本地时，相对位置代表文件本地存储的相对目录。
     * 文件存在在网络上（如：分布式存储MinIO、FastDFS等），相对位置代表文件存储的不同分区等相对位置。
     * 文件绝对路径 =  配置文件root_path + "/" +   file_relative_dir
     * 例：abnormal/190612010093_0a753cc61259329c488f1c6d6d2a634b.txt
     */
    private String fileRelativeSite;

    /**
     * 文件存储类型
     * 如：filesystem、minio、FastDFS
     * 注：当前默认为filesystem（存储在本地文件系统中）
     */
    private String storageType;

    /**
     * 文件资源定位符
     * 例：b3c07f90-d1fd-46e4-af06-d57fc08ad547  (以minio示例)
     */
    private String fileUri;

    /**
     * 是否为公文
     * 公务文书是法定机关与组织在公务活动中，按照特定的体式、经过一定的处理程序形成和使用的书面材料，又称公务文件。
     * 0（未判定），1（是），2（否）
     */
    private Integer officialDoc;

    /**
     * 文件标识
     * <p>
     * 文件标识由程序自动判定（由深度分析模块判定），默认0未判定。
     * 0（未判定），1（密标），2（标密），3（疑似）
     * 注：若有与当前文件相关告警记录，且告警记录中字段alert_type值为1 ，则该字段值默认设置为1，否则该字段值设置为0 。
     */
    private Integer fileFlag;

    /**
     * 处置标识
     * <p>
     * 人工判定标志，默认0未判定。
     * 0（待处置/未判定），1（涉密），2（非涉密），3（标记）
     */
    private Integer dealFlag;

    /**
     * 处置用户
     * 谁完成的处置操作
     */
    private String dealUser;
    /**
     * 处置时间
     * 例：2021-11-16 03:51:00
     */
    private Date dealTime;
    /**
     * 创建时间
     * 例：2021-11-16 03:51:00
     */
    private Date createTime;
    /**
     * 更新时间
     * 例：2021-11-16 03:51:00
     */
    private Date updateTime;

}
