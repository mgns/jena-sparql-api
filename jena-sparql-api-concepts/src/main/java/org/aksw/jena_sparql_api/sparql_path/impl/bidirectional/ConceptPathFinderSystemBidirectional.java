package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderFactory;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderFactorySummaryBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearchBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class ConceptPathFinderSystemBidirectional
	//extends ConceptPathFinderFactorySummaryBase
	implements ConceptPathFinderSystem
{
	

	@Override
	public Single<Model> computeDataSummary(SparqlQueryConnection dataConnection) {
		return ConceptPathFinderBidirectionalUtils.createDefaultDataSummary(dataConnection);
	}

	@Override
	public ConceptPathFinderFactory newPathFinderBuilder() {
		return new ConceptPathFinderFactorySummaryBase() {
			@Override
			public ConceptPathFinder build() {
				return new ConceptPathFinderBase(dataSummary.getGraph(), dataConnection) {

					@Override
					public PathSearch<Path> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept) {
						return new PathSearchBase<Path>() {
							@Override
							public Flowable<Path> exec() {
								return ConceptPathFinderBidirectionalUtils
									.findPaths(dataConnection, sourceConcept, targetConcept, maxResults, maxLength, dataSummary);
							}
						};
					}					
				};
			}			
		};
	}
}
