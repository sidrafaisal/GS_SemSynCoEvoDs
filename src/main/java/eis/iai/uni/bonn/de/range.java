package eis.iai.uni.bonn.de;

import java.io.File;
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
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.NodeSet;

public class range extends cGenerator {

	static int total_triples_generatedRan1 = 0;
	static int total_triples_generatedRan2 = 0;

	////////create conflicting type triples for S,A,O ran 1.1
	protected static int createTriples_forType (int count) throws IOException {

		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);
		temp1_model.add(bmodel.listStatements());

		ResIterator resource_iter = bmodel.listSubjectsWithProperty(type_property);
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);
			temp1_model.remove(stmt_iter);
		}

		Model temp_model = getRandomTriples_withResourceObject(temp1_model, (Property)null, count);		
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			RDFNode stmt_object = stmt.getObject();
			if(stmt_object.isResource()) {
				Property current_property = stmt.getPredicate();
				if (!avoid_property.contains(current_property)) {
					OntProperty op = ont_model.getOntProperty(current_property.toString());
					OntResource ran = getRange(op);
					if(ran != null) {
						OWLClass owlclass = fac.getOWLClass(IRI.create(ran.toString()));
						NodeSet<OWLClass> disclass = reasoner.getDisjointClasses(owlclass);		
						for (OWLClass c : disclass.getFlattened()) {

							Node object =	NodeFactory.createURI(c.getIRI().toString());				
							Triple ctriple = Triple.create(stmt_object.asNode(), type_property.asNode(), object);	
							Triple itriple = Triple.create(stmt_object.asNode(), type_property.asNode(), ran.asNode());	
							cmodel.add(cmodel.asStatement(ctriple));
							imodel.add(imodel.asStatement(itriple));
							total_triples_generatedRan1++; 
							break;
						}	
					}
				}
			}
		}		
		temp_model.close();
		temp1_model.close();
		File file = new File("temp1");
		if(file.exists())
			file.delete();
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
