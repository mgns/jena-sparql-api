package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface OpHdtHeader
	extends Op1
{
	
	@Override
	OpHdtHeader setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
//	public static OpHdtHeader create(Op subOp) {
//		OpHdtHeader result = create(subOp);
//		
//		return result;
//	}
	
	public static OpHdtHeader create(Op subOp) {
		OpHdtHeader result = subOp.getModel().createResource().as(OpHdtHeader.class)
			.setSubOp(subOp);

		return result;
	}
}