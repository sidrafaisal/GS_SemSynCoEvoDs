package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Random;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
public class Main {

	static String location = "ExperimentalData/" ;	
	static int required = 0;
	public static void main (String [] args) {	
		try {
			required = Integer.parseInt(args[5]);
			//	convertRDF2CSV
			//	ConvertCSVtoRDF.convertCSVToRDF ("test.csv", "test_slice.nt", "NT");
			//			ConvertCSVtoRDF.convertCSVToRDF ("train.csv", "train_slice.nt", "NT");
			//////////////		generate changes		
			ChangeGenerator cg = new ChangeGenerator(location+args[0], location+args[1], "NT",  location+args[2]);	
			GenerateNumberAndTypeOfChanges(cg, args[3], args[4]);
			cg.trimdata(required);

			//	cg.save_changes(ChangeGenerator.ifilename, ChangeGenerator.srcfilename, ChangeGenerator.tarfilename);
			//ConflictsGenerator.generateConflicts(ChangeGenerator.tcg_model, ChangeGenerator.tcg_smodel, ChangeGenerator.tcg_tmodel);
			ChangeGenerator.writer(ChangeGenerator.createfile(location+args[6]), ConflictsGenerator.new_content);
			cg.close();
			System.out.println ("# of conflicting triples generated using:\n"
					+ "------------------------------------------------\nfunctional property: " + ChangeGenerator.total_triples_generated_fp 
					+ "\ninverse functional property: " + ChangeGenerator.total_triples_generated_ifp 
					+"\ndisjoint property: " + ChangeGenerator.total_triples_generatedDC1 
					+ "\ndomain property: " + ChangeGenerator.total_triples_generatedDom1 
					+ "\nrange property: " + (ChangeGenerator.total_triples_generatedRan1 + ChangeGenerator.total_triples_generatedRan2 + ChangeGenerator.total_triples_generatedRan3)
					+ "\nequivalent property: " + ChangeGenerator.total_triples_generated_ep1
					+ "\nequivalent/sub property: " + ChangeGenerator.total_triples_generated_ep2
					+ "\nsubproperty: " + ChangeGenerator.total_triples_generated_sp1
					+ "\nsameas property: " + (ChangeGenerator.total_triples_generated_sap1 + ChangeGenerator.total_triples_generated_sap2)
					+ "\ndiffrom property: " + (ChangeGenerator.total_triples_generated_dfp2 + ChangeGenerator.total_triples_generated_dfp3));

		} catch (OWLOntologyCreationException|IOException e) {
			e.printStackTrace();
		} 	
	}

	public static void GenerateNumberAndTypeOfChanges(ChangeGenerator cg, String s, String t) throws IOException, OWLOntologyCreationException {
		int hours = 100;
		boolean stop=false;
		int arr[][] = new int[hours][];
		for (int i = 1; i <= hours; i++) {
			int no = getPoisson(2);
			if (no == 0) {
				i--;
				continue;
			}
			arr[i - 1] = getUniform(no);
		}
		int changeType;
		//cg.initialize(location+"inferencedtriples", location+s, location+t);
		int m = 0, k = 1;	
		L1: for (int i = 0; i < arr.length; i++) {
			if (k == 1) {
				System.out.println ("Generating changes.....");
				cg.initialize(location+"inferencedtriples"+m, location+s.substring(0, s.indexOf("."))+m+".nt", location+t.substring(0, t.indexOf("."))+m+".nt");	
			}
			for (int j = 0; j < arr[i].length; j++) {
				changeType  = arr[i][j];	
			/*	if(changeType==1) 	
					Sameas.createTriples_sap1(20);
				else if(changeType==2) 				
					Disjointclass.createTriples_forExistingType(20);
				else if(changeType==3) 				
					Domain.createTriples_forType(20);
				else if(changeType==4) 				
					Range.createTriples_forType(20);
				else if(changeType==5) 					
					Range.createTriples_ran2(20);
				else if(changeType==6) 					
					EqvProperty.createTriples_ep1(20);
				else if(changeType==7) 
					EqvProperty.createTriples_ep2(20);
				else if(changeType==8) 	
					SubProperty.createTriples_sp1(20);
				else if(changeType==9) 	
					Diffrom.createTriples_dfp2(20);
				else if(changeType==10) 	
					Diffrom.createTriples_dfp3(20);*/
				if(changeType==1) 	
					Sameas.createTriples_sap1(1);
				else if(changeType==2) 				
					Disjointclass.createTriples_forExistingType(1);
				else if(changeType==3) 				
					Domain.createTriples_forType(1);
				else if(changeType==4) 				
					Range.createTriples_forType(1);
				else if(changeType==5) 					
					Range.createTriples_ran2(1);
				else if(changeType==6) 					
					EqvProperty.createTriples_ep1(1);
				else if(changeType==7) 
					EqvProperty.createTriples_ep2(1);
				else if(changeType==8) 	
					SubProperty.createTriples_sp1(1);
				else if(changeType==9) 	
					Diffrom.createTriples_dfp2(1);
				else if(changeType==10) 	
					Diffrom.createTriples_dfp3(1);

				/*	if(changeType==13) 
				fp += FunProperty.createTriples(1);
			else if(changeType==1) 
				ifp += InvfunProperty.createTriples(1);
				else 					
			else if(changeType==10) 	
				sap2 += Sameas.createTriples_sap2(1);
			else */
				/*	else if(changeType==14) 					
					Range.createTriples_ran3(1);*/

				if (ChangeGenerator.tcg_model.size() >= required) 
					stop = true;
			}
			if (k >= hours/5 || stop) {
				k = 1;
				cg.save_changes(location+"inferencedtriples"+m+".nt", location+s.substring(0, s.indexOf("."))+m+".nt", location+t.substring(0, t.indexOf("."))+m+".nt", true);
//				ConflictsGenerator.generateConflicts(ChangeGenerator.tcg_model, ChangeGenerator.srcmodel, ChangeGenerator.tarmodel);
				cg.close_changes();
				m++;
				if(stop){
					break L1;
				}
			} else 
				k++;
		}	
		System.out.println("no of files="+m);		
		for (int i=0; i < m; i++){
			Model m1 = FileManager.get().loadModel(location+s.substring(0, s.indexOf("."))+i+".nt", "NT");
					Model m2 = FileManager.get().loadModel(location+t.substring(0, t.indexOf("."))+i+".nt","NT");
					Model m3 = FileManager.get().loadModel(location+"inferencedtriples"+i+".nt", "NT");
			ConflictsGenerator.generateConflicts(ChangeGenerator.tcg_model, m1, m2);
			ChangeGenerator.tcg_model.add(m3);
			m2.close();
			m1.close();
			m3.close();
		}

	}

	public static int getPoisson(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		do {
			k++;
			p *= Math.random();
		} while (p > L);
		return k - 1;
	}

	public static int[] getUniform(int _param) {
		int N = _param;
		int numberOfChangeTypes = 10;
		int[] a = new int[N];
		Random generator = new Random();
		for (int i = 0; i < a.length; i++) {
			a[i] = generator.nextInt(numberOfChangeTypes) + 1;
		}
		return a;
	}
}
