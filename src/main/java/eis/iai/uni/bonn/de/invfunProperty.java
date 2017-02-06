package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.ArrayList;
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

public class InvfunProperty extends ChangeGenerator {

	protected static void createTriples (int count) throws IOException {
	//	String str ="";
		List<Property> propertyList = new ArrayList<Property>();
		ExtendedIterator<InverseFunctionalProperty> ifps = ont_model.listInverseFunctionalProperties();
		while (ifps.hasNext()) {
			InverseFunctionalProperty ifp = ifps.next();
			propertyList.add(ifp.asProperty());
		}

		Model temp_model = getRandomTriples(bmodel, propertyList, count, "", false);
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;

		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			RDFNode object = stmt.getObject();
			Property property = stmt.getPredicate();

			Triple ctriple = null, itriple = null; 
			RDFNode new_subject = getsame_resource(subject);

			//generate conflict t,p,o for s,p,o where s=t

			if (new_subject != null) {
				ctriple = Triple.create(new_subject.asNode(), property.asNode() , object.asNode());	
				itriple = Triple.create(subject.asNode(), sameas_property.asNode(), new_subject.asNode());

				if (total_triples_generated_ifp <= mid) {
					if (total_triples_generated_ifp <= mid/2)
						srcmodel.add(srcmodel.asStatement(ctriple));
					else
						tarmodel.add(tarmodel.asStatement(ctriple));			

		/*			if (stmt.getObject().isResource())
						str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
								"<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">";
					else if (stmt.getObject().isLiteral())
						str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
								"<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">";						
*/
				} else {
					srcmodel.add(srcmodel.asStatement(ctriple));
					tarmodel.add(stmt);
/*
					if (stmt.getObject().isResource())
						str = "<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">|"+
								"<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">";								
					else if (stmt.getObject().isLiteral())
						str = "<"+ctriple.getSubject() +"> <" +ctriple.getPredicate()+"> <" + ctriple.getObject() + ">|"+
								"<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"";
		*/		}
			/*		if (!content.contains(str))
					content += str + "\n";*/
				imodel.add(imodel.asStatement(itriple));	
				total_triples_generated_ifp++; 
			}
		}
		temp_model.close();
	}
}
