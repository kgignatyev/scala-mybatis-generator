cd ../mybatis-scala-gen-doclet;mvn install;cd -
export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
scaladoc   -toolcp ../mybatis-scala-gen-doclet/target/mybatis-scala-gen-doclet-1.0-SNAPSHOT.jar @generate-options.txt