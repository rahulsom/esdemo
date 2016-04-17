package esdemo

class PatientSnapshotJob {

    PatientQueryUtil patientQueryUtil

    static triggers = {
        simple repeatInterval: 30000l
    }

    def execute() {
        log.debug "Starting..."
        def threshold = lastEventDate
        log.debug "Threshold is $threshold"

        def events = lastEventDate ?
                PatientEvent.findAllByDateCreatedGreaterThan(lastEventDate) :
                PatientEvent.list()

        log.debug "Aggregating ${events.size()} events"

        def aggregates = events*.aggregate.unique()
        log.debug "Aggregating ${aggregates.size()} aggregates"

        aggregates.each {
            def snapshot = patientQueryUtil.computeSnapshot(it, Long.MAX_VALUE)
            log.debug "Id before save: ${snapshot.id}"
            snapshot.save()
            log.info "Id after save: ${snapshot.id}"
        }

        if (events) {
            def maxTime = events.dateCreated*.time.max()
            new File('build/date.txt').text = maxTime
            log.info "Done computing snapshots for ${aggregates.size()} aggregates. Last Event was ${new Date(maxTime)}."
        } else {
            log.debug "No new snapshots"
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
