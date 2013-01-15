package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.mybSortBy

/**
 * User: kgignatyev
 * Date: 1/9/13
 */
trait Loc extends Serializable with Identifiable{
  @mybSortBy  var lat:Double = 0.0
   var lng:Double = 0.0

}
