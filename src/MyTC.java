import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class MyTC {

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
	public static Map<String, Set<String>> getTC(File f) {
		Map<String, Set<String>> ret = getAllRelations(f);
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
	private static Map<String, Set<String>> getAllRelations(File f) {
		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();

		String line;
		String uri1 = null;
		String uri2 = null;

		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			while (((line = br.readLine()) != null)) {
				uri1 = line.substring(1, line.indexOf('>'));
				uri2 = line.substring(line.lastIndexOf('<') + 1, line.lastIndexOf('>'));
				if (ret.containsKey(uri1)) {
					ret.get(uri1).add(uri2);
				} else {
					Set<String> obj = new HashSet<String>();
					obj.add(uri2);
					ret.put(uri1, obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
