package org.elasticsearch.mapper.test;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.mapper.settings.AnalysisBuilder;
import org.elasticsearch.mapper.utils.AnalysisUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Rassyan on 2019/5/29.
 */
public class AnalyzerTest {

    @Test
    public void testAnalyzer() throws IOException {
        String configPath = new File("src/test/resources/conf").getAbsolutePath();
        AnalysisBuilder analysisBuilder = new AnalysisBuilder(configPath);
        Analyzer analyzer = analysisBuilder.getAnalyzer("route_analyzer");
        System.out.println(AnalysisUtils.analyze(analyzer, "11380 NW 77th Pl"));
    }

}
