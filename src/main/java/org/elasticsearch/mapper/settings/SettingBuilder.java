package org.elasticsearch.mapper.settings;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.mapper.annotations.IndexSetting;
import org.elasticsearch.mapper.mapping.MappingBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by Rassyan on 2019/5/30.
 */
public class SettingBuilder {
    private final AnalysisBuilder analysisBuilder;
    private final MappingBuilder mappingBuilder;
    private final String configPath;

    public SettingBuilder() throws IOException {
        this(CoreConstant.CONFIG_PATH);
    }

    public SettingBuilder(String configPath) throws IOException {
        this.analysisBuilder = new AnalysisBuilder(configPath);
        this.mappingBuilder = new MappingBuilder();
        this.configPath = CoreConstant.CONFIG_PATH;
    }

    public XContentBuilder buildSetting(Class<?> documentClazz) throws IOException {
        try (XContentBuilder xContentBuilder = XContentBuilder.builder(XContentType.JSON.xContent())) {
            xContentBuilder.prettyPrint();
            xContentBuilder.startObject();

            xContentBuilder.startObject("mappings");
            Set<String> analyzers = mappingBuilder.buildMapping(documentClazz, xContentBuilder);
            xContentBuilder.endObject();

            xContentBuilder.startObject("settings");
            Settings.Builder builder = Settings.builder();

            IndexSetting indexSetting = documentClazz.getAnnotation(IndexSetting.class);
            String name = indexSetting.name();
            String env = indexSetting.env();
            if (!"".equals(name) && !"".equals(env)) {
                File settingFile = new File(StringUtils.join(new String[]{configPath, "setting", env, name+".setting.json"}, File.separator));
                if (settingFile.exists()) {
                    builder.loadFromPath(settingFile.toPath());
                }
            }

            Predicate<String> predicate = s -> false;
            for (String analyzer : analyzers) {
                predicate = predicate.or(analysisBuilder.getAnalyzerSettingsFilter(analyzer));
            }
            builder.put(analysisBuilder.buildAnalysisSettings(predicate));
            builder.build().toXContent(xContentBuilder, new ToXContent.MapParams(Collections.singletonMap("flat_settings", "false")));

            xContentBuilder.endObject();

            xContentBuilder.endObject();
            return xContentBuilder;
        }
    }
}
