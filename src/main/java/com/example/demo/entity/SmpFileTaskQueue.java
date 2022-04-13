package com.example.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * 文件处理任务队列
 * 各类基于文件进行解析、分析、处理等任务，待执行任务均通过此表进行获取。
 * <p>
 * 处理流程：
 * 1、数据处理器发现新文件，将文件移动存储到指定目录下（或分布式存储中）；
 * 2、程序生成文件全局唯一id ,记录文件具体信息到smp_file_record表；
 * 3、向file_handle_task_queue表中插入所要执行的待执行任务记录；
 * 4、执行任务记录
 * 5、独立线程做任务队列清除功能（当前id对应全部任务执行成功）
 *
 * @author hanbinwei
 * @date 2022/4/12 10:41
 */
@Data
public class SmpFileTaskQueue {

    /**
     * 主键
     */
    private Long id;

    /**
     * 文件id，由解析器生成，类型为varchar。
     */
    private String fileId;

    /**
     * 任务类型，英文type值:
     * 全文检索 file.full.text.search,
     * 深度分析 file.depth.analysis,
     * 相似度分析 file.similarity.analysis
     */
    private String taskType;

    /**
     * 任务状态 0待执行、1执行中、2执行成功、3执行失败
     */
    private Integer taskStatus;

    /**
     * 扩展信息： 由具体任务定义
     */
    private String extInfo;

    /**
     * 创建时间 例：2021-11-16 03:51:00
     */
    private Date createTime;

    /**
     * 更新时间 例：2021-11-16 03:51:00
     */
    private Date updateTime;

}
