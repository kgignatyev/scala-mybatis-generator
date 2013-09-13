package org.kgi.mybatis.scala.generator.doclet

import collection.mutable

/**
 * Created by 
 * User: kgignatyev
 */
class GenerationData {

  var destinationDir = ""
  var tableName = ""
  var targetPackage = ""
  var entityClassPackage = ""
  var entityClassName = ""
  var id:Prop2columnMapping = _
  var manualId = false
  val sortBy = new mutable.ArrayBuffer[(String,String)]()
  val properties = new mutable.ArrayBuffer[Prop2columnMapping]()
  val noIdProperties = new mutable.ArrayBuffer[Prop2columnMapping]()

}

class Prop2columnMapping( val propName:String, val colName:String, val propType:String, val propFullType:String)
