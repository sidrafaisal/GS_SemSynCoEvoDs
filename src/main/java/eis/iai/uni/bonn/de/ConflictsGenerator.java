package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ConflictsGenerator extends ChangeGenerator {
	public static String new_content = "";

	protected static void generateConflicts(Model m, Model m1, Model m2) throws IOException{
		System.out.println("Generating conflicting patterns (to be compared with ones detected by PSL).");
		domain_range_patterns(m, m1, m2);
		disjoint_patterns(m, m1, m2);
		sub_equivalentclass_patterns(m, m1, m2); 
		sameas_patterns(m, m1, m2);
	}
	//		m.add rule : (fromDataset(T, A, P) & fromConsumer1(S, A, N) & fromConsumer2(S, A, O) & sameas(T,S) & nsame(N,O)) >> relatedTo(S, A, P), weight : weightMap["R15"];
	private static void sameas_patterns(Model m, Model m1, Model m2) throws IOException {
		StmtIterator c1stmt_iter = m1.listStatements(); //S,A,N
		while(c1stmt_iter.hasNext()) {
			Statement c1stmt = c1stmt_iter.next();						
			Iterator<Resource>  s_subject_iter = getAllsame_resources(c1stmt.getSubject(), m, m1, m2).iterator();
			while (s_subject_iter.hasNext()) {		
				Resource subject = s_subject_iter.next();
				StmtIterator stmt_iter = m.listStatements(subject, c1stmt.getPredicate(), (RDFNode)null); //T,A,P
				Set<Statement> c2_stmts = m2.listStatements(c1stmt.getSubject(), c1stmt.getPredicate(), (RDFNode)null).toSet(); //S,A,O
				while(stmt_iter.hasNext()) {
					Statement stmt = stmt_iter.next();
					Iterator<Statement> c2_stmt_iter = c2_stmts.iterator(); 
					while(c2_stmt_iter.hasNext()) {
						Statement c2_stmt = c2_stmt_iter.next();
						if (!same(c1stmt.getObject(), c2_stmt.getObject(),m,m1,m2)) 
							get_pattern(stmt, c1stmt, c2_stmt);
					}
				}
			}
		}				
	}

	private static void sub_equivalentclass_patterns(Model m, Model m1, Model m2) {
		//m.add rule : (eqv_property(A,B, UID) & fromDataset(S, A, P) & fromConsumer1(S, B, N) & fromConsumer2(S, B, O) & diffrom(N,O)) >> relatedTo(S, B, P), weight : weightMap["R10"];
		//	m.add rule : (sub_propertyOf(A,B, UID) & fromDataset(S, B, P) & fromConsumer1(S, A, N) & fromConsumer2(S, A, O) & nsame(N,O)) >> relatedTo(S, A, P), weight : weightMap["R13"];
		//equivalent property??
		//	m.add rule : (eqv_property(C,B, UID) & sub_propertyOf(A,B, UID1) & fromDataset(S, C, P) & fromConsumer1(S, A, N) & fromConsumer2(S, A, O) & nsame(N,O)) >> relatedTo(S, A, P), weight : weightMap["R11"];

		StmtIterator c1stmt_iter = m1.listStatements(); //S,A,N
		while(c1stmt_iter.hasNext()) {
			Statement c1stmt = c1stmt_iter.next();
			if (c1stmt.getObject().isResource()) {
				Resource subject = c1stmt.getSubject();
				Property property = c1stmt.getPredicate();
				Iterator<Statement> c2_stmt_iter =m2.listStatements(subject, property, (RDFNode)null).toSet().iterator(); //S,A,O	
				while(c2_stmt_iter.hasNext()) {
					Statement c2_stmt = c2_stmt_iter.next();
					if (!same(c1stmt.getObject(), c2_stmt.getObject(),m,m1,m2)) {
						Set<OntProperty> sop = getAllSuperProperty(property);
						sop.addAll(getAllEqvProperty(property));
						Iterator<OntProperty> sp_iter = sop.iterator();
						while (sp_iter.hasNext()) {		
							StmtIterator stmt_iter = m.listStatements(subject, ResourceFactory.createProperty(sp_iter.next().getURI()), (RDFNode)null); //S,B,P
							while(stmt_iter.hasNext()) {
								get_pattern(stmt_iter.next(), c1stmt, c2_stmt);
							}
						}
					}

				}
			}
		}
	}

	private static void get_pattern(Statement stmt, Statement src_stmt) {
		String str ="";
		if (stmt.getObject().isResource())
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
					"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
		else 
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
					"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

		if (!new_content.contains(str))
			new_content += str + "\n";
	}

	private static void disjoint_patterns(Model m, Model m1, Model m2) {
		// source/target and fragment
		ExtendedIterator<Statement> src_stmt_iter = m1.listStatements((Resource)null,type_property,(RDFNode)null).andThen(
				m2.listStatements((Resource)null,type_property,(RDFNode)null)); //S,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			StmtIterator stmt_iter = m.listStatements(src_stmt.getSubject(), type_property, (RDFNode)null);//S,type,B
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				if (isDisjoint(stmt.getObject().asResource(), src_stmt.getObject().asResource())) 
					get_pattern(stmt, src_stmt);
			}
		}	
	}

	private static void domain_range_patterns(Model m, Model m1, Model m2) {		
		// domain
		STDSAO_patterns(m1, m2, false); // source and target
		STDSAO_patterns(m2, m1, true); // target and source
		STDSAO_patterns(m1, m, true); // slice and source
		STDSAO_patterns(m2, m, true); // slice and target		
		// range
		OTDSAO_patterns(m1, m2, false); // source and target
		OTDSAO_patterns(m2, m1, true); // target and source
		OTDSAO_patterns(m1, m, true); // slice and source
		OTDSAO_patterns(m2, m, true); // slice and target
	}
	private static void OTDSAO_patterns(Model m1, Model m2, boolean invert) {	
		StmtIterator src_stmt_iter = m1.listStatements((Resource)null,type_property,(RDFNode)null); //O,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			StmtIterator stmt_iter = m2.listStatements((Resource)null,(Property)null, src_stmt.getSubject());//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Resource object = src_stmt.getObject().asResource();
					Set<OntResource> ranges = getAllRange(property);	
					Iterator<OntResource> range_iter = ranges.iterator();
					while (range_iter.hasNext()) {
						OntResource range = range_iter.next();
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) {
							if (invert)
								get_pattern(stmt, src_stmt);
							else
								get_pattern(src_stmt, stmt);
						}
					}
				}	
			}
		}
	}

	private static void STDSAO_patterns(Model m1, Model m2, boolean invert) {	
		StmtIterator src_stmt_iter = m1.listStatements((Resource)null,type_property,(RDFNode)null); //S,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			StmtIterator stmt_iter = m2.listStatements(src_stmt.getSubject(), (Property)null, (RDFNode)null);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Resource object = src_stmt.getObject().asResource();
					Iterator<OntResource> domain_iter = getAllDomain(property).iterator();
					while (domain_iter.hasNext()) {
						OntResource domain = domain_iter.next();
						if (domain!= null && !domain.equals(object) && isDisjoint(domain, object)) {
							if (invert)
								get_pattern(stmt, src_stmt);
							else
								get_pattern(src_stmt, stmt);
						}
					}
				}	
			}
		}
	}
	private static boolean same(RDFNode node1, RDFNode node2, Model m, Model m1, Model m2) {
		if(node1.equals(node2) || 
				m.contains(node1.asResource(), sameas_property, node2) ||
				m.contains(node2.asResource(), sameas_property, node1) ||
				m2.contains(node1.asResource(), sameas_property, node2) ||
				m1.contains(node1.asResource(), sameas_property, node2) ||
				m2.contains(node2.asResource(), sameas_property, node1) ||
				m1.contains(node2.asResource(), sameas_property, node1)|| 
				!(m.contains(node1.asResource(), difffrom_property, node2) ||
						m.contains(node2.asResource(), difffrom_property, node1) ||
						m2.contains(node1.asResource(), difffrom_property, node2) ||
						m1.contains(node1.asResource(), difffrom_property, node2) ||
						m2.contains(node2.asResource(), difffrom_property, node1) ||
						m1.contains(node2.asResource(), difffrom_property, node1)))
			return true;
		else 
			return false;
	}
	private static void get_pattern(Statement stmt, Statement src_stmt, Statement tar_stmt) {
		String str ="";								
		if (stmt.getObject().isResource())							
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
		else 
			str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

		if (src_stmt.getObject().isResource())							
			str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
		else 
			str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

		if (tar_stmt.getObject().isResource())							
			str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
		else 
			str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";
		if (!new_content.contains(str))
			new_content += str + "\n";
	}
}