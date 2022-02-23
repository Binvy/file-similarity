package com.example.demo.service;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * 相似度
 * @author hanbinwei
 * @date 2022/2/22 14:58
 */
public interface SimilarityService {

    /**
     * search similar files
     * @param filename
     * @param dirname
     * @return similar files
     */
    List<Pair<String, Double>> search(String filename, String dirname);

    /**
     * search similar files
     * @param dirname
     * @return
     */
    Map<String, List<Pair<String, Double>>> search(String dirname);

    /**
     * get similarity score between two files
     * @param leftFilename
     * @param rightFilename
     * @return similarity score
     */
    Double getSimilarityScore(String leftFilename, String rightFilename);

    /**
     * get similarity score in dir
     * @param dirname
     * @return
     */
    Map<String, List<Pair<String, Double>>> getSimilarityScore(String dirname);
}
