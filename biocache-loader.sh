mvn clean 
mvn assembly:assembly -Dmaven.test.skip=true -npu -o
cd target
export CLASSPATH=bie-profile-assembly.jar
jar xf $CLASSPATH lib lib
ulimit -v unlimited
java -Xmx2g -Xms2g -classpath $CLASSPATH org.ala.hbase.RepoDataLoader -reindex -biocache