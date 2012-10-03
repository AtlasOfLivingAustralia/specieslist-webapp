package au.org.ala.specieslist

class QueryService {

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
