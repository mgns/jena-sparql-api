package org.aksw.jena_sparql_api.rx;

import java.util.Collections;
import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraphQuads;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.QuadTable;

public class DatasetGraphQuadsImpl
	extends DatasetGraphQuads
{
	protected QuadTable table;

	public DatasetGraphQuadsImpl() {
		this(new QuadTableLinkedHashMap());
	}
	
	public DatasetGraphQuadsImpl(QuadTable table) {
		super();
		this.table = table;
	}
	
	@Override
	public boolean supportsTransactions() {
		return false;
	}

	@Override
	public void begin(TxnType type) {
	}

	@Override
	public void begin(ReadWrite readWrite) {
		table.begin(readWrite);		
	}

	@Override
	public boolean promote(Promote mode) {
		return false;
	}

	@Override
	public void commit() {
		table.commit();
	}

	@Override
	public void abort() {
		table.abort();
	}

	@Override
	public void end() {
		table.end();
	}

	@Override
	public ReadWrite transactionMode() {
		return null;
	}

	@Override
	public TxnType transactionType() {
		return null;
	}

	@Override
	public boolean isInTransaction() {
		return false;
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		Iterator<Quad> result = table.find(g, s, p, o).iterator();
		return result;
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {

		Node gm = g == null || Quad.isUnionGraph(g) ? Node.ANY : g;
		
		Iterator<Quad> result;
		if(Quad.isDefaultGraph(gm)) {
			result = Collections.emptyIterator();
//		} else if(Quad.isUnionGraph(gm)) {
//			result = GraphOps.unionGraph(this).find(s, p, o).mapWith(t -> new Quad(Quad.unionGraph, t));
		} else {
			result = table.find(gm, s, p, o)
					.filter(q -> !Quad.isDefaultGraph(q.getGraph()))
					.iterator();

		}
				
		return result;
	}

	@Override
	public void add(Quad quad) {
		table.add(quad);
	}

	@Override
	public void delete(Quad quad) {
		table.delete(quad);
	}

	@Override
	public Graph getDefaultGraph() {
		return GraphView.createDefaultGraph(this);
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return GraphView.createNamedGraph(this, graphNode);
	}

	@Override
	public void addGraph(Node graphName, Graph graph) {
		graph.find().forEachRemaining(t -> add(new Quad(graphName, t)));
	}
	
    @Override
    public Iterator<Node> listGraphNodes() {
    	return table.listGraphNodes().iterator();
    }
    
    public static DatasetGraphQuadsImpl create(Iterator<Quad> it) {
    	DatasetGraphQuadsImpl result = new DatasetGraphQuadsImpl();
    	while(it.hasNext()) {
    		Quad quad = it.next();
    		result.add(quad);
    	}
    	return result;
    }

    public static DatasetGraphQuadsImpl create(Iterable<Quad> quads) {
    	DatasetGraphQuadsImpl result = new DatasetGraphQuadsImpl();
    	quads.forEach(result::add);
    	return result;
    }
    
    @Override
    public long size() {
    	return table.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).count();
    }
}
