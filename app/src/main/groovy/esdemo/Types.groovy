package esdemo

interface Aggregate {

}

interface Event<A extends Aggregate, T> {
    A getAggregate()
    abstract String getAudit()

    Date getDateCreated()
    String getCreatedBy()

    T getRevertedBy()
    void setRevertedBy(T revertEventId)

    T getId()
}

interface RevertEvent<A extends Aggregate, T> extends Event<A, T> {
    Event<A, T> getEvent()
}

interface Deprecates<A extends Aggregate, T> extends Event<A, T> {
    DeprecatedBy<A, T> getConverse()
    A getDeprecated()
}

interface DeprecatedBy<A extends Aggregate, T> extends Event<A, T> {
    Deprecates<A, T> getConverse()
    A getDeprecator()
}

interface Snapshot<A extends Aggregate> {
    A getAggregate()

    A getDeprecatedBy()
    void setDeprecatedBy(A aggregate)

    Set<A> getDeprecates()
}