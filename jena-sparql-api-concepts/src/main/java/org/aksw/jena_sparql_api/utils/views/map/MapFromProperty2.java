package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Set;

import org.aksw.commons.accessors.CollectionFromConverter;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.google.common.base.Converter;

/**
 * A map view for over the values of a specific property of a specific resource,
 * modeled in the following way:
 * 
 * :subject
 *   :entryProperty ?value .
 *   
 *  ?value
 *     :keyProperty ?key .
 *     
 *  The map associates each ?key with ?value.
 *  
 *  Use a converter to convert the value to e.g. a property of ?value
 *  (this way, the map will lose its put capability)
 *  
 * @author raven
 *
 */
public class MapFromProperty2
	extends AbstractMap<RDFNode, RDFNode>
{
	protected final Resource subject;
	protected final Property entryProperty;
	protected final Property keyProperty;
	protected final Property valueProperty;

	//protected fin
	//protected Function<String, Resource> entryResourceFactory;
	
	public MapFromProperty2(
			Resource subject,
			Property entryProperty,
			Property keyProperty,
			Property valueProperty) {
		super();
		this.subject = subject;
		this.entryProperty = entryProperty;
		this.keyProperty = keyProperty;
		this.valueProperty = valueProperty;
	}

	@Override
	public RDFNode get(Object key) {
		Resource entry = key instanceof RDFNode ? getEntry((RDFNode)key) : null;
		
		RDFNode result = entry == null ? null : ResourceUtils.getPropertyValue(entry, valueProperty);

		return result;
	}

//	public Resource getEntry( key) {
//		Resource result = key instanceof RDFNode ? getEntry((RDFNode)key) : null;
//		return result;
//	}

	public Resource getEntry(RDFNode key) {
//		Stopwatch sw = Stopwatch.createStarted();
		Resource result = getEntryViaModel(key);
//		System.out.println("Elapsed (s): " + sw.stop().elapsed(TimeUnit.NANOSECONDS) / 1000000000.0);

		return result;
	}
	
	
	public Resource getEntryViaModel(RDFNode key) {
		Model model = subject.getModel();
		Resource result = model.listStatements(null, keyProperty, key)
			.mapWith(Statement::getSubject)
			.filterKeep(e -> model.contains(subject, entryProperty, e))
			.nextOptional()
			.orElse(null);
		
		return result;
	}

	public Resource getEntryViaSparql(RDFNode key) {

		UnaryRelation e = new Concept(
				ElementUtils.createElementTriple(
						new Triple(Vars.e, keyProperty.asNode(), key.asNode()),
						new Triple(subject.asNode(), entryProperty.asNode(), Vars.e))
				, Vars.e);
			
			Query query = RelationUtils.createQuery(e);
			
			Model model = subject.getModel();
			
			Resource result = ReactiveSparqlUtils.execSelectQs(() -> QueryExecutionFactory.create(query, model))
				.map(qs -> qs.get(e.getVar().getName()).asResource())
				.singleElement()
				.blockingGet();

		return result;
	}
	
	@Override
	public boolean containsKey(Object key) {
		RDFNode r = get(key);
		boolean result = r != null;
		return result;
	}
	
	@Override
	public Resource put(RDFNode key, RDFNode value) {
		Resource entry = getEntry(key);
		
		if(entry == null) {
			entry = subject.getModel().createResource();
			// TODO Add support for custom (non-blank node) resource generation here
		}
		
		//Resource e = entry.inModel(subject.getModel());
		
//		if(!Objects.equals(existing, entry)) {
//			if(existing != null) {
//				subject.getModel().remove(subject, entryProperty, existing);
//			}
//		}

		subject.addProperty(entryProperty, entry);
		
		ResourceUtils.setProperty(entry, keyProperty, key);
		ResourceUtils.setProperty(entry, valueProperty, value);
		
		return entry;
	}

	
	@Override
	public Set<Entry<RDFNode, RDFNode>> entrySet() {
		Converter<Resource, Entry<RDFNode, RDFNode>> converter = Converter.from(
				e -> new RdfEntry(e.asNode(), (EnhGraph)e.getModel(), keyProperty, valueProperty),
				e -> (Resource)e); // TODO Ensure to add the resource and its key to the subject model

		Set<Entry<RDFNode, RDFNode>> result =
			new SetFromCollection<>(
				new CollectionFromConverter<>(
					new SetFromPropertyValues<>(subject, entryProperty, Resource.class),
					converter.reverse()));
		
		return result;
	}
	
	
}
