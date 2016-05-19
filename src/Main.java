import java.io.File;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;

public class Main {

	public static void main(String[] args) {
//		t2();
		t3();
	}
	
	public static void t1()
	{
		try {
			File f = new File("input_transitive.nt");
			Map<String, Set<String>> result = MyTC.getTC(f);
			System.out.println("Output: " + result);
			MyTC.generateFile(result, "input_transitive.nt");
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
	
	public static void t3() {
		System.out.println("==== owl:sameAs (reflexive + symmetric + transitive) ====");
		MyTCPellet.closure(OWL.sameAs, new File("input_reflexive_symmetric_transitive.nt"), new File("pellet_output_reflexive_symmetric_transitive.nt"));
		
		System.out.println("==== saws:hasAncestor (transitive) ====");
		Property hasAncestor = ResourceFactory.createProperty("http://purl.org/saws/ontology#hasAncestor");
		MyTCPellet.closure(hasAncestor, new File("input_transitive.nt"), new File("pellet_output_transitive.nt"));
	}
	
}
