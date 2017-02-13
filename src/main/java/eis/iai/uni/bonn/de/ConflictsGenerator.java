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
	String new_content = "";

	ConflictsGenerator(Model m) {
		System.out.println("Generating conflicting patters (to be compared with ones detected by PSL)...");
		range_patterns(m);
		domain_patterns(m);
		disjoint_patterns(m);
		equivalent_patterns(m); 
		subproperty_patterns(m); 
		try {
			sameas_patterns(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void get_pattern(Statement stmt, Statement src_stmt, Statement tar_stmt) {
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
	
	private void sameas_patterns(Model m) throws IOException {
		StmtIterator stmt_iter = m.listStatements(); //T,A,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Iterator<Resource>  s_subject_iter = getAllsame_resources(stmt.getSubject(), m.add(srcmodel).add(tarmodel)).iterator();
			while (s_subject_iter.hasNext()) {		
				Resource subject = s_subject_iter.next();
				StmtIterator src_stmt_iter = srcmodel.listStatements(subject, stmt.getPredicate(), (RDFNode)null); //S,A,N
				Set<Statement> tar_stmts = tarmodel.listStatements(subject, stmt.getPredicate(), (RDFNode)null).toSet(); //S,A,O
				while(src_stmt_iter.hasNext()) {
					Statement src_stmt = src_stmt_iter.next();
					Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
					while(tar_stmt_iter.hasNext()) {
						Statement tar_stmt = tar_stmt_iter.next();
						RDFNode node1 = src_stmt.getObject(), node2 = tar_stmt.getObject();
						if (!(node1.equals(node2) || 
								m.contains(node1.asResource(), sameas_property, node2) ||
								m.contains(node2.asResource(), sameas_property, node1) ||
								tarmodel.contains(node1.asResource(), sameas_property, node2) ||
								srcmodel.contains(node1.asResource(), sameas_property, node2) ||
								tarmodel.contains(node2.asResource(), sameas_property, node1) ||
								srcmodel.contains(node2.asResource(), sameas_property, node1))) 
							get_pattern(stmt, src_stmt, tar_stmt);
					}
				}
			}
		}				
	}
	private void subproperty_patterns(Model m) {
		/*		m.add rule : (subpropertyOf(A,B,UID) & fromFragment(S, B, N) & fromConsumer1(S, A, N) & fromConsumer2(S, A, O) & nrepeat(A,B) & nsame(N,O)) >> relatedTo(S, A, N), weight : weightMap["sp1"];
		m.add rule : (subpropertyOf(A,B,UID) & fromFragment(S, B, N) & fromConsumer1(S, A, M) & fromConsumer2(S, A, O) & nrepeat(A,B) & nsame(N,M) & nsame(N,O) & nsame(M,O)) >> relatedTo(S, A, N), weight : weightMap["sp3"];
		 */
		StmtIterator stmt_iter = m.listStatements(); //S,B,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Iterator<OntProperty> sp_iter = getAllSubProperty(stmt.getPredicate()).iterator();
				while (sp_iter.hasNext()) {
					Property subProperty = ResourceFactory.createProperty(sp_iter.next().getURI());				
					StmtIterator src_stmt_iter = srcmodel.listStatements(subject, subProperty, (RDFNode)null); //S,A,N
					Set<Statement> tar_stmts = tarmodel.listStatements(subject, subProperty, (RDFNode)null).toSet(); //S,A,O
					while(src_stmt_iter.hasNext()) {
						Statement src_stmt = src_stmt_iter.next();
						Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
						while(tar_stmt_iter.hasNext()) {
							Statement tar_stmt = tar_stmt_iter.next();
							RDFNode node1 = src_stmt.getObject(), node2 = tar_stmt.getObject();
							if (!(node1.equals(node2) || 
									m.contains(node1.asResource(), sameas_property, node2) ||
									m.contains(node2.asResource(), sameas_property, node1) ||
									tarmodel.contains(node1.asResource(), sameas_property, node2) ||
									srcmodel.contains(node1.asResource(), sameas_property, node2) ||
									tarmodel.contains(node2.asResource(), sameas_property, node1) ||
									srcmodel.contains(node2.asResource(), sameas_property, node1))) 
								get_pattern(stmt, src_stmt, tar_stmt);
						}
					}
				}
			}
		}
	}

	private void equivalent_patterns(Model m) {
		/*		m.add rule : (eqvproperty(C,B,UID) & subpropertyOf(A,B,UID1) & fromFragment(S, C, N) & fromConsumer1(S, A, N)
				& fromConsumer2(S, A, O) & nrepeat(N,O) & nrepeat(C,B) & nrepeat(A,B) & nrepeat(A,C) ) >> relatedTo(S, A, N), weight : weightMap["ep5"];	// & nsame(N,O)			
				m.add rule : (eqvproperty(C,B,UID) & subpropertyOf(A,B,UID1) & fromFragment(S, C, N) & fromConsumer1(S, A, M)
				& fromConsumer2(S, A, O) & nrepeat(N,O) & nrepeat(N,M) & nrepeat(M,O) & nrepeat(C,B) & nrepeat(A,B) & nrepeat(A,C) ) >> relatedTo(S, A, N), weight : weightMap["ep7"];*/
		StmtIterator stmt_iter = m.listStatements(); //S,C,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Iterator<OntProperty> ep_iter = getAllEqvProperty(stmt.getPredicate()).iterator();
				while (ep_iter.hasNext()) {
					Iterator<OntProperty> sp_iter = getAllSubProperty(ResourceFactory.createProperty(ep_iter.next().getURI())).iterator();
					while (sp_iter.hasNext()) {
						Property subProperty = ResourceFactory.createProperty(sp_iter.next().getURI());
						StmtIterator src_stmt_iter = srcmodel.listStatements(subject, subProperty, (RDFNode)null); //S,B,N
						Set<Statement> tar_stmts = tarmodel.listStatements(subject, subProperty, (RDFNode)null).toSet(); //S,B,O
						while(src_stmt_iter.hasNext()) {
							Statement src_stmt = src_stmt_iter.next();
							Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
							while(tar_stmt_iter.hasNext()) {
								Statement tar_stmt = tar_stmt_iter.next();
								RDFNode node1 = src_stmt.getObject(), node2 = tar_stmt.getObject();
								if (!(node1.equals(node2) || 
										m.contains(node1.asResource(), sameas_property, node2) ||
										m.contains(node2.asResource(), sameas_property, node1) ||
										tarmodel.contains(node1.asResource(), sameas_property, node2) ||
										srcmodel.contains(node1.asResource(), sameas_property, node2) ||
										tarmodel.contains(node2.asResource(), sameas_property, node1) ||
										srcmodel.contains(node2.asResource(), sameas_property, node1))) 
									get_pattern(stmt, src_stmt, tar_stmt);
							}
						}
					}
				}
			}
		}
		/*
		 m.add rule : (eqvproperty(A,B,UID) & fromFragment(S, A, N) & fromConsumer1(S, B, N) & fromConsumer2(S, B, O) & nsame(N,O)) >> relatedTo(S, B, N), weight : weightMap["ep1"];
		m.add rule : (eqvproperty(A,B,UID) & fromFragment(S, A, N) & fromConsumer1(S, B, M) & fromConsumer2(S, B, O) & nsame(N,M) & nsame(N,O) & nsame(M,O)) >> relatedTo(S, B, N), weight : weightMap["ep3"];
				sao sa1o sa1t */
		stmt_iter = m.listStatements(); //S,A,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Iterator<OntProperty> ep_iter =  getAllEqvProperty(stmt.getPredicate()).iterator();
				while (ep_iter.hasNext()) {
					Property eqvProperty = ResourceFactory.createProperty(ep_iter.next().getURI());				
					StmtIterator src_stmt_iter = srcmodel.listStatements(subject, eqvProperty, (RDFNode)null); //S,B,N
					Set<Statement> tar_stmts = tarmodel.listStatements(subject, eqvProperty, (RDFNode)null).toSet(); //S,B,O
					while(src_stmt_iter.hasNext()) {
						Statement src_stmt = src_stmt_iter.next();
						Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
						while(tar_stmt_iter.hasNext()) {
							Statement tar_stmt = tar_stmt_iter.next();
							RDFNode node1 = src_stmt.getObject(), node2 = tar_stmt.getObject();
							if (!(node1.equals(node2) || 
									m.contains(node1.asResource(), sameas_property, node2) ||
									m.contains(node2.asResource(), sameas_property, node1) ||
									tarmodel.contains(node1.asResource(), sameas_property, node2) ||
									srcmodel.contains(node1.asResource(), sameas_property, node2) ||
									tarmodel.contains(node2.asResource(), sameas_property, node1) ||
									srcmodel.contains(node2.asResource(), sameas_property, node1))) 
								get_pattern(stmt, src_stmt, tar_stmt);
						}
					}
				}
			}
		}
	}

	private void get_pattern(Statement stmt, Statement src_stmt) {
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
	/* 	m.add rule : ( fromFragment(S, rdftype, B) & fromConsumer1(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["sim1"];
		m.add rule : ( fromFragment(S, rdftype, B) & fromConsumer2(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["sim2"]; */
	private void disjoint_patterns(Model m) {
		// source/target and fragment
		ExtendedIterator<Statement> src_stmt_iter = srcmodel.listStatements((Resource)null,type_property,(RDFNode)null).andThen(
				tarmodel.listStatements((Resource)null,type_property,(RDFNode)null)); //S,type,D
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

	/*m.add rule : ( domainOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer1(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["dom1"];
	m.add rule : ( domainOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer2(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["dom2"];
	 */
	private void domain_patterns(Model m) {
		// source/target and fragment
		ExtendedIterator<Statement> src_stmt_iter = srcmodel.listStatements((Resource)null,type_property,(RDFNode)null).andThen(
				tarmodel.listStatements((Resource)null,type_property,(RDFNode)null)); //S,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = m.listStatements(src_stmt.getSubject(),(Property)null, (RDFNode)null);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Iterator<OntResource> domain_iter = getAllDomain(property).iterator();
					while (domain_iter.hasNext()) {
						OntResource domain = domain_iter.next();
						if (domain!= null && !domain.equals(object) && isDisjoint(domain, object)) 
							get_pattern(stmt, src_stmt);
					}
				}	
			}
		}
	}
	/* m.add rule : ( rangeOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer1(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran1"];
		m.add rule : ( rangeOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer2(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran2"];
		//2.2//& notinFragment(S, A, O)
		m.add rule : ( rangeOf(A, B, UID1) & fromConsumer1(S, A, O) & fromConsumer2(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran3"];
		m.add rule : ( rangeOf(A, B, UID1) & fromConsumer2(S, A, O) & fromConsumer1(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran4"];*/
	public void range_patterns(Model m) {
		// source and fragment
		StmtIterator src_stmt_iter = srcmodel.listStatements((Resource)null,type_property,(RDFNode)null); //O,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = m.listStatements((Resource)null,(Property)null, src_stmt.getSubject());//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Iterator<OntResource> range_iter = getAllRange(property).iterator();
					while (range_iter.hasNext()) {
						OntResource range = range_iter.next();
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) 
							get_pattern(stmt, src_stmt);
					}
				}	
			}
		}
		// source and target
		src_stmt_iter = srcmodel.listStatements((Resource)null,type_property,(RDFNode)null); //O,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = tarmodel.listStatements((Resource)null,(Property)null, subject);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Set<OntResource> ranges = getAllRange(property);	//	rangeOf(A, B, UID1) 
					Iterator<OntResource> range_iter = ranges.iterator();
					while (range_iter.hasNext()) {
						OntResource range = range_iter.next();
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) 
							get_pattern(src_stmt, stmt);
					}
				}	
			}
		}
		// target and fragment/source
		src_stmt_iter = tarmodel.listStatements((Resource)null,type_property,(RDFNode)null); //O,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource object = src_stmt.getObject().asResource();
			ExtendedIterator<Statement> stmt_iter = m.listStatements((Resource)null,(Property)null, src_stmt.getSubject()).andThen(
					srcmodel.listStatements((Resource)null,(Property)null, src_stmt.getSubject()));//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Iterator<OntResource> range_iter = getAllRange(property).iterator();
					while (range_iter.hasNext()) {
						OntResource range = range_iter.next();
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) 
							get_pattern(stmt, src_stmt);
					}
				}	
			}
		}
	}
}