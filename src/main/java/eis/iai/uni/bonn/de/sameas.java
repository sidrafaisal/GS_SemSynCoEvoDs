package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

public class sameas extends cGenerator {

	static int total_triples_generated_sap1 = 0;

	protected static int createTriples_sap1 (int count) throws IOException {

		// get resources which have diff_from info to get maximum number of required conflicts
		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

		Set<Resource> d_sub = bmodel.listSubjectsWithProperty(difffrom_property).toSet();
		Set<RDFNode> d_obj = bmodel.listObjectsOfProperty(difffrom_property).toSet();

		ResIterator resource_iter = bmodel.listSubjectsWithProperty(sameas_property);
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);
			while(stmt_iter.hasNext()) {
				Statement s = stmt_iter.next();
				if (s.getObject().isResource()) {
					Resource object = s.getObject().asResource();
					if(d_sub.contains(object) || d_obj.contains(object))
						temp1_model.add(s);
				}
			}
		}
		NodeIterator obj_iter = bmodel.listObjectsOfProperty(sameas_property);
		while (obj_iter.hasNext()) {		
			Resource subject = obj_iter.next().asResource();
			StmtIterator stmt_iter = bmodel.listStatements(subject, (Property)null, (RDFNode)null);
			while(stmt_iter.hasNext()) {
				Statement s = stmt_iter.next();
				if (s.getObject().isResource()) {
					Resource object = s.getObject().asResource();
					if(d_sub.contains(object) || d_obj.contains(object))
						temp1_model.add(s);
				}
			}
		}		
		temp1_model.removeAll((Resource)null, type_property, (RDFNode)null);

		//get triples S1,A,N where, N is resource 
		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "", true);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		StmtIterator stmt_iter = temp_model.listStatements();
		while ( stmt_iter.hasNext() ) {
			Statement stmt = stmt_iter.next();		 
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			//create triple S,A,N where S1=S
			StmtIterator s_iter = bmodel.listStatements(subject, sameas_property, (RDFNode)null);
			Resource  s_subject = null;

			if (s_iter.hasNext()) {
				s_subject = s_iter.next().getObject().asResource();
			} else {
				s_iter = bmodel.listStatements((Resource)null, sameas_property, (RDFNode)subject.asResource());	
				if (s_iter.hasNext()) 
					s_subject = s_iter.next().getSubject();				
			}
			//create triple S,A,O where O is different from N	
			if (s_subject != null) {	
				Resource arr[] = getdiff_resources (object); 
				Resource r1 = arr[0];
				Resource r2 = arr[1];

				Triple ctriple1, ctriple2;
				if (r1 != null) { 
					ctriple1 = Triple.create(s_subject.asNode(), property.asNode(), r1.asResource().asNode());
					if(r2 != null) 
						ctriple2 = Triple.create(s_subject.asNode(), property.asNode(), r2.asResource().asNode());	
					else 
						ctriple2 = Triple.create(s_subject.asNode(), property.asNode(), object.asNode());

					if (total_triples_generated_sap1 < mid) {
						srcmodel.add(srcmodel.asStatement(ctriple1));
						tarmodel.add(tarmodel.asStatement(ctriple2));
					} else {
						tarmodel.add(tarmodel.asStatement(ctriple1));
						srcmodel.add(srcmodel.asStatement(ctriple2));
					}
					Triple itriple1 = Triple.create(s_subject.asNode(), property.asNode(), object.asNode());	
					imodel.add(imodel.asStatement(itriple1));
					total_triples_generated_sap1++;
				}	
			}
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
		return total_triples_generated_sap1;
	}

}
