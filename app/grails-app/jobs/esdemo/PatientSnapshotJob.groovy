package esdemo
import grails.compiler.GrailsCompileStatic
class PatientSnapshotJob {
    def patientQueryUtil
    static triggers = {
        simple repeatInterval: 30000l
    }
    @GrailsCompileStatic
    def execute() {
        log.debug "Starting..."
        def threshold = lastEventDate
        log.debug "Threshold is $threshold"
        List<PatientEvent> events = lastEventDate ?
                PatientEvent.findAllByDateCreatedGreaterThan(lastEventDate) :
                PatientEvent.list()
        log.debug "Aggregating ${events.size()} events"
        def aggregates = events*.aggregate.unique()
        log.debug "Aggregating ${aggregates.size()} aggregates"
        aggregates.each {
            (patientQueryUtil as PatientQueryUtil).computeSnapshot(it, Long.MAX_VALUE).ifPresent { PatientSnapshot snapshot ->
                log.debug "Id before save: ${snapshot.id}"
                snapshot.save()
                log.info "Id after save: ${snapshot.id}"
            }
        }
        if (events) {
            def maxTime = events.dateCreated*.time.max()
            new File('build/date.txt').text = maxTime
            log.info "Done computing snapshots for ${aggregates.size()} aggregates. Last Event was ${new Date(maxTime)}."
        } else {
            log.debug "No new snapshots"
        }
    }
    @GrailsCompileStatic
    Date getLastEventDate() {
        def dateFile = new File('build/date.txt')
        if (dateFile.exists() && dateFile.text.isLong()) {
            new Date(dateFile.text.toLong())
        } else {
            null
        }
    }
}
