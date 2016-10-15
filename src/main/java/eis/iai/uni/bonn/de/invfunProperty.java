package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.InverseFunctionalProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.rdf.model.Property;

public class invfunProperty extends cGenerator{
	
	static int total_triples_generated = 0;

	protected static int createTriples (int count) throws IOException {

		List<Property> propertyList = new ArrayList<Property>();
		HashSet <Statement> removefrombmodel = new HashSet<Statement>();

		ExtendedIterator<InverseFunctionalProperty> ifps = ont_model.listInverseFunctionalProperties();
		while (ifps.hasNext()) {
			InverseFunctionalProperty ifp = ifps.next();
			propertyList.add(ifp.asProperty());
		}

		Model temp_model = getRandomTriples(bmodel, propertyList, count);
		StmtIterator stmt_iter = temp_model.listStatements();

		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;
	
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			RDFNode object = stmt.getObject();
			Property property = stmt.getPredicate();
			StmtIterator stmt_iter1 = bmodel.listStatements(subject, sameAs_property, (RDFNode)null);

			//generate conflict using object value
			if (stmt_iter1.hasNext()) {
				Statement stmt1 = stmt_iter1.next();

				Triple ctriple = Triple.create(stmt1.getObject().asNode(), property.asNode() , object.asNode());	
				Triple itriple = Triple.create(subject.asNode(), sameAs_property.asNode(), stmt1.getObject().asNode());	
				if (total_triples_generated <= mid) {
					cmodel.add(cmodel.asStatement(ctriple));
				} else {
					removefrombmodel.add(stmt);
					srcmodel.add(srcmodel.asStatement(ctriple));
					ctriple = Triple.create(subject.asNode(), property.asNode() , object.asNode());	
					tarmodel.add(stmt);
				}
				imodel.add(imodel.asStatement(itriple));									
			} else {
				stmt_iter1 = bmodel.listStatements((Resource)null, sameAs_property, subject);
				if (stmt_iter1.hasNext()) {
					Statement stmt1 = stmt_iter1.next();

					Triple ctriple = Triple.create(stmt1.getSubject().asNode(), property.asNode(), object.asNode());	
					Triple itriple = Triple.create(subject.asNode(), sameAs_property.asNode(), stmt1.getSubject().asNode());	
					if (total_triples_generated <= mid) {
						cmodel.add(cmodel.asStatement(ctriple));
					} else {
						removefrombmodel.add(stmt);
						srcmodel.add(srcmodel.asStatement(ctriple));
						ctriple = Triple.create(subject.asNode(), property.asNode(), object.asNode());	
						tarmodel.add(stmt);
					}
					imodel.add(imodel.asStatement(itriple));									
				}
			}
			temp_model.close();
			total_triples_generated++; 
		}

		Iterator<Statement> hs_iter = removefrombmodel.iterator();
		while(hs_iter.hasNext()) {
			Statement t = hs_iter.next();
			bmodel.remove(t);
		}
		return total_triples_generated;
	}
}
