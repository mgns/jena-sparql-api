package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.nio.file.Files;

import org.apache.jena.query.ARQ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionFs {
    public static String ns = "http://jsa.aksw.org/fn/fs/";
    
    public static void register() {
        FunctionRegistry.get().put(ns + "rdfLang", E_RdfLang.class);
        FunctionRegistry.get().put(ns + "probeRdf", E_ProbeRdf.class);
        FunctionRegistry.get().put(ns + "get", E_PathGet.class);
        FunctionRegistry.get().put(ns + "size", E_UnaryPathFunction.newFactory(path -> NodeValue.makeInteger(Files.size(path))));
        FunctionRegistry.get().put(ns + "isDirectory", E_UnaryPathFunction.newFactory(path -> NodeValue.makeBoolean(Files.isDirectory(path))));
        FunctionRegistry.get().put(ns + "isRegularFile", E_UnaryPathFunction.newFactory(path -> NodeValue.makeBoolean(Files.isRegularFile(path))));
        //FunctionRegistry.get().put(ns + "lastModifiedTime", E_UnaryPathFunction.newFactory(path -> NodeValue.makeInteger(Files.getLastModifiedTime(path).toInstant())));
        
		PropertyFunctionRegistry.get().put(ns + "find", new PropertyFunctionFactoryFsFind(PropertyFunctionFactoryFsFind::find));
		PropertyFunctionRegistry.get().put(ns + "parents", new PropertyFunctionFactoryFsFind(PropertyFunctionFactoryFsFind::parents));
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("fs", ns);
    }

    // Better not register the handler automatically; it is a quite intrusive deed
    public static void registerFileServiceHandler() {
        QC.setFactory(ARQ.getContext(), execCxt -> new OpExecutorServiceOrFile(execCxt));
    }
}