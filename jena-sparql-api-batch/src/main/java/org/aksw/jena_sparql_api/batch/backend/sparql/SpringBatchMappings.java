package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jena_sparql_api.beans.model.EntityModel;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngine;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.springframework.batch.core.JobExecution;

public class SpringBatchMappings {
    public static void main(String[] args) {
        // TODO: For each java class, set a white / blacklist of properties which to process
        //Map<Class<?>, Map<String, String>> defs = new HashMap<>();
        

        // TODO: Add support for suppliers of new instance
        // TODO: Add support to provide a setter for read only property
        // createForcedFieldSetter(Class, fieldName).setValue(obj, foo);
        
        Map<Class<?>, EntityOps> customOps = new HashMap<>();
        
        {
            EntityModel entityModel = EntityModel.createDefaultModel(JobExecution.class);
            entityModel.setNewInstance(() -> new JobExecution(0l));
            
            Object inst = entityModel.newInstance();
            entityModel.getProperty("id").setValue(inst, 12l);
            customOps.put(JobExecution.class, entityModel);
        }
        
        
        Function<Class<?>, EntityOps> classToOps = (clazz) -> {
            EntityOps result;
            
            EntityOps cops = customOps.get(clazz);
            
            result = cops != null
                    ? cops
                    : EntityModel.createDefaultModel(clazz);
            
            return result;
        };

        RdfTypeFactory typeFactory =  RdfTypeFactoryImpl.createDefault(null, classToOps);
        RdfType t = typeFactory.forJavaType(JobExecution.class);
        //t.emitTriples(persistenceContext, emitterContext, out, obj);
        
        SparqlService sparqlService = FluentSparqlService.forModel().create();
        
        RdfMapperEngine engine = new RdfMapperEngineImpl(sparqlService);
        
        Graph graph = GraphFactory.createDefaultGraph();
        engine.emitTriples(graph, new JobExecution(0l));
        
        Model model = ModelFactory.createModelForGraph(graph);
        model.write(System.out, "TTL");
        
        
        
        //EntityManagerJena em = new EntityManagerJena(engine)
        
        //Function<Object, String> iriFn = (je) -> ":" + ((JobExecution)je).getJobId();
        //RdfClass map = new RdfClass(typeFactory, JobExecution.class, (je) -> ":" + ((JobExecution)je).getJobId());

        
        //System.out.println(inst);
//        
//        Supplier<JobExecution> newInstance = () -> new JobExecution(0l);
//        Function<Object, String> iriFn = (je) -> ":" + ((JobExecution)je).getJobId();
//        Map<String, String> pmap = BeanUtils.getPropertyNames(o).stream()
//                .collect(Collectors.toMap(e -> e, e -> "http://batch.aksw.org/ontology/" + e));
//        builder.setSetterOverride("id", (val) -> {
//            Field field = JobExecution.class.getDeclaredField("id");
//            field.setAccessible(true);
//        });
//        
        
        //System.out.println(entityModel.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList()));
        
//        //listMethodNames
//        BeanWrapper wrapper;
//        wrapper.getPropertyDescriptors()
//        Map<String, Object> 
//        
//        
//        RdfTypeFactory typeFactory =  RdfTypeFactoryImpl.createDefault();
//        typeFactory.forJavaType(clazz)
//        
//        //Function<JobExecution, String> iriFn = (je) -> ":" + je.getJobId();
//        RdfClass map = new RdfClass(typeFactory, JobExecution.class, (je) -> ":" + ((JobExecution)je).getJobId());
//        
//        RdfPropertyDescriptor
//        map.addPropertyDescriptor(propertyDescriptor);
    }
}
