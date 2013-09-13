if [ "$1" = "debug" ]
then
 export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
else
 export JAVA_OPTS=""
fi

cd ../mybatis-scala-gen-doclet;mvn install;cd -

cp -f generate-options-seed.txt generate-options.txt
find src -name "*.scala" >> generate-options.txt


scaladoc  -Dmyb-gen-destination=generated -toolcp ../mybatis-scala-gen-doclet/target/mybatis-scala-gen-doclet-1.0-2.10-SNAPSHOT.jar @generate-options.txt