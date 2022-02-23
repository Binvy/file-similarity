package com.example.demo.constant;

/**
 * similarity constants
 * @author hanbinwei
 * @date 2022/2/22 16:01
 */
public class SimilarityConstants {

    /**
     * 相似度：0；完全无关
     */
    public static final Double SCORE_ZERO = Double.valueOf(0.0);
    /**
     *  相似度：0.5；关系不大
     */
    public static final Double SCORE_HALF = Double.valueOf(0.5);
    /**
     * 相似度：0.7；有点相似，但相似度不高
     */
    public static final Double SCORE_SIMILAR_LOW = Double.valueOf(0.7);
    /**
     * 相似度：0.75；有点相似
     */
    public static final Double SCORE_SIMILAR = Double.valueOf(0.75);
    /**
     * 相似度：0.8；有点相似，且相似度很高
     */
    public static final Double SCORE_SIMILAR_HIGH = Double.valueOf(0.8);
    /**
     * 相似度：0.9；很相似，几乎一样
     */
    public static final Double SCORE_SIMILAR_ALMOST = Double.valueOf(0.95);
    /**
     * 相似度：1.0；完全一样
     */
    public static final Double SCORE_SAME = Double.valueOf(1);

}
