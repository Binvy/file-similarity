package com.example.demo.mapper;

import com.example.demo.entity.SmpFdistrTaskConfig;
import com.example.demo.entity.SmpFileRecord;
import com.example.demo.entity.SmpFileSimilarityPending;
import com.example.demo.entity.SmpFileSimilarityResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * desc
 *
 * @author hanbinwei
 * @date 2022/4/8 14:50
 */
@SpringBootTest
public class SmpFdistrTaskConfigMapperTest {

    @Autowired
    private SmpFdistrTaskConfigMapper smpFdistrTaskConfigMapper;

    @Autowired
    private SmpFileRecordMapper smpFileRecordMapper;

    @Autowired
    private SmpFileSimilarityPendingMapper smpFileSimilarityPendingMapper;

    @Autowired
    private SmpFileSimilarityResultMapper smpFileSimilarityResultMapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<SmpFdistrTaskConfig> userList = smpFdistrTaskConfigMapper.selectList(null);
        userList.forEach(System.out::println);

        System.out.println("------------------------------------");

        List<SmpFileRecord> smpFileRecords = smpFileRecordMapper.selectList(null);
        smpFileRecords.forEach(System.out::println);

        System.out.println("------------------------------------");

        List<SmpFileSimilarityPending> smpFileSimilarityPendings = smpFileSimilarityPendingMapper.selectList(null);
        smpFileSimilarityPendings.forEach(System.out::println);

        System.out.println("------------------------------------");

        List<SmpFileSimilarityResult> smpFileSimilarityResults = smpFileSimilarityResultMapper.selectList(null);
        smpFileSimilarityResults.forEach(System.out::println);
    }

}
