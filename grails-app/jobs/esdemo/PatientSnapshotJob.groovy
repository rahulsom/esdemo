package esdemo

class PatientSnapshotJob {

    static triggers = {
        simple repeatInterval: 30000l
    }

    def execute() {
        log.error "Starting..."
        def threshold = lastEventDate
        log.error "Threshold is $threshold"

        def events = lastEventDate ?
                PatientEvent.findAllByDateCreatedGreaterThan(lastEventDate) :
                PatientEvent.list()

        log.error "Aggregating ${events.size()} events"

        def aggregates = events*.aggregate.unique()
        log.error "Aggregating ${aggregates.size()} aggregates"

        aggregates.each {
            def snapshot = PatientQueryUtil.findPatient(it.identifier, it.authority, Long.MAX_VALUE)
            snapshot.save()
        }

        if (events) {
            def maxTime = events.dateCreated*.time.max()
            new File('build/date.txt').text = maxTime
        }
    }

    Date getLastEventDate() {
        def dateFile = new File('build/date.txt')
        if (dateFile.exists() && dateFile.text.isLong()) {
            new Date(dateFile.text.toLong())
        } else {
            null
        }
    }
}
