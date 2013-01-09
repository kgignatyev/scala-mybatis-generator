package org.kgi.mybatis.scala.generator.doclet

import java.io.{FileWriter, PrintWriter, File}
import collection.mutable.ArrayBuffer

/**
 * Created by 
 * User: kgignatyev
 */
class MyBatisDAOGenerator(gd: GenerationData) {

  val outputDirName = gd.destinationDir + "/" + gd.targetPackage.replace('.', '/')


  def makeDaoFileName(cn: String): String = {
    outputDirName + "/" + cn + "_DAO.scala"
  }

  def writeHeader(out: PrintWriter) {
    out.println("package " + gd.targetPackage +
            """|
              |import org.mybatis.scala.mapping._
              |import %2$s.%1$s
              |import org.mybatis.scala.mapping.Binding._
              |
              |object %1$s_DAO {
              | """.stripMargin.format(gd.entityClassName, gd.entityClassPackage))
  }


  def writeResultMap(out: PrintWriter) {
    out.println("val result_Map = new ResultMap[" + gd.entityClassName + "] {")
    out.println("  id(column = \"%1$s\", property = \"%2$s\")".format(gd.id.colName, gd.id.propName))
    gd.properties.foreach(p => {
      out.println("  result(column = \"%1$s\", property = \"%2$s\") ".format(p.colName, p.propName))
    })
    out.println("}")
    out.println("")
  }

  def writeSelectSQL(out: PrintWriter) {

    out.println( """
                   |
                   |val SELECT_SQL =
                   |    <xsql>
                   |      SELECT *
                   |      FROM %s
                   |    </xsql>
                   |    """.stripMargin.format(gd.tableName))
  }

  def getTypeName(r: String): String = {
    if ("int".equals(r)) "Int" else if ("long".equals(r)) "Long" else if ("double".equals(r)) "Double" else r
  }

  def makeOrderBy():String = {
    if( gd.sortBy.isEmpty) {""} else {
       gd.sortBy.mkString("ORDER BY ",",","")
    }
  }

  def generateEqFinder(out: PrintWriter, p: Prop2columnMapping) {
    val ord = makeOrderBy()
    out.println( """
                   |val find%1$sBy_%2$s = new SelectOneBy[%4$s, %1$s] {
                   |    resultMap = result_Map
                   |
                   |    def xsql =
                   |      <xsql>
                   |        {SELECT_SQL}
                   |        WHERE %3$s = {"%2$s" ?}
                   |        %5$s
                   |      </xsql>
                   |  }
                   |  """.stripMargin.format(gd.entityClassName, p.propName, p.propName, p.propType, ord))
  }

  def generateLikeFinder(out: PrintWriter, p: Prop2columnMapping) {
    out.println( """
                   |val find%1$s_%2$s_like = new SelectOneBy[%4$s, %1$s] {
                   |    resultMap = result_Map
                   |
                   |    def xsql =
                   |      <xsql>
                   |        {SELECT_SQL}
                   |        WHERE %3$s LIKE {"%2$s" ?}
                   |        %5$s
                   |      </xsql>
                   |  }
                   |  """.stripMargin.format(gd.entityClassName, p.propName, p.colName, getTypeName(p.propType), makeOrderBy()))
  }


  def writeDelete(out: PrintWriter) {
    out.println(
      """
        |
        | val delete%1$sById = new Delete[Long]{
        |    def xsql =
        |          <xsql>
        |            DELETE FROM %2$s
        |            WHERE id = {"id" ?}
        |          </xsql>
        |  }
        |
      """.stripMargin.format(gd.entityClassName, gd.tableName))
  }

  def writeInsert(out: PrintWriter) {
    out.println( """
                   |val insert = new Insert[%4$s] {
                   |def xsql =
                   |      <xsql>
                   |        INSERT INTO %1$s( %2$s )
                   |        VALUES ( %3$s )
                   |      </xsql>
                   |
                   |    keyGenerator = new SqlGeneratedKey[Long] {
                   |      keyProperty = "%5$s"
                   |
                   |      def xsql = "SELECT LAST_INSERT_ID()"
                   |    }
                   |}
                   | """.stripMargin.format(gd.tableName,
      gd.noIdProperties.map(f => f.colName).mkString(", "),
      gd.noIdProperties.map(f => "{ \"" + f.propName + "\" ?}").mkString(", "),
      gd.entityClassName,
      gd.id.propName
    ))
  }

  def writeUpdate(out: PrintWriter) {
    out.println( """
                   |
                   |val update = new Update[%1$s]{
                   |    def xsql = <xsql>
                   |      UPDATE %2$s SET
                   |      %3$s
                   |      WHERE %4$s = {"%5$s"?}
                   |    </xsql>
                   |  }
                 """.stripMargin.format(gd.entityClassName, gd.tableName,
      gd.noIdProperties.map(f => f.colName + " = { \"" + f.propName + "\" ?}").mkString(",\n"),
      gd.id.colName, gd.id.propName
    )
    )
  }

  def generateBind(out: PrintWriter, fields: Iterable[Prop2columnMapping], stringFields: Iterable[Prop2columnMapping]) {
    """val bind = Seq(findPersonById, findPersonByUserID,findPersonByEmail,
      |    findPeopleWithEmailLike,
      |    deleteUserById, deleteUserSelections,deleteUserSelection,
      |    insertPerson,insertUserSelection,findUserSelection,
      |    updatePerson)
      | """

    out.println("val bind = Seq(")
    out.println("insert,delete%1$sById,update,".format(gd.entityClassName))
    val items: Iterable[String] = fields.map(f => {
      "find%1$sBy_%2$s".format(gd.entityClassName, f.propName)
    })

    val items2: Iterable[String] = stringFields.map(f => {
      "find%1$s_%2$s_like".format(gd.entityClassName, f.propName)
    })

    out.println(List(items, items2).flatMap(f => f).mkString(", \n"))

    out.println(")")
  }

  def generateDAO() {
    val outputDir = new File(outputDirName)
    if (!outputDir.exists()) {
      println("creating directory::" + outputDirName)
      outputDir.mkdirs()
    }
    val daoFileName: String = makeDaoFileName(gd.entityClassName)
    println("writing DAO to file:" + daoFileName)
    val out = new PrintWriter(new FileWriter(daoFileName))
    writeDAOContent(out)
    out.flush
    out.close
  }


  def writeDAOContent(out: PrintWriter) {
    writeHeader(out)
    writeResultMap(out)
    writeSelectSQL(out)
    writeInsert(out)
    writeUpdate(out)
    writeDelete(out)


    gd.properties.foreach(p => {
      generateEqFinder(out, p)
    })

    val stringProperties: ArrayBuffer[Prop2columnMapping] = gd.properties.filter(f => f.propType.endsWith("String"))
    stringProperties.foreach(f => {
      generateLikeFinder(out, f)
    })

    generateBind(out, gd.properties, stringProperties)

    out.println("}")
  }
}
