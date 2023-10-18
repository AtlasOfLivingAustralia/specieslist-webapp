package au.org.ala.specieslist

class RematchLog {
    static transients = [ "saveToDB" ]

    boolean saveToDB
    String byWhom
    Date startTime
    Date endTime
    Date recentProcessTime

    int total
    int remaining
    String status
    String logs
    // the id of species list item was just processed
    // It is used to select those which are not matched yet.
    long currentRecordId

    static constraints = {
        endTime(nullable: true)
        recentProcessTime(nullable: true)
        status(nullable: true)
        logs(nullable: true)
    }

    def persist() {
        if (this.saveToDB) {
            this.save()
        }
    }

    def toMap() {
        this.class.declaredFields.findAll { it.modifiers == java.lang.reflect.Modifier.PRIVATE }.
                collectEntries { [it.name, this[it.name]] }
    }
}
