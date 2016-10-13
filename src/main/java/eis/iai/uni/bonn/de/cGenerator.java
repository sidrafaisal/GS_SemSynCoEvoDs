package eis.iai.uni.bonn.de;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class cGenerator {
	protected static boolean initialize = false;
	protected static  Model imodel;
	protected static  Model cmodel;
	protected static Model bmodel;
	protected static  OntModel ont_model;


	protected static  Property sameAs_property = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs");
	protected static Property type_property = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	protected static Property difffrom_property = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#differentFrom");
	protected static List<Property> avoid_property = new ArrayList<Property>();
	
	protected static  OWLDataFactory fac; 
	private static  OWLOntology ontology; 
	private static  OWLReasonerFactory reasonerFactory;
	protected static OWLReasoner reasoner;
	protected static OWLOntologyManager manager;
	
	protected static String bfilename;
	protected static String ifilename;
	protected static String cfilename; 
	protected static String filesyntax;
	protected static String srcfilename;
	protected static String tarfilename;
	
	protected static Model srcmodel, tarmodel;

	public static void main (String [] args) {		
		cGenerator cg;

		try {
			cg = new cGenerator("slice","dbpedia_2014.owl", "inferencedtriples", "conflictingtriples", "NT",
					"srcChanges", "tarChanges");	
			//	findavoid_Properties();
			
				int ifp = invfunProperty.createTriples(5),
				type = domain.createTriples_forExistingType(1),
				dom1 = domain.createTriples_forType(1),
				dom2 = range.createTriples_forType(4);
			//	eqv1 = eqvProperty.createTriples(4);
			
			System.out.println ("# of conflicting triples generated for type using inverse functional property: " + ifp);						
			System.out.println ("# of conflicting triples generated for type using type property: " + type);
			System.out.println ("# of conflicting triples generated for type using domain property: " + dom1);
			System.out.println ("# of conflicting triples generated for type using range property: " + dom2);
		//	System.out.println ("# of conflicting triples generated for relatedTo using equivalent property: " + eqv1);
			
			//	domain.createTriples_forSubProperty(4);
			cg.saveandclose();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}
	protected static void findavoid_Properties() {
		ExtendedIterator<OntProperty> prop_iter = ont_model.listAllOntProperties();	
		while(prop_iter.hasNext()) {
			OntProperty prop = prop_iter.next();
			if( prop.getDomain() == null) {				
				System.out.println("im nulll1"+ prop.toString());	

				if (prop.getSuperProperty() != null) {
					OntProperty sp = prop.getSuperProperty() ;
					if(sp.getDomain()!=null)
						avoid_property.add(prop);
				}
			}
		}
		System.out.println(avoid_property.size());
		//			avoid_property.add(ResourceFactory.createProperty("http://wikidata.dbpedia.org/resource/P569"));



	}

	public cGenerator(){


	}

	protected static void createfile (String fname) throws IOException {
		File file = new File(fname);
		if(!file.exists())
			file.createNewFile();
		else {
			file.delete();
			file.createNewFile();
		}
	}

	public cGenerator(String bfname, String ofilename, String ifname, String cfname, String fsyntx, 
			String sfilename, String tfilename) throws OWLOntologyCreationException, IOException {	

		filesyntax = fsyntx;
		bfilename =	bfname;	

		createfile(ifilename = ifname);	
		createfile(cfilename = cfname);			
		createfile(srcfilename = sfilename); 
		createfile(tarfilename = tfilename);

		ont_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		bmodel = FileManager.get().loadModel(bfilename, filesyntax);
		imodel =  FileManager.get().loadModel(ifilename, filesyntax);
		cmodel =  FileManager.get().loadModel(cfilename, filesyntax);
		InputStream ins = FileManager.get().open(ofilename);

		if (ins == null)
			throw new IllegalArgumentException("File: "+ofilename+" not found");
		ont_model.read(ins, null);		

		manager = OWLManager.createOWLOntologyManager();

		File f = new File(ofilename);
		ontology = manager.loadOntologyFromOntologyDocument(f);
		reasonerFactory = new Reasoner.ReasonerFactory();
		reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		fac = manager.getOWLDataFactory();

		srcmodel = FileManager.get().loadModel(srcfilename, filesyntax);
		tarmodel = FileManager.get().loadModel(tarfilename, filesyntax);
	}


	protected static Model getRandomTriples_withResourceObject(Model model, Property property, int count) throws IOException {
		createfile("temp");		
		Model temp_model = FileManager.get().loadModel("temp", filesyntax);
		createfile("temp1");		
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

		StmtIterator stmt_iter = model.listStatements((Resource)null, (Property)property ,(RDFNode)null);
		while(stmt_iter.hasNext()) {
			Statement stmt = stmt_iter.next();
			if (stmt.getObject().isResource())
				temp1_model.add(stmt);
		}

		Set<Statement> s_stmt = temp1_model.listStatements().toSet();		
		if(s_stmt.size() > 0) {
			long [] arr = getRandomNumbers(count, 0, s_stmt.size());
			Arrays.sort(arr);

			long current_element = 0;			
			int indextosearch = 0;

			Iterator<Statement> iter = s_stmt.iterator();
			while(iter.hasNext() && indextosearch < count) {
				Statement s = iter.next();
				if (s.getObject().isResource()) {
					if(arr[indextosearch] == current_element) {
						temp_model.add(s);
						indextosearch++;
					} else {
						while (arr[indextosearch] == current_element-1 && indextosearch < count) {
							indextosearch++;
						}	
						temp_model.add(s);
						indextosearch++;				
					}
					current_element++;
				}		
			}
		}			
			temp1_model.close();
			File file = new File("temp1");
			if(file.exists())
				file.delete();
			return temp_model;		
		
	}

	protected static Model getRandomTriples(Model model, List<Property> property, int count) throws IOException {
		createfile("temp");
		createfile("temp1");

		Model temp_model = FileManager.get().loadModel("temp", filesyntax);
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);

		for (int j=0; j < property.size(); j++) {
			temp1_model.add(model.listStatements((Resource)null, (Property) property.get(j), (RDFNode)null).toList());
		}

		StmtIterator stmt_iter = temp1_model.listStatements();
		Set<Statement> s_stmt = stmt_iter.toSet();

		long [] arr = getRandomNumbers(count, 0, s_stmt.size());
		Arrays.sort(arr);

		long current_element = 0;			
		int indextosearch = 0;

		Iterator<Statement> iter = s_stmt.iterator();
		while(iter.hasNext() && indextosearch < count) {
			Statement s = iter.next();
			if(arr[indextosearch] == current_element) {
				temp_model.add(s);
				indextosearch++;
			} else {
				while (arr[indextosearch] == current_element-1 && indextosearch < count) {
					indextosearch++;
				}	
				temp_model.add(s);
				indextosearch++;				
			}
			current_element++;
		}				
		temp1_model.close();
		File file = new File("temp1");
		if(file.exists())
			file.delete();

		return temp_model;		
	}

	protected static Model getRandomTriples(Model model, Property property, int count) throws IOException {

		List<Property> property_list = new ArrayList<Property>();
		property_list.add(property);
		return getRandomTriples(model, property_list, count);		
	}

	private static long[] getRandomNumbers(int count, long min, long max) {
		long [] arr = new long[count];	
		
		if (min >= max) {
			System.out.println("max must be greater than min");
		} else {			
			for (int i = 0; i < count; i++) {
				Random r = new Random();
				arr[i] = min + (long)(r.nextDouble()*(max - min));
			}
		}
		return arr;
	}

	private void saveandclose() throws IOException {

		bmodel.write(new FileOutputStream(bfilename), filesyntax);
		cmodel.write(new FileOutputStream(cfilename), filesyntax); 
		imodel.write(new FileOutputStream(ifilename), filesyntax);

		conflictsDivision();

		srcmodel.write(new FileOutputStream(srcfilename), filesyntax);
		tarmodel.write(new FileOutputStream(tarfilename), filesyntax);
		srcmodel.close();
		tarmodel.close();

		bmodel.close();
		imodel.close();
		cmodel.close();
		ont_model.close();

		File file = new File(cfilename);
		if(file.exists())
			file.delete();
		file = new File("temp");
		if(file.exists())
			file.delete();
	}



	static void conflictsDivision() throws IOException {		
		long mid = cmodel.size()/2 + (cmodel.size()%2) - 1;
		long counter = 0;

		StmtIterator iter1 = cmodel.listStatements();
		while (iter1.hasNext()) {
			Statement stmt = iter1.next();			

			if (counter <= mid)
				srcmodel.add(stmt);
			else if (counter > mid) {
				//	counter = -1;
				tarmodel.add(stmt);//srcmodel);
				//	srcmodel.removeAll();				
			} 
			counter++;			
		}
	}

}
