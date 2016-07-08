import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class Verifier {
	public static boolean isReflexive(File f)
	{
		boolean ret = false;
		Map<String, Set<String>> rel = getAllRelations(f.getAbsolutePath());
		// System.out.println("Input: " + ret);
		for (Map.Entry<String, Set<String>> entry : rel.entrySet()) {
			String keyplus = entry.getKey();
			Set<String> value = entry.getValue();
			for (String key : rel.keySet()) {
				if (value.contains(key)) {
					ret=true;
				}
			}
		}
		
		return ret;
	}
	
	public static boolean isSymmetric(File f)
	{
		boolean ret = false;
		Map<String, Set<String>> rel = getAllRelations(f.getAbsolutePath());
		// System.out.println("Input: " + ret);
		for (Map.Entry<String, Set<String>> entry : rel.entrySet()) {
			String keyplus = entry.getKey();
			Set<String> value = entry.getValue();
			for (String key : rel.keySet()) {
				if (value.contains(key)) {
					ret=true;
				}
			}
		}
		
		return ret;
	}
	
	/*
	 * Organize the input file in a hashMap. <uriA><owl:sameAs><uriB>
	 * <uriB><owl:sameAs><uriC> <uriB><owl:sameAs><uriD>
	 * <uriD><owl:sameAs><uriE> <uriF><owl:sameAs><uriD>
	 * 
	 * HashMapOutput: hMap("<uriA>","<uriB>") hMap("<uriB>","<uriC> <uriD>")
	 * hMap("<uriD>","<uriE>") hMap("<uriE>","<uriD>")
	 */
	private static Map<String, Set<String>> getAllRelations(String pathFile) {
		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();

		String uriS = null;
		String uriO = null;

		Model model = ModelFactory.createDefaultModel();
		InputStream is = FileManager.get().open(pathFile);

		if (is != null) {
			model.read(is, null, "N-TRIPLE");
		} else {
			System.err.println("cannot read " + pathFile);
			;
		}
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			RDFNode object = stmt.getObject(); // get the object
			uriS = subject.getURI();
			uriO = object.toString();
			if (ret.containsKey(uriS)) {
				ret.get(uriS).add(uriO);
			} else {
				Set<String> obj = new HashSet<String>();
				obj.add(uriO);
				ret.put(uriS, obj);
			}
		}
		return ret;
	}
}
