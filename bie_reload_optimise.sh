#!/bin/sh

# bie2 setting
#havester & repoLoader for biocache image
#ulimit -v unlimited
#cd /usr/local/src/havester
#export CLASSPATH=bie-repository-1.0-SNAPSHOT-assembly.jar:.
#java  -Xmx2g -Xms2g -classpath $CLASSPATH org.ala.harvester.BiocacheHarvester

# bie1 setting 
cd /data/src/ala-bie/bie-repository/target
jar xf bie-repository-1.0-SNAPSHOT-assembly.jar lib lib
export CLASSPATH=bie-repository-1.0-SNAPSHOT-assembly.jar:.:lib/*
ulimit -v unlimited
java  -Xmx2g -Xms2g -classpath $CLASSPATH org.ala.harvester.BiocacheHarvester

# bie2 setting
#cd /usr/local/src
#export CLASSPATH=bie-hbase-assembly.jar:.
#java -Xmx2g -Xms2g -classpath $CLASSPATH org.ala.hbase.RepoDataLoader -reindex -biocache

# bie1 setting
cd /data/src/ala-bie/bie-profile/target
jar xf bie-profile-assembly.jar lib lib
export CLASSPATH=bie-profile-assembly.jar:.:lib/*
ulimit -v unlimited
java -Xmx2g -Xms2g -classpath $CLASSPATH org.ala.hbase.RepoDataLoader -reindex -biocache

# bie2 setting
# solr index optimisation
#cd /usr/local/src

# bie1 setting
cd /data/src/ala-bie/bie-webapp
 
# sample url
login_reloadCollections=https://auth.ala.org.au/cas/login?service=http://bie.ala.org.au/admin/reloadCollections
login_reloadInstitutions=https://auth.ala.org.au/cas/login?service=http://bie.ala.org.au/admin/reloadInstitutions
login_reloadDataResources=https://auth.ala.org.au/cas/login?service=http://bie.ala.org.au/admin/reloadDataResources
login_reloadDataProviders=https://auth.ala.org.au/cas/login?service=http://bie.ala.org.au/admin/reloadDataProviders
login_optimise=https://auth.ala.org.au/cas/login?service=http://bie.ala.org.au/admin/optimise

username=your.name@csiro.au
password=password
ws_optimise=http://bie.ala.org.au/admin/optimise
ws_collections=http://bie.ala.org.au/admin/reloadCollections
ws_institutions=http://bie.ala.org.au/admin/reloadInstitutions
ws_providers=http://bie.ala.org.au/admin/reloadDataProviders
ws_resources=http://bie.ala.org.au/admin/reloadDataResources

cookies=cookies.txt

# init session
wget -O /dev/null --no-check-certificate --keep-session-cookies --save-cookies=$cookies --load-cookies=$cookies "${login_reloadCollections}"

# authenicate with CAS server.
wget --no-check-certificate --keep-session-cookies \
	--save-cookies=$cookies --load-cookies=$cookies -O /dev/null \
        --post-data="username=$username&password=$password&lt=e1s1&_eventId=submit&submit=LOGIN" \
        "${login_reloadCollections}"

# call reloadCollections ws.
wget --no-check-certificate --keep-session-cookies \
	--header="Cookie: $cookie" $ws_collections

# init session
wget -O /dev/null --no-check-certificate --keep-session-cookies --save-cookies=$cookies --load-cookies=$cookies "${login_reloadInstitutions}"

# authenicate with CAS server.
wget --no-check-certificate --keep-session-cookies \
	--save-cookies=$cookies --load-cookies=$cookies -O /dev/null \
        --post-data="username=$username&password=$password&lt=e1s1&_eventId=submit&submit=LOGIN" \
        "${login_reloadInstitutions}"

# call reloadInstitutions ws.
wget --no-check-certificate --keep-session-cookies \
	--header="Cookie: $cookie" $ws_institutions

# init session
wget -O /dev/null --no-check-certificate --keep-session-cookies --save-cookies=$cookies --load-cookies=$cookies "${login_reloadDataProviders}"

# authenicate with CAS server.
wget --no-check-certificate --keep-session-cookies \
	--save-cookies=$cookies --load-cookies=$cookies -O /dev/null \
        --post-data="username=$username&password=$password&lt=e1s1&_eventId=submit&submit=LOGIN" \
        "${login_reloadDataProviders}"

# call reloadDataProviders ws.
wget --no-check-certificate --keep-session-cookies \
	--header="Cookie: $cookie" $ws_providers

# init session
wget -O /dev/null --no-check-certificate --keep-session-cookies --save-cookies=$cookies --load-cookies=$cookies "${login_reloadDataResources}"

# authenicate with CAS server.
wget --no-check-certificate --keep-session-cookies \
	--save-cookies=$cookies --load-cookies=$cookies -O /dev/null \
        --post-data="username=$username&password=$password&lt=e1s1&_eventId=submit&submit=LOGIN" \
        "${login_reloadDataResources}"

# call reloadDataResources ws.
wget --no-check-certificate --keep-session-cookies \
	--header="Cookie: $cookie" $ws_resources

# init session
wget -O /dev/null --no-check-certificate --keep-session-cookies --save-cookies=$cookies --load-cookies=$cookies "${login_optimise}"

# authenicate with CAS server.
wget --no-check-certificate --keep-session-cookies \
	--save-cookies=$cookies --load-cookies=$cookies -O /dev/null \
        --post-data="username=$username&password=$password&lt=e1s1&_eventId=submit&submit=LOGIN" \
        "${login_optimise}"

# call optimise ws.
wget --no-check-certificate --keep-session-cookies \
	--header="Cookie: $cookie" $ws_optimise


