start_time=$(date +%s)

mvn clean package -DskipTests=true

cd target

jar xf bie-hbase-assembly.jar lib lib

export CLASSPATH=bie-hbase-assembly.jar

# echo "LOAD INDEX : running checklistbank data load $('date')"
#java -Xmx1g -Xms1g -classpath $CLASSPATH org.ala.hbase.ChecklistBankLoader

echo "LOAD INDEX : running Create Search Indexes from BIE for the Web Application $('date')"
java -classpath $CLASSPATH org.ala.lucene.CreateSearchIndex

echo "LOAD INDEX : running Create Search Indexes from External databases for the Web Application $('date')"
java -classpath $CLASSPATH org.ala.lucene.ExternalIndexLoader

echo "LOAD INDEX : Index creation complete $('date')"

finish_time=$(date +%s)

echo "LOAD INDEX : Time taken: $(( $((finish_time - start_time)) /3600 )) hours $(( $((finish_time - start_time)) /60 )) minutes."