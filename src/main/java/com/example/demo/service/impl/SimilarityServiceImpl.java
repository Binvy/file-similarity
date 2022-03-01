package com.example.demo.service.impl;

import com.example.demo.constant.SimilarityConstants;
import com.example.demo.model.SimilarFile;
import com.example.demo.service.SimilarityService;
import com.example.demo.util.SimilarityUtils;
import com.example.demo.util.TikaUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 相似度服务实现类
 * @author hanbinwei
 * @date 2022/2/22 15:02
 */
@Service
public class SimilarityServiceImpl implements SimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(SimilarityServiceImpl.class);

    @Override
    public List<SimilarFile> search(String filename, String dirname) {
        Assert.hasText(filename, "filename must has text");
        Path path = Paths.get(filename);
        if (!path.toFile().exists()) {
            return null;
        }
        Path folder = StringUtils.isEmpty(dirname) ? path.getParent() : Paths.get(dirname);
        List<SimilarFile> similarFiles = new ArrayList<>();
        FileUtils.listFiles(folder.toFile(), null, true).stream().forEach(file -> {
            String targetPath = file.toString();
            String targetName = FilenameUtils.getName(targetPath);
            if (filename.equals(targetPath)) {
                return;
            }
            Double score = getSimilarityScore(filename, targetPath);
            if (isSimilar(score)) {
                similarFiles.add(new SimilarFile(targetPath, targetName, score));
            }
        });
        Collections.sort(similarFiles, Comparator.comparing(SimilarFile::getScore).reversed());
        return similarFiles;
    }

    @Override
    public Map<String, List<SimilarFile>> search(String dirname) {
        Assert.hasText(dirname, "dirname must has text");
        return processFolder(dirname, score -> SimilarityConstants.SCORE_SIMILAR.compareTo(score) <= 0);
    }

    @Override
    public Double getSimilarityScore(String leftFilename, String rightFilename) {
        String leftContent = TikaUtils.parse(leftFilename);
        if (StringUtils.isBlank(leftContent)) {
            return SimilarityConstants.SCORE_ZERO;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("left file content: {}", leftContent);
        }
        String rightContent = TikaUtils.parse(rightFilename);
        if (StringUtils.isBlank(rightContent)) {
            return SimilarityConstants.SCORE_ZERO;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("right file content: {}", rightContent);
        }
        Double score = SimilarityUtils.getJaccardSimilarity(leftContent, rightContent);
        logger.info("similarity score between [{}] and [{}] is {}", leftFilename, rightFilename, score);
        return score;
    }

    @Override
    public Map<String, List<SimilarFile>> getSimilarityScore(String dirname) {
        return processFolder(dirname, score -> true);
    }

    /**
     * 判断相似度是否大于临界值。默认大于0.75，认为是相似的。
     * @param score 相似度
     * @return true 相似 false 不相似
     */
    public boolean isSimilar(Double score) {
        return score != null && score.compareTo(SimilarityConstants.SCORE_SIMILAR) > 0;
    }

    /**
     * 判断指定的两个文件是否相似
     * @param leftFilename
     * @param rightFilename
     * @return true 相似 false 不相似
     */
    public boolean isSimilar(String leftFilename, String rightFilename) {
        Double score = getSimilarityScore(leftFilename, rightFilename);
        return isSimilar(score);
    }

    /**
     * 遍历计算指定文件夹下的文件的相似度
     * @param dirname 文件夹
     * @param predicate 断言函数
     * @return 文件相似度映射
     */
    public Map<String, List<SimilarFile>> processFolder(String dirname, Predicate<Double> predicate) {
        List<String> files = FileUtils.listFiles(new File(dirname), null, true)
                .stream()
                .map(File::toString)
                .collect(Collectors.toList());
        Map<String, List<SimilarFile>> scoreMap = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(files)) {
            return scoreMap;
        }
        int size = files.size();
        for (int i = 0; i < size; i++) {
            String leftPath = files.get(i);
            String leftName = FilenameUtils.getName(leftPath);
            for (int j = i + 1; j < size - i; j++) {
                String rightPath = files.get(j);
                String rightName = FilenameUtils.getName(rightPath);
                Double score = getSimilarityScore(leftPath, rightPath);
                if (predicate != null && !predicate.test(score)) {
                    continue;
                }
                scoreMap.compute(leftPath, (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>(size);
                    }
                    v.add(new SimilarFile(rightPath, rightName, score));
                    return v;
                });
                scoreMap.compute(rightPath, (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>(size);
                    }
                    v.add(new SimilarFile(leftPath, leftName, score));
                    return v;
                });
            }
        }
        return scoreMap;
    }

}