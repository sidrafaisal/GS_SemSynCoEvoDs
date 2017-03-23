package eis.iai.uni.bonn.de;

import java.io.IOException;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class Domain extends ChangeGenerator {
	////////create conflicting type triples for S,A,O dom 1.1
	protected static void createTriples_forType (int count) throws IOException {
		Model temp_model = getRandomTriples(bmodel, (Property)null, count,"domain", false);	
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			OntResource dom = getDomain(ont_model.getOntProperty(stmt.getPredicate().getURI()));
			if(dom!=null) {				
				Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), getDisjointClass(dom));
				if (total_triples_generatedDom1 < mid) {
					if (!srcmodel.contains(srcmodel.asStatement(ctriple))){
						total_triples_generatedDom1++;	
						srcmodel.add(srcmodel.asStatement(ctriple));}
				} else {
					if (!tarmodel.contains(tarmodel.asStatement(ctriple))){
						total_triples_generatedDom1++;	
						tarmodel.add(tarmodel.asStatement(ctriple));}
				}
				imodel.add(imodel.asStatement(Triple.create(stmt.getSubject().asNode(), type_property.asNode(), dom.asNode())));
				tcg_model.add(stmt);
			}
		}		
		temp_model.close();
	}
}