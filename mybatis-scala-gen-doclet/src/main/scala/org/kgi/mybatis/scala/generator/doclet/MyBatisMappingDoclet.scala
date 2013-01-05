package org.kgi.mybatis.scala.generator.doclet

import scala.tools.nsc.doc.doclet._
import tools.nsc.doc.model
import model.MemberEntity


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


  def investigate(p: model.Package) {
    println("investigating::" + p)
    p.packages.foreach(mp => {
      investigate(mp)
    })

    p.members.foreach(me => {
      println(me)

      if (me.isInstanceOf[model.Class]) {
        println("class:" + me)
        val c = me.asInstanceOf[model.Class]
        c.members.foreach(e => {
          if (!e.annotations.isEmpty) {
            if (e.isVar) {
              //var i =  doclet.this.mybSort.init$default$1
              println("" + e + " member ann::" + e.annotations.mkString("|"))
              e.annotations.foreach(a => {
                println("ann arguments:")
                a.arguments.foreach(arg => {
                  println("\targ:" + arg)
                  println("\targ.value:" + arg.value)
                })
              })
            }
          }
        })

      }

      me.annotations.foreach(a => {
        println("ann::" + a)
        a.arguments.foreach(va => {
          println("arg:" + va.value)

          va.parameter.foreach(p => {
            println("param::" + p.name + " ")
          })
        })

      })

    })
  }

}
