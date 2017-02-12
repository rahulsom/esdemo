package com.github.rahulsom.es4g.api

interface Event<A extends Aggregate> {
    A getAggregate()

    abstract String getAudit()

    Date getDateCreated()

    String getCreatedBy()

    Long getRevertedBy()

    void setRevertedBy(Long revertEventId)

    Long getId()
}



