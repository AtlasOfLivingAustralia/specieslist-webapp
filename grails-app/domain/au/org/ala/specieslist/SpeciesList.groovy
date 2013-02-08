package au.org.ala.specieslist

class SpeciesList {
    String listName
    String firstName
    String surname
    String username
    String dataResourceUid
    String description
    String url
    Date dateCreated
    Date lastUpdated
    ListType listType
    Boolean isPrivate



    static hasMany = [items: SpeciesListItem]

    static constraints = {

        url(nullable:true)
        description(nullable: true)
        listType nullable: true, index: 'idx_listtype'
        isPrivate nullable:true, index: 'idx_listprivate'
        firstName nullable: true
        surname nullable: true
    }

    static mapping = {

        listType index: 'idx_listtype'
        username index: 'idx_username'
    }



}
