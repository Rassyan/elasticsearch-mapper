package org.elasticsearch.mapper.annotations;

import java.lang.annotation.*;

/**
 * Created by Rassyan on 2019/5/31.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface IndexSetting {

    String name() default "";

    String env() default "";

}
