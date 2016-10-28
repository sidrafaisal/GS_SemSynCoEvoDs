package eis.iai.uni.bonn.de;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
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


	protected static  Property sameas_property = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs");
	protected static Property type_property = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	protected static Property difffrom_property = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#differentFrom");

	protected static Property ran_property = (Property)ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#range");
	protected static Property dom_property = (Property)ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#domain");
	protected static Property sub_property = (Property)ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
	
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

			int ifp = invfunProperty.createTriples(5),
					type = domain.createTriples_forExistingType(10),
					dom1 = domain.createTriples_forType(10),
					dom2 = range.createTriples_forType(10),
					eqv1 = eqvProperty.createTriples_ep1(10),
					sp1 = subProperty.createTriples_sp1(10),
					sap1 = sameas.createTriples_sap1(10),
					dfp2 = difffrom.createTriples_dfp2(10),
					dfp3 = difffrom.createTriples_dfp3(10);
			//	dfp1 = difffrom.createTriples_dfp1(4);

			System.out.println ("# of conflicting triples generated for type using inverse functional property: " + ifp);						
			System.out.println ("# of conflicting triples generated for type using type property: " + type);
			System.out.println ("# of conflicting triples generated for type using domain property: " + dom1);
			System.out.println ("# of conflicting triples generated for type using range property: " + dom2);
			System.out.println ("# of conflicting triples generated for relatedTo using equivalent property: " + eqv1);
			System.out.println ("# of conflicting triples generated for relatedTo using subproperty: " + sp1);
			System.out.println ("# of conflicting triples generated for relatedTo using sameas property: " + sap1);
			System.out.println ("# of conflicting triples generated for relatedTo using diffrom property (2): " + dfp2);
			System.out.println ("# of conflicting triples generated for relatedTo using diffrom property (3): " + dfp3);

			cg.saveandclose();
		} catch (OWLOntologyCreationException|IOException e) {
			e.printStackTrace();
		} 	
	}
	public cGenerator(){


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

	protected static Set<Statement> filter(Model model, List<Property> property, String forProperty, boolean resourceObject) throws IOException {

		createfile("temp1");
		createfile("temp2");
		Model temp1_model = FileManager.get().loadModel("temp1", filesyntax);			
		Model temp2_model = FileManager.get().loadModel("temp2", filesyntax);

		for (int j=0; j < property.size(); j++) {

			temp2_model.add(model.listStatements((Resource)null, (Property) property.get(j), (RDFNode)null));
			temp2_model.removeAll((Resource)null,ran_property, (RDFNode)null);
			temp2_model.removeAll((Resource)null,dom_property, (RDFNode)null);
			temp2_model.removeAll((Resource)null, sameas_property, (RDFNode)null);
			temp2_model.removeAll((Resource)null, difffrom_property, (RDFNode)null);
			temp2_model.removeAll((Resource)null, sub_property, (RDFNode)null);

			StmtIterator stmt_iter = temp2_model.listStatements();
			while (stmt_iter.hasNext()) {
				Statement stmt = stmt_iter.next();
				OntProperty op = ont_model.getOntProperty(stmt.getPredicate().toString());
				if ((op != null) && (resourceObject == false || (resourceObject == true && stmt.getObject().isResource() &&
							!stmt.getSubject().equals(stmt.getObject().asResource())))) {
						if ((forProperty=="domain" && op.getDomain()!=null) ||
						(forProperty=="range" && op.getRange()!=null) ||
						(forProperty=="" || forProperty=="type") ||
						(forProperty=="df2" && getSubProperty(stmt.getPredicate())!=null) ||
						(forProperty=="df3" && getEqvProperty(stmt.getPredicate())!=null) ||
						(forProperty=="ep" && getEqvProperty(stmt.getPredicate())!=null))
							temp1_model.add(stmt);
				}
			}
		}

		Set<Statement> s_stmt = temp1_model.listStatements().toSet();
		temp1_model.close();
		deletefile("temp1");
		deletefile("temp2");
		return s_stmt;
	}

	protected static Model getRandomTriples(Model model, List<Property> property, int count, String forProperty, boolean resourceObject) throws IOException {
		Set<Statement> s_stmt = filter(model, property, forProperty, resourceObject) ;

		createfile("temp");	
		Model temp_model = FileManager.get().loadModel("temp", filesyntax);		
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
				while (indextosearch < count && arr[indextosearch] == current_element-1) {
					indextosearch++;
				}	
				temp_model.add(s);
				indextosearch++;				
			}
			current_element++;
		}				

		return temp_model;		
	}
	protected static Resource [] getdiff_resources (RDFNode object) throws IOException {
		Resource r1 = null;
		Resource r2 = null;
		Resource r = object.asResource();
		ResIterator dstmt_iter = bmodel.listResourcesWithProperty(difffrom_property, r);
		while (dstmt_iter.hasNext()) {
			if (r1 == null)
				r1 = dstmt_iter.next();
			else if (r2 == null) {
				Resource res = dstmt_iter.next(); 
				if (!r1.equals(res))
					r2 = res; 
			} else
				break;
		}
		if (r1 == null || r2 == null ) {
			//r1 = r.getPropertyResourceValue(difffrom_property);
			NodeIterator obj_iter = bmodel.listObjectsOfProperty(r, difffrom_property);
			while (obj_iter.hasNext()) {
				RDFNode obj = obj_iter.next();
				if (obj.isResource()) {
					if (r1 == null)
						r1 = obj.asResource();
					else if (r2 == null) {
						Resource res = obj.asResource(); 
						if (!r1.equals(res))
							r2 = res; 
					} else							
						break;
				}
			}
		}
		Resource arr[] = {r1,r2};
		return arr;
	}
	protected static Node getDisjointClass(OntResource dom){

		OWLClass owlclass = fac.getOWLClass(IRI.create(dom.toString()));		
		Set<OWLClass> disclass = reasoner.getDisjointClasses(owlclass).getFlattened();	
		Node object = null;
		for (OWLClass c : disclass) {
			object = NodeFactory.createURI(c.getIRI().toString());
			break;		
		}
		return object;
	}

	protected static OntProperty getEqvProperty(Property property)	{
		OntProperty op = ont_model.getOntProperty(property.toString());			 
		ExtendedIterator<? extends OntProperty> eps = null;
		OntProperty ep = null;
		
		if (op != null) {
			eps = op.listEquivalentProperties();
			while(eps.hasNext()) {
				OntProperty p = eps.next();
				if (!p.equals(op)){
					ep = p;
					break;
				}
			}
		}
		return ep;
	}	
	protected static OntProperty getSubProperty(Property property)	{
		OntProperty op = ont_model.getOntProperty(property.toString());			 
		ExtendedIterator<? extends OntProperty> sps = null;
		Set<? extends OntProperty> s_eps = null;
		OntProperty sp = null;
		if( op != null) {
			sps = op.listSubProperties();

			s_eps = op.listEquivalentProperties().toSet();
			while(sps.hasNext()) {
				OntProperty p = sps.next();
				if (!(p.equals(op) || s_eps.contains(p))) {			
					sp = p;
					break;
				}
			}
		}
			return sp;
	}
	protected static Model getRandomTriples(Model model, Property property, int count, String p, boolean resourceObject) throws IOException {

		List<Property> property_list = new ArrayList<Property>();
		property_list.add(property);
		return getRandomTriples(model, property_list, count, p, resourceObject);		
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

		deletefile(cfilename);
		deletefile("temp");
	}
	protected static void deletefile (String fname) throws IOException {
		File file = new File(fname);
		if(file.exists())
			file.delete();		
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
