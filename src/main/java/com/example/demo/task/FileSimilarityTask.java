package com.example.demo.task;

import com.example.demo.entity.AbstractSimilarityFile;
import com.example.demo.entity.SmpFileRecord;
import com.example.demo.entity.SmpFileSimilarityPending;
import com.example.demo.entity.SmpFileSimilarityResult;
import com.example.demo.mapper.SmpFileRecordMapper;
import com.example.demo.mapper.SmpFileSimilarityPendingMapper;
import com.example.demo.mapper.SmpFileSimilarityResultMapper;
import com.example.demo.service.SimilarityService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.Date;
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

    private final SmpFileSimilarityPendingMapper smpFileSimilarityPendingMapper;

    private final SmpFileSimilarityResultMapper smpFileSimilarityResultMapper;

    private final SimilarityService similarityService;

    public FileSimilarityTask(SmpFileRecordMapper smpFileRecordMapper,
                              SmpFileSimilarityPendingMapper smpFileSimilarityPendingMapper,
                              SmpFileSimilarityResultMapper smpFileSimilarityResultMapper,
                              SimilarityService similarityService) {
        this.smpFileRecordMapper = smpFileRecordMapper;
        this.smpFileSimilarityPendingMapper = smpFileSimilarityPendingMapper;
        this.smpFileSimilarityResultMapper = smpFileSimilarityResultMapper;
        this.similarityService = similarityService;
    }

    /**
     * 相似度分析任务：10分钟一次
     */
    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void processFileSimilarity() {
        logger.info("process similarity task start");

        List<SmpFileSimilarityPending> pendingFileList = smpFileSimilarityPendingMapper.selectList(null);
        if (CollectionUtils.isEmpty(pendingFileList)) {
            logger.info("no pending file to process");
            return;
        }
        List<SmpFileRecord> fileRecordList = smpFileRecordMapper.selectList(null);
        if (CollectionUtils.isEmpty(fileRecordList)) {
            logger.info("no file record to process");
            return;
        }

        List<AbstractSimilarityFile> pendingList = pendingFileList.stream()
                .map(record -> (AbstractSimilarityFile) record)
                .collect(Collectors.toList());
        List<String> pendingIdList = pendingList.stream()
                .map(AbstractSimilarityFile::getId)
                .collect(Collectors.toList());
        List<AbstractSimilarityFile> completedList = fileRecordList.stream()
                .filter(record -> !pendingIdList.contains(record.getId()))
                .map(record -> (AbstractSimilarityFile) record)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(completedList)) {
            // 老->新
            processFileSimilarity(completedList, pendingList);
            // 新->老
            processFileSimilarity(pendingList, completedList);
        }
        // 新->新
        processFileSimilarity(pendingList, pendingList);

        smpFileSimilarityPendingMapper.deleteBatchIds(pendingIdList);

        logger.info("process similarity task end");
    }

    private void processFileSimilarity(List<AbstractSimilarityFile> srcFileList,
                                       List<AbstractSimilarityFile> dstFileList) {
        if (CollectionUtils.isEmpty(srcFileList) || CollectionUtils.isEmpty(dstFileList)) {
            logger.info("no file to process similarity");
            return;
        }

        Date currentTime = new Date();
        for (AbstractSimilarityFile srcFile : srcFileList) {
            String srcFileUri = Paths.get(smpParseFileDir,
                    getDirNameByBusiType(srcFile.getBusiType()), srcFile.getFileName()).toString();
            for (AbstractSimilarityFile dstFile : dstFileList) {
                String dstFileUri = Paths.get(smpParseFileDir,
                        getDirNameByBusiType(dstFile.getBusiType()), dstFile.getFileName()).toString();
                if (Objects.equals(srcFileUri, dstFileUri)) {
                    continue;
                }

                Double score = similarityService.getSimilarityScore(srcFileUri, dstFileUri);

                SmpFileSimilarityResult record = new SmpFileSimilarityResult();
                record.setSrcFileId(srcFile.getId());
                record.setSrcFileName(srcFile.getFileName());
                record.setSrcFileCheckSum(srcFile.getCheckSum());
                record.setSrcFileBusiType(srcFile.getBusiType());
                record.setSrcFileExtName(srcFile.getExtName());
                record.setSrcFileUri(srcFileUri);
                record.setDstFileId(dstFile.getId());
                record.setDstFileName(dstFile.getFileName());
                record.setDstFileCheckSum(dstFile.getCheckSum());
                record.setDstFileBusiType(dstFile.getBusiType());
                record.setDstFileExtName(dstFile.getExtName());
                record.setDstFileUri(dstFileUri);
                record.setSimilarityScore(score);
                record.setCreateTime(currentTime);
                smpFileSimilarityResultMapper.insert(record);
            }
        }
    }

    private String getDirNameByBusiType(String busiType) {
        return StringUtils.hasText(busiType) ? busiType : "";
    }

}
