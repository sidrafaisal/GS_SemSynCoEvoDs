package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.FunctionalProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

public class FunProperty extends ConflictGenerator {

	static int total_triples_generated = 0;

	protected static int createTriples (int count) throws IOException {

		List<Property> propertyList = new ArrayList<Property>();
		ExtendedIterator<FunctionalProperty> fps = ont_model.listFunctionalProperties();
		while (fps.hasNext()) {
			FunctionalProperty fp = fps.next();
			propertyList.add(fp.asProperty());
		}

		Model temp_model = getRandomTriples(bmodel, propertyList, count, "", true);
		StmtIterator stmt_iter = temp_model.listStatements();

		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;
		// get S,A,O 
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			RDFNode object = stmt.getObject();
			Property property = stmt.getPredicate();

			Resource [] arr = getsame_resources (object);
			Resource r1 = arr[0];
			Resource r2 = arr[1];
			// create S,A,X and S,A,Y where X=Y=O 
			Triple ctriple1 = null, ctriple2 = null, itriple1 = null, itriple2 = null;			
			if (r1 != null) {
				ctriple1 = Triple.create(subject.asNode(), property.asNode() , r1.asNode());
				itriple1 = Triple.create(subject.asNode(), property.asNode(), r1.asNode());

				if(r2 != null) {
					ctriple2 = Triple.create(subject.asNode(), property.asNode(), r2.asNode());
					itriple2 = Triple.create(subject.asNode(), property.asNode(), r2.asNode());
				}
				if (ctriple1 != null & ctriple2 == null) {
					imodel.add(imodel.asStatement(itriple1));
					if (total_triples_generated <= mid)
						srcmodel.add(srcmodel.asStatement(ctriple1));
					else
						tarmodel.add(tarmodel.asStatement(ctriple1));
				} else if (ctriple1 != null & ctriple2 != null) {
					imodel.add(imodel.asStatement(itriple1));
					imodel.add(imodel.asStatement(itriple2));
					if (total_triples_generated <= mid) {
						srcmodel.add(srcmodel.asStatement(ctriple1));
						tarmodel.add(tarmodel.asStatement(ctriple2));
					} else {
						srcmodel.add(srcmodel.asStatement(ctriple2));
						tarmodel.add(tarmodel.asStatement(ctriple1));
					}
				}
				total_triples_generated++; 
			}	
		}
		temp_model.close();
		return total_triples_generated;
	}
}