echo "BULK DELETE : running processing $('date')"
cd target
jar xf bie-hbase-assembly.jar lib lib
export CLASSPATH=bie-hbase-assembly.jar:$HBASE_HOME/conf
args=("$@")
echo "args = $@"
java -Xms128m -Xmx512m -classpath $CLASSPATH org.ala.util.CassandraBatchDelete "$@"
