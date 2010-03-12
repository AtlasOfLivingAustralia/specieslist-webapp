echo "running processing $('date')"

mvn clean install jar:jar -npu -o -Dmaven.test.skip=true

mvn dependency:build-classpath -npu -o

export CLASSPATH=target/bie-hbase.jar:$(cat classpath.txt)

echo "initialising HBase $('date')"
java -classpath $CLASSPATH org.ala.hbase.InitProfiler

echo "creating loading indicies $('date')"
java -classpath $CLASSPATH org.ala.lucene.CreateLoadingIndex

echo "running ANBG data load $('date')"
java -classpath $CLASSPATH org.ala.hbase.ANBGDataLoader

echo "running Col Names Processing $('date')"
java -classpath $CLASSPATH org.ala.preprocess.ColFamilyNamesProcessor

echo "running Repository Data Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.RepoDataLoader

echo "running Bio Cache Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.BioCacheLoader

echo "running DwC Classification Loader $('date')"
java -classpath $CLASSPATH org.ala.hbase.DwcClassificationLoader

echo "running Create Taxon Concept Index $('date')"
java -classpath $CLASSPATH org.ala.lucene.CreateTaxonConceptIndex

echo "processing complete at $('date')"