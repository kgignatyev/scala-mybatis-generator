package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.{table, mybSortBy, DBO}

/** Created by
 * @author kgignatyev
 */
@table("person" )
class Person extends Identifiable{


 @mybSortBy var name:String = ""
 @mybSortBy var email:String = ""


}
