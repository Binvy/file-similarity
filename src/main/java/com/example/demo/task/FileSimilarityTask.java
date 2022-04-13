package com.example.demo.task;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.entity.SmpFileRecord;
import com.example.demo.entity.SmpFileSimilarityResult;
import com.example.demo.entity.SmpFileTaskQueue;
import com.example.demo.mapper.SmpFileRecordMapper;
import com.example.demo.mapper.SmpFileSimilarityResultMapper;
import com.example.demo.mapper.SmpFileTaskQueueMapper;
import com.example.demo.service.SimilarityService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件相似度调度任务
 * @author hanbinwei
 * @date 2022/4/8 15:27
 */
@Service
public class FileSimilarityTask {

    private static final Logger logger = LoggerFactory.getLogger(FileSimilarityTask.class);

    @Value("${smp.parser.file.dir:}")
    private String smpParseFileDir;

    private final SmpFileRecordMapper smpFileRecordMapper;

    private final SmpFileSimilarityResultMapper smpFileSimilarityResultMapper;

    private final SimilarityService similarityService;

    private final SmpFileTaskQueueMapper smpFileTaskQueueMapper;

    public FileSimilarityTask(SmpFileRecordMapper smpFileRecordMapper,
                              SmpFileSimilarityResultMapper smpFileSimilarityResultMapper,
                              SimilarityService similarityService,
                              SmpFileTaskQueueMapper smpFileTaskQueueMapper) {
        this.smpFileRecordMapper = smpFileRecordMapper;
        this.smpFileSimilarityResultMapper = smpFileSimilarityResultMapper;
        this.similarityService = similarityService;
        this.smpFileTaskQueueMapper = smpFileTaskQueueMapper;
    }

    /**
     * 相似度分析任务：10分钟一次
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void processFileSimilarity() {
        logger.info("process similarity task start");
        try {
            Wrapper<SmpFileTaskQueue> taskQueueParam = new QueryWrapper<SmpFileTaskQueue>()
                    .eq("task_type", "file.similarity.analysis")
                    .eq("task_status", 0);
            List<SmpFileTaskQueue> taskQueueList = smpFileTaskQueueMapper.selectList(taskQueueParam);
            if (CollectionUtils.isEmpty(taskQueueList)) {
                logger.info("没有【待执行】的文件【相似度分析】任务列队");
                return;
            }
            List<String> queueIdList = taskQueueList.stream()
                    .map(SmpFileTaskQueue::getFileId).collect(Collectors.toList());
            List<SmpFileRecord> queueFileList = smpFileRecordMapper.selectBatchIds(queueIdList);
            if (CollectionUtils.isEmpty(queueFileList)) {
                logger.info("没有对应的文件记录");
            }
            List<SmpFileRecord> fileList = smpFileRecordMapper.selectList(null);
            if (CollectionUtils.isEmpty(fileList)) {
                logger.info("文件记录列表为空");
                return;
            }
            List<SmpFileRecord> completedFileList = fileList.stream()
                    .filter(record -> !queueIdList.contains(record.getId()))
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(completedFileList)) {
                // 老->新
                processFileSimilarity(completedFileList, queueFileList);
                // 新->老
                processFileSimilarity(queueFileList, completedFileList);
            }
            // 新->新
            processFileSimilarity(queueFileList, queueFileList);

            executeSuccess(queueIdList);
        } catch (Exception e) {
            logger.error("process file similarity task exception.", e);
        }
        logger.info("process similarity task end");
    }

    private void processFileSimilarity(List<SmpFileRecord> srcFileList,
                                       List<SmpFileRecord> dstFileList) {
        if (CollectionUtils.isEmpty(srcFileList) || CollectionUtils.isEmpty(dstFileList)) {
            logger.info("no file to process similarity");
            return;
        }

        for (SmpFileRecord srcFile : srcFileList) {
            String srcFileUri = Paths.get(smpParseFileDir,
                    srcFile.getFileRelativeSite(), srcFile.getFileName()).toString();
            for (SmpFileRecord dstFile : dstFileList) {
                try {
                    String dstFileUri = Paths.get(smpParseFileDir,
                            dstFile.getFileRelativeSite(), dstFile.getFileName()).toString();
                    if (Objects.equals(srcFileUri, dstFileUri)) {
                        continue;
                    }

                    Double score = similarityService.getSimilarityScore(srcFileUri, dstFileUri);

                    SmpFileSimilarityResult record = new SmpFileSimilarityResult();
                    record.setSrcFileId(srcFile.getId());
                    record.setDstFileId(dstFile.getId());
                    record.setSimilarityScore(score);
                    smpFileSimilarityResultMapper.insert(record);
                } catch (Exception e) {
                    logger.error("save result exception", e);
                }
            }
        }
    }

    private void executeSuccess(List<String> queueIdList) {
        try {
            Wrapper<SmpFileTaskQueue> updateWrapper = new UpdateWrapper<SmpFileTaskQueue>().
                    in("file_id", queueIdList)
                    .eq("task_type", "file.similarity.analysis")
                    .eq("task_status", 0);
            SmpFileTaskQueue entity = new SmpFileTaskQueue();
            entity.setTaskStatus(2);
            smpFileTaskQueueMapper.update(entity, updateWrapper);
        } catch (Exception e) {
            logger.error("execute success exception", e);
        }
    }

}
