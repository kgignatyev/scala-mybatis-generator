package org.kgi.mybatis.scala.generator.doclet

import annotation.target.getter

/**
 * User: kgignatyev
 * Date: 1/4/13
 */
//by some strange reasons @field annotation does not visible to scaladocs
@getter
class mybSort(val order:String = "ASC")  extends scala.annotation.StaticAnnotation{

}
