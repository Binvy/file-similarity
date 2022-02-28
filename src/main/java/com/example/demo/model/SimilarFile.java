package com.example.demo.model;

import java.util.Objects;

/**
 *  相似文件
 *  @author hanbinwei
 * @date 2022/2/25 17:09
 */
public class SimilarFile {

    private String path;
    private String name;
    private Double score;

    public SimilarFile() {}

    public SimilarFile(String path, String name, Double score) {
        this.path = path;
        this.name = name;
        this.score = score;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimilarFile that = (SimilarFile) o;
        return Objects.equals(path, that.path) && Objects.equals(name, that.name) && Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name, score);
    }

    @Override
    public String toString() {
        return "SimilarFile{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
