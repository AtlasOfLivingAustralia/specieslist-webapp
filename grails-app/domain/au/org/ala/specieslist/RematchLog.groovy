package au.org.ala.specieslist

class RematchLog {

    String byWhom
    Date startTime
    Date endTime
    Date latestProcessingTime
    String status
    String processing // e.g. "2/3000" the 2nd list of 3000 lists
    String history

    List logs = []

    static constraints = {
        endTime(nullable: true)
        status(nullable: true)
        history(nullable: true)
    }

    static transients = ['logs'] // Transient property for the list

    static mapping = {
        history type: 'text'
    }

    def beforeInsert() {
        history = logs.join('|')
    }

    def beforeUpdate() {
        history = logs.join('|')
    }

    def afterLoad() {
        if (history) {
            logs = history.split(/\|/)
        }
    }

    void appendLog(String log) {
        logs << log
    }


    def toMap() {
        def map = [
                id: id,
                byWhom: byWhom,
                startTime: startTime,
                latestProcessingTime: latestProcessingTime,
                endtime: endTime,
                status: status,
                processing: processing,
                logs : logs,
        ]
    }
}
