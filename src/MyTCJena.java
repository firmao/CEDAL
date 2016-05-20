import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class MyTCJena {
	public static void testReadNtriple(String pathFile)
	{
	    Model model = ModelFactory.createDefaultModel();
	    InputStream is = FileManager.get().open(pathFile);
	    if (is != null) {
	        model.read(is, null, "N-TRIPLE");
	        ResIterator s= model.listSubjects();
	            while (s.hasNext()) {
	                System.out.println("  " +
	                    ((Resource) s.next()).getURI());
	            }
	    } else {
	        System.err.println("cannot read " + pathFile);;
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
	        ResIterator s= model.listSubjects();
	        NodeIterator o= model.listObjects();
	        while (s.hasNext() || o.hasNext()) {
	        	
	        	uriS=((Resource) s.next()).getURI();
	        	uriO=((Resource) o.next()).getURI();
				if (ret.containsKey(uriS)) {
					ret.get(uriS).add(uriO);
				} else {
					Set<String> obj = new HashSet<String>();
					obj.add(uriO);
					ret.put(uriS, obj);
				}
	        }
	    } else {
	        System.err.println("cannot read " + pathFile);;
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
				IntStream.range(0, fdir.length).parallel().forEach(id -> {
					System.out.println("File: " + fdir[id].getName());
					Map<String, Set<String>> result = MyTC.getTC(fdir[id]);
					File fOut = new File("closure");
					if(!fOut.exists()) fOut.mkdirs();
					MyTC.generateFile(result, "closure\\" + fdir[id].getName());
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
