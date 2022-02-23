package com.example.demo.util;

import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.FuzzyScore;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Locale;

/**
 * Similarity Utils
 * @author hanbinwei
 * @date 2022/2/22 16:26
 */
public class SimilarityUtils {

    public static Double getJaccardSimilarity(final CharSequence left, final CharSequence right) {
        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        return jaccardSimilarity.apply(left, right);
    }

    public static Double getCosineSimilarity(final CharSequence left, final CharSequence right) {
        CosineDistance cosineDistance = new CosineDistance();
        Double distance = cosineDistance.apply(left, right);
        return 1.0 - distance;
    }

    public static Double getJaroWinklerSimilarity(final CharSequence left, final CharSequence right) {
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        return similarity.apply(left, right);
    }

    public static Integer getFuzzyDistance(final CharSequence term, final CharSequence query) {
        FuzzyScore score = new FuzzyScore(Locale.getDefault());
        return score.fuzzyScore(term, query);
    }

    public static Integer getLevenshteinDistance(final CharSequence left, final CharSequence right) {
        LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();
        return distance.apply(left, right);
    }

}
