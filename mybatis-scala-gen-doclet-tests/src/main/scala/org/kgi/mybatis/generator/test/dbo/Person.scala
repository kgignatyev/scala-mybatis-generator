package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.DBO

/** Created by
 * @author kgignatyev
 */
@DBO("person" )
class Person {

  var id:Long = _

  var name:String = ""
  var email:String = ""


}
