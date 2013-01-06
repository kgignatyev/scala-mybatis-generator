package org.kgi.mybatis.generator.test.dbo

import org.kgi.mybatis.scala.generator.doclet.mybId

/**
 * Created by 
 * User: kgignatyev
 */
trait Identifiable {

  @mybId var id:Long = -1

}
