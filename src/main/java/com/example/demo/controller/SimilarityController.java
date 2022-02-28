package com.example.demo.controller;

import com.example.demo.model.SimilarFile;
import com.example.demo.service.SimilarityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 相似度 Controller
 * @author hanbinwei
 * @date 2022/2/22 14:50
 */
@RestController
@RequestMapping("/similar")
public class SimilarityController {

    private final SimilarityService similarityService;

    public SimilarityController(SimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    /**
     * 在指定文件夹下，查询指定文件相似的文件。
     * @param filename 文件全路径
     * @param dirname 待查文件夹，若为空，则默认在指定文件的所在目录内进行查找。
     * @return 相似的文件列表
     */
    @PostMapping("/search")
    public List<SimilarFile> searchSimilarFile(String filename, String dirname) {
        return similarityService.search(filename, dirname);
    }

    /**
     * 查询指定文件夹下的相似文件
     * @param dirname
     * @return
     */
    @PostMapping("/search/dir")
    public Map<String, List<SimilarFile>> searchSimilarFile(String dirname) {
        return similarityService.search(dirname);
    }

    /**
     * 获取指定文件的相似度
     * @param leftFilename
     * @param rightFilename
     * @return 文件相似度
     */
    @PostMapping("/score")
    public Double getSimilarityScore(String leftFilename, String rightFilename) {
        return similarityService.getSimilarityScore(leftFilename, rightFilename);
    }

    /**
     * 查询指定文件夹下的文件相似度
     * @param dirname
     * @return
     */
    @PostMapping("/score/dir")
    public Map<String, List<SimilarFile>> getSimilarityScore(String dirname) {
        return similarityService.getSimilarityScore(dirname);
    }

}
