package org.aksw.jena_sparql_api.conjure.datapod.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;

public interface RdfDataPod
	extends DataPod
{
//	@Override
//	default RdfEntityInfo persist(Path file) throws IOException {
//		try(RDFConnection conn = openConnection()) {
//			Model m = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
//			try(OutputStream out = new FileOutputStream(file.toFile())) {
//				RDFDataMgr.write(out, m, RDFFormat.TURTLE_PRETTY);
//			}
//		}
//		
//		RdfEntityInfo result = ModelFactory.createDefaultModel().createResource()
//				.as(RdfEntityInfo.class)
//				.setContentType(WebContent.contentTypeTurtle);
//
//		return result;
//	}
	
	
	// Pro/Contra for allowing only a single connection per datapod:
	// Pro: DataPods may be backed by a triple store, such as virtuoso
	// the open source version has a limit on the number of connections that can be established.
	// Obtaining a data object 'reserves' the connection
	// If more than one connection is desired, than multiple data pods could be created

	// Contra:
	// I envisioned DataPods to be ideally isolated databases
	// So unless one interferes with the system from the outside, use of a database should
	// guarantee that datapods only change when it is explicitly requested.
	// Then again, if someone really want's to modify the data of the same pod concurrently,
	// then maybe its ok to have 2 pods backed by the same database fragment
	
	// So a pod is a view over a subset of a database system
	
	/**
	 * Attempt to establish establish an {@link RDFConnection} to the RDF dataset represented
	 * by this object
	 * 
	 * Clients should always eventually invoke RDFConnection.close() on the returned connection
	 * Only a single connection should be established to a dataset at a given time.
	 * Implementations should throw an exception if openConnection is invoked while a prior
	 * connection has not yet been closed.
	 * 
	 * Closing a connection allows to obtain another one at a later time
	 * However, it is not valid to invoke openConnection() after release() was called. 
	 * 
	 * See also {@link java.sql.DataSource}
	 * 
	 * @return
	 */
	RDFConnection openConnection();
}
