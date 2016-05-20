import java.io.File;
import java.util.Map;
import java.util.Set;

public class Main {

	public static void main(String[] args) {
		t4();
	}
	
	public static void t1()
	{
		try {
			File f = new File("input.nt");
			Map<String, Set<String>> result = MyTC.getTC(f);
			System.out.println("Output: " + result);
			MyTC.generateFile(result, "output.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void t2()
	{
		try {
			File f = new File("correct");
			MyTC.generateAllFiles(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void tPellet() {
 		System.out.println("==== owl:sameAs (reflexive + symmetric + transitive) ====");
 		MyTCPellet.closure(OWL.sameAs, new File("input_reflexive_symmetric_transitive.nt"), new File("pellet_output_reflexive_symmetric_transitive.nt"));
 		
 		System.out.println("==== saws:hasAncestor (transitive) ====");
		Property hasAncestor = ResourceFactory.createProperty("http://purl.org/saws/ontology#hasAncestor");
 		MyTCPellet.closure(hasAncestor, new File("input_transitive.nt"), new File("pellet_output_transitive.nt"));
 	}
 	
	public static void t3()
	{
		try {
			MyTCJena.testReadNtriple("input.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void t4()
	{
		try {
			Map<String, Set<String>> result = MyTCJena.getTC("input.nt");
			System.out.println("Output: " + result);
			MyTC.generateFile(result, "outputJena.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
