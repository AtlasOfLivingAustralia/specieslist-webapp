package au.org.ala.specieslist

import grails.web.JSONBuilder

/*
Provides service to turn domain classes into builders that can be used to render JSON
 */
class JsonService {

     def convertListAsSummary(SpeciesList sl){
         def builder = new JSONBuilder()

         def retValue = builder.build{
             dataResourceUid:sl.dataResourceUid
             listName:sl.listName
             listType:sl.listType
         }
         return retValue
     }


}
