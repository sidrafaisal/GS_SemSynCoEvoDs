package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.InverseFunctionalProperty;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
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

public class eqvProperty extends cGenerator{

	static int total_triples_generated_ep1 = 0;

	protected static int createTriples (int count) throws IOException {

		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);
		
		ResIterator resource_iter = bmodel.listSubjectsWithProperty(difffrom_property);
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);
			temp1_model.add(stmt_iter);
		}
		
		//ResIterator resource_iter = bmodel.listResourcesWithProperty(difffrom_property);
		NodeIterator obj_iter = bmodel.listObjectsOfProperty(difffrom_property);
		while (obj_iter.hasNext()) {
			RDFNode obj = obj_iter.next();
			if (obj.isResource())
				obj.asResource();
	}
		System.out.println(resource_iter.toSet().size()+".....");
//		

	//	Model temp_model = getRandomTriples(temp1_model, (Property)null, count);	
		
	//	Property sameAs_property = ResourceFactory.createProperty(sameAs_property);
	
		//get triples S,A,N where, N is resource 
		Model temp_model = getRandomTriples_withResourceObject(bmodel, (Property)null, count);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;
		long counter = 0;		
		
		StmtIterator stmt_iter = temp_model.listStatements();
		while ( stmt_iter.hasNext() ) {
					
			 Statement stmt = stmt_iter.next();			 
			 Resource subject = stmt.getSubject();
			 Property property = stmt.getPredicate();
			 RDFNode object = stmt.getObject();
			 
			 //create triple S,B,N where A=B
			OntProperty op = ont_model.getOntProperty(property.toString());			 
			ExtendedIterator<? extends OntProperty> eps = op.listEquivalentProperties();
			OntProperty ep = null;
				while(eps.hasNext()) {
					ep = eps.next();
					if (!ep.equals(op))
						break;
				}
			if (ep != null) { System.out.println();	
				Property eq_property = ResourceFactory.createProperty(ep.getURI());				
				Triple ctriple1 = Triple.create(subject.asNode(), eq_property.asNode(), object.asNode());

				//create triple S,B,O where O is different from N	
				Resource r1 = null;
				Resource r = bmodel.getResource(object.toString());
				StmtIterator dstmt_iter = bmodel.listStatements((Resource)null, difffrom_property, r);
				while(dstmt_iter.hasNext()) {
					Statement dstmt = dstmt_iter.next();
					r1 = dstmt.getSubject();
					break;
				}
				if (r1 == null) 
					r1 = r.getPropertyResourceValue(difffrom_property);
				
				if (r1 != null) { 
				Triple ctriple2 = Triple.create(subject.asNode(), eq_property.asNode(), r1.asResource().asNode());					

				if (total_triples_generated_ep1 < mid) {
					srcmodel.add(srcmodel.asStatement(ctriple1));
					tarmodel.add(tarmodel.asStatement(ctriple2));
				} else {
					tarmodel.add(tarmodel.asStatement(ctriple1));
					srcmodel.add(srcmodel.asStatement(ctriple2));
				}
				Triple itriple1 = Triple.create(subject.asNode(), eq_property.asNode(), object.asNode());	
				imodel.add(imodel.asStatement(itriple1));
				total_triples_generated_ep1++;
				}
			}
		}
		return total_triples_generated_ep1;
	}
	
}
