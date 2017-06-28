package eis.iai.uni.bonn.de;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
public class Main {

	static String location = "ExperimentalData/" ;	
	public static void main (String [] args) {	
		try {
			//	convertRDF2CSV
			//	ConvertCSVtoRDF.convertCSVToRDF ("test.csv", "test_slice.nt", "NT");
			//			ConvertCSVtoRDF.convertCSVToRDF ("train.csv", "train_slice.nt", "NT");
			//////////////		generate changes		
			ChangeGenerator cg = new ChangeGenerator(location+args[0], location+args[1], "NT", location+args[2]);
			GenerateNumberAndTypeOfChanges(cg, args[3], args[4], Integer.parseInt(args[5]), Integer.parseInt(args[7]), args[8]);
			ChangeGenerator.writer(ChangeGenerator.createfile(location+args[6]), ConflictsGenerator.conflicts);
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
					+ "\ndiffrom property: " + (ChangeGenerator.total_triples_generated_dfp2 + ChangeGenerator.total_triples_generated_dfp3)
					+ "\nliteral objects: " + ChangeGenerator.total_triples_generated_String);
		} catch (OWLOntologyCreationException|IOException e) {
			e.printStackTrace();
		} 	
	}

	public static void GenerateNumberAndTypeOfChanges(ChangeGenerator cg, String s, String t, int required, int hours, 
			String infer) throws IOException, OWLOntologyCreationException {
		boolean stop=false;
		int arr[][] = new int[hours][];
		for (int i = 1; i <= hours; i++) {
			int no = getPoisson(2);//number
			if (no == 0) {
				i--;
				continue;
			}
			arr[i - 1] = getUniform(no);//type
		}
		int changeType;
		//cg.initialize(location+"inferencedtriples", location+s, location+t);
		int m = 0; 

		L1: for (int i = 0; i < arr.length; i++) {
		//	System.out.println("out...."+i);
		//	if (k == 1) {
				System.out.println ("Generating changes.....");
				cg.initialize(location+infer+m+".nt", location+s.substring(0, s.indexOf("."))+m+".nt", 
						location+t.substring(0, t.indexOf("."))+m+".nt", location+infer+"truth"+m+".nt");	
		//	}
			for (int j = 0; j < arr[i].length; j++) {
				changeType  = arr[i][j];	
				if(changeType==1) 	
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
					Diffrom.createTriples_dfp3(20);
				else if(changeType==11) 
					StringObjects.createTriples_forStrings(20);
			/*	if(changeType>=1 && changeType<=2) 					
					EqvProperty.createTriples_ep1(10);
				else if(changeType>=3 && changeType<=4) 	
					EqvProperty.createTriples_ep2(10);
				else if(changeType>=5 && changeType<=6) 	
					StringObjects.createTriples_forStrings(20);
				else if(changeType>=7 && changeType<=8) 		
					Diffrom.createTriples_dfp2(10);
				else if(changeType>=9 && changeType<=10) 		
					Diffrom.createTriples_dfp3(10);
*/
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

				if (ChangeGenerator.tcg_model.size() >= required) {
					stop = true;
					break;
				}
			}
		//	if (k >= hours/5 || stop) {
		//		k = 1;				
				cg.save_changes(location+infer+m+".nt", location+s.substring(0, s.indexOf("."))+m+".nt", 
						location+t.substring(0, t.indexOf("."))+m+".nt", location+infer+"truth"+m+".nt");		
			
		/*		ConflictsGenerator.generateConflicts(ChangeGenerator.tcg_model, ChangeGenerator.srcmodel, ChangeGenerator.tarmodel);
				String sname = ChangeGenerator.tcgfilename.substring(0, ChangeGenerator.tcgfilename.indexOf("."))+m+".nt";
				ChangeGenerator.tcg_model.write(new FileOutputStream(ChangeGenerator.createfile(sname)), "NT");					
				ChangeGenerator.tcg_model = (ChangeGenerator.tcg_model.add(ChangeGenerator.srcmodel).add(ChangeGenerator.tarmodel).remove(ConflictsGenerator.conflicts2remove)).add(ChangeGenerator.imodel);
				ChangeGenerator.truthmodel.add(ConflictsGenerator.m4).write(new FileOutputStream(location+infer+"truth"+m+".nt"), "NT").close();
				ConflictsGenerator.conflicts2remove.removeAll();
				ConflictsGenerator.m4.removeAll();
		*/		cg.close_changes();			
				m++;
				if(stop){
					break L1;
				}
		//	} else 
		//		k++;
		}	
		
		System.out.println("no of files="+m);
		cg.trimdata(required);

		for (int i=0; i < m; i++) {
						Model m1 = FileManager.get().loadModel(location+s.substring(0, s.indexOf("."))+i+".nt", "NT");
					Model m2 = FileManager.get().loadModel(location+t.substring(0, t.indexOf("."))+i+".nt","NT");
					Model m3 = FileManager.get().loadModel(location+infer+i+".nt", "NT");
					Model m4= FileManager.get().loadModel(location+infer+"truth"+i+".nt", "NT");
			ConflictsGenerator.generateConflicts(ChangeGenerator.tcg_model, m1, m2);
			String sname = ChangeGenerator.tcgfilename.substring(0, ChangeGenerator.tcgfilename.indexOf("."))+ i +".nt";
			ChangeGenerator.tcg_model.write(new FileOutputStream(ChangeGenerator.createfile(sname)), "NT");	
			
			ChangeGenerator.tcg_model = (ChangeGenerator.tcg_model.add(m1).add(m2).remove(ConflictsGenerator.conflicts2remove)).add(m3);
			m4.add(ConflictsGenerator.truthvalues).write(new FileOutputStream(location+infer+"truth"+i+".nt"), "NT").close();
			m2.close();
			m1.close();
			m3.close();
			ConflictsGenerator.conflicts2remove.removeAll();
			ConflictsGenerator.truthvalues.removeAll();
		}
		String sname = ChangeGenerator.tcgfilename.substring(0, ChangeGenerator.tcgfilename.indexOf("."))+ m +".nt";
		ChangeGenerator.tcg_model.write(new FileOutputStream(ChangeGenerator.createfile(sname)), "NT");	
		
		ConflictsGenerator.truthvalues.close();
		ConflictsGenerator.conflicts2remove.close();
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
		int numberOfChangeTypes = 11;
		int[] a = new int[N];
		Random generator = new Random();
		for (int i = 0; i < a.length; i++) {
			a[i] = generator.nextInt(numberOfChangeTypes) + 1;
		}
		return a;
	}
}
