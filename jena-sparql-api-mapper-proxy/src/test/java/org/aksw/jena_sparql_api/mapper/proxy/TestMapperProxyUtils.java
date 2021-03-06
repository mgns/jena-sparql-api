package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;


public class TestMapperProxyUtils {
	
	public static interface TestResource
		extends Resource
	{
		String getString();		
		TestResource setString(String str);

		String getIri();
		TestResource setIri(String str);

		
		Integer getInteger();
		TestResource setInteger(Integer str);


		TestResource setList(List<String> strs);
		List<String> getList();

		
		<T> Collection<T> getDynamicSet(Class<T> clazz);
		//TestResource setDynamicSet(Iterable<T> items);
		
		Set<String> getItems();
		String getRandomItem();

		
//		@Iri("eg:collection")
//		TestResource setList(List<String> strs);
//		List<String> getList();
		
		
		
//		@Iri("eg:list")
//		TestResource setRDFNodes(List<?> strs);
//
//		List<?> getRDFNodes();
	}

	@ResourceView(TestResource.class)
	@RdfType
	public static interface TestResourceDefault
		extends TestResource
	{
		@Iri("rdfs:label")
		String getString();		
		
		@IriType
		@Iri("rdfs:seeAlso")
		TestResource setIri(String str);
		
		@Iri("owl:maxCardinality")
		Integer getInteger();
	
		@Iri("eg:stringList")
		TestResource setList(List<String> strs);

		@Iri("eg:set")
		Set<String> getItems();
		
		@Iri("eg:set")
		String getRandomItem();
		
		@Iri("eg:dynamicSet")
		<T> Collection<T> getDynamicSet(Class<T> clazz);
		//TestResource setDynamicSet(Iterable<T> items);
		
	
//	@Iri("eg:collection")
//	TestResource setList(List<String> strs);
//	List<String> getList();
	
	
	
//	@Iri("eg:list")
//	TestResource setRDFNodes(List<?> strs);
//
//	List<?> getRDFNodes();
	}
	

	@Test
	public void testTypeDecider() {
		JenaSystem.init();
		
		JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
		TestResource sb = ModelFactory.createDefaultModel().createResource().as(TestResource.class);
		String iri = ResourceUtils.getPropertyValue(sb, RDF.type, NodeMappers.uriString);
//		System.out.println("Iri is " + iri);
		Assert.assertEquals(iri, "java://" + TestResourceDefault.class.getCanonicalName());
	}
	
	
	@Test
	public void testMixedUseOfScalarAndCollectionGetter() {
		JenaSystem.init();
		JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
		TestResource sb = ModelFactory.createDefaultModel().createResource().as(TestResource.class);
		
		Set<String> items = Sets.newHashSet("hello", "world");
		sb.getItems().addAll(items);
		
		String randomItem = sb.getRandomItem();
//		System.out.println(randomItem + " " + sb.getItems());
		Assert.assertEquals(sb.getItems(), items);
		Assert.assertTrue(items.contains(randomItem));

	}

	
	@Test
	public void testScalarString() {
		JenaSystem.init();
		JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
		TestResource sb = ModelFactory.createDefaultModel().createResource().as(TestResource.class);
		
		Assert.assertNull(sb.getString());
		Assert.assertEquals(sb, sb.setString("Hello World"));
		Assert.assertEquals("Hello World", sb.getString());
		
//		RDFDataMgr.write(System.out, sb.getModel(), RDFFormat.TURTLE_PRETTY);
	}

	@Test
	public void testScalarInteger() {
		JenaSystem.init();
		JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
		TestResource sb = ModelFactory.createDefaultModel().createResource().as(TestResource.class);

		Assert.assertNull(sb.getInteger());
		Assert.assertEquals(sb, sb.setInteger(10));
		Assert.assertEquals(10l, (long)sb.getInteger());		
	}

	@Test
	public void testScalarIri() {
		JenaSystem.init();
		JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
		TestResource sb = ModelFactory.createDefaultModel().createResource().as(TestResource.class);
		
		
		Assert.assertNull(sb.getIri());
		Assert.assertEquals(sb, sb.setIri("http://www.example.org/"));
		Assert.assertEquals("http://www.example.org/", sb.getIri());
		
//		System.out.println("<START:");
//		RDFDataMgr.write(System.out, sb.getModel(), RDFFormat.TURTLE_PRETTY);
//		System.out.println("END>");
		//sb.getModel().getProperty(sb, RDFS.seeAlso)
		Statement stmt = Objects.requireNonNull(sb.getProperty(RDFS.seeAlso), "Statement expected to exist");
		Assert.assertTrue(stmt.getObject().isURIResource());
	}

	@Test
	public void testList() {
		JenaSystem.init();
		JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
		TestResource sb = ModelFactory.createDefaultModel().createResource().as(TestResource.class);
		
		
		Assert.assertEquals(Collections.emptyList(), sb.getList());
		List<String> list = Arrays.asList("hello", "world");
		Assert.assertEquals(sb, sb.setList(list));
		Assert.assertEquals(list, sb.getList());
		
//		System.out.println("<START:");
//		RDFDataMgr.write(System.out, sb.getModel(), RDFFormat.TURTLE_PRETTY);
//		System.out.println("END>");
		//sb.getModel().getProperty(sb, RDFS.seeAlso)
//		Statement stmt = Objects.requireNonNull(sb.getProperty(RDFS.seeAlso), "Statement expected to exist");
//		Assert.assertTrue(stmt.getObject().isURIResource());		
	}

	
	@Test
	public void testDynamicSet() {		
		JenaSystem.init();
		JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
		TestResource sb = ModelFactory.createDefaultModel().createResource().as(TestResource.class);
		
		Assert.assertEquals(Collections.emptySet(), sb.getDynamicSet(Integer.class));
		Set<Integer> set = new HashSet<>(Arrays.asList(1, 2));
		sb.getDynamicSet(Integer.class).addAll(set);
		//Assert.assertEquals(sb, sb.setList(list));
		Assert.assertEquals(set, sb.getDynamicSet(Integer.class));
	}
}

