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



    static hasMany = [items: SpeciesListItem]

    static constraints = {

        url(nullable:true)
        description(nullable: true)
        listType nullable: true, index: 'idx_listtype'
    }

    static mapping = {
        items lazy: false
        listType index: 'idx_listtype'
        username index: 'idx_username'
    }



}
