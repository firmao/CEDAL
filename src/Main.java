import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;

public class Main {

	static String input = "input2.nt";
	static String output = "output2.nt";
	public static void main(String[] args) {
		File dir = new File("c1_to_3");
		String predicate = "http://www.w3.org/2002/07/owl#sameAs";
		allMyTCJena(dir, predicate);
		allMyTCAndre(dir, predicate);
//		// allMyTC(dir);
//		// tPellet();
		allTCPellet(dir);
		
		// t1(predicate);
		// t2();
		//tRC(predicate);
		//tSC(predicate);
		//tTC(predicate);
		//tClosure(predicate);
	}

	private static void tClosure(String predicate) {
		try {
			ClosureAndre.cPredicate = predicate;
			File f = new File(input);
			Map<String, Set<String>> result = ClosureAndre.getClosure(f);
			System.out.println("Closure: " + result);
			ClosureAndre.generateFile(result, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void tTC(String predicate) {
		try {
			MyTCJena.cPredicate = predicate;
			File f = new File(input);
			Map<String, Set<String>> result = MyTCJena.getTC(f.getAbsolutePath());
			System.out.println("Transitive Closure: " + result);
			//MyTCJena.generateFile(result, "output2.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void tSC(String predicate) {
		try {
			MyTCJena.cPredicate = predicate;
			File f = new File(input);
			Map<String, Set<String>> result = MyTCJena.getSC(f.getAbsolutePath());
			System.out.println("Symmmetric Closure: " + result);
			//MyTCJena.generateFile(result, "output2.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void tRC(String predicate) {
		try {
			MyTCJena.cPredicate = predicate;
			File f = new File(input);
			Map<String, Set<String>> result = MyTCJena.getRC(f.getAbsolutePath());
			System.out.println("Reflexive Closure: " + result);
			//MyTCJena.generateFile(result, "output2.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void t1(String predicate) {
		try {
			MyTCJena.cPredicate = predicate;
			File f = new File("input2.nt");
			Map<String, Set<String>> result = MyTCJena.getClosure(f);
			System.out.println("Output: " + result);
			MyTCJena.generateFile(result, "output2.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void t2() {
		try {
			File f = new File("input.nt");
			MyTCPellet.closure(OWL.sameAs, f, new File("pellet_output.nt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void tPellet() {
		System.out.println("==== owl:sameAs (reflexive + symmetric + transitive) ====");
		MyTCPellet.closure(OWL.sameAs, new File("input_reflexive_symmetric_transitive.nt"),
				new File("pellet_output_reflexive_symmetric_transitive.nt"));

		System.out.println("==== saws:hasAncestor (transitive) ====");
		Property hasAncestor = ResourceFactory.createProperty("http://purl.org/saws/ontology#hasAncestor");
		MyTCPellet.closure(hasAncestor, new File("input_transitive.nt"), new File("pellet_output_transitive.nt"));
	}

	public static void allTCPellet(File f) {
		try {
			long startTime = System.currentTimeMillis();
			MyTCPellet.generateAllFilesPellet(f);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			PrintWriter writer = new PrintWriter("results\\" + f.getName() + "_Pellet.txt", "UTF-8");
			writer.println("Directory: " + f.getAbsolutePath());
			writer.println("TotalFiles: " + MyTCPellet.totalFiles);
			writer.println("TotalTime is: " + totalTime + " ms");
			writer.println("Number of triples Before TC: " + MyTCPellet.cTriplesBefore);
			writer.println("Number of triples After TC: " + MyTCPellet.cTriplesAfter);
			writer.println("Files with problems: " + MyTCPellet.filesProblem.size() +", Problems: " + MyTCPellet.filesProblem.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void t3() {
		try {
			MyTCJena.testReadNtriple("input.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void allMyTC(File f) {
		try {
			MyTC.generateAllFiles(f);
			PrintWriter writer = new PrintWriter("results\\" + f.getName() + ".txt", "UTF-8");
			writer.println("Directory: " + f.getAbsolutePath());
			writer.println("TotalFiles: " + MyTC.totalFiles);
			writer.println("TotalTime is: " + MyTC.totalTime + " ms");
			writer.println("Number of triples Before TC: " + MyTC.cTriplesBefore);
			writer.println("Number of triples After TC: " + MyTC.cTriplesAfter);
			writer.println("Files with problems: " + MyTC.filesProblem.size() +", Problems: " + MyTC.filesProblem.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void allMyTCJena(File f, String predicate) {
		try {
			MyTCJena.generateAllFiles(f, predicate);
			PrintWriter writer = new PrintWriter("results\\" + f.getName() + "_Jena.txt", "UTF-8");
			writer.println("Directory: " + f.getAbsolutePath());
			writer.println("TotalFiles: " + MyTCJena.totalFiles);
			writer.println("TotalTime is: " + MyTCJena.totalTime + " ms");
			writer.println("Number of triples Before TC: " + MyTCJena.cTriplesBefore);
			writer.println("Number of triples After TC: " + MyTCJena.cTriplesAfter);
			writer.println("Files with problems: " + MyTCJena.filesProblem.size() +", Problems: " + MyTCJena.filesProblem.toString());

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void allMyTCAndre(File f, String predicate) {
		try {
			ClosureAndre.generateAllFiles(f, predicate);
			PrintWriter writer = new PrintWriter("results\\" + f.getName() + "_Andre.txt", "UTF-8");
			writer.println("Directory: " + f.getAbsolutePath());
			writer.println("TotalFiles: " + ClosureAndre.totalFiles);
			writer.println("TotalTime is: " + ClosureAndre.totalTime + " ms");
			writer.println("Number of triples Before TC: " + ClosureAndre.cTriplesBefore);
			writer.println("Number of triples After TC: " + ClosureAndre.cTriplesAfter);
			writer.println("Files with problems: " + ClosureAndre.filesProblem.size() +", Problems: " + ClosureAndre.filesProblem.toString());

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
