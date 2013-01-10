package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.{table, mybSortBy}

/** Created by
 * @author kgignatyev
 */
@table("person" )
class Person  extends Serializable with Identifiable with Loc{


 @mybSortBy var name:String = ""
 @mybSortBy var email:String = ""


}
