package org.kgi.mybatis.scala.generator.doclet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by
 * User: kgignatyev
 */
@Retention(RetentionPolicy.SOURCE)
public @interface DBO {
    String value();
}
