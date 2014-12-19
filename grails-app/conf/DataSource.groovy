dataSource {
    pooled = true
    logSql = false
//    driverClassName = "com.mysql.jdbc.Driver"
    username = ""
    password = ""
    properties {
        maxActive = -1
        minEvictableIdleTimeMillis=1800000
        timeBetweenEvictionRunsMillis=1800000
        numTestsPerEvictionRun=3
        testOnBorrow=true
        testWhileIdle=true
        testOnReturn=true
        validationQuery="SELECT 1"
    }
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
//        dataSource {
//            //dialect = org.hibernate.dialect.MySQL5InnoDBDialect
//            //dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
//            //url = "jdbc:mysql://localhost/specieslist?autoReconnect=true&connectTimeout=0"
//        }
    }
    test {
        dataSource {
            dialect = "org.hibernate.dialect.H2Dialect"
            dbCreate = "create-drop"
            driverClassName = "org.h2.Driver"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE;"
        }
    }
    production {
        dbCreate = "update"
        // must be set via external config
    }
}
