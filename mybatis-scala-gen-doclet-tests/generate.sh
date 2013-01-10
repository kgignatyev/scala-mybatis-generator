cd ../mybatis-scala-gen-doclet;mvn install;cd -
export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
cp -f generate-options-seed.txt generate-options.txt
find src -name "*.scala" >> generate-options.txt


scaladoc  -Dmyb-gen-destination=generated -toolcp ../mybatis-scala-gen-doclet/target/mybatis-scala-gen-doclet-1.0-SNAPSHOT.jar @generate-options.txt