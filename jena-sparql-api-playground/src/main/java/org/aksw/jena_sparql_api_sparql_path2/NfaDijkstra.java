package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Just a scratch pad for my thoughts whether dijkstra could be applied to nfa matching.
 *
 * classic dijstra tracks
 * cost(v)
 * predecessor(v) - vertex from which the minimal cost originated
 *
 *
 * we could track the minimum cost per each predecessor state
 *
 * vertex B was reached in state Y by predecessor A in this state X
 *
 * Essentially this is dijkstra on a graph that is dynamically generated.
 * The vertices are (state, node) pairs, wherease for edges it is (transition, triple) pairs (transition is a triplet<S, PredicateClass>)
 * %Note, that it is essential for the edge also to include the transition, because
 *
 * Given the RPQ: <p>/<p>/<p> - should a p a match? if not, we do not have to track the automaton transitions.
 *
 * An edge in the
 *
 *
 *
 *
 *
 * @author raven
 *
 */
public class NfaDijkstra {

    /**
     * Check whether a triplet originated from a certain transition
     *
     * @param transition
     * @param transToVertexClass
     * @param vertex
     * @param triplet
     * @return
     */
    public static <T, V, E> boolean isOrigin(Pair<ValueSet<V>> vertexClass, V vertex, Triplet<V, E> triplet) { //T transition, Function<T, Pair<ValueSet<V>>> transToVertexClass,
        List<Boolean> dirs = Triplet.getDirections(triplet, vertex);
        //Pair<ValueSet<V>> vertexClass = transToVertexClass.apply(transition);

        boolean result = dirs.stream().anyMatch(dir -> {
            int index = dir == false ? 0 : 1;
            ValueSet<V> valueSet = vertexClass.get(index);

            E p = triplet.getPredicate();
            boolean r = valueSet.contains(p);
            return r;
        });

        return result;
    }

    public static <S, T, V, E> void run(
            Nfa<S, T> nfa,
            Function<T, Pair<ValueSet<V>>> transToVertexClass,
            Function<Pair<ValueSet<V>>, LookupService<V, Set<Triplet<V,E>>>> createTripletLookupService,
            V start)
    {
        // A graph for the data loaded so far
        //DirectedGraph<Entry<S, V>, Triplet<Entry<S, V>, E>> dynamicGraph = new DefaultDirectedGraph<>(Triplet.class);

        //Map<? super Entry<S, V>, Number> vertexToCost = new HashMap<>(); //Comparable<Number>

        //Set<Entry<S, V>> open = new HashSet<>();
        Multimap<S, V> open = HashMultimap.create();
        Multimap<S, V> next = HashMultimap.create();

        nfa.getStartStates().forEach(s -> open.put(s, start));

        DirectedGraph<S, T> nfaGraph = nfa.getGraph();
        while(!open.isEmpty()) {
            open.asMap().entrySet().forEach(e -> {
                // Get all successor states and
                S state = e.getKey();
                Collection<V> vertices = e.getValue();
                Set<T> transitions = nfaGraph.outgoingEdgesOf(state);

                Pair<ValueSet<V>> vertexClass = transitions.stream().reduce(
                        (Pair<ValueSet<V>>)new VertexClass<V>(),
                        (a, b) -> transToVertexClass.apply(b),
                        (a, b) -> VertexClass.union(a, b));

                // TODO reduce the vertex classes to a final one
                //LookupService<V, Triplet<V, E>> tripletService = createTripletLookupService.apply(vertexClass);
                    //return r;

                LookupService<V, Set<Triplet<V, E>>> tripletService = createTripletLookupService.apply(vertexClass);

                // For all nodes in the state
                Map<V, Set<Triplet<V, E>>> vToTriplets = tripletService.apply(vertices);

                // We now need to check which triplet matched which transition so that we associate the right successor state
                vToTriplets.entrySet().forEach(vToTriplet -> {
                    V v = vToTriplet.getKey();
                    // for every individual transition
                    transitions.stream().forEach(t -> {
                        Pair<ValueSet<V>> vc = transToVertexClass.apply(t);

                        // for every triplet check for its origin
                        vToTriplet.getValue().stream().forEach(triplet -> {
                            V targetVertex = Triplet.getTarget(triplet, v);
                            //V targetVertex = Triplet.getTarget(triplet, reverse);
                            boolean isOrig = isOrigin(vc, v, triplet);
                            if(isOrig) {
                                // Get the transitions target state
                                S targetState = nfaGraph.getEdgeTarget(t);
                                next.put(targetState, targetVertex);
                            }
                        });

                    });
                });
            });
        }

        //Map<Entry<S, V>, <S, V>> vertextToPredecessor;



        //NfaExecutionUtils.advanceFrontier(frontier, nfaGraph, isEpsilon, getMatchingTriplets, pathGrouper, earlyPathReject)

        // PathExecutionUtils.execNfa(nfa, startStates, isEpsilon, startVertices, pathGrouper, getMatchingTriplets, pathCallback);


        // Map<V, Miap<S, Integer>> vertexToStateToCost
        // Map<V, Map<S, V>> vertexToStateToPredecessor

        /*
         * again, we need a successor function that fetche
         */
        //NfaExecution.nextFrontier();
    }
}
