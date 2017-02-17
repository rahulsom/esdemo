package com.github.rahulsom.es4g.internal

import com.github.rahulsom.es4g.annotations.Event
import com.github.rahulsom.es4g.annotations.Query
import com.github.rahulsom.es4g.api.EventApplyOutcome
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static com.github.rahulsom.es4g.internal.AggregateASTTransformation.SNAPSHOT_PLACEHOLDER
import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Adds methods corresponding to event into the query interface
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
@GroovyASTTransformation()
@Log
class EventASTTransformation extends AbstractASTTransformation {

    private static final Class<Event> MY_CLASS = Event.class
    private static final ClassNode MY_TYPE = make(MY_CLASS)

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotatedNode annotatedNode = nodes[1] as AnnotatedNode
        AnnotationNode annotationNode = nodes[0] as AnnotationNode

        if (MY_TYPE == annotationNode.classNode && annotatedNode instanceof ClassNode) {
            def theAggregate = annotationNode.getMember('value')
            def theClassNode = annotatedNode as ClassNode
            log.warning "[Event    ] Adding apply${theClassNode.nameWithoutPackage} to interface ${theAggregate.type.name}\$Query"
            def queryInterfaceNode = AggregateASTTransformation.interfaces[theAggregate.type.name]

            queryInterfaceNode.addMethod("apply${theClassNode.nameWithoutPackage}",
                    ACC_PUBLIC | ACC_ABSTRACT,
                    make(EventApplyOutcome),
                    [
                            new Parameter(make(theClassNode.name), 'event'),
                            new Parameter(make(SNAPSHOT_PLACEHOLDER), 'snapshot')
                    ] as Parameter[],
                    new ClassNode[0],
                    null)

        }
    }
}

