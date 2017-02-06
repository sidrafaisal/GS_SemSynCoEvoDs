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
				Property current_property = stmt.getPredicate();
				OntProperty op = ont_model.getOntProperty(current_property.toString());
				if (op!=null) {
					OntResource ran = getRange(op);

					if(ran != null) {
						int total_triples_generated = total_triples_generatedRan1;
						Triple ctriple = Triple.create(stmt_object.asNode(), type_property.asNode(), getDisjointClass(ran));
				/*		//other conflicts generated as side effect
						Set<Statement> additional_conflicts = bmodel.listStatements((Resource)null, (Property)null, stmt_object).toSet();//bmodel.listStatements(stmt.getSubject(), type_property, (RDFNode)null).toSet();						
						Iterator<Statement> additional_conflicts_iter = additional_conflicts.iterator();
					//	long additional_conflicts_mid = additional_conflicts.size()/2 + (additional_conflicts.size()%2) - 1;	
						while (additional_conflicts_iter.hasNext()) {
							Statement additional_conflicts_stmt = additional_conflicts_iter.next();
							if (!additional_conflicts_stmt.getPredicate().equals(difffrom_property) && 
									!additional_conflicts_stmt.getPredicate().equals(ran_property) && 
									!additional_conflicts_stmt.getPredicate().equals(dom_property) &&
									!additional_conflicts_stmt.getPredicate().equals(sameas_property) && 
									!additional_conflicts_stmt.getPredicate().equals(sub_property) && 
									!additional_conflicts_stmt.getPredicate().equals(type_property)) {
								addConflict_only_Ran1(additional_conflicts_stmt, ctriple);
							}

						//	addConflict_Ran1(additional_conflicts_stmt, ctriple, additional_conflicts_stmt.getObject().asNode(), additional_conflicts_mid, total_triples_generated);						
						}*/
						addConflict_Ran1(stmt, ctriple, ran.asNode(), mid, total_triples_generated); 
						total_triples_generatedRan1++; 	
					}
				}
			}
		}		
		temp_model.close();
	}

	static void addConflict_Ran1(Statement stmt, Triple ctriple, Node node, long mid, int total_triples_generated) {
		/*	String str ="";
		if (stmt.getObject().isResource())
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
	
	/*	static void addConflict_only_Ran1(Statement stmt, Triple ctriple) {
		String str ="";
		if (stmt.getObject().isResource())
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
					"<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">";
		else if (stmt.getObject().isLiteral())
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
					"<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">";

		if (!content.contains(str))
			content += str + "\n";
	}
	
	static void addConflict_only(Triple ctriple1, Triple add_triple1, long mid, int total_triples_generated) {
		String str ="";
		if (total_triples_generated < mid) {	
			if (ctriple1.getObject().isURI())
				str = "<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> <" + ctriple1.getObject() + ">|" +
						"<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">";
			else if (ctriple1.getObject().isLiteral())
				str = "<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> \"" + ctriple1.getObject() + "\"|" +
						"<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">";						
		} else {	
			if (ctriple1.getObject().isURI())
				str = "<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">|"+
						"<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> <" + ctriple1.getObject() + ">";						
			else if (ctriple1.getObject().isLiteral())
				str = "<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">|"+
						"<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> \"" + ctriple1.getObject() + "\"";
		}
		if (!content.contains(str))
			content += str + "\n";
	}
	/*/
	static void addConflict_in_ST(Triple ctriple1, Triple add_triple1, Triple itriple, long mid, int total_triples_generated) {
//	String str ="";
		if (total_triples_generated < mid) {
			srcmodel.add(srcmodel.asStatement(ctriple1));
			tarmodel.add(tarmodel.asStatement(add_triple1));
			/*
			if (ctriple1.getObject().isURI())
				str = "<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> <" + ctriple1.getObject() + ">|" +
						"<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">";
			else if (ctriple1.getObject().isLiteral())
				str = "<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> \"" + ctriple1.getObject() + "\"|" +
						"<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">";						
		/*/} else {
			tarmodel.add(tarmodel.asStatement(ctriple1));
			srcmodel.add(srcmodel.asStatement(add_triple1));
			/*
			if (ctriple1.getObject().isURI())
				str = "<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">|"+
						"<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> <" + ctriple1.getObject() + ">";						
			else if (ctriple1.getObject().isLiteral())
				str = "<"+add_triple1.getSubject() +"> <" +add_triple1.getPredicate()+"> <" + add_triple1.getObject() + ">|"+
						"<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> \"" + ctriple1.getObject() + "\"";
		/*/}
		/*		if (!content.contains(str))
			content += str + "\n";
*/
		imodel.add(imodel.asStatement(itriple));

	}

	protected static void createTriples_ran2 (int count) throws IOException {
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
			OntProperty op = ont_model.getOntProperty(property.toString());
			OntResource ran = getRange(op);

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
							Triple ctriple1 = Triple.create(subject.asNode(), property.asNode(), new_object.asNode());
							Triple itriple = Triple.create(new_object.asNode(), type_property.asNode(), obj_class.asNode());	
							
							addConflict_in_ST(ctriple1, add_triple1, itriple, mid, total_triples_generated);
							/*	//other conflicts generated as side effect

							Set<Statement> additional_conflicts = bmodel.listStatements((Resource)null, (Property)null, (RDFNode)new_object).toSet();//
							Iterator<Statement> additional_conflicts_iter = additional_conflicts.iterator();
							while (additional_conflicts_iter.hasNext()) {
								Statement additional_conflicts_stmt = additional_conflicts_iter.next();
								Property current_property = additional_conflicts_stmt.getPredicate();
								Set<OntResource> ranges = getAllRange(ont_model.getOntProperty(current_property.toString()));
								if (ranges.contains(ran)) {
									addConflict_only(additional_conflicts_stmt.asTriple(), add_triple1, mid, total_triples_generated);
//									addConflict_in_ST(additional_conflicts_stmt.asTriple(), add_triple1, itriple, str, mid, total_triples_generatedRan2);
									total_triples_generatedRan2++; 
								}
								}
							//for src
							additional_conflicts = srcmodel.listStatements((Resource)null, property, (RDFNode)new_object).toSet();//(Property)null
							additional_conflicts_iter = additional_conflicts.iterator();
							while (additional_conflicts_iter.hasNext()) {
								Statement additional_conflicts_stmt = additional_conflicts_iter.next();
								Property current_property = additional_conflicts_stmt.getPredicate();
								Set<OntResource> ranges = getAllRange(ont_model.getOntProperty(current_property.toString()));
								if (ranges.contains(ran)) {
									addConflict_only(additional_conflicts_stmt.asTriple(), add_triple1, mid, total_triples_generated);
									total_triples_generatedRan2++; 
								}
								}
							//for tar
							additional_conflicts = tarmodel.listStatements((Resource)null, property, (RDFNode)new_object).toSet();//(Property)null
							additional_conflicts_iter = additional_conflicts.iterator();
							while (additional_conflicts_iter.hasNext()) {
								Statement additional_conflicts_stmt = additional_conflicts_iter.next();
								Property current_property = additional_conflicts_stmt.getPredicate();
								Set<OntResource> ranges = getAllRange(ont_model.getOntProperty(current_property.toString()));
								if (ranges.contains(ran)) {
									addConflict_only(additional_conflicts_stmt.asTriple(), add_triple1, mid, total_triples_generated);
									total_triples_generatedRan2++; 
								}
								}
							*/
							total_triples_generatedRan2++; 	
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
		//String str ="";
		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

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
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();

			OntProperty op = ont_model.getOntProperty(property.toString());
			Iterator<OntResource> ran_iter = getAllRange(op).iterator();
			Set<Node> dcs = new HashSet<Node>();
			while (ran_iter.hasNext()) {
				OntResource ran = ran_iter.next(); 
				System.out.println("range"+ran.toString());
				dcs.addAll(getDisjointClasses(ran.asResource()));
			}
			Iterator<Node> dcs_iter = dcs.iterator(); 
			RDFNode object = stmt.getObject();
			if(object.isResource()) {

				L1:		while (dcs_iter.hasNext()) {
					Triple ctriple1 = null;
					Iterator<Statement> i_stmt = s_stmt.iterator();
					while(i_stmt.hasNext()) {
						Statement s = i_stmt.next();
						if (dcs_iter.next().getURI().equals(s.getObject().asResource().getURI()))
						{
							//create new triple  
							ctriple1 = Triple.create(subject.asNode(), property.asNode(), s.getSubject().asNode());
							if (total_triples_generatedRan3 < mid) 
								srcmodel.add(srcmodel.asStatement(ctriple1));
							else 
								tarmodel.add(tarmodel.asStatement(ctriple1));
							Triple itriple = stmt.asTriple();
						/*	if (ctriple1.getObject().isURI())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|"+
										"<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> <" + ctriple1.getObject() + ">";						
							else if (ctriple1.getObject().isLiteral())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|"+
										"<"+ctriple1.getSubject() +"> <" +ctriple1.getPredicate()+"> \"" + ctriple1.getObject() + "\"";					
								if (!content.contains(str))
								content += str + "\n";*/

							imodel.add(imodel.asStatement(itriple));
							total_triples_generatedRan3++; 	
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