package eis.iai.uni.bonn.de;

import java.io.IOException;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class Domain extends ConflictGenerator {

	static int total_triples_generatedDom1 = 0;

	////////create conflicting type triples for S,A,O dom 1.1
	protected static int createTriples_forType (int count) throws IOException {
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
				Triple itriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), dom.asNode());
				
				if (total_triples_generatedDom1 < mid) 
					srcmodel.add(srcmodel.asStatement(ctriple));
				else
					tarmodel.add(tarmodel.asStatement(ctriple));
				imodel.add(imodel.asStatement(itriple));
				total_triples_generatedDom1++; 
			}
	}		
		temp_model.close();
		return total_triples_generatedDom1;
	}

}
