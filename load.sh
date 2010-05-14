start_time=$(date +%s)

echo "LOAD : running processing $('date')"

mvn clean package -DskipTests=true

cd target

jar xf bie-hbase-assembly.jar lib lib
export CLASSPATH=bie-hbase-assembly.jar:$HBASE_HOME/conf

echo "LOAD : creating lucene indexes for concept lookups $('date')"
java -classpath $CLASSPATH au.org.ala.checklist.lucene.CBCreateLuceneIndex /data/bie-staging/checklistbank/ /data/lucene/namematching

echo "LOAD : initialising HBase $('date')"
java -classpath $CLASSPATH org.ala.hbase.InitProfiler

echo "LOAD : running checklistbank data load $('date')"
java -classpath $CLASSPATH org.ala.hbase.ChecklistBankLoader

echo "LOAD : creating loading indicies $('date')"
java -classpath $CLASSPATH org.ala.lucene.CreateLoadingIndex

echo "LOAD : running ANBG data load $('date')"
java -classpath $CLASSPATH org.ala.hbase.ANBGDataLoader

echo "LOAD : running Col Names Processing $('date')"
java -classpath $CLASSPATH org.ala.preprocess.ColFamilyNamesProcessor

echo "LOAD : running Create Taxon Concept Index $('date')"
java -classpath $CLASSPATH org.ala.lucene.CreateTaxonConceptIndex

echo "LOAD : running Repository Data Loader $('date')"
java -Xmx1g -Xms1g -classpath $CLASSPATH -Xmx1g -Xms1g org.ala.hbase.RepoDataLoader

echo "LOAD : running Bio Cache Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.BioCacheLoader

echo "LOAD : running Irmng Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.IrmngDataLoader

echo "LOAD : running BHL Data Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.BHLDataLoader

echo "LOAD : running Re-Create Taxon Concept Index $('date')"
java -classpath $CLASSPATH org.ala.lucene.CreateTaxonConceptIndex

echo "LOAD : processing complete at $('date')"

finish_time=$(date +%s)

echo "LOAD : Time taken: $(( $((finish_time - start_time)) /3600 )) hours $(( $((finish_time - start_time)) /60 )) minutes."
