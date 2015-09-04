package org.aksw.jena_sparql_api.modifier;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Retrieve remove to enricht the current model
 *
 * @author raven
 *
 */
public class ModifierModelEnrich
    implements Modifier<Model>
{
    private LookupService<Resource, Model> lookupService;

    /**
     * The concept is executed against the input model to select the set of
     * resources to be resolved for enrichment
     */
    private Concept concept;

    @Override
    public void apply(Model input) {
        QueryExecutionFactory qef = new QueryExecutionFactoryModel(input);
        List<Resource> resources = ServiceUtils.fetchListResources(qef, concept);

        Map<Resource, Model> extra = lookupService.apply(resources);
        for(Entry<Resource, Model> entry : extra.entrySet()) {
            Model m = entry.getValue();
            input.add(m);
        }
    }
}
