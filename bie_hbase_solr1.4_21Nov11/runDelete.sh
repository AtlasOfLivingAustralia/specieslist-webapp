echo "BULK DELETE : running processing $('date')"
args=("$@")
echo "args = $@"
cd target
jar xf bie-hbase-assembly.jar lib lib
export CLASSPATH=bie-hbase-assembly.jar
java -Xms128m -Xmx512m -classpath $CLASSPATH org.ala.util.CassandraBatchDelete "$@"
