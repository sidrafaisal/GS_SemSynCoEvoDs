package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

public class Diffrom extends ConflictGenerator {

	static int total_triples_generated_dfp2 = 0;	
	static int total_triples_generated_dfp3 = 0;	

	protected static int createTriples_dfp2 (int count) throws IOException {

		// get resources which have diff_from info to get maximum number of required conflicts
		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

		Iterator<Resource> resource_iter = diff_resource_iter.iterator();
		Iterator<RDFNode> obj_iter = diff_obj_iter.iterator();
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, (RDFNode)subject));
		}
		while (obj_iter.hasNext()) {
			RDFNode obj = obj_iter.next();
			if (obj.isResource()) 
				temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, obj.asResource()));
		}

		//get triples S,A,N where, N is resource 
		Model temp_model = getRandomTriples(temp1_model, (Property)null, count, "df2", true);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		StmtIterator stmt_iter = temp_model.listStatements();
		while ( stmt_iter.hasNext() ) {
			Statement stmt = stmt_iter.next();			 
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			//create triple S,B,N where B is subproperty of A
			OntProperty sp = getSubProperty(property);
			if (sp != null) { 
				Property sub_property = ResourceFactory.createProperty(sp.getURI());				
				Resource arr[] = getdiff_resources(object);
				Resource r1 = arr[0];
				Resource r2 = arr[1];

				//create triple S,B,O where O is different from N		
				Triple ctriple1, ctriple2;
				if (r1 != null) { 
					ctriple1 = Triple.create(subject.asNode(), sub_property.asNode(), r1.asNode());
					if(r2 != null) 
						ctriple2 = Triple.create(subject.asNode(), sub_property.asNode(), r2.asNode());	//(subpropertyOf(A,B,UID) & fromDataset(S, B, N) & fromSrcDataset(S, A, M) & fromTarDataset(S, A, O) & diffrom(N,M) & diffrom(N,O) & diffrom(M,O)) 
					else 
						ctriple2 = Triple.create(subject.asNode(), sub_property.asNode(), object.asNode()); //(subpropertyOf(A,B,UID) & fromDataset(S, B, N) & fromSrcDataset(S, A, N) & fromTarDataset(S, A, O) & diffrom(N,O))

					if (total_triples_generated_dfp2 < mid) {
						srcmodel.add(srcmodel.asStatement(ctriple1));
						tarmodel.add(tarmodel.asStatement(ctriple2));
					} else {
						tarmodel.add(tarmodel.asStatement(ctriple1));
						srcmodel.add(srcmodel.asStatement(ctriple2));
					}
					Triple itriple1 = Triple.create(subject.asNode(), sub_property.asNode(), object.asNode());	
					imodel.add(imodel.asStatement(itriple1));
					total_triples_generated_dfp2++;
				}
			}			
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
		return total_triples_generated_dfp2;
	}

	protected static int createTriples_dfp3 (int count) throws IOException {

		// get resources which have diff_from info to get maximum number of required conflicts
		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

		Iterator<Resource> resource_iter = diff_resource_iter.iterator();
		Iterator<RDFNode> obj_iter = diff_obj_iter.iterator();
		
		while (resource_iter.hasNext()) {		
			Resource subject = resource_iter.next();
			temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, (RDFNode)subject));
		}
		while (obj_iter.hasNext()) {
			RDFNode obj = obj_iter.next();
			if (obj.isResource()) 
				temp1_model.add(bmodel.listStatements((Resource)null, (Property)null, obj.asResource()));			
		}

		//get triples S,A,N where, N is resource 
		Model temp_model = getRandomTriples(temp1_model, (Property)null, count,"df3", true);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	

		StmtIterator stmt_iter = temp_model.listStatements();
		while ( stmt_iter.hasNext() ) {
			Statement stmt = stmt_iter.next();			 
			Resource subject = stmt.getSubject();
			Property property = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			//create triple S,B,N where A=B
			OntProperty ep = getEqvProperty(property);
			if (ep != null) { 
				Property eq_property = ResourceFactory.createProperty(ep.getURI());				

				//create triple S,B,O where O is different from N	
				Resource arr[] = getdiff_resources(object);
				Resource r1 = arr[0];
				Resource r2 = arr[1];
				Triple ctriple1, ctriple2;
				if (r1 != null) { 
					ctriple1 = Triple.create(subject.asNode(), eq_property.asNode(), r1.asResource().asNode());
					if(r2 != null) 
						ctriple2 = Triple.create(subject.asNode(), eq_property.asNode(), r2.asResource().asNode());	
					else 
						ctriple2 = Triple.create(subject.asNode(), eq_property.asNode(), object.asNode());

					if (total_triples_generated_dfp3 < mid) {
						srcmodel.add(srcmodel.asStatement(ctriple1));
						tarmodel.add(tarmodel.asStatement(ctriple2));
					} else {
						tarmodel.add(tarmodel.asStatement(ctriple1));
						srcmodel.add(srcmodel.asStatement(ctriple2));
					}
					Triple itriple1 = Triple.create(subject.asNode(), eq_property.asNode(), object.asNode());	
					imodel.add(imodel.asStatement(itriple1));
					total_triples_generated_dfp3++;
				}
			}
		}
		temp_model.close();
		temp1_model.close();
		deletefile("temp1");
		return total_triples_generated_dfp3;
	}
}