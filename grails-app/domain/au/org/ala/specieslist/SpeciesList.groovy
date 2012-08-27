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

    static hasMany = [items: SpeciesListItem]

    static constraints = {
        username index: 'idx_username'
        url(nullable:true)
        description(nullable: true)
    }

    static mapping = {
        items lazy: false
    }
}
