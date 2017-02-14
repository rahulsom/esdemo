package com.github.rahulsom.es4g.internal

import com.github.rahulsom.es4g.annotations.Query
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import java.util.logging.Level

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

//            theClassNode.addInterface(replaceGenericsPlaceholders(queryInterfaceNode, [
//                    'S': make(theSnapshot.type.name)
//            ]))
        }
    }

    static ClassNode replaceGenericsPlaceholders(ClassNode type, Map<String, ClassNode> genericsPlaceholders) {
        return replaceGenericsPlaceholders(type, genericsPlaceholders, null)
    }
    
    static ClassNode replaceGenericsPlaceholders(
            ClassNode type, Map<String, ClassNode> genericsPlaceholders, ClassNode defaultPlaceholder) {
        if (type.isArray()) {
            return replaceGenericsPlaceholders(type.getComponentType(), genericsPlaceholders).makeArray()
        }

        if (!type.isUsingGenerics() && !type.isRedirectNode()) {
            return type.getPlainNodeReference()
        }

        if (type.isGenericsPlaceHolder() && genericsPlaceholders != null) {
            final ClassNode placeHolderType
            if (genericsPlaceholders.containsKey(type.getUnresolvedName())) {
                placeHolderType = genericsPlaceholders.get(type.getUnresolvedName())
            } else {
                placeHolderType = defaultPlaceholder
            }
            if (placeHolderType != null) {
                return placeHolderType.getPlainNodeReference()
            } else {
                return make(Object.class).getPlainNodeReference()
            }
        }

        final ClassNode nonGen = type.getPlainNodeReference()

        if ("java.lang.Object".equals(type.getName())) {
            nonGen.setGenericsPlaceHolder(false)
            nonGen.setGenericsTypes(null)
            nonGen.setUsingGenerics(false)
        } else {
            if (type.isUsingGenerics()) {
                GenericsType[] parametrized = type.getGenericsTypes()
                if (parametrized != null && parametrized.length > 0) {
                    GenericsType[] copiedGenericsTypes = new GenericsType[parametrized.length]
                    for (int i = 0; i < parametrized.length; i++) {
                        GenericsType parametrizedType = parametrized[i]
                        GenericsType copiedGenericsType = null
                        if (parametrizedType.isPlaceholder() && genericsPlaceholders != null) {
                            ClassNode placeHolderType = genericsPlaceholders.get(parametrizedType.getName())
                            if (placeHolderType != null) {
                                copiedGenericsType = new GenericsType(placeHolderType.getPlainNodeReference())
                            } else {
                                copiedGenericsType = new GenericsType(make(Object.class).getPlainNodeReference())
                            }
                        } else {
                            copiedGenericsType = new GenericsType(
                                    replaceGenericsPlaceholders(parametrizedType.getType(), genericsPlaceholders))
                        }
                        copiedGenericsTypes[i] = copiedGenericsType
                    }
                    nonGen.setGenericsTypes(copiedGenericsTypes)
                }
            }
        }

        return nonGen
    }

}

