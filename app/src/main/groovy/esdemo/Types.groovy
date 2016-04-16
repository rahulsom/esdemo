package esdemo

interface Aggregate {

}

interface Event<A extends Aggregate> {
    A getAggregate()
    abstract String getAudit()

    Date getDateCreated()
    String getCreatedBy()

    Long getRevertedBy()
    void setRevertedBy(Long revertEventId)

    Long getId()
}

interface RevertEvent<A extends Aggregate> extends Event<A> {
    Event<A> getEvent()
}

interface Deprecates<A extends Aggregate> extends Event<A> {
    DeprecatedBy<A> getConverse()
    A getDeprecated()
}

interface DeprecatedBy<A extends Aggregate> extends Event<A> {
    Deprecates<A> getConverse()
    A getDeprecator()
}

interface Snapshot<A extends Aggregate> {
    Long getId()
    A getAggregate()
    void setAggregate(A aggregate)

    A getDeprecatedBy()
    void setDeprecatedBy(A aggregate)
    
    Long getLastEvent()
    void setLastEvent(Long id)

    Set<A> getDeprecates()
}