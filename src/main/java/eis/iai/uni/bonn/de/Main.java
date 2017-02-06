package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Random;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Main {

	static String location = "ExperimentalData/" ;

	public static void main (String [] args) {	
		try {
		//	convertRDF2CSV
			ConvertCSVtoRDF.convertCSVToRDF ("test.csv", "test_slice.nt", "NT");
			//			ConvertCSVtoRDF.convertCSVToRDF ("train.csv", "train_slice.nt", "NT");
			generatechanges ();
		} catch (OWLOntologyCreationException|IOException e) {
			e.printStackTrace();
		} 	
	}
	
	public static void generatechanges () throws OWLOntologyCreationException, IOException {		
		ChangeGenerator cg = new ChangeGenerator(location+"slice", location+"dbpedia_2014.owl", location+"inferencedtriples",  "NT", location+"srcChanges", location+"tarChanges");	
		GenerateNumberAndTypeOfChanges();

		System.out.println ("# of conflicting triples generated using functional property: " + ChangeGenerator.total_triples_generated_fp);	
		System.out.println ("# of conflicting triples generated for type using inverse functional property: " + ChangeGenerator.total_triples_generated_ifp);						
		System.out.println ("# of conflicting triples generated for type using disjoint property: " + ChangeGenerator.total_triples_generatedDC1);
		System.out.println ("# of conflicting triples generated for type using domain property: " + ChangeGenerator.total_triples_generatedDom1);
		System.out.println ("# of conflicting triples generated for type using range property: " + ChangeGenerator.total_triples_generatedRan1);
		System.out.println ("# of conflicting triples generated for relatedTo using range property: " + ChangeGenerator.total_triples_generatedRan2);
		System.out.println ("# of conflicting triples generated for relatedTo using range property: " + ChangeGenerator.total_triples_generatedRan3);
		System.out.println ("# of conflicting triples generated for relatedTo using equivalent property: " + ChangeGenerator.total_triples_generated_ep1);

		System.out.println ("# of conflicting triples generated for relatedTo using equivalent/sub property: " + ChangeGenerator.total_triples_generated_ep2);
		System.out.println ("# of conflicting triples generated for relatedTo using subproperty: " + ChangeGenerator.total_triples_generated_sp1);
		System.out.println ("# of conflicting triples generated for relatedTo using sameas property: " + ChangeGenerator.total_triples_generated_sap1);
		System.out.println ("# of conflicting triples generated for relatedTo using sameas property: " + ChangeGenerator.total_triples_generated_sap2);
		System.out.println ("# of conflicting triples generated for relatedTo using diffrom property (2): " + ChangeGenerator.total_triples_generated_dfp2);
		System.out.println ("# of conflicting triples generated for relatedTo using diffrom property (3): " + ChangeGenerator.total_triples_generated_dfp3);

		cg.save();
		ConflictsGenerator generate_conflicts = new ConflictsGenerator();
		ChangeGenerator.createfile("ExperimentalData/"+"conflict_combination");
		ChangeGenerator.writer ("ExperimentalData/"+"conflict_combination", generate_conflicts.new_content);
		cg.close();
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
