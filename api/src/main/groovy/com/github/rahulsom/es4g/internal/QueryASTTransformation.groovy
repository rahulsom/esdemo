package com.github.rahulsom.es4g.internal

import com.github.rahulsom.es4g.annotations.Query
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static com.github.rahulsom.es4g.internal.AstUtils.replaceGenericsPlaceholders
import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Adds the query interface to a query type
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
@GroovyASTTransformation()
@Log
class QueryASTTransformation extends AbstractASTTransformation {

    private static final Class<Query> MY_CLASS = Query.class
    private static final ClassNode MY_TYPE = make(MY_CLASS)

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1]
        AnnotationNode annotationNode = (AnnotationNode) nodes[0]

        if (MY_TYPE == annotationNode.classNode && annotatedNode instanceof ClassNode) {
            def theSnapshot = annotationNode.getMember('snapshot')
            def theAggregate = annotationNode.getMember('aggregate')
            def theClassNode = annotatedNode as ClassNode
            log.warning("[Query    ] Adding interface ${theAggregate.type.name}\$Query to ${theClassNode.name}")

            def queryInterfaceNode = AggregateASTTransformation.createInterface(theAggregate.type.name)

//            theClassNode.addInterface(
//                    replaceGenericsPlaceholders(queryInterfaceNode, ['S': make(theSnapshot.type.name)])
//            )
        }
    }

}

