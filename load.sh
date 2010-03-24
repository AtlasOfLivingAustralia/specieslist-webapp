echo "LOAD : running processing $('date')"

mvn clean install jar:jar -Dmaven.test.skip=true

mvn dependency:build-classpath

export CLASSPATH=.:target/bie-hbase.jar:$(cat classpath.txt)

echo "LOAD : initialising HBase $('date')"
java -classpath $CLASSPATH org.ala.hbase.InitProfiler

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

echo "LOAD : running Bio Cache Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.BHLDataLoader

echo "LOAD : running DwC Classification Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.DwcClassificationLoader

echo "LOAD : running Re-Create Taxon Concept Index $('date')"
java -classpath $CLASSPATH org.ala.lucene.CreateTaxonConceptIndex

echo "LOAD : processing complete at $('date')"