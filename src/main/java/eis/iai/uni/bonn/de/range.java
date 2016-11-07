package eis.iai.uni.bonn.de;

import java.io.IOException;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

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

	protected static int createTriples_ran2 (int count) throws IOException {
		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

		ResIterator resource_iter = bmodel.listSubjectsWithProperty(type_property);
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, (RDFNode)subject));
		}

		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "range", true);		
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		//get s,a,x
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {	
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			Property property =stmt.getPredicate();
			RDFNode object = stmt.getObject();
			if(object.isResource()) {
				//get type T of x
				NodeIterator iter = bmodel.listObjectsOfProperty(object.asResource(), type_property);
				while(iter.hasNext()) {
					RDFNode obj_class = iter.next();
					//now, find other resource y of this type T
					StmtIterator iter1 = bmodel.listStatements((Resource)null, type_property, obj_class);
					while (iter1.hasNext()){
						Statement s = iter1.next();
						Resource new_object = s.getSubject();
						if (!new_object.equals(object.asResource())) {
							//create new triple  y,type,ST
							OntClass sc = getsubclass(obj_class);
							Triple add_triple1 = Triple.create(new_object.asNode(), type_property.asNode(), sc.asNode());							
							Triple add_triple2 = Triple.create(object.asNode(), type_property.asNode(), obj_class.asNode());
							//create new triple  s,a,y
							Triple ctriple1 = Triple.create(subject.asNode(), property.asNode(), new_object.asNode());
							Triple ctriple2 = stmt.asTriple();	
							Triple itriple = stmt.asTriple();								

							if (total_triples_generatedRan2 < mid) {
								srcmodel.add(srcmodel.asStatement(ctriple1));
								srcmodel.add(srcmodel.asStatement(add_triple1));
								tarmodel.add(tarmodel.asStatement(ctriple2));
								tarmodel.add(tarmodel.asStatement(add_triple2));
							} else {
								tarmodel.add(tarmodel.asStatement(ctriple1));
								tarmodel.add(tarmodel.asStatement(add_triple1));
								srcmodel.add(srcmodel.asStatement(ctriple2));
								srcmodel.add(srcmodel.asStatement(add_triple2));
							}
							imodel.add(imodel.asStatement(itriple));
							total_triples_generatedRan2++; 	
							break;
						}
					}
					break;
				}
			}	
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
		return total_triples_generatedRan2;
	}

}
