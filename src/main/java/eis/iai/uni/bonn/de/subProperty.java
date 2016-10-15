package eis.iai.uni.bonn.de;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.jena.graph.Triple;
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

public class subProperty extends cGenerator{

	static int total_triples_generated_sp1 = 0;
	
	protected static int createTriples_sp1 (int count) throws IOException {

		// get resources which have diff_from info to get maximum number of required conflicts
		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

		ResIterator resource_iter = bmodel.listSubjectsWithProperty(difffrom_property);
		NodeIterator obj_iter = bmodel.listObjectsOfProperty(difffrom_property);

		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);
			temp1_model.add(stmt_iter);
			stmt_iter = bmodel.listStatements((Resource)null, (Property)null, (RDFNode)subject);
			temp1_model.add(stmt_iter);
		}

		while (obj_iter.hasNext()) {
			RDFNode obj = obj_iter.next();
			if (obj.isResource()) {
				StmtIterator stmt_iter = bmodel.listStatements(obj.asResource(), (Property)null, (RDFNode)null);
				temp1_model.add(stmt_iter);
				stmt_iter = bmodel.listStatements((Resource)null, (Property)null, obj.asResource());
				temp1_model.add(stmt_iter);
			}
		}
		
		//get triples S,A,N where, N is resource 
		Model temp_model = getRandomTriples_withResourceObject(temp1_model, (Property)null, count);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		StmtIterator stmt_iter = temp_model.listStatements();
		while ( stmt_iter.hasNext() ) {

			Statement stmt = stmt_iter.next();			 
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			//create triple S,B,N where A=B
			OntProperty op = ont_model.getOntProperty(property.toString());			 
			ExtendedIterator<? extends OntProperty> sps = op.listSubProperties();
			ExtendedIterator<? extends OntProperty> eps = op.listEquivalentProperties();
			Set<? extends OntProperty> s_eps= eps.toSet();
			OntProperty sp = null;
			while(sps.hasNext()) {
				OntProperty p = sps.next();
				if (!(p.equals(op) || s_eps.contains(p))) {
					sp = p;
					break;
				}
			}
			if (sp != null) { 
				Property sub_property = ResourceFactory.createProperty(sp.getURI());				

				//create triple S,B,O where O is different from N	
				Resource r1 = null;
				Resource r2 = null;
				Resource r = object.asResource();///temp1_model.getResource(object.toString());
				ResIterator dstmt_iter = temp1_model.listResourcesWithProperty(difffrom_property, r);
				while (dstmt_iter.hasNext()) {
					if (r1 == null)
						r1 = dstmt_iter.next();
					else if (r2 == null) {
						Resource res = dstmt_iter.next(); 
						if (!r1.equals(res))
							r2 = res; 
					} else
						break;
				}
				if (r1 == null || r2 == null ) {
				//r1 = r.getPropertyResourceValue(difffrom_property);
					obj_iter = temp1_model.listObjectsOfProperty(r, difffrom_property);
					while (obj_iter.hasNext()) {
						RDFNode obj = obj_iter.next();
						if (obj.isResource()) {
							if (r1 == null)
								r1 = obj.asResource();
							else if (r2 == null) {
								Resource res = obj.asResource(); 
								if (!r1.equals(res))
									r2 = res; 
							} else							
								break;
						}
					}
				}	
				Triple ctriple1, ctriple2;
				if (r1 != null) { 
					ctriple1 = Triple.create(subject.asNode(), sub_property.asNode(), r1.asResource().asNode());
					if(r2 != null) 
						ctriple2 = Triple.create(subject.asNode(), sub_property.asNode(), r2.asResource().asNode());	
					else 
						ctriple2 = Triple.create(subject.asNode(), sub_property.asNode(), object.asNode());

					
					if (total_triples_generated_sp1 < mid) {
						srcmodel.add(srcmodel.asStatement(ctriple1));
						tarmodel.add(tarmodel.asStatement(ctriple2));
					} else {
						tarmodel.add(tarmodel.asStatement(ctriple1));
						srcmodel.add(srcmodel.asStatement(ctriple2));
					}
					Triple itriple1 = Triple.create(subject.asNode(), sub_property.asNode(), object.asNode());	
					imodel.add(imodel.asStatement(itriple1));
					total_triples_generated_sp1++;
				}
			}
		}
		temp_model.close();
		temp1_model.close();
		File file = new File("temp1");
		if(file.exists())
			file.delete();
		return total_triples_generated_sp1;
	}
}
