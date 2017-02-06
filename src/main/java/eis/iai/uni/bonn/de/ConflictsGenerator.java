package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class ConflictsGenerator extends ChangeGenerator {
	String new_content = "";

	ConflictsGenerator() {
		range_patterns();
		domain_patterns();
		disjoint_patterns();
		equivalent_patterns(); 
		subproperty_patterns(); 
		try {
			sameas_patterns();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sameas_patterns() throws IOException {
		/*
	 			m.add rule : (fromFragment(T, A, N) & fromConsumer1(S, A, N) & fromConsumer2(S, A, O) & sameas(T,S)  & nrepeat(N,O) & diffrom(N,O)) >> relatedTo(S, A, N), weight : weightMap["sa4"];
				m.add rule : (fromFragment(T, A, N) & fromConsumer2(S, A, N) & fromConsumer1(S, A, O) & sameas(T,S) & nrepeat(N,O) & diffrom(N,O)) >> relatedTo(S, A, N), weight : weightMap["sa5"];
				m.add rule : (fromFragment(T, A, N) & fromConsumer1(S, A, M) & fromConsumer2(S, A, O) & sameas(T,S) & nrepeat(N,O) & nrepeat(N,M) & nrepeat(M,O) & diffrom(N,M) & diffrom(N,O) & diffrom(M,O)) >> relatedTo(S, A, N), weight : weightMap["sa6"];
				m.add rule : (fromFragment(T, A, N) & fromConsumer2(S, A, M) & fromConsumer1(S, A, O) & sameas(T,S) & nrepeat(N,O) & nrepeat(N,M) & nrepeat(M,O) & diffrom(N,M) & diffrom(N,O) & diffrom(M,O)) >> relatedTo(S, A, N), weight : weightMap["sa7"];
		 */
		StmtIterator stmt_iter = bmodel.listStatements(); //T,A,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Iterator<Resource>  s_subject_iter = getAllsame_resources(stmt.getSubject()).iterator();
			while (s_subject_iter.hasNext()) {		
				Resource subject = s_subject_iter.next();
				StmtIterator src_stmt_iter = srcmodel.listStatements(subject, stmt.getPredicate(), (RDFNode)null); //S,A,N
				Set<Statement> tar_stmts = tarmodel.listStatements(subject, stmt.getPredicate(), (RDFNode)null).toSet(); //S,A,O
				while(src_stmt_iter.hasNext()) {
					Statement src_stmt = src_stmt_iter.next();
					Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
					while(tar_stmt_iter.hasNext()) {
						Statement tar_stmt = tar_stmt_iter.next();
						if (!stmt.getObject().equals(src_stmt.getObject()) && !stmt.getObject().equals(tar_stmt.getObject())) {
							String str ="";								
							if (stmt.getObject().isResource())							
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
							else if (stmt.getObject().isLiteral())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

							if (src_stmt.getObject().isResource())							
								str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
							else if (src_stmt.getObject().isLiteral())
								str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

							if (tar_stmt.getObject().isResource())							
								str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
							else if (tar_stmt.getObject().isLiteral())
								str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";


							if (!new_content.contains(str))
								new_content += str + "\n";
						}
					}
				}
			}
		}				
	}
	public void subproperty_patterns() {
		/*		m.add rule : (subpropertyOf(A,B,UID) & fromFragment(S, B, N) & fromConsumer1(S, A, N) & fromConsumer2(S, A, O) & nrepeat(A,B) & nsame(N,O)) >> relatedTo(S, A, N), weight : weightMap["sp1"];
		m.add rule : (subpropertyOf(A,B,UID) & fromFragment(S, B, N) & fromConsumer1(S, A, M) & fromConsumer2(S, A, O) & nrepeat(A,B) & nsame(N,M) & nsame(N,O) & nsame(M,O)) >> relatedTo(S, A, N), weight : weightMap["sp3"];
		 */
		StmtIterator stmt_iter = bmodel.listStatements(); //S,B,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Property property = stmt.getPredicate();
				Set<OntProperty> subProperties = getAllSubProperty(property);
				Iterator<OntProperty> sp_iter = subProperties.iterator();
				while (sp_iter.hasNext()) {
					Property subProperty = ResourceFactory.createProperty(sp_iter.next().getURI());				
					StmtIterator src_stmt_iter = srcmodel.listStatements(subject, subProperty, (RDFNode)null); //S,A,N
					Set<Statement> tar_stmts = tarmodel.listStatements(subject, subProperty, (RDFNode)null).toSet(); //S,A,O
					while(src_stmt_iter.hasNext()) {
						Statement src_stmt = src_stmt_iter.next();
						Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
						while(tar_stmt_iter.hasNext()) {
							Statement tar_stmt = tar_stmt_iter.next();
							if (!src_stmt.getObject().equals(tar_stmt.getObject())) {
								String str ="";								
								if (stmt.getObject().isResource())							
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
								else if (stmt.getObject().isLiteral())
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

								if (src_stmt.getObject().isResource())							
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
								else if (src_stmt.getObject().isLiteral())
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

								if (tar_stmt.getObject().isResource())							
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
								else if (tar_stmt.getObject().isLiteral())
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";


								if (!new_content.contains(str))
									new_content += str + "\n";
							}
						}
					}
				}
			}
		}
		/*		
m.add rule : (subpropertyOf(A,B,UID) & fromFragment(S, B, N) & fromConsumer2(S, A, N) & fromConsumer1(S, A, O) & nrepeat(A,B) & nsame(N,O)) >> relatedTo(S, A, N), weight : weightMap["sp2"];
		m.add rule : (subpropertyOf(A,B,UID) & fromFragment(S, B, N) & fromConsumer2(S, A, M) & fromConsumer1(S, A, O) & nrepeat(A,B) & nsame(N,M) & nsame(N,O) & nsame(M,O)) >> relatedTo(S, A, N), weight : weightMap["sp4"];
		 */
		stmt_iter = bmodel.listStatements(); //S,A,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();

			if (stmt.getObject().isResource()) {
				Property property = stmt.getPredicate();
				Set<OntProperty> subProperties = getAllSubProperty(property);
				Iterator<OntProperty> sp_iter = subProperties.iterator();
				while (sp_iter.hasNext()) {
					Property subProperty = ResourceFactory.createProperty(sp_iter.next().getURI());	
					StmtIterator tar_stmt_iter = tarmodel.listStatements(subject, subProperty, (RDFNode)null); //S,B,N
					Set<Statement> src_stmts = srcmodel.listStatements(subject, subProperty, (RDFNode)null).toSet(); //S,B,O
					while(tar_stmt_iter.hasNext()) {
						Statement tar_stmt = tar_stmt_iter.next();
						Iterator<Statement> src_stmt_iter = src_stmts.iterator(); 
						while(src_stmt_iter.hasNext()) {
							Statement src_stmt = src_stmt_iter.next();
							if (!tar_stmt.getObject().equals(src_stmt.getObject())) {
								String str ="";								
								if (stmt.getObject().isResource())							
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
								else if (stmt.getObject().isLiteral())
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

								if (src_stmt.getObject().isResource())							
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
								else if (src_stmt.getObject().isLiteral())
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

								if (tar_stmt.getObject().isResource())							
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
								else if (tar_stmt.getObject().isLiteral())
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";


								if (!new_content.contains(str))
									new_content += str + "\n";
							}
						}
					}
				}
			}
		}
	}

	public void equivalent_patterns() {
		/*		m.add rule : (eqvproperty(C,B,UID) & subpropertyOf(A,B,UID1) & fromFragment(S, C, N) & fromConsumer1(S, A, N)
				& fromConsumer2(S, A, O) & nrepeat(N,O) & nrepeat(C,B) & nrepeat(A,B) & nrepeat(A,C) ) >> relatedTo(S, A, N), weight : weightMap["ep5"];	// & nsame(N,O)			
				m.add rule : (eqvproperty(C,B,UID) & subpropertyOf(A,B,UID1) & fromFragment(S, C, N) & fromConsumer1(S, A, M)
				& fromConsumer2(S, A, O) & nrepeat(N,O) & nrepeat(N,M) & nrepeat(M,O) & nrepeat(C,B) & nrepeat(A,B) & nrepeat(A,C) ) >> relatedTo(S, A, N), weight : weightMap["ep7"];*/
		StmtIterator stmt_iter = bmodel.listStatements(); //S,C,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Property property = stmt.getPredicate();
				Set<OntProperty> eqvProperties = getAllEqvProperty(property);
				Iterator<OntProperty> ep_iter = eqvProperties.iterator();
				while (ep_iter.hasNext()) {
					Property eqvProperty = ResourceFactory.createProperty(ep_iter.next().getURI());
					Iterator<OntProperty> sp_iter = getAllSubProperty(eqvProperty).iterator();
					while (sp_iter.hasNext()) {
						Property subProperty = ResourceFactory.createProperty(sp_iter.next().getURI());
						StmtIterator src_stmt_iter = srcmodel.listStatements(subject, subProperty, (RDFNode)null); //S,B,N
						Set<Statement> tar_stmts = tarmodel.listStatements(subject, subProperty, (RDFNode)null).toSet(); //S,B,O
						while(src_stmt_iter.hasNext()) {
							Statement src_stmt = src_stmt_iter.next();
							Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
							while(tar_stmt_iter.hasNext()) {
								Statement tar_stmt = tar_stmt_iter.next();
								if (!src_stmt.getObject().equals(tar_stmt.getObject())) {
									String str ="";								
									if (stmt.getObject().isResource())							
										str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
									else if (stmt.getObject().isLiteral())
										str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

									if (src_stmt.getObject().isResource())							
										str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
									else if (src_stmt.getObject().isLiteral())
										str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

									if (tar_stmt.getObject().isResource())							
										str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
									else if (tar_stmt.getObject().isLiteral())
										str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";


									if (!new_content.contains(str))
										new_content += str + "\n";
								}
							}
						}
					}
				}
			}
		}
		/*		
		m.add rule : (eqvproperty(C,B,UID) & subpropertyOf(A,B,UID1) & fromFragment(S, C, N) & fromConsumer2(S, A, N)
		& fromConsumer1(S, A, O) & nrepeat(N,O) & nrepeat(C,B) & nrepeat(A,B) & nrepeat(A,C) ) >> relatedTo(S, A, N), weight : weightMap["ep6"];
		 */
		stmt_iter = bmodel.listStatements(); //S,C,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Property property = stmt.getPredicate();
				Set<OntProperty> eqvProperties = getAllEqvProperty(property);
				Iterator<OntProperty> ep_iter = eqvProperties.iterator();
				while (ep_iter.hasNext()) {
					Property eqvProperty = ResourceFactory.createProperty(ep_iter.next().getURI());
					Iterator<OntProperty> sp_iter = getAllSubProperty(eqvProperty).iterator();
					while (sp_iter.hasNext()) {
						Property subProperty = ResourceFactory.createProperty(sp_iter.next().getURI());
						StmtIterator tar_stmt_iter = tarmodel.listStatements(subject, subProperty, (RDFNode)null); //S,B,N
						Set<Statement> src_stmts = srcmodel.listStatements(subject, subProperty, (RDFNode)null).toSet(); //S,B,O
						while(tar_stmt_iter.hasNext()) {
							Statement tar_stmt = tar_stmt_iter.next();
							Iterator<Statement> src_stmt_iter = src_stmts.iterator(); 
							while(src_stmt_iter.hasNext()) {
								Statement src_stmt = src_stmt_iter.next();
								if (!tar_stmt.getObject().equals(src_stmt.getObject())) {
									String str ="";								
									if (stmt.getObject().isResource())							
										str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
									else if (stmt.getObject().isLiteral())
										str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

									if (src_stmt.getObject().isResource())							
										str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
									else if (src_stmt.getObject().isLiteral())
										str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

									if (tar_stmt.getObject().isResource())							
										str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
									else if (tar_stmt.getObject().isLiteral())
										str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";


									if (!new_content.contains(str))
										new_content += str + "\n";
								}
							}
						}
					}
				}
			}
		}
		/*
		 m.add rule : (eqvproperty(A,B,UID) & fromFragment(S, A, N) & fromConsumer1(S, B, N) & fromConsumer2(S, B, O) & nsame(N,O)) >> relatedTo(S, B, N), weight : weightMap["ep1"];
		m.add rule : (eqvproperty(A,B,UID) & fromFragment(S, A, N) & fromConsumer1(S, B, M) & fromConsumer2(S, B, O) & nsame(N,M) & nsame(N,O) & nsame(M,O)) >> relatedTo(S, B, N), weight : weightMap["ep3"];
		 */
		stmt_iter = bmodel.listStatements(); //S,A,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Property property = stmt.getPredicate();
				Set<OntProperty> eqvProperties = getAllEqvProperty(property);
				Iterator<OntProperty> ep_iter = eqvProperties.iterator();
				while (ep_iter.hasNext()) {
					Property eqvProperty = ResourceFactory.createProperty(ep_iter.next().getURI());				
					StmtIterator src_stmt_iter = srcmodel.listStatements(subject, eqvProperty, (RDFNode)null); //S,B,N
					Set<Statement> tar_stmts = tarmodel.listStatements(subject, eqvProperty, (RDFNode)null).toSet(); //S,B,O
					while(src_stmt_iter.hasNext()) {
						Statement src_stmt = src_stmt_iter.next();
						Iterator<Statement> tar_stmt_iter = tar_stmts.iterator(); 
						while(tar_stmt_iter.hasNext()) {
							Statement tar_stmt = tar_stmt_iter.next();
							if (!src_stmt.getObject().equals(tar_stmt.getObject())) {
								String str ="";								
								if (stmt.getObject().isResource())							
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
								else if (stmt.getObject().isLiteral())
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

								if (src_stmt.getObject().isResource())							
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
								else if (src_stmt.getObject().isLiteral())
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

								if (tar_stmt.getObject().isResource())							
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
								else if (tar_stmt.getObject().isLiteral())
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";


								if (!new_content.contains(str))
									new_content += str + "\n";
							}
						}
					}
				}
			}
		}
		/* 	m.add rule : (eqvproperty(A,B,UID) & fromFragment(S, A, N) & fromConsumer2(S, B, N) & fromConsumer1(S, B, O) & nsame(N,O)) >> relatedTo(S, B, N), weight : weightMap["ep2"];
		 	m.add rule : (eqvproperty(A,B,UID) & fromFragment(S, A, N) & fromConsumer2(S, B, M) & fromConsumer1(S, B, O) & nsame(N,M) & nsame(N,O) & nsame(M,O)) >> relatedTo(S, B, N), weight : weightMap["ep4"];
		 * */
		stmt_iter = bmodel.listStatements(); //S,A,N
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			Resource subject = stmt.getSubject();
			if (stmt.getObject().isResource()) {
				Property property = stmt.getPredicate();
				Set<OntProperty> eqvProperties = getAllEqvProperty(property);
				Iterator<OntProperty> ep_iter = eqvProperties.iterator();
				while (ep_iter.hasNext()) {
					Property eqvProperty = ResourceFactory.createProperty(ep_iter.next().getURI());		
					//Property eqvProperty = ep_iter.next();
					StmtIterator tar_stmt_iter = tarmodel.listStatements(subject, eqvProperty, (RDFNode)null); //S,B,N
					Set<Statement> src_stmts = srcmodel.listStatements(subject, eqvProperty, (RDFNode)null).toSet(); //S,B,O
					while(tar_stmt_iter.hasNext()) {
						Statement tar_stmt = tar_stmt_iter.next();
						Iterator<Statement> src_stmt_iter = src_stmts.iterator(); 
						while(src_stmt_iter.hasNext()) {
							Statement src_stmt = src_stmt_iter.next();
							if (!tar_stmt.getObject().equals(src_stmt.getObject())) {
								String str ="";								
								if (stmt.getObject().isResource())							
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|";
								else if (stmt.getObject().isLiteral())
									str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|";

								if (src_stmt.getObject().isResource())							
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|";
								else if (src_stmt.getObject().isLiteral())
									str += "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|";

								if (tar_stmt.getObject().isResource())							
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> <" + tar_stmt.getObject() + ">";
								else if (tar_stmt.getObject().isLiteral())
									str += "<"+tar_stmt.getSubject() +"> <" +tar_stmt.getPredicate()+"> \"" + tar_stmt.getObject() + "\"";


								if (!new_content.contains(str))
									new_content += str + "\n";
							}
						}
					}
				}
			}
		}
	}
	/*
	 * 	m.add rule : ( fromFragment(S, rdftype, B) & fromConsumer1(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["sim1"];
		m.add rule : ( fromFragment(S, rdftype, B) & fromConsumer2(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["sim2"];

	 */
	public void disjoint_patterns() {
		// source and fragment
		StmtIterator src_stmt_iter = srcmodel.listStatements((Resource)null,type_property,(RDFNode)null); //S,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = bmodel.listStatements(subject, type_property, (RDFNode)null);//S,type,B
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				if (isDisjoint(stmt.getObject().asResource(), object)) {
					String str ="";
					if (stmt.getObject().isResource())
						str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
								"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
					else if (stmt.getObject().isLiteral())
						str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
								"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

					if (!new_content.contains(str))
						new_content += str + "\n";
				}
			}
		}	
		// target and fragment
		src_stmt_iter = tarmodel.listStatements((Resource)null,type_property,(RDFNode)null); //S,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = bmodel.listStatements(subject, type_property, (RDFNode)null);//S,type,B
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				if (isDisjoint(stmt.getObject().asResource(), object)) {
					String str ="";
					if (stmt.getObject().isResource())
						str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
								"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
					else if (stmt.getObject().isLiteral())
						str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
								"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

					if (!new_content.contains(str))
						new_content += str + "\n";
				}
			}
		}	
	}


	/*m.add rule : ( domainOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer1(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["dom1"];
	m.add rule : ( domainOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer2(S, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(S,B), weight : weightMap["dom2"];
	 */

	public void domain_patterns() {
		// source and fragment
		StmtIterator src_stmt_iter = srcmodel.listStatements((Resource)null,type_property,(RDFNode)null); //S,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = bmodel.listStatements(subject,(Property)null, (RDFNode)null);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Set<OntResource> domains = getAllDomain(property);	//	domainOf(A, B, UID1) 
					Iterator<OntResource> domain_iter = domains.iterator();
					while (domain_iter.hasNext()) {
						OntResource domain = domain_iter.next();
						if (domain!= null && !domain.equals(object) && isDisjoint(domain, object)) {
							String str ="";
							if (stmt.getObject().isResource())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
							else if (stmt.getObject().isLiteral())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

							if (!new_content.contains(str))
								new_content += str + "\n";
						}
					}
				}	
			}
		}
		// target and fragment
		src_stmt_iter = tarmodel.listStatements((Resource)null,type_property,(RDFNode)null); //S,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = bmodel.listStatements(subject,(Property)null, (RDFNode)null);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Set<OntResource> domains = getAllDomain(property);	//	domainOf(A, B, UID1) 
					Iterator<OntResource> domain_iter = domains.iterator();
					while (domain_iter.hasNext()) {
						OntResource domain = domain_iter.next();
						if (domain!= null && !domain.equals(object) && isDisjoint(domain, object)) {
							String str ="";
							if (stmt.getObject().isResource())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
							else if (stmt.getObject().isLiteral())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

							if (!new_content.contains(str))
								new_content += str + "\n";
						}
					}
				}	
			}
		}
	}
	/*
	 * m.add rule : ( rangeOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer1(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran1"];
		m.add rule : ( rangeOf(A, B, UID1) & fromFragment(S, A, O) & fromConsumer2(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran2"];
		//2.2//& notinFragment(S, A, O)
		m.add rule : ( rangeOf(A, B, UID1) & fromConsumer1(S, A, O) & fromConsumer2(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran3"];
		m.add rule : ( rangeOf(A, B, UID1) & fromConsumer2(S, A, O) & fromConsumer1(O, rdftype, D) & disjointfrom(D,B) & nrepeat(D,B)) >> type(O,B), weight : weightMap["ran4"];

	 * */

	public void range_patterns() {
		// source and fragment
		StmtIterator src_stmt_iter = srcmodel.listStatements((Resource)null,type_property,(RDFNode)null); //O,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = bmodel.listStatements((Resource)null,(Property)null, subject);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Set<OntResource> ranges = getAllRange(property);	//	rangeOf(A, B, UID1) 
					Iterator<OntResource> range_iter = ranges.iterator();
					while (range_iter.hasNext()) {
						OntResource range = range_iter.next();
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) {
							String str ="";
							if (stmt.getObject().isResource())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
							else if (stmt.getObject().isLiteral())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

							if (!new_content.contains(str))
								new_content += str + "\n";
						}
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
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) {
							String str ="";
							if (stmt.getObject().isResource())
								str = "<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">|" +
										"<"+stmt.getSubject() +"> <" + stmt.getPredicate()+"> <" + stmt.getObject() + ">";
							else if (stmt.getObject().isLiteral())
								str = "<"+ src_stmt.getSubject() +"> <" + src_stmt.getPredicate()+"> \"" + src_stmt.getObject() + "\"|" +
										"<"+ stmt.getSubject() +"> <" + stmt.getPredicate()+"> <" + stmt.getObject() + ">";

							if (!new_content.contains(str))
								new_content += str + "\n";
						}
					}
				}	
			}
		}
		// target and fragment
		src_stmt_iter = tarmodel.listStatements((Resource)null,type_property,(RDFNode)null); //O,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = bmodel.listStatements((Resource)null,(Property)null, subject);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Set<OntResource> ranges = getAllRange(property);	//	rangeOf(A, B, UID1) 
					Iterator<OntResource> range_iter = ranges.iterator();
					while (range_iter.hasNext()) {
						OntResource range = range_iter.next();
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) {
							String str ="";
							if (stmt.getObject().isResource())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
							else if (stmt.getObject().isLiteral())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

							if (!new_content.contains(str))
								new_content += str + "\n";
						}
					}
				}	
			}
		}
		// target and source
		src_stmt_iter = tarmodel.listStatements((Resource)null,type_property,(RDFNode)null); //O,type,D
		while(src_stmt_iter.hasNext()) {
			Statement src_stmt = src_stmt_iter.next();
			Resource subject = src_stmt.getSubject();
			Resource object = src_stmt.getObject().asResource();
			StmtIterator stmt_iter = srcmodel.listStatements((Resource)null,(Property)null, subject);//S,A,O
			while(stmt_iter.hasNext()) {	
				Statement stmt = stmt_iter.next();
				OntProperty property = ont_model.getOntProperty(stmt.getPredicate().toString());
				if(property!=null) {
					Set<OntResource> ranges = getAllRange(property);	//	rangeOf(A, B, UID1) 
					Iterator<OntResource> range_iter = ranges.iterator();
					while (range_iter.hasNext()) {
						OntResource range = range_iter.next();
						if (range!= null && !range.equals(object) && isDisjoint(range, object)) {
							String str ="";
							if (stmt.getObject().isResource())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> <" + stmt.getObject() + ">|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";
							else if (stmt.getObject().isLiteral())
								str = "<"+stmt.getSubject() +"> <" +stmt.getPredicate()+"> \"" + stmt.getObject() + "\"|" +
										"<"+src_stmt.getSubject() +"> <" +src_stmt.getPredicate()+"> <" + src_stmt.getObject() + ">";

							if (!new_content.contains(str))
								new_content += str + "\n";
						}
					}
				}	
			}
		}
	}
}
