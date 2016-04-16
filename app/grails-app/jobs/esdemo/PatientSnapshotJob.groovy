package esdemo

class PatientSnapshotJob {

    def patientQueryUtil

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
            def snapshot = patientQueryUtil.findPatient(it.identifier, it.authority, Long.MAX_VALUE)
            snapshot.save()
        }

        if (events) {
            def maxTime = events.dateCreated*.time.max()
            new File('build/date.txt').text = maxTime
            log.error "Done computing snapshots for ${aggregates.size()} aggregates. Last Event was ${new Date(maxTime)}."
        } else {
            log.error "No new snapshots"
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
