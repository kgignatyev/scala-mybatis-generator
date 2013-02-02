package org.kgi.mybatis.scala.generator.doclet

import scala.tools.nsc.doc.doclet._
import tools.nsc.doc.model
import model.{Val, TypeEntity, ValueArgument, MemberEntity}
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

  val nonCollections = List("Char","Double","Float","Int","Long","String","Date","DateTime","Boolean")
  def isCollection(entity: TypeEntity): Boolean = {
    val n = entity.name
    !(nonCollections.contains(n))
  }

  def makeColName(m: MemberEntity): String = {

    m.annotations.find(a => "mybCol".equals(a.name)).map(a => {
      val expr = a.arguments.head.value.expression
      if (expr.contains("$default$")) makeDbName(m.name) else expr
    }).getOrElse(makeDbName(m.name))

  }

  def makeProp2ColMapping(m: MemberEntity): Prop2columnMapping = {

    val resultType: TypeEntity = m.resultType
    val pack = resultType.refEntity
    val typeName = if ( pack.size == 0) resultType.name else {
      pack.head._2._1.qualifiedName
    }
    new Prop2columnMapping(m.name, makeColName(m), typeName)
  }

  def generateDAO(c: model.Class, tableName: String) {
    val gd = new GenerationData()

    gd.entityClassName = c.name
    gd.entityClassPackage = c.toRoot.filter(m => m.isPackage && (!m.isRootPackage)).map(dt => dt.name).reverse.mkString(".")
    gd.tableName = makeTableName(tableName)
    gd.targetPackage = System.getProperty("myb-gen-destination-package",gd.entityClassPackage )
    if( gd.targetPackage.equals(gd.entityClassPackage)){
      println("Property [myb-gen-destination-package] is not defined, generating DAOs next to persistent classes")
    }

    var variables = c.values.filter(m => m.isVar && !isCollection(m.resultType))

    //variables ++= c.linearizationTypes.filter( te => { te.isClass } ).map( te=> te.asInstanceOf[model.Class].members).flatMap(l=>l)

    val idOption = variables.find(v => {
      v.annotations.find(a => "mybId".equals(a.name)).isDefined
    })
    val nonIdProperties = variables.filter(v => {
      v.annotations.find(a => "mybId".equals(a.name)).isEmpty  && v.annotations.find(a => "mybIgnore".equals(a.name)).isEmpty

    })

    val sortProperties = variables.filter(v => {
          v.annotations.find(a => "mybSortBy".equals(a.name)).isDefined
    }).map( v=> (makeColName(v),defineSortOf(v)) )

    if(idOption.isEmpty){
      println("Persistent class must have [mybId] annotation on id field")
    }

    gd.id = idOption.map(m => makeProp2ColMapping(m)).get
    gd.properties ++= variables.map(m => makeProp2ColMapping(m))
    gd.noIdProperties ++= nonIdProperties.map(m => makeProp2ColMapping(m))
    gd.sortBy ++= sortProperties
    gd.sortBy += ((gd.id.colName,"ASC"))
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

  def defineSortOf( v:Val ):String = {
    val annArgs = v.annotations.find(a => "mybSortBy".equals(a.name) ).map( a=> a.arguments )
    annArgs.map( args=> args.head.value).map( treeEnt=> treeEnt.expression ).map( exp=> {
      if ( exp.contains("default")) "ASC" else exp.replaceAll("[\"'*]","")
    }).getOrElse("ASC")
  }

  def investigate(p: model.Package) {
   // println("investigating::" + p)
    p.packages.foreach(mp => {
      investigate(mp)
    })

    p.members.foreach(me => {
      if (me.isInstanceOf[model.Class]) {

        val c = me.asInstanceOf[model.Class]
        me.annotations.find(a => "table".eq(a.annotationClass.name)).foreach(a => {
          println("generating DAO for class:" + me)
          //now we have annotated with table class and can generate dao
          generateDAO(me.asInstanceOf[model.Class], defineTableName(me.name, a.arguments))
        })

      }
    })
  }

}
