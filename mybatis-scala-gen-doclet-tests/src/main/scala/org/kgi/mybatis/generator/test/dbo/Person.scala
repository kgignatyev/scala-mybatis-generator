package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.{mybIgnore, table, mybSortBy}
import java.util.Date

/** Created by
 * @author kgignatyev
 */
@table
class Person  extends Loc{


 var name:String = ""
 @mybSortBy(order = "DESC") var email:String = ""

  var date:Date = _

  @mybIgnore
  var ignorable:String = _

}
