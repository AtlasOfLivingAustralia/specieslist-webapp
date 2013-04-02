package au.org.ala.specieslist

class SpeciesList {
    def authService

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

    static transients = [ "fullName" ]

    static hasMany = [items: SpeciesListItem, editors: String]

    static constraints = {

        url(nullable:true)
        description(nullable: true)
        listType nullable: true, index: 'idx_listtype'
        isPrivate nullable:true, index: 'idx_listprivate'
        firstName nullable: true
        surname nullable: true
        editors nullable: true
    }

    static mapping = {
        items cascade: "all-delete-orphan"
        listType index: 'idx_listtype'
        username index: 'idx_username'
    }

    def String getFullName(){
        authService.getDisplayNameFor(username)
    }

}
