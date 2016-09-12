package org.aksw.jena_sparql_api.mapper.context;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.impl.engine.EntityGraphMap;
import org.apache.jena.graph.Node;

/**
 * TODO Maybe this could subclass from BeanFactory?
 *
 * A population context holds information needed to
 * populate a Java object graph.
 *
 * For *non-primitive types*, a population context
 * must yield the same Java object for a given rdfType and id.
 *
 * TODO Clarify for whether the condition should also hold for primitive types
 *
 *
 *
 * @author raven
 *
 */
public interface RdfPersistenceContext
{
    Map<Object, Node> getPrimaryNodeMap();
    
    void requestResolution(PropertyOps propertyOps, Object entity, Node node);
    List<ResolutionRequest> getResolutionRequests();
    Object entityFor(Class<?> clazz, Node Node, Supplier<Object> newInstance);
    EntityGraphMap getEntityGraphMap();    
}
