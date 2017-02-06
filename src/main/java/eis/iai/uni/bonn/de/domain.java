package eis.iai.uni.bonn.de;

import java.io.IOException;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class Domain extends ChangeGenerator {

	////////create conflicting type triples for S,A,O dom 1.1
	protected static void createTriples_forType (int count) throws IOException {
	//	String str ="";
		Model temp_model = getRandomTriples(bmodel, (Property)null, count,"domain", false);	
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Property current_property = stmt.getPredicate();
			OntProperty op = ont_model.getOntProperty(current_property.getURI());
			OntResource dom = getDomain(op);
			if(dom!=null) {				
				Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), getDisjointClass(dom));
				
/*				//other conflicts generated as side effect
				Set<Statement> additional_conflicts = bmodel.listStatements(stmt.getSubject(), type_property, (RDFNode)null).toSet();
				Iterator<Statement> additional_conflicts_iter = additional_conflicts.iterator();
				long additional_conflicts_mid = additional_conflicts.size()/2 + (additional_conflicts.size()%2) - 1;	
				while (additional_conflicts_iter.hasNext()) {
					Statement additional_conflicts_stmt = additional_conflicts_iter.next();
					addConflict_into_ST(additional_conflicts_stmt, ctriple, additional_conflicts_stmt.getObject().asNode(), str, additional_conflicts_mid, total_triples_generatedDC1);
					total_triples_generatedDC1++;
				}
	*/			addConflict_into_ST(stmt, ctriple, dom.asNode(), mid, total_triples_generatedDom1); 
				total_triples_generatedDom1++; 
			}
		}		
		temp_model.close();
	}

	static void addConflict_into_ST(Statement stmt, Triple ctriple, Node node, long mid, int total_triples_generated) {
/*		if (stmt.getObject().isResource())
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
					"<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">";
		else if (stmt.getObject().isLiteral())
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
					"<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">";

		if (!content.contains(str))
			content += str + "\n";
*/
		Triple itriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), node);

		if (total_triples_generated < mid) 
			srcmodel.add(srcmodel.asStatement(ctriple));
		else
			tarmodel.add(tarmodel.asStatement(ctriple));
		imodel.add(imodel.asStatement(itriple));
	}
}