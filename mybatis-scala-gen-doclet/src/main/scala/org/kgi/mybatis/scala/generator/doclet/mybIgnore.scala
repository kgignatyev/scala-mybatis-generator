package org.kgi.mybatis.scala.generator.doclet

import annotation.target.getter

/**
 * do not use field in the myBatis mappings
 *
 * User: kgignatyev
 * Date: 2/1/13
 */
@getter
class mybIgnore extends scala.annotation.StaticAnnotation
