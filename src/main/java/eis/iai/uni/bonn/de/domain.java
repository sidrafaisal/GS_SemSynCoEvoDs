package eis.iai.uni.bonn.de;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

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
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.NodeSet;

public class domain extends cGenerator {

	static int total_triples_generatedType = 0;
	static int total_triples_generatedDom1 = 0;
	static int total_triples_generatedDom2 = 0;

	////// Read model to get triples whose resource type is already available then, generate conflicts for them (disjoint)   

	protected static int createTriples_forExistingType(int count) throws IOException {
		Model temp_model = getRandomTriples(bmodel, type_property, count);		
		Set<Resource> resources= temp_model.listSubjects().toSet();
		Iterator<Resource> resource_iter = resources.iterator();

		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);

			while (stmt_iter.hasNext()) {		
				Statement stmt = stmt_iter.next();			
				Property current_property = stmt.getPredicate();
				if (!(current_property.equals(type_property) || avoid_property.contains(current_property))) { 
					//generate conflict for any random property of this resource

					OntProperty op = ont_model.getOntProperty(current_property.toString());	
					OntResource dom = getDomain(op);

					if(dom != null) {				
						OWLClass owlclass = fac.getOWLClass(IRI.create(dom.toString()));
						NodeSet<OWLClass> disclass = reasoner.getDisjointClasses(owlclass);		
						for (OWLClass c : disclass.getFlattened()) {
							Node object =	NodeFactory.createURI(c.getIRI().toString());				
							Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), object);	
							Triple itriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), dom.asNode());	

							cmodel.add(cmodel.asStatement(ctriple));
							imodel.add(imodel.asStatement(itriple));
							total_triples_generatedType++;
							break;
						}	
						break;
					}
				}				
			}			
		}	
		temp_model.close();
		return total_triples_generatedType;
	}

	////////create conflicting type triples for S,A,O dom 1.1
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

		Model temp_model = getRandomTriples(temp1_model, (Property)null, count);		
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Property current_property = stmt.getPredicate();
			if (!avoid_property.contains(current_property)) {
				OntProperty op = ont_model.getOntProperty(current_property.toString());
				OntResource dom = getDomain(op);
				if(dom!=null) {

					OWLClass owlclass = fac.getOWLClass(IRI.create(dom.toString()));
					NodeSet<OWLClass> disclass = reasoner.getDisjointClasses(owlclass);		
					for (OWLClass c : disclass.getFlattened()) {

						Node object =	NodeFactory.createURI(c.getIRI().toString());				
						Triple ctriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), object);	
						Triple itriple = Triple.create(stmt.getSubject().asNode(), type_property.asNode(), dom.asNode());	
						cmodel.add(cmodel.asStatement(ctriple));
						imodel.add(imodel.asStatement(itriple));
						total_triples_generatedDom1++; 
						break;
					}	
				}
			}
		}		
		temp1_model.close();
		File file = new File("temp1");
		if(file.exists())
			file.delete();
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

		Model temp_model = getRandomTriples(temp1_model, avoid_property, count);		
		StmtIterator stmt_iter = temp_model.listStatements();

		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Property current_property = stmt.getPredicate();
			OntProperty op = ont_model.getOntProperty(current_property.toString());

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
		temp_model.close();
		temp1_model.close();
		File file = new File("temp1");
		if(file.exists())
			file.delete();
		return total_triples_generatedDom2;
	}

	///////////////////get domain of some property
	static OntResource getDomain(OntProperty op){
		OntResource dom = null;
		if( op.getDomain() != null)
			dom=op.getDomain();
		else {
			System.out.println("Domain not found for property: "+ op.toString());
			ExtendedIterator<? extends OntProperty> eps = op.listEquivalentProperties();
			while(eps.hasNext()) {
				OntProperty ep = eps.next();
				if (ep.getDomain()!=null) {
					dom = ep.getDomain();
					break;
				}
			}
		}
		return dom;
	}
}
