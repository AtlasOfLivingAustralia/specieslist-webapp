mkdir -p /data/bie
mkdir -p /data/bie-staging
mysql -u root -ppassword < db/bie.sql
mysql -u root -ppassword < db/infosources.sql
mysql -u root -ppassword < db/harvesters.sql
mysql -u root -ppassword < db/vocabularies.sql
