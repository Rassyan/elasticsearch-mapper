package org.elasticsearch.mapper.settings;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.AnalysisModule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Rassyan on 2019/5/30.
 */
public class AnalysisBuilder {
    private final Settings mockSettings;
    private final IndexAnalyzers analyzers;

    public AnalysisBuilder() throws IOException {
        this(CoreConstant.CONFIG_PATH);
    }

    public AnalysisBuilder(String configPath) throws IOException {
        Settings.Builder builder = Settings.builder();
        File analysisFile = new File(StringUtils.join(new String[]{configPath, "analysis", "analysis.json"}, File.separator));
        if (analysisFile.exists()) {
            builder.loadFromPath(analysisFile.toPath())
                   .normalizePrefix("index.analysis.")
                   .put("index.number_of_replicas", 0)
                   .put("index.number_of_shards", 1)
                   .put("index.version.created", 1)
                   .build();
        }
        this.mockSettings = builder.build();

        Environment environment = new Environment(Settings.builder().put("path.home", ".").build(), null);
        CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin();
        AnalysisModule analysisModule = new AnalysisModule(environment, Arrays.asList(commonAnalysisPlugin));
        AnalysisRegistry analysisRegistry = analysisModule.getAnalysisRegistry();
        IndexMetaData mockIndexMetaData = IndexMetaData.builder("mockIndex").settings(this.mockSettings).build();
        IndexSettings mockIndexSettings = new IndexSettings(mockIndexMetaData, Settings.EMPTY);
        this.analyzers = analysisRegistry.build(mockIndexSettings);
    }

    public Analyzer getAnalyzer(String name) {
        NamedAnalyzer namedAnalyzer = this.analyzers.get(name);
        if (namedAnalyzer == null) {
            return null;
        }
        return namedAnalyzer.analyzer();
    }

    public Predicate<String> getAnalyzerSettingsFilter(String name) {
        Analyzer analyzer = this.getAnalyzer(name);
        if (analyzer instanceof CustomAnalyzer) {
            Predicate<String> predicate = s -> s.startsWith(String.format("index.analysis.analyzer.%s.", name));

            String tokenizerName = mockSettings.get(String.format("index.analysis.analyzer.%s.tokenizer", name));
            assert tokenizerName != null;
            predicate = predicate.or(s -> s.startsWith(String.format("index.analysis.tokenizer.%s.", tokenizerName)));

            List<String> charFilterNames = mockSettings.getAsList(String.format("index.analysis.analyzer.%s.char_filter", name));
            for (String charFilterName : charFilterNames) {
                predicate = predicate.or(s -> s.startsWith(String.format("index.analysis.char_filter.%s.", charFilterName)));
            }

            List<String> tokenFilterNames = mockSettings.getAsList(String.format("index.analysis.analyzer.%s.filter", name));
            for (String tokenFilterName : tokenFilterNames) {
                predicate = predicate.or(s -> s.startsWith(String.format("index.analysis.filter.%s.", tokenFilterName)));
            }
            return predicate;
        }
        return s -> false;
    }

    public Settings buildAnalysisSettings(Predicate<String> predicate) {
        return this.mockSettings.filter(predicate);
    }

}
