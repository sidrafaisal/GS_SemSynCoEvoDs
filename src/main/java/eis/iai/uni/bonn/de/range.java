package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
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

public class Range extends ChangeGenerator {

	////////create conflicting type triples for S,A,O ran 1.1
	protected static void createTriples_forType (int count) throws IOException {
		Model temp_model = getRandomTriples(bmodel, (Property)null, count, "range", true);	
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			RDFNode stmt_object = stmt.getObject();
			if(stmt_object.isResource()) {
				OntProperty op = ont_model.getOntProperty(stmt.getPredicate().toString());
				if (op!=null) {
					OntResource ran = getRange(op);
					if(ran != null) {
						int total_triples_generated = total_triples_generatedRan1;
						Triple ctriple = Triple.create(stmt_object.asNode(), type_property.asNode(), getDisjointClass(ran));
						if (total_triples_generated < mid) {
							if (!srcmodel.contains(srcmodel.asStatement(ctriple))){
								total_triples_generatedRan1++;	
								srcmodel.add(srcmodel.asStatement(ctriple));}
						} else {
							if (!tarmodel.contains(tarmodel.asStatement(ctriple))){
								total_triples_generatedRan1++;	
								tarmodel.add(tarmodel.asStatement(ctriple));
							} }
						imodel.add(imodel.asStatement(Triple.create(stmt.getSubject().asNode(), type_property.asNode(), ran.asNode())));	
						tcg_model.add(stmt);
					}
				}
			}
		}		
		temp_model.close();
	}

	protected static void createTriples_ran2 (int count) throws IOException {
		Model temp1_model = FileManager.get().loadModel(createfile("temp1"), filesyntax);
		ResIterator resource_iter = bmodel.listSubjectsWithProperty(type_property);
		while (resource_iter.hasNext()) 
			temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, (RDFNode)resource_iter.next()));
		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "range", true);		
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		//get s,a,x
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {	
			Statement stmt = stmt_iter.next();
			Property property =stmt.getPredicate();
			OntResource ran = getRange(ont_model.getOntProperty(property.toString()));

			RDFNode object = stmt.getObject();
			if(object.isResource()) {
				//get type T of x
				NodeIterator iter = bmodel.listObjectsOfProperty(object.asResource(), type_property);
				L1: while(iter.hasNext()) {
					RDFNode obj_class = iter.next();
					//now, find other resource y of this type T
					StmtIterator iter1 = bmodel.listStatements((Resource)null, type_property, obj_class);
					while (iter1.hasNext()) {
						Statement s = iter1.next();
						Resource new_object = s.getSubject();
						if (!new_object.equals(object.asResource())) {
							int total_triples_generated = total_triples_generatedRan2;
							//create new triple  y,type,dis(ran(prop))
							Triple add_triple1 = Triple.create(new_object.asNode(), type_property.asNode(), getDisjointClass(ran));							
							//create new triple  s,a,y
							Triple ctriple1 = Triple.create(stmt.getSubject().asNode(), property.asNode(), new_object.asNode());
							Triple itriple = Triple.create(new_object.asNode(), type_property.asNode(), obj_class.asNode());	

							if (total_triples_generated < mid) {
								if (!srcmodel.contains(srcmodel.asStatement(ctriple1)) && 
										!tarmodel.contains(tarmodel.asStatement(add_triple1))){
									total_triples_generatedRan2++;	
									srcmodel.add(srcmodel.asStatement(ctriple1));
									tarmodel.add(tarmodel.asStatement(add_triple1));}
							} else {
								if (!tarmodel.contains(tarmodel.asStatement(ctriple1)) && 
										!srcmodel.contains(srcmodel.asStatement(add_triple1))){
									total_triples_generatedRan2++;	
									tarmodel.add(tarmodel.asStatement(ctriple1));
									srcmodel.add(srcmodel.asStatement(add_triple1));
								}}
							imodel.add(imodel.asStatement(itriple));
							tcg_model.add(stmt);
							tcg_model.add(s);
							break L1;
						}
					}
				}
			}	
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
	}

	protected static void createTriples_ran3 (int count) throws IOException {
		Model temp1_model = FileManager.get().loadModel(createfile("temp1"), filesyntax);

		ResIterator resource_iter = bmodel.listSubjectsWithProperty(type_property);
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, (RDFNode)subject));
		}

		Set<Statement> s_stmt = bmodel.listStatements((Resource)null, type_property, (RDFNode)null).toSet();
		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "range", true);		
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	
		//get s,a,x
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {	
			Statement stmt = stmt_iter.next();
			Property property = stmt.getPredicate();

			OntProperty op = ont_model.getOntProperty(property.toString());
			Iterator<OntResource> ran_iter = getAllRange(op).iterator();
			Set<Node> dcs = new HashSet<Node>();
			while (ran_iter.hasNext()) 
				dcs.addAll(getDisjointClasses(ran_iter.next().asResource()));

			Iterator<Node> dcs_iter = dcs.iterator(); 
			RDFNode object = stmt.getObject();
			if(object.isResource()) {
				L1:		while (dcs_iter.hasNext()) {
					Triple ctriple1 = null;
					Iterator<Statement> i_stmt = s_stmt.iterator();
					while(i_stmt.hasNext()) {
						Statement s = i_stmt.next();
						if (dcs_iter.next().getURI().equals(s.getObject().asResource().getURI())) {
							//create new triple  
							ctriple1 = Triple.create(stmt.getSubject().asNode(), property.asNode(), s.getSubject().asNode());
							if (total_triples_generatedRan3 < mid) {
								if (!srcmodel.contains(srcmodel.asStatement(ctriple1))){
									total_triples_generatedRan3++;	
									srcmodel.add(srcmodel.asStatement(ctriple1));
								}
							} else {
								if (!tarmodel.contains(tarmodel.asStatement(ctriple1))){
									total_triples_generatedRan3++;	
									tarmodel.add(tarmodel.asStatement(ctriple1));
								}
							}
							imodel.add(stmt);
							tcg_model.add(stmt);
							tcg_model.add(s);
							break L1;
						}
					}				
				}	
			}
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
	}
}