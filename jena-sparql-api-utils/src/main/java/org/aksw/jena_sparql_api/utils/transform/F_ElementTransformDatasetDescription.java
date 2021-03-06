package org.aksw.jena_sparql_api.utils.transform;

import com.google.common.base.Function;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.syntax.Element;

public class F_ElementTransformDatasetDescription
	implements Function<Element, Element>
{
	protected DatasetDescription dd;

	public F_ElementTransformDatasetDescription(DatasetDescription dd) {
		super();
		this.dd = dd;
	}

	@Override
	public Element apply(Element element) {
		Element result = ElementTransformDatasetDescription.rewrite(element, dd);
		return result;
	}
}
