package eis.iai.uni.bonn.de;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

public class ConvertCSVtoRDF {

	public static void convertRDF2CSV(String filename, String outputfilename, String filesyntax) throws IOException {
			Model model = FileManager.get().loadModel(filename, filesyntax);  
			String content = "subject,predicate,object \n";

			StmtIterator iter1 = model.listStatements();
			while (iter1.hasNext()) {
				Statement stmt = iter1.next();		
				content += stmt.getSubject().toString() +","+ stmt.getPredicate().toString() + "," + stmt.getObject().toString() + "\n";
			}
			File file = new File(outputfilename);
			if(file.exists())
				file.delete();
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			model.close();
		  }
	  
	public static void convertCSVToRDF(String inputFilename, String ofilename, String outputType) throws IOException {
		File file = new File(ofilename);
		if(file.exists())			
			file.delete();
		file.createNewFile();
		Model model = FileManager.get().loadModel(ofilename, outputType);
		String line = "";
		String cvsSplitBy = ",";
		BufferedReader br = new BufferedReader(new FileReader(inputFilename));
		if ((line = br.readLine()) != null) //skipping s,p,o
			;
		while ((line = br.readLine()) != null) {
			String[] spo = line.split(cvsSplitBy);
			Triple t = null;
			if (spo[2].contains("http"))
				t = new Triple(ResourceFactory.createResource(spo[0]).asNode(),ResourceFactory.createProperty(spo[1]).asNode(),
					ResourceFactory.createResource(spo[2]).asNode());
			else
				t = new Triple(ResourceFactory.createResource(spo[0]).asNode(),ResourceFactory.createProperty(spo[1]).asNode(),
						NodeFactory.createLiteral(spo[2]));
			model.add(model.asStatement(t));

		}
		model.write(new FileOutputStream(ofilename), outputType);
		model.close();
	}

//	public static void sol (String inputFilename, String outputFilename,String outputType) throws IOException {
//		CSV2RDF.init();//Initialise the CSV conversion engine in Jena
//		GraphCSV newGraph = new GraphCSV(inputFilename);
//		Model model = ModelFactory.createModelForGraph(newGraph);
//
//		/*   
//
//	              //Manually insert class triples for each instance in the CSV file
//	              String sparqlQueryString = "select distinct ?s where  {?s ?p ?o}";
//	              Query query = QueryFactory.create(sparqlQueryString);
//	              QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, model);
//	              ResultSet s = qexec.execSelect();
//	              Model m2 = ModelFactory.createDefaultModel();
//	              while(s.hasNext()) {
//	                     QuerySolution so = s.nextSolution();
//	                     Triple t = new Triple(so.getResource("s").asNode(),RDF.type.asNode(),
//	                                  NodeFactory.createBlankNode(file));
//	                     Statement stmt = ResourceFactory.createStatement(so.getResource("s"), RDF.type, 
//	                                         ResourceFactory.createResource(file));
//	                     m2.add(stmt);
//	              }
//	              Model m3 = ModelFactory.createUnion(model, m2); //create a new RDF graph which "unions"
//	                                                              //the old graph with the new graph containing
//	                                                              //the new rows
//		 */Model m3 = ModelFactory.createDefaultModel();
//		 m3.add(model.listStatements());
//		 //Now serialize the RDF graph to an output file using the outputType input variable  
//		 //you specify. It should be “N-Triple” in our case.
//
//		 //   FileWriter out = new FileWriter(outputFilename);
//		 //    m3.write(out,outputType);
//
//		 try {
//			 m3.write(new FileOutputStream(outputFilename), "NT");
//		 } catch (FileNotFoundException e) {
//			 // TODO Auto-generated catch block
//			 e.printStackTrace();
//		 }
//		 /* } catch (Exception e) {
//	                     System.out.println("Error in the file output process!");
//	                     e.printStackTrace();
//	              }*/
//		 /*                   
//	              //Delete specific triples of a specific predicate called ¨row¨                 
//	              File output = new File(outputFilename);
//	              File tempFile = new File("temp.nt");
//	              BufferedReader reader = null;
//	              BufferedWriter writer = null;
//	              try {
//	                     reader = new BufferedReader(new FileReader(output));
//	                     writer = new BufferedWriter(new FileWriter(tempFile));
//	                     String currentLine;
//	                     //Delete triples from the old file by skipping it while reading the input N-Triple
//	             //        file from the last step, otherwise write the triple to a new temp file!
//	                     while ((currentLine = reader.readLine()) != null) {
//	                           if (currentLine.contains("http://w3c/future-csv-vocab/row")) {
//	                                  continue;
//	                           } else {
//	                                  writer.write(currentLine);
//	                                  writer.newLine();
//	                           }
//	                     }
//	                     writer.close();
//	                     reader.close();
//
//	                     PrintWriter printer = new PrintWriter(output);
//	                     printer.print("");
//	                     printer.close();
//
//	                     //copy content from temp file to final output file, overwriting it.
//	                     FileUtils.copyFile(tempFile, output);
//	              } catch (FileNotFoundException e1) {
//	                     // TODO Auto-generated catch block
//	                     e1.printStackTrace();
//	              } catch (IOException e) {
//	                     // TODO Auto-generated catch block
//	                     e.printStackTrace();
//	              } */
//	}
}
