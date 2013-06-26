package org.kgi.mybatis.scala.generator.doclet


import scala.annotation.meta.getter

/**
 * User: kgignatyev
 * Date: 1/4/13
 */
//by some strange reasons @field annotation is not visible to scaladocs
@getter
class mybSortBy(val order:String = "ASC")  extends scala.annotation.StaticAnnotation{

}
