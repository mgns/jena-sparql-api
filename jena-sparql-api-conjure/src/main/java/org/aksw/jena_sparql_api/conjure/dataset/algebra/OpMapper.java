package org.aksw.jena_sparql_api.conjure.dataset.algebra;

public class OpMapper
	implements OpVisitor<Op>
{
	@Override
	public Op visit(OpDataRefResource op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpConstruct op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpUpdateRequest op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpUnion op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpPersist op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpVar op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpData op) {
		System.out.println("Op: " + op);
		return null;
	}
}