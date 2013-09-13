package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.{mybId, table}

/**
 * Created by 
 * User: kgignatyev
 */
@table("email")
class EmailAddress {
  @mybId(manual=true) var address:String = ""
   var status:String = ""
}
