package org.aksw.jena_sparql_api.rdf.collections;

import java.util.Objects;

import org.aksw.jena_sparql_api.mapper.proxy.TypeDecider;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.ModelUtils;

public class RDFNodeMapperImpl<T>
	implements RDFNodeMapper<T>
{
	protected TypeMapper typeMapper;
	protected TypeDecider typeDecider;
	protected Class<T> viewClass;
	
	// Flag to indicate that requested resource views should be applied even if the TypeDecider cannot find a better view class
	protected boolean isViewAll;

	protected transient NodeMapper<T> nodeMapper;

	
	public RDFNodeMapperImpl(Class<T> viewClass, TypeMapper typeMapper, TypeDecider typeDecider, boolean isViewAll) {
		super();
		this.typeMapper = typeMapper;
		this.typeDecider = typeDecider;
		this.viewClass = viewClass;
		this.isViewAll = isViewAll;

		this.nodeMapper = new NodeMapperFromTypeMapper<>(viewClass, typeMapper); //NodeMapperFactory.from(viewClass, typeMapper);
	}
	
	public boolean canMap(RDFNode rdfNode) {			
		Object tmp = toJava(rdfNode);
		boolean result = tmp != null;
		return result;
	}
	
	public T toJava(RDFNode rdfNode) {
		Objects.requireNonNull(rdfNode);
		Objects.requireNonNull(viewClass);
		
		Node n = rdfNode.asNode();
		
		T result;
		if(nodeMapper.canMap(n)) {
			result = nodeMapper.toJava(n);
		} else {
			result = castRdfNode(rdfNode, viewClass, typeDecider, isViewAll);
		}

		return result;
	}

	public static <T extends RDFNode> T castRdfNode(RDFNode rdfNode,  Class<?> viewClass, TypeDecider typeDecider, boolean isViewAll) {
		Class<?> effectiveType;
		if(rdfNode.isResource()) {
			Resource r = rdfNode.asResource();
			effectiveType = getEffectiveType(r, viewClass, typeDecider, isViewAll);
		} else {
			effectiveType = viewClass;
		}
		
		T result = effectiveType == null
				? null
				: rdfNode.canAs((Class)effectiveType) 
					? (T)rdfNode.as((Class)effectiveType)
					: null;

		return result;
	}
	
	public static Class<?> getEffectiveType(Resource r, Class<?> viewClass, TypeDecider typeDecider, boolean isViewAll) {
		Class<?> effectiveType;
		effectiveType = ResourceUtils.getMostSpecificSubclass(r, viewClass, typeDecider);
		
		if(effectiveType == null) {
			// If we could not obtain a specific type, and the request was for
			// a super class of RDFNode/Resource, yield a generic RDFNode view
			if(viewClass.isAssignableFrom(Resource.class)) {
				effectiveType = RDFNode.class;
			} else if(Resource.class.isAssignableFrom(viewClass)) {
				if(isViewAll) {
					effectiveType = viewClass;
				}
			}

			// We could not obtain a more specific type that the one requested -
			// try the requested type as a fallback
			// NOTE This case happens, if a resource with a model x was added to a model y:
			// In this case, all triples and thus the type information is lost, so no more
			// specific type is found
		}
		return effectiveType;
	}


	@Override
	public Class<?> getJavaClass() {
		return viewClass;
	}

	@Override
	public RDFNode toNode(T obj) {
		RDFNode result;
		
		// If the view demands subclasses of RDFNode, use the type decider system
//		if(RDFNode.class.isAssignableFrom(viewClass) && obj instanceof Resource) {
		if(obj instanceof Resource) {
			Resource r = (Resource)obj;
			Class<?> effectiveViewClass = ResourceUtils.getMostSpecificSubclass(r, viewClass, typeDecider);
			
			if(effectiveViewClass == null && isViewAll) {
				effectiveViewClass = viewClass;
			}
			
			Objects.requireNonNull(effectiveViewClass);
			
			// If we ended up with parent of RDFNode, constrain to RDFNode 
			if(effectiveViewClass.isAssignableFrom(RDFNode.class)) {
				effectiveViewClass = RDFNode.class;
			}
			
			// TODO If there are multiple types, we return null  for now
			// We could however under certain circumstances create a proxy that implements all types
			// (i.e. all but one types must be interfaces)
			result = effectiveViewClass == null ? null : r.as((Class)effectiveViewClass);
		} else {
			Node n = nodeMapper.toNode(obj);
			result = ModelUtils.convertGraphNodeToRDFNode(n);
		}
		
		return result;
	}

}