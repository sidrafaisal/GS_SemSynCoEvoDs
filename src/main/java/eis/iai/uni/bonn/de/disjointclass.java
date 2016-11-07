package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class disjointclass extends cGenerator {

	static int total_triples_generatedDC1 = 0;

	////// Read model to get triples whose resource type is already available then, generate conflicts for them (disjoint)   
	protected static int createTriples_forExistingType(int count) throws IOException {
		Model temp_model = getRandomTriples(bmodel, type_property, count, "type", false);		
		Set<Resource> resources= temp_model.listSubjects().toSet();
		Iterator<Resource> resource_iter = resources.iterator();

		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);

			while (stmt_iter.hasNext()) {					
				Statement stmt = stmt_iter.next();			
				Property current_property = stmt.getPredicate();
				if (!current_property.equals(type_property) ) { 
					//generate conflict for any random property of this resource

					OntProperty op = ont_model.getOntProperty(current_property.toString());
					if (op!=null) {
						OntResource dom = getDomain(op);
						if(dom != null) {							
							Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), getDisjointClass(dom));	
							Triple itriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), dom.asNode());	

							cmodel.add(cmodel.asStatement(ctriple));
							imodel.add(imodel.asStatement(itriple));
							total_triples_generatedDC1++;	
							break;
						}
					}
				}				
			}			
		}	
		temp_model.close();
		return total_triples_generatedDC1;
	}

}
