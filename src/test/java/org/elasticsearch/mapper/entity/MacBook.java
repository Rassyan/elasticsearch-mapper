package org.elasticsearch.mapper.entity;

import org.elasticsearch.mapper.annotations.Document;
import org.elasticsearch.mapper.annotations.IndexSetting;
import org.elasticsearch.mapper.annotations.enums.StringType;
import org.elasticsearch.mapper.annotations.fieldtype.*;
import org.elasticsearch.mapper.annotations.meta.MetaField_All;

import java.util.List;

@Document(_all = @MetaField_All(enabled = false))
@IndexSetting(name = "macbook", env = "dev")
public class MacBook extends Component {

    // default: keyword
    private String deviceName;

    @StringField(type = StringType.Text)
    private String manufacturer;

    @MultiField(
            mainField = @StringField(type = StringType.Keyword, boost = 2.0f),
            fields = {
                    @MultiNestedField(name = "en", field = @StringField(type = StringType.Text))
            },
            tokenFields = {
                    @TokenCountField(name = "enTokenCount", analyzer = "standard")
            }
    )
    private String introduction;

    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword)),
                    @MultiNestedField(name = "last_word", field = @StringField(type = StringType.Text,
                            analyzer = "last_word_analyzer", fielddata = @Fielddata(enable = true))),
                    @MultiNestedField(name = "route", field = @StringField(type = StringType.Text,
                            analyzer = "route_analyzer", fielddata = @Fielddata(enable = true))),
                    @MultiNestedField(name = "street_number", field = @StringField(type = StringType.Text,
                            analyzer = "street_number_analyzer", fielddata = @Fielddata(enable = true)))
            }
    )
    private String productionAddress;

    // nested doc
    private List<User> users;

    // inner doc
    private Cpu cpu;

    //inner doc
    private Memory memory;
}
