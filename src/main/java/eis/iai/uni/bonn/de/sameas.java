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

public class Sameas extends ChangeGenerator {	
	protected static void createTriples_sap1 (int count) throws IOException {
		// get resources which have diff_from info - i.e. filter to get maximum number of required conflicts	
		Model temp1_model = FileManager.get().loadModel(createfile("temp1"), filesyntax);

		Set<Resource> d_sub = diff_resource_iter;
		Set<RDFNode> d_obj = diff_obj_iter;

		ResIterator resource_iter = bmodel.listSubjectsWithProperty(sameas_property);
		while (resource_iter.hasNext()) {	
			StmtIterator stmt_iter = bmodel.listStatements(resource_iter.next(), (Property)null, (RDFNode)null);
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
			StmtIterator stmt_iter = bmodel.listStatements(obj_iter.next().asResource(), (Property)null, (RDFNode)null);
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
		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "", true);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		//get triples S1,A,N where, N is resource 
		StmtIterator stmt_iter = temp_model.listStatements();
		while ( stmt_iter.hasNext() ) {
			Statement stmt = stmt_iter.next();	
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			Resource  s_subject = getsame_resource(stmt.getSubject());		//to create triple S,A,N where S1=S

			//create triple S,A,O where O is different from N	
			if (s_subject != null) {	
				Resource arr[] = getdiff_resources (object); 
				Resource r1 = arr[0];
				Resource r2 = arr[1];

				Triple ctriple1, ctriple2;
				if (r1 != null) { 
					tcg_model.add(tcg_model.asStatement(Triple.create(r1.asNode(), difffrom_property.asNode(), object.asNode())));
					ctriple1 = Triple.create(s_subject.asNode(), property.asNode(), r1.asResource().asNode());
					if(r2 != null) {
						tcg_model.add(tcg_model.asStatement(Triple.create(r2.asNode(), difffrom_property.asNode(), object.asNode())));
						ctriple2 = Triple.create(s_subject.asNode(), property.asNode(), r2.asResource().asNode());	
					} else 
						ctriple2 = Triple.create(s_subject.asNode(), property.asNode(), object.asNode());
					if (total_triples_generated_sap1 < mid) {
						if (!srcmodel.contains(srcmodel.asStatement(ctriple1)) && 
								!tarmodel.contains(tarmodel.asStatement(ctriple2))){
							total_triples_generated_sap1++;
						srcmodel.add(srcmodel.asStatement(ctriple1));
						tarmodel.add(tarmodel.asStatement(ctriple2));}
					} else {
						if (!tarmodel.contains(tarmodel.asStatement(ctriple1)) && 
								!srcmodel.contains(srcmodel.asStatement(ctriple2))){
							total_triples_generated_sap1++;
						tarmodel.add(tarmodel.asStatement(ctriple1));
						srcmodel.add(srcmodel.asStatement(ctriple2));
					}
					}
					Triple itriple1 = Triple.create(s_subject.asNode(), property.asNode(), object.asNode());	
					imodel.add(imodel.asStatement(itriple1));
					tcg_model.add(stmt);
				}	
			}
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
	}

	protected static void createTriples_sap2 (int count) throws IOException {
		Model temp1_model = FileManager.get().loadModel(createfile("temp1"), filesyntax);
		
		// get resources which have sameas info - i.e. filter to get maximum number of required conflicts
		ResIterator resource_iter = bmodel.listSubjectsWithProperty(sameas_property);
		while (resource_iter.hasNext()) 		
			temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, (RDFNode)resource_iter.next()));					
		NodeIterator obj_iter = bmodel.listObjectsOfProperty(sameas_property);
		while (obj_iter.hasNext()) 
			temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, (RDFNode)obj_iter.next().asResource()));					

		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "", true);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	
		//get s,a,n
		StmtIterator stmt_iter = temp_model.listStatements();
		while ( stmt_iter.hasNext() ) {
			Statement stmt = stmt_iter.next();		 
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			//create 2 triples like S,A,O where O is sameas N	
				Resource arr[] = getsame_resources(object);
				Resource r1 = arr[0];
				Resource r2 = arr[1];

				Triple ctriple1, ctriple2 = null;
				if (r1 != null) { 
					tcg_model.add(tcg_model.asStatement(Triple.create(r1.asNode(), difffrom_property.asNode(), object.asNode())));
					ctriple1 = Triple.create(subject.asNode(), property.asNode(), r1.asResource().asNode());
					if(r2 != null) 
						ctriple2 = Triple.create(subject.asNode(), property.asNode(), r2.asResource().asNode());
					
					if (total_triples_generated_sap2 < mid) {
						if (!srcmodel.contains(srcmodel.asStatement(ctriple1)) && 
								(ctriple2!=null && !tarmodel.contains(tarmodel.asStatement(ctriple2)))){
							total_triples_generated_sap2++;					
						srcmodel.add(srcmodel.asStatement(ctriple1));						
						if (ctriple2!=null) 
							tarmodel.add(tarmodel.asStatement(ctriple2));
						}
					} else {							
						if (!tarmodel.contains(tarmodel.asStatement(ctriple1)) && 
								(ctriple2!=null && !srcmodel.contains(srcmodel.asStatement(ctriple2)))){
							total_triples_generated_sap2++;			
						tarmodel.add(tarmodel.asStatement(ctriple1));
						if (ctriple2!=null) 
							srcmodel.add(srcmodel.asStatement(ctriple2));
						}
					}
					Triple itriple1 = Triple.create(subject.asNode(), property.asNode(), r1.asResource().asNode());	
					imodel.add(imodel.asStatement(itriple1));
					if(r2 != null) {
						tcg_model.add(tcg_model.asStatement(Triple.create(r2.asNode(), difffrom_property.asNode(), object.asNode())));
						itriple1 = Triple.create(subject.asNode(), property.asNode(), r2.asResource().asNode());	
						imodel.add(imodel.asStatement(itriple1));
					}
					tcg_model.add(stmt);
				}				
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
	}
}