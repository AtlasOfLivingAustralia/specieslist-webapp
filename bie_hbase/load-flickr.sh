start_time=$(date +%s)

echo "LOAD : running processing $('date')"

cd target

jar xf bie-hbase-assembly.jar lib lib

export CLASSPATH=bie-hbase-assembly.jar

echo "LOAD : running Repository Data Loader $('date')"
java -Xmx1g -Xms1g -classpath $CLASSPATH org.ala.hbase.RepoDataLoader 1013

echo "LOAD : processing complete at $('date')"

finish_time=$(date +%s)

echo "LOAD : Time taken: $(( $((finish_time - start_time)) /3600 )) hours $(( $((finish_time - start_time)) /60 )) minutes."
