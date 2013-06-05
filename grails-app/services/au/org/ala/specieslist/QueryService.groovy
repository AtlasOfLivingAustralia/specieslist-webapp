package au.org.ala.specieslist

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

class QueryService {
    def authService

    /** A regular expression to split the  */
    public final def filterRegEx = /([a-zA-Z]+):([\x00-\x7F\s]*)/
    /** A map of domain property names to data types */
    def speciesListProperties= new DefaultGrailsDomainClass(SpeciesList.class).persistentProperties.collectEntries{[it.name, it.type]}
    /** A map of criteria method names to Methods - allow the use of reflection to gain the criteria method to use */
    def criteriaMethods = grails.orm.HibernateCriteriaBuilder.class.getMethods().findAll{it.getParameterTypes().length<3}.collectEntries{[it.name, it]}
    /**
     * retrieves the lists that obey the supplied filters
     * @param params
     */
    def getFilterListResult(params){
        //list should be based on the user that is logged in
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ?: "listName"
        params.fetch = [items: 'lazy']

        def c = SpeciesList.createCriteria()

        def lists =c.list(params){
            and{
                params.each {key, value ->
                    //the value suffix tells us which filter operation to perform
                    def matcher = (value =~ filterRegEx)
                    if(matcher.matches()){
                        def fvalue = matcher[0][2]
                        //handle the situation where the enum needs to be replaced
                        if (key == 'listType')
                            fvalue = ListType.valueOf(fvalue)
                        //now handle the supported filter conditions
                        switch(matcher[0][1]){
                            case "eq":eq(key,fvalue)
                        }
                    }
                }
            }
        }
        //remove the extra condition "fetch" condition
        params.remove('fetch')
        lists
    }
    /**
     * retrieves the species list items that obey the supplied fiilters.
     *
     * When a distinct field is provided the values of the field are returned rather than a SpeciesListItem
     * @param props
     * @param params
     * @param guid
     * @param lists
     * @param distinctField
     * @return
     */
    def getFilterListItemResult(props, params, guid, lists, distinctField){
        def c = SpeciesListItem.createCriteria()

        //log.debug("CRITERIA METHODS: " +criteriaMethods)
        c.list(props){
            //set the results transformer so that we don't get duplicate records because of the 1:many relationship between a list item and KVP
            setResultTransformer(org.hibernate.criterion.CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            and{
                if(distinctField){
                    distinct(distinctField)
                    isNotNull(distinctField)
                }
                if(guid)eq('guid', guid)
                if(lists){
                    'in'('dataResourceUid', lists)
                }

                params.each {key, value ->
                    log.debug("KEYS: " +key+" " + speciesListProperties.containsKey(key))
                    if(speciesListProperties.containsKey(key)){
                        mylist{
                            //the value suffix tells us which filter operation to perform
                            def matcher = (value =~ filterRegEx)
                            if(matcher.matches()){
                                def fvalue = matcher[0][2] //
                                //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                                def method =criteriaMethods.get(matcher[0][1])
                                if(method){
                                    Object[] args =[getValueBasedOnType(speciesListProperties[key],fvalue)]
                                    if(method.getParameterTypes().size()>1)
                                        args = [key] +args[0]
                                    //log.debug("ARGS : " +args + " method : " + method)
                                    method.invoke(c, args)
                                }
                            }
                        }
                    }
                    else{
                        //the value suffix tells us which filter operation to perform
                        def matcher = (value =~ filterRegEx)
                        if(matcher.matches()){
                            def fvalue = matcher[0][2]

                            //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                            def method =criteriaMethods.get(matcher[0][1])
                            if(method){
                                Object[] args =[getValueBasedOnType(speciesListProperties[key],fvalue)]
                                if(method.getParameterTypes().size()>1)
                                     args = [key] +args
                                method.invoke(c, args)
                            }
//                            switch(matcher[0][1]){
//                                case "eq":eq(key,getValueBasedOnType(speciesListProperties[key],fvalue));break;
//                                case "isNull":isNull(key);break;
//
//                            }
                        }
                    }
                }
            }
        }
    }

    def getValueBasedOnType(type, value){
        switch(type){
            case Boolean.class: Boolean.parseBoolean(value); break;
            case ListType.class: ListType.valueOf(value); break;
            default:value; break;
        }
    }

    def constructWithFacets(String base, List facets, String dataResourceUid) {
        StringBuilder query = new StringBuilder(base)
        StringBuilder whereBuilder = new StringBuilder(" where sli.dataResourceUid=? ")
        //query.append(" from SpeciesListItem sli join sli.kvpValues kvp where sli.dataResourceUid=? ")
        def queryparams = [dataResourceUid]
        if(facets){

        facets.eachWithIndex { facet, index ->
            if(facet.startsWith("kvp")){
                String sindex = index.toString();
                facet = facet.replaceFirst("kvp ","")
                String key = facet.substring(0,facet.indexOf(":"))
                String value = facet.substring(facet.indexOf(":")+1)
                query.append(" join sli.kvpValues kvp").append(sindex)
                whereBuilder.append(" AND kvp").append(sindex).append(".key=? AND kvp").append(sindex).append(".value=?")
                queryparams.addAll([key, value])
                //println queryparams
            }
            else{
                //must be a facet with the same table
                String key = facet.substring(0,facet.indexOf(":"))
                String value = facet.substring(facet.indexOf(":")+1)
                whereBuilder.append( "AND sli.").append(key)
                if(value.equalsIgnoreCase("null")){
                    whereBuilder.append(" is null")
                }
                else{
                    whereBuilder.append("=?")
                    queryparams.add(value)
                }
            }
        }
        }
        query.append(whereBuilder.toString())
        log.debug(println(query.toString()))
         //println(queryparams)
        [query.toString(), queryparams]

    }
}
