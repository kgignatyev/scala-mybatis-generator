package org.kgi.mybatis.scala.generator.doclet

import scala.tools.nsc.doc.doclet._
import tools.nsc.doc.model

/**
 * Created by 
 * User: kgignatyev
 */
class MyBatisMappingDoclet extends Generator with Universer with Indexer {

  protected def generateImpl() {
    println("+++++++++++++++++++++++++++++")
     val u = universe
    investigate(u.rootPackage)

  }


  def investigate( p:model.Package) {
    println("investigating::" + p)
      p.packages.foreach( mp =>{
         investigate( mp )
      })
      p.members.foreach(me=>{
        println(me)
        me.annotations.foreach(a=>{
          println("ann::"+ a)
          a.arguments.foreach( arg=>{
            println( "arg::" + arg)
          })
        })

      })
  }

}
