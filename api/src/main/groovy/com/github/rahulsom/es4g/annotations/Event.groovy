package com.github.rahulsom.es4g.annotations

import com.github.rahulsom.es4g.internal.AggregateASTTransformation
import com.github.rahulsom.es4g.internal.EventASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a class as an Event
 *
 * @author Rahul Somasunderam
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass(classes = EventASTTransformation)
@interface Event {
    /**
     * Aggregates to which this event can be applied
     * @return
     */
    Class[] value()
}
