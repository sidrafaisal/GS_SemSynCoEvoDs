package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class StringObjects extends ChangeGenerator {

	protected static void createTriples_forStrings (int count) throws IOException {		
		List<Property> property_list = new ArrayList<Property>();
		property_list.add(ResourceFactory.createProperty("http://dbpedia.org/ontology/birthPlace"));
		property_list.add(ResourceFactory.createProperty("http://dbpedia.org/ontology/deathPlace"));
		property_list.add(ResourceFactory.createProperty("http://dbpedia.org/ontology/name"));
		property_list.add(ResourceFactory.createProperty("http://dbpedia.org/ontology/birthName"));
		property_list.add(ResourceFactory.createProperty("http://dbpedia.org/ontology/deathDate"));
		property_list.add(ResourceFactory.createProperty("http://dbpedia.org/ontology/birthDate"));
		Model temp_model = getRandomTriples(bmodel, property_list, count,"str", false);	
		long mid = temp_model.size()/2 + (temp_model.size()%2) - 1;	
		StmtIterator stmt_iter = temp_model.listStatements();
		while (stmt_iter.hasNext()) {			
			Statement stmt = stmt_iter.next();
			String object = stmt.getObject().toString();
			StringBuilder orig = new StringBuilder(object);
			Property property = stmt.getPredicate();
			String newobject = "";
			if (property.getURI().toLowerCase().contains("date")) {
				// DateFormat format1 = new SimpleDateFormat("MM-dd-yyyy");
				DateFormat sdf = new SimpleDateFormat("YYYY-mm-dd");
				sdf.setLenient(true);
				try {
					newobject = new SimpleDateFormat("MMMMM dd, yyyy").format(sdf.parse(object));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				System.out.println(newobject); 
				Triple ctriple = Triple.create(stmt.getSubject().asNode(), property.asNode(), bmodel.createLiteral(newobject).asNode());
				if (total_triples_generated_String < mid) {
					if (!srcmodel.contains(srcmodel.asStatement(ctriple)))	
						srcmodel.add(srcmodel.asStatement(ctriple));
				} else {
					if (!tarmodel.contains(tarmodel.asStatement(ctriple)))
						tarmodel.add(tarmodel.asStatement(ctriple));
				}
			}  else { 
				if (object.length()<20 && object.contains(",")) {
					object = object.replaceAll("\"", "").replaceAll(" ", "");
					int i = object.indexOf(",");
					newobject = object.substring(i+1, object.length()) + "," + object.substring(0, i);
					Triple ctriple = Triple.create(stmt.getSubject().asNode(), property.asNode(), bmodel.createLiteral(newobject).asNode());
					if (total_triples_generated_String < mid) {
						if (!srcmodel.contains(srcmodel.asStatement(ctriple))){
							total_triples_generated_String++;	
							srcmodel.add(srcmodel.asStatement(ctriple));
						}
					} else {
						if (!tarmodel.contains(tarmodel.asStatement(ctriple))){
							total_triples_generated_String++;	
							tarmodel.add(tarmodel.asStatement(ctriple));
						}
					}
					imodel.add(imodel.asStatement(ctriple));
					ConflictsGenerator.truthvalues.add(imodel.asStatement(ctriple));
				} 
				//97-a,65-A
				Random rnd = new Random();
				int maskSize = rnd.nextInt(orig.length()+1);
				for (int i = 0; i < maskSize; i++) {
					int pos = rnd.nextInt(orig.length());
					int ac = (int)orig.charAt(pos); 
					/*   while (orig.charAt(pos) == '*') {
				        pos++;
				        if (pos >= orig.length()) {
				            pos = 0;
				        }
				    }*/
					if ((ac>=65 && ac<=89) || (ac>=97 && ac<=121))
						orig.setCharAt(pos, ((char) (ac+1)));
				}
				String object1 = orig.toString();
				System.out.println(object1);

				Triple ctriple1 = Triple.create(stmt.getSubject().asNode(), property.asNode(), bmodel.createLiteral(object1).asNode());
				if (total_triples_generated_String < mid) {
					if (!srcmodel.contains(srcmodel.asStatement(ctriple1))){
						total_triples_generated_String++;	
						srcmodel.add(srcmodel.asStatement(ctriple1));
					}
				} else {
					if (!tarmodel.contains(tarmodel.asStatement(ctriple1))){
						total_triples_generated_String++;	
						tarmodel.add(tarmodel.asStatement(ctriple1));
					}
				}
				double dissimiliar = 0.0;

				int maxLen = Math.max(object.length(), object1.length());
				if (maxLen == 0)
					dissimiliar = 0.0;		
				if ((object.matches("[0-9,.,-,/]+") && !object1.matches("[0-9,.,-,/]+")) || (!object.matches("[0-9,.,-,/]+") && object1.matches("[0-9,.,-,/]+")))
					dissimiliar = 0.0;
				else {
					double ldist = StringUtils.getLevenshteinDistance(object, object1); //greater the diff,lesser the similarity
					double sim = 1.0 - (ldist / maxLen);
					if (sim < 0.5)
						dissimiliar = 1.0 - sim;
				}	
				tcg_model.add(stmt);
				if (dissimiliar > 0.0){
					imodel.add(stmt);
					ConflictsGenerator.truthvalues.add(stmt);
					get_pattern(stmt, imodel.asStatement(ctriple1));
				} else {
					imodel.add(imodel.asStatement(ctriple1));
					ConflictsGenerator.truthvalues.add(imodel.asStatement(ctriple1));
				}	
			}
		}
		temp_model.close();
	}
	private static void get_pattern(Statement stmt, Statement src_stmt) {
		String str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
				"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"";

		if (!ConflictsGenerator.conflicts.contains(str))
			ConflictsGenerator.conflicts += str + "\n";
	}
}
