package org.aksw.jena_sparql_api.cache.tests;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.jgrapht.LabeledEdge;
import org.aksw.jena_sparql_api.jgrapht.LabeledEdgeImpl;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.SimpleDirectedGraph;


public class QueryToGraph {
	public static void addEdge(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Node edgeLabel, Node source, Node target) {
		graph.addVertex(source);
		graph.addVertex(target);
		graph.addEdge(source, target, new LabeledEdgeImpl<>(source, target, edgeLabel));
	}

	public static void addQuad(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Quad quad) {
		// Allocate a fresh node for the quad
		Node quadNode = NodeFactory.createBlankNode();
		addEdge(graph, Vars.s, quad.getSubject(), quadNode);
		addEdge(graph, Vars.p, quadNode, quad.getPredicate());
		addEdge(graph, Vars.o, quadNode, quad.getObject());
		addEdge(graph, Vars.g, quadNode, quad.getGraph());
	}


	public static void quadsToGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Collection<Quad> quads) {
		//graph = new DirectedPseudograph<>(LabeledEdgeImpl.class);
		for(Quad quad : quads) {
			addQuad(graph, quad);
		}
	}

	// Filters: Extract all equality filters
	public static void equalExprsToGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Collection<? extends Collection<? extends Expr>> dnf) {
		Set<Map<Var, NodeValue>> maps = DnfUtils.extractConstantConstraints(dnf);

		for(Map<Var, NodeValue> map : maps) {
			// Create a blank node for each clause
			Node orNode = NodeFactory.createBlankNode();
			graph.addVertex(orNode);

			for(Entry<Var, NodeValue> e : map.entrySet()) {
				Var v = e.getKey();
				Node c = e.getValue().getNode();

				graph.addVertex(v);
				graph.addVertex(c);

				graph.addEdge(orNode, v, new LabeledEdgeImpl<Node, Node>(orNode, v, Vars.x));
				graph.addEdge(c, orNode, new LabeledEdgeImpl<Node, Node>(c, orNode, Vars.y));
			}
		}
	}


	public static void toGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, QuadFilterPatternCanonical qfpc) {
		quadsToGraph(graph, qfpc.getQuads());
		equalExprsToGraph(graph, qfpc.getFilterDnf());
	}

	public static void toGraph(DirectedGraph<Node, LabeledEdge<Node, Node>> graph, Query query) {
		QuadFilterPatternCanonical qfpc = SparqlCacheUtils.fromQuery(query);
		toGraph(graph, qfpc);
	}

	public static DirectedGraph<Node, LabeledEdge<Node, Node>> toGraph(QuadFilterPatternCanonical qfpc) {
		DirectedGraph<Node, LabeledEdge<Node, Node>> graph = new SimpleDirectedGraph<>(LabeledEdgeImpl.class);

		toGraph(graph, qfpc);

		return graph;
	}

	public static Stream<Map<Var, Var>> match(QuadFilterPatternCanonical view, QuadFilterPatternCanonical user) {
		DirectedGraph<Node, LabeledEdge<Node, Node>> a = toGraph(view);
		DirectedGraph<Node, LabeledEdge<Node, Node>> b = toGraph(user);

		Stream<Map<Var, Var>> result = match(a, b);
		return result;
	}

	public static Stream<Map<Var, Var>> match(DirectedGraph<Node, LabeledEdge<Node, Node>> a, DirectedGraph<Node, LabeledEdge<Node, Node>> b) {

		System.out.println("EDGES:");
		a.edgeSet().forEach(System.out::println);
		System.out.println("done with edges");

		Comparator<Node> nodeCmp = (x, y) -> {
			int  r = (x.isVariable() && y.isVariable()) || (x.isBlank() && y.isBlank()) ? 0 : x.toString().compareTo(y.toString());
			//System.out.println("" + x + " - " + y + ": " + r);
			return r;
		};
		Comparator<LabeledEdge<Node, Node>> edgeCmp = (x, y) -> x.getLabel().toString().compareTo(y.getLabel().toString());
//		Comparator<//LabeledEd>
		VF2SubgraphIsomorphismInspector<Node, LabeledEdge<Node, Node>> inspector = new VF2SubgraphIsomorphismInspector<>(a, b, nodeCmp, edgeCmp, true);
		Iterator<GraphMapping<Node, LabeledEdge<Node, Node>>> it = inspector.getMappings();

		Stream<Map<Var, Var>> result = StreamUtils.stream(it)
				.map(x -> (IsomorphicGraphMapping<Node, LabeledEdge<Node, Node>>)x)
				.map(x -> {
					Map<Var, Var> varMap = new HashMap<>();
					boolean r = true;
					for(Node node : a.vertexSet()) {
						if(node.isVariable()) {
							Var s = (Var)node;
							if(x.hasVertexCorrespondence(s)) {
								Node fff = x.getVertexCorrespondence(s, true);
								if(fff.isVariable()) {
									varMap.put(s, (Var)fff);
								} else {
									r = false;
									break;
								}
							}
						}
					}

					Map<Var, Var> s = r ? varMap : null;
					return s;
				}).
				filter(x -> x != null);

		return result;
	}

	public static boolean tryMatch(Query view, Query user) {
		DirectedGraph<Node, LabeledEdge<Node, Node>> a = new SimpleDirectedGraph<>(LabeledEdgeImpl.class);
		DirectedGraph<Node, LabeledEdge<Node, Node>> b = new SimpleDirectedGraph<>(LabeledEdgeImpl.class);

		toGraph(a, view);
		toGraph(b, user);

		Stream<Map<Var, Var>> tmp = match(a, b);
		tmp = tmp.peek(System.out::println);
		boolean result = tmp.count() > 0;
		return result;
	}


}

