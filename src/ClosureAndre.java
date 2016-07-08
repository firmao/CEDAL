import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class ClosureAndre {
	public static int cTriplesBefore = 0;
	public static int cTriplesAfter = 0;
	public static int totalFiles = 0;
	public static long totalTime = 0;
	public static Map<String, String> filesProblem = new HashMap<String, String>();
	public static String cPredicate = null;

	public static Map<String, Set<String>> getTC(Map<String, Set<String>> ret) throws Exception {
		for (Map.Entry<String, Set<String>> entry : ret.entrySet()) {
			String keyplus = entry.getKey();
			Set<String> value = entry.getValue();
			for (String key : ret.keySet()) {
				if (value.contains(key)) {
					for (String elem : ret.get(key))
						ret.get(keyplus).add(elem);
				}
			}
		}

		return ret;
	}

	public static Map<String, Set<String>> getRC(Map<String, Set<String>> ret) throws Exception {
		List<String> lstKeys = new ArrayList<String>();
		
		for (String key : ret.keySet()) {
			lstKeys.add(key);
		}
		
		for (String key : lstKeys) {
			ret.get(key).add(key);
			Set<String> objs = ret.get(key);
			for (String obj : objs) {
				if(!ret.containsKey(obj))
				{
					Set<String> value = new HashSet<String>();
					value.add(obj);
					ret.put(obj, value);
				}
			}
		} 

		return ret;
	}
	
	public static Map<String, Set<String>> getSC(Map<String, Set<String>> ret) throws Exception {
		List<String> lstKeys = new ArrayList<String>();
		
		for (String key : ret.keySet()) {
			lstKeys.add(key);
		}
		
		for (String key : lstKeys) {
			//ret.get(key).add(key);
			Set<String> objs = ret.get(key);
			for (String obj : objs) {
				if(!ret.containsKey(obj))
				{
					Set<String> value = new HashSet<String>();
					value.add(key);
					ret.put(obj, value);
				}
				else {
					ret.get(obj).add(key);
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
	private static Map<String, Set<String>> getAllRelations(String pathFile) throws Exception {
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

			if (cPredicate != null)
				if (cPredicate.equalsIgnoreCase(stmt.getPredicate().getURI())) {
					cTriplesBefore++;
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
				else
					System.err.println("Different predicate: " + stmt.getPredicate().getURI());
		}
		return ret;
	}

	public static void generateFile(Map<String, Set<String>> result, String fileName) {
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			for (String key : result.keySet()) {
				for (String value : result.get(key)) {
					String elem = "<" + key + "> <" + cPredicate + "> <" + value + "> .";
					writer.println(elem);
					cTriplesAfter++;
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateAllFiles(File fPath, String predicate) {
		try {
			cPredicate = predicate;
			if (fPath.isDirectory()) {
				List<File> filesInFolder = Files.walk(Paths.get(fPath.getPath())).filter(Files::isRegularFile)
						.map(Path::toFile).collect(Collectors.toList());

				File[] fdir = filesInFolder.stream().toArray(File[]::new);
				File fOut = new File("closureAndre");
				if (!fOut.exists())
					fOut.mkdirs();
				totalFiles = fdir.length;
				final long start = System.currentTimeMillis();

				IntStream.range(0, fdir.length).parallel().forEach(id -> {
					try {
						// System.out.println("File: " + fdir[id].getName());
						Map<String, Set<String>> result = getClosure(fdir[id]);
						generateFile(result, "closureAndre\\" + fdir[id].getName());
					} catch (Exception e) {
						filesProblem.put(fdir[id].getName(), e.getMessage());
						e.printStackTrace();
					}
				});
				totalTime = System.currentTimeMillis() - start;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Set<String>> getClosure(File file) throws Exception {
		
		Map<String, Set<String>> ret = getAllRelations(file.getAbsolutePath());
		//System.out.println("Input: " + ret);
		
		ret=getRC(ret);
		ret=getSC(ret);
		ret=getTC(ret);
		
		return ret;
	}
}
