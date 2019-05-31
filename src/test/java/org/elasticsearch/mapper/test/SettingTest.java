package org.elasticsearch.mapper.test;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.mapper.entity.MacBook;
import org.elasticsearch.mapper.settings.SettingBuilder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Rassyan on 2019/5/27.
 */
public class SettingTest {

    @Test
    public void testSetting() throws IOException {
        String configPath = new File("src/test/resources/conf").getAbsolutePath();
        SettingBuilder indexMetadataBuilder = new SettingBuilder(configPath);
        XContentBuilder xContentBuilder = indexMetadataBuilder.buildSetting(MacBook.class);
        System.out.println(Strings.toString(xContentBuilder));
    }
}
