package org.elasticsearch.mapper.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rassyan on 2019/5/31.
 */
public class AnalysisUtils {
    public static List<String> analyze(Analyzer analyzer, String text) throws IOException {
        List<String> result = new LinkedList<>();
        TokenStream tokenStream = analyzer.tokenStream("content", text);
        tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
            result.add(charTermAttribute.toString());
        }
        return result;
    }
}
