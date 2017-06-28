package eis.iai.uni.bonn.de;

import java.io.IOException;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class Disjointclass extends ChangeGenerator {
	////// Read model to get triples whose resource type is already available then, generate conflicts for them (disjoint)   
	protected static void createTriples_forExistingType(int count) throws IOException {
		Model temp_model = getRandomTriples(bmodel, type_property, count, "type", false);	
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {					
			Statement stmt = stmt_iter.next();	
			Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), getDisjointClass(stmt.getObject().asResource()));	
			if (total_triples_generatedDC1 < mid)  {
				if (!srcmodel.contains(srcmodel.asStatement(ctriple))){
					total_triples_generatedDC1++;	
				srcmodel.add(srcmodel.asStatement(ctriple));}
			} else {
				if (!tarmodel.contains(tarmodel.asStatement(ctriple))){
					total_triples_generatedDC1++;	
				tarmodel.add(tarmodel.asStatement(ctriple));}
			} 
			imodel.add(stmt);
			truthmodel.add(stmt);
			tcg_model.add(stmt);
		}
		temp_model.close();
	}
}
