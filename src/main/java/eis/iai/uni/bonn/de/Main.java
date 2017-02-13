package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Random;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
public class Main {

	static String location = "ExperimentalData/" ;	

	public static void main (String [] args) {	
		try {
			//	convertRDF2CSV
			//	ConvertCSVtoRDF.convertCSVToRDF ("test.csv", "test_slice.nt", "NT");
			//			ConvertCSVtoRDF.convertCSVToRDF ("train.csv", "train_slice.nt", "NT");
			//////////////		generate changes
			ChangeGenerator cg = new ChangeGenerator(location+"drugbank_dump.nt", location+"dbpedia_2014.owl", location+"inferencedtriples",  "NT", location+"srcChanges", 
					location+"tarChanges", location+"slice.nt");	
			System.out.println ("Generating changes.....");
			GenerateNumberAndTypeOfChanges();
			cg.trimdata();
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

			cg.save();
			ConflictsGenerator generate_conflicts = new ConflictsGenerator(ChangeGenerator.tcg_model);
			ChangeGenerator.writer(ChangeGenerator.createfile(location+"conflict_combination"), generate_conflicts.new_content);
			cg.close();
		} catch (OWLOntologyCreationException|IOException e) {
			e.printStackTrace();
		} 	
	}

	public static void GenerateNumberAndTypeOfChanges() throws IOException {

		int arr[][] = new int[8][];
		for (int i = 1; i <= 8; i++) {
			int no = getPoisson(2);
			if (no == 0) {
				i--;
				continue;
			}
			arr[i - 1] = getUniform(no);
		}

		int changeType;
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				changeType  = arr[i][j];				
				if(changeType==1) 	
					Sameas.createTriples_sap1(2);
				else if(changeType==2) 				
					Disjointclass.createTriples_forExistingType(2);
				else if(changeType==3) 				
					Domain.createTriples_forType(2);
				else if(changeType==4) 				
					Range.createTriples_forType(2);
				else if(changeType==5) 					
					Range.createTriples_ran2(2);
				else if(changeType==6) 					
					EqvProperty.createTriples_ep1(2);
				else if(changeType==7) 
					EqvProperty.createTriples_ep2(2);
				else if(changeType==8) 	
					SubProperty.createTriples_sp1(2);
				else if(changeType==9) 	
					Diffrom.createTriples_dfp2(2);
				else if(changeType==10) 	
					Diffrom.createTriples_dfp3(2);

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
			}
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
