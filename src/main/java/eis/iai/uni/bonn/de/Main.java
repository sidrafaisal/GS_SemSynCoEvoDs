package eis.iai.uni.bonn.de;

import java.io.IOException;
import java.util.Random;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Main {
	
	static int fp=0, ifp=0, dc=0, dom1=0, ran1=0, ran2=0, eqv1=0, eqv2=0, sp1=0, sap1=0 , sap2=0, dfp2=0, dfp3=0;
	
	public static void main (String [] args) {		
		ConflictGenerator cg;		

		try {
			cg = new ConflictGenerator("slice","dbpedia_2014.owl", "inferencedtriples", "conflictingtriples", "NT",
					"srcChanges", "tarChanges");	
			
			GenerateNumberAndTypeOfChanges();
			
			System.out.println ("# of conflicting triples generated using functional property: " + fp);	
			System.out.println ("# of conflicting triples generated for type using inverse functional property: " + ifp);						
			System.out.println ("# of conflicting triples generated for type using type property: " + dc);
			System.out.println ("# of conflicting triples generated for type using domain property: " + dom1);
			System.out.println ("# of conflicting triples generated for type using range property: " + ran1);

			System.out.println ("# of conflicting triples generated for relatedTo using range property: " + ran2);
			System.out.println ("# of conflicting triples generated for relatedTo using equivalent property: " + eqv1);

			System.out.println ("# of conflicting triples generated for relatedTo using equivalent/sub property: " + eqv2);
			System.out.println ("# of conflicting triples generated for relatedTo using subproperty: " + sp1);
			System.out.println ("# of conflicting triples generated for relatedTo using sameas property: " + sap1);
			System.out.println ("# of conflicting triples generated for relatedTo using sameas property: " + sap2);
			System.out.println ("# of conflicting triples generated for relatedTo using diffrom property (2): " + dfp2);
			System.out.println ("# of conflicting triples generated for relatedTo using diffrom property (3): " + dfp3);

			cg.saveandclose();
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
				if(changeType==13) 
					fp += FunProperty.createTriples(1);
				else if(changeType==1) 
					ifp += InvfunProperty.createTriples(1);
				else if(changeType==2) 				
					dc += Disjointclass.createTriples_forExistingType(1);
				else if(changeType==3) 				
					dom1 += Domain.createTriples_forType(1);
				else if(changeType==4) 				
					ran1 += Range.createTriples_forType(1);
				else if(changeType==5) 					
					ran2 += Range.createTriples_ran2(1);
				else if(changeType==6) 					
					eqv1 += EqvProperty.createTriples_ep1(1);
				else if(changeType==7) 
					eqv2 += EqvProperty.createTriples_ep2(1);
				else if(changeType==8) 	
					sp1 += SubProperty.createTriples_sp1(1);
				else if(changeType==9) 	
					sap1 += Sameas.createTriples_sap1(1);					
				else if(changeType==10) 	
					sap2 += Sameas.createTriples_sap2(1);
				else if(changeType==11) 	
					dfp2 += Diffrom.createTriples_dfp2(1);
				else if(changeType==12) 	
					dfp3 += Diffrom.createTriples_dfp3(1);
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
		int numberOfChangeTypes = 13;
		int[] a = new int[N];
		Random generator = new Random();
		for (int i = 0; i < a.length; i++) {
			a[i] = generator.nextInt(numberOfChangeTypes) + 1;
		}
		return a;
	}
}
