package eis.iai.uni.bonn.de;

import java.io.IOException;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

public class range extends cGenerator {

	static int total_triples_generatedRan1 = 0;
	static int total_triples_generatedRan2 = 0;

	////////create conflicting type triples for S,A,O ran 1.1
	protected static int createTriples_forType (int count) throws IOException {

		Model temp_model = getRandomTriples(bmodel, (Property)null, count, "range", true);		
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			RDFNode stmt_object = stmt.getObject();
			if(stmt_object.isResource()) {
				Property current_property = stmt.getPredicate();
					OntProperty op = ont_model.getOntProperty(current_property.toString());
					if (op!=null) {
						OntResource ran = getRange(op);

						if(ran != null) {
							Triple ctriple = Triple.create(stmt_object.asNode(), type_property.asNode(), getDisjointClass(ran));	
							Triple itriple = Triple.create(stmt_object.asNode(), type_property.asNode(), ran.asNode());	
							cmodel.add(cmodel.asStatement(ctriple));
							imodel.add(imodel.asStatement(itriple));
							total_triples_generatedRan1++; 	
						}
					}
				}
			}		
		temp_model.close();
		return total_triples_generatedRan1;
	}

	////////////////////get range
	static OntResource getRange(OntProperty op){
		OntResource ran = null;
		if( op.getRange() != null && op.getRange().isResource()) {
			ran = op.getRange();
		} else {
			System.out.println("Range not found for property: "+op.toString());
			ExtendedIterator<? extends OntProperty> eps = op.listEquivalentProperties();
			while(eps.hasNext()) {
				OntProperty ep = eps.next();
				if (ep.getRange() != null && ep.getRange().isResource()) {
					ran = ep.getRange();
					break;
				}
			}
		}
		return ran;
	}

}
