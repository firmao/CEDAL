import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class MyTCJena {
	public static void testReadNtriple(String pathFile) {
		Model model = ModelFactory.createDefaultModel();
		InputStream is = FileManager.get().open(pathFile);
		if (is != null) {
			model.read(is, null, "N-TRIPLE");
			ResIterator s = model.listSubjects();
			while (s.hasNext()) {
				System.out.println("  " + ((Resource) s.next()).getURI());
			}
		} else {
			System.err.println("cannot read " + pathFile);
			;
		}
	}

	/*
	 * input file: <A> <http://www.w3.org/2002/07/owl#sameAs> <B> . <B>
	 * <http://www.w3.org/2002/07/owl#sameAs> <C> . <B>
	 * <http://www.w3.org/2002/07/owl#sameAs> <D> . <D>
	 * <http://www.w3.org/2002/07/owl#sameAs> <E> . <F>
	 * <http://www.w3.org/2002/07/owl#sameAs> <D> .
	 * 
	 * output file: <A> <http://www.w3.org/2002/07/owl#sameAs> <B> . <A>
	 * <http://www.w3.org/2002/07/owl#sameAs> <C> . <A>
	 * <http://www.w3.org/2002/07/owl#sameAs> <D> . <A>
	 * <http://www.w3.org/2002/07/owl#sameAs> <E> . <B>
	 * <http://www.w3.org/2002/07/owl#sameAs> <C> . <B>
	 * <http://www.w3.org/2002/07/owl#sameAs> <D> . <B>
	 * <http://www.w3.org/2002/07/owl#sameAs> <E> . <D>
	 * <http://www.w3.org/2002/07/owl#sameAs> <E> . <F>
	 * <http://www.w3.org/2002/07/owl#sameAs> <D> . <F>
	 * <http://www.w3.org/2002/07/owl#sameAs> <E> .
	 */
	public static Map<String, Set<String>> getTC(String pathFile) {
		Map<String, Set<String>> ret = getAllRelations(pathFile);
		//System.out.println("Input: " + ret);
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
	        System.err.println("cannot read " + pathFile);;
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

	public static void generateFile(Map<String, Set<String>> result, String fileName) {
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			for (String key : result.keySet()) {
				for (String value : result.get(key)) {
					String elem = "<" + key + "> <http://www.w3.org/2002/07/owl#sameAs> <" + value + "> .";
					writer.println(elem);
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateAllFiles(File fPath) {
		try {
			if (fPath.isDirectory()) {
				List<File> filesInFolder = Files.walk(Paths.get(fPath.getPath())).filter(Files::isRegularFile)
						.map(Path::toFile).collect(Collectors.toList());

				File[] fdir = filesInFolder.stream().toArray(File[]::new);
				File fOut = new File("closure");
				if (!fOut.exists())
					fOut.mkdirs();
				
				long startTime = System.currentTimeMillis();
				
				IntStream.range(0, fdir.length).parallel().forEach(id -> {
					System.out.println("File: " + fdir[id].getName());
					Map<String, Set<String>> result = getTC(fdir[id].getAbsolutePath());
					generateFile(result, "closure\\" + fdir[id].getName());
				});
				
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				System.out.println("TotalTime is: " + totalTime + " milliseconds.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
