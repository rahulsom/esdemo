package com.github.rahulsom.es4g.internal

import com.github.rahulsom.es4g.annotations.Aggregate
import com.github.rahulsom.es4g.api.Snapshot
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Creates an interface for the query
 *
 * @author Rahul Somasunderam
 *
 */
@CompileStatic
@GroovyASTTransformation()
@Log
class AggregateASTTransformation extends AbstractASTTransformation {

    private static final Class<Aggregate> MY_CLASS = Aggregate.class
    private static final ClassNode MY_TYPE = make(MY_CLASS)
    static final Map<String, ClassNode> interfaces = [:]
    public static final String SNAPSHOT_PLACEHOLDER = 'S'

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1]
        AnnotationNode annotationNode = (AnnotationNode) nodes[0]

        if (MY_TYPE == annotationNode.classNode && annotatedNode instanceof ClassNode) {
            def theClassNode = annotatedNode as ClassNode
            log.warning("[Aggregate] Adding interface \$Query to ${theClassNode.name}")
            ClassNode queryInterfaceNode = createInterface(theClassNode.name)
            theClassNode.module.addClass(queryInterfaceNode)
            interfaces[theClassNode.name] = queryInterfaceNode
        }
    }

    static ClassNode createInterface(String parentClass) {
        GenericsType genericsType = createGenericsType()

        def queryInterfaceNode = new ClassNode(
                "${parentClass}\$Query", ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT , make(Object)
        )
        queryInterfaceNode.setGenericsTypes([genericsType] as GenericsType[])
        queryInterfaceNode
    }

    static GenericsType createGenericsType() {
        ClassNode genericTypeForInterface = make(SNAPSHOT_PLACEHOLDER)
        def genericsType = new GenericsType(genericTypeForInterface, [make(Snapshot)] as ClassNode[], make(Object))
        genericsType.setPlaceholder(true)
        genericsType
    }

}
