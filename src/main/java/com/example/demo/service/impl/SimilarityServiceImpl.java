package com.example.demo.service.impl;

import com.example.demo.constant.SimilarityConstants;
import com.example.demo.model.SimilarFile;
import com.example.demo.service.SimilarityService;
import com.example.demo.util.SimilarityUtils;
import com.example.demo.util.TikaUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /** 相似文件缓存: key: 指定文件名-指定查询文件夹, value: 相似文件列表 */
    private static final Cache<FileCacheKey, List<SimilarFile>> SIMILAR_FILE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(60))
            .build();

    /** 文件夹相似度缓存: key: 指定查询文件夹, value: 相似文件列表映射 */
    private static final Cache<String, Map<String, List<SimilarFile>>> SIMILAR_DIR_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(60))
            .build();

    @Override
    public List<SimilarFile> search(String filename, String dirname) {
        Assert.hasText(filename, "filename must has text");
        FileCacheKey cacheKey = new FileCacheKey(filename, dirname);
        List<SimilarFile> cachedSimilarFiles = SIMILAR_FILE_CACHE.getIfPresent(cacheKey);
        if (cachedSimilarFiles != null) {
            return cachedSimilarFiles;
        }
        Path path = Paths.get(filename);
        if (!path.toFile().exists()) {
            return null;
        }
        Path folder = StringUtils.isEmpty(dirname) ? path.getParent() : Paths.get(dirname);
        List similarFiles = new ArrayList<>();
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
        SIMILAR_FILE_CACHE.put(cacheKey, similarFiles);
        return similarFiles;
    }

    @Override
    public Map<String, List<SimilarFile>> search(String dirname) {
        return processFolder(dirname, score -> SimilarityConstants.SCORE_SIMILAR.compareTo(score) <= 0);
    }

    @Override
    public Double getSimilarityScore(String leftFilename, String rightFilename) {
        try {
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
        } catch (Exception e) {
            logger.error("get similarity score exception.", e);
        }
        return SimilarityConstants.SCORE_ZERO;
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
        Assert.hasText(dirname, "dirname must has text");
        Map<String, List<SimilarFile>> cachedSimilarFiles = SIMILAR_DIR_CACHE.getIfPresent(dirname);
        if (cachedSimilarFiles != null) {
            return cachedSimilarFiles;
        }
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
        SIMILAR_DIR_CACHE.put(dirname, scoreMap);
        return scoreMap;
    }




    class FileCacheKey {
        private String file;
        private String folder;

        public FileCacheKey() {
        }

        public FileCacheKey(String file, String folder) {
            this.file = file;
            this.folder = folder;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FileCacheKey that = (FileCacheKey) o;
            return Objects.equals(file, that.file) && Objects.equals(folder, that.folder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, folder);
        }

        @Override
        public String toString() {
            return "FileCacheKey{" +
                    "file='" + file + '\'' +
                    ", folder='" + folder + '\'' +
                    '}';
        }
    }

}