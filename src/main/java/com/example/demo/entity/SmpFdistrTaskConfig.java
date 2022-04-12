package com.example.demo.entity;

import lombok.Data;

/**
 * desc
 *
 * @author hanbinwei
 * @date 2022/4/8 14:44
 */
@Data
public class SmpFdistrTaskConfig {

    /**
     * 主键
     */
    private Long id;

    /**
     * 任务名
     */
    private String taskName;

    /**
     * 字符集
     */
    private String fileCharset;

    /**
     * 源目录
     */
    private String sourcePath;

    /**
     * 目标目录
     */
    private String targetPath;

    /**
     * 传输目录层级
     */
    private Integer deepLevel;

    /**
     * 分发策略
     */
    private Integer handlePolicy;

    /**
     * 临时文件扩展名
     */
    private String tmpFileSuffix;

    /**
     * 任务间隔
     */
    private Long scanInterval;

    /**
     * 过滤模式
     */
    private Integer filterPolicy;

    /**
     * 文件过滤类型
     */
    private String filterType;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 是否自启
     */
    private Integer autoStart;

    /**
     * 运行状态
     */
    private Integer taskStatus;

    /**
     * 任务消息
     */
    private String taskMessage;

}
