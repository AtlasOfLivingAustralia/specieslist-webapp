package au.org.ala.specieslist

class SpeciesList {
    String listName
    String firstName
    String surname
    String username
    String dataResourceUid

    static hasMany = [items: SpeciesListItem]

    static constraints = {
        username index: 'idx_username'
    }

    static mapping = {
        items lazy: false
    }
}
