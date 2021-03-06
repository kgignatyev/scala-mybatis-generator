package org.kgi.mybatis.scala.generator.doclet

import scala.tools.nsc.doc.doclet._
import tools.nsc.doc.model
import scala.tools.nsc.doc.model._
import java.io.{PrintWriter, StringWriter}
import scala.collection.{SortedMap}
import scala.tools.nsc.doc.base.Tooltip


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
    annotationArguments.filter(tn=> tn.value.expression.indexOf("$default")== -1).headOption.map( tn=> tn.value.expression.replaceAll("['\"]","") ).getOrElse( makeTableName(className) )
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
    val pack:SortedMap[Int,(scala.tools.nsc.doc.base.LinkTo, Int)] = resultType.refEntity
    val fullTypeName = pack(0)._1.asInstanceOf[Tooltip].name
    val typeName = resultType.name
    new Prop2columnMapping(m.name, makeColName(m), typeName, fullTypeName)
  }

  def generateDAO(c: model.Class, tableName: String) {
    val gd = new GenerationData()

    gd.entityClassName = c.name
    gd.entityClassPackage = c.toRoot.filter(m => m.asInstanceOf[DocTemplateEntity].isPackage && (!m.asInstanceOf[DocTemplateEntity].isRootPackage)).map(dt => dt.name).reverse.mkString(".")
    gd.tableName = makeTableName(tableName)
    gd.targetPackage = System.getProperty("myb-gen-destination-package",gd.entityClassPackage )
    if( gd.targetPackage.equals(gd.entityClassPackage)){
      println("Property [myb-gen-destination-package] is not defined, generating DAOs next to persistent classes")
    }

    var variables = c.asInstanceOf[DocTemplateEntity].values.filter(m => m.isVar && !isCollection(m.resultType))

    //variables ++= c.linearizationTypes.filter( te => { te.isClass } ).map( te=> te.asInstanceOf[model.Class].members).flatMap(l=>l)

    val idOption = variables.find(v => {
      v.annotations.find(a => "mybId".equals(a.name)).isDefined
    })
    val nonIdProperties = variables.filter(v => {
      v.annotations.find(a => "mybId".equals(a.name)).isEmpty  && v.annotations.find(a => "mybIgnore".equals(a.name)).isEmpty

    }).sortBy( p=> p.name)

    val sortProperties = variables.filter(v => {
          v.annotations.find(a => "mybSortBy".equals(a.name)).isDefined
    }).map( v=> (makeColName(v),defineSortOf(v)) )

    if(idOption.isEmpty){
      println("Persistent class must have [mybId] annotation on id field")
    }

    gd.id = idOption.map(m => makeProp2ColMapping(m)).get
    val idAnnotation = idOption.get.annotations.find(a => "mybId".equals(a.name))
    val b = idAnnotation.map( a=> a.arguments.exists(arg=> "true".equalsIgnoreCase(arg.value.expression))).getOrElse(false)
    gd.manualId = b
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
