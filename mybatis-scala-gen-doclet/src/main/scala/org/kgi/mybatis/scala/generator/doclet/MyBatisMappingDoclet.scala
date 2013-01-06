package org.kgi.mybatis.scala.generator.doclet

import scala.tools.nsc.doc.doclet._
import tools.nsc.doc.model
import model.{TypeEntity, ValueArgument, MemberEntity}
import java.io.{PrintWriter, StringWriter}


/**
 * Created by 
 * User: kgignatyev
 */
class MyBatisMappingDoclet extends Generator with Universer with Indexer {

  protected def generateImpl() {
    investigate(universe.rootPackage)
  }

  def makeDbName(s: String): String = {
    s.replaceAll("([A-Z])", "_$1").toLowerCase
  }

  def makeTableName(s: String): String = {
    makeDbName(s.head.toLower + s.substring(1))
  }

  def defineTableName(className: String, annotationArguments: List[ValueArgument]): String = {
    makeTableName(className)
  }

  def isCollection(entity: TypeEntity): Boolean = {
    val n = entity.name
    !("Long".equals(n) || "String".equals(n) || "Int".equals(n))
  }

  def makeColName(m: MemberEntity): String = {

    m.annotations.find(a => "mybCol".equals(a.name)).map(a => {
      val expr = a.arguments.head.value.expression
      if (expr.contains("$default$")) makeDbName(m.name) else expr
    }).getOrElse(makeDbName(m.name))

  }

  def makeProp2ColMapping(m: MemberEntity): Prop2columnMapping = {
    new Prop2columnMapping(m.name, makeColName(m), m.resultType.name)
  }

  def generateDAO(c: model.Class, tableName: String) {
    val gd = new GenerationData()

    gd.entityClassName = c.name
    gd.entityClassPackage = c.toRoot.filter(m => m.isPackage && (!m.isRootPackage)).map(dt => dt.name).reverse.mkString(".")
    gd.tableName = makeTableName(tableName)
    gd.targetPackage = gd.entityClassPackage

    val variables = c.members.filter(m => m.isVar && !isCollection(m.resultType))
    val idOption = variables.find(v => {
      v.annotations.find(a => "mybId".equals(a.name)).isDefined
    })
    val nonIdProperties = variables.filter(v => {
      v.annotations.find(a => "mybId".equals(a.name)).isEmpty
    })


    gd.id = idOption.map(m => makeProp2ColMapping(m)).get
    gd.properties ++= nonIdProperties.map(m => makeProp2ColMapping(m))
    gd.destinationDir = System.getProperty("myb-gen-destination", "")
    val g = new MyBatisDAOGenerator(gd)
    if ("".equals(gd.destinationDir)) {
      println("Property  [myb-gen-destination] is not defined therefore use stdo")
      val buf = new StringWriter()
      g.writeDAOContent(new PrintWriter(buf))
      println("\n\n\n\n" + buf)
    }else{
      g.generateDAO()
    }
  }

  def investigate(p: model.Package) {
    println("investigating::" + p)
    p.packages.foreach(mp => {
      investigate(mp)
    })

    p.members.foreach(me => {
      if (me.isInstanceOf[model.Class]) {
        println("class:" + me)
        val c = me.asInstanceOf[model.Class]
        me.annotations.find(a => "table".eq(a.annotationClass.name)).foreach(a => {
          //now we have annotated with table class and can generate dao
          generateDAO(me.asInstanceOf[model.Class], defineTableName(me.name, a.arguments))
        })

      }
    })
  }

}
