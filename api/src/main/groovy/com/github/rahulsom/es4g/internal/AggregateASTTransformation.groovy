package com.github.rahulsom.es4g.internal

import com.github.rahulsom.es4g.annotations.Aggregate
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.tools.GenericsUtils
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
        ClassNode genericTypeForInterface = createSnapshotGeneric()

        def queryInterfaceNode = new ClassNode(
                "${parentClass}\$Query", ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT , make(Object)
        )
        queryInterfaceNode.setGenericsTypes([
                new GenericsType(genericTypeForInterface)
        ].toArray(GenericsUtils.EMPTY_GENERICS_ARRAY))
        queryInterfaceNode.setGenericsPlaceHolder(true)
//        queryInterfaceNode.addMethod('createEmptySnapshot', ACC_PUBLIC | ACC_ABSTRACT, genericTypeForInterface,
//                new Parameter[0], new ClassNode[0], null)
        queryInterfaceNode
    }

    static ClassNode createSnapshotGeneric() { make('S') }
}
