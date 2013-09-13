package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.{table, mybId}
import org.joda.time.DateTime

/**
 * Created by 
 * User: kgignatyev
 */
@table("objects")
class ManualIdObj {

  @mybId(manual=true) var id:Long = -1
  var status:String = ""
  var createdAt:DateTime = _
}
