package eis.iai.uni.bonn.de;

import java.io.IOException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.NodeSet;

public class domain extends cGenerator {

	static int total_triples_generatedDom1 = 0;
	static int total_triples_generatedDom2 = 0;

	////////create conflicting type triples for S,A,O dom 1.1
	protected static int createTriples_forType (int count) throws IOException {
		Model temp_model = getRandomTriples(bmodel, (Property)null, count,"domain", false);		
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Property current_property = stmt.getPredicate();
			OntProperty op = ont_model.getOntProperty(current_property.getURI());
			OntResource dom = getDomain(op);
	//		if (dom.toString().equals("http://dbpedia.org/ontology/Person")) 
		//		System.out.println(current_property.toString()+"----------------"+dom.toString());
			if(dom!=null) {				
				Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), getDisjointClass(dom));	
				Triple itriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), dom.asNode());	
				cmodel.add(cmodel.asStatement(ctriple));
				imodel.add(imodel.asStatement(itriple));
			//	if (dom.toString().equals("http://dbpedia.org/ontology/Person")) 
				//	System.out.println(stmt.toString()+"..........."+ctriple.toString());
				total_triples_generatedDom1++; 
			}
	}		
		temp_model.close();
		return total_triples_generatedDom1;
	}


	//Generate conflicts for dom 1.2 With SP (in progress...)
	protected static int createTriples_forSubProperty (int count) throws IOException {

		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);
		temp1_model.add(bmodel.listStatements());

		ResIterator resource_iter = bmodel.listSubjectsWithProperty(type_property);
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);
			temp1_model.remove(stmt_iter);
		}

		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "domain", false);		
		StmtIterator stmt_iter = temp_model.listStatements();

		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Property current_property = stmt.getPredicate();
			OntProperty op = ont_model.getOntProperty(current_property.toString());
			if (op!=null) {
				if (op.getSuperProperty()!=null)
					op = op.getSuperProperty();			
				OntResource dom = getDomain(op);
				if (dom!=null) {
					OWLClass owlclass = fac.getOWLClass(IRI.create(dom.toString()));
					NodeSet<OWLClass> disclass = reasoner.getDisjointClasses(owlclass);		
					for (OWLClass c : disclass.getFlattened()) {

						Node object =	NodeFactory.createURI(c.getIRI().toString());				
						Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), object);	
						Triple itriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), dom.asNode());	
						cmodel.add(cmodel.asStatement(ctriple));
						imodel.add(imodel.asStatement(itriple));
						total_triples_generatedDom2++;
						break;
					}
				}				
			}
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
		return total_triples_generatedDom2;
	}
}
