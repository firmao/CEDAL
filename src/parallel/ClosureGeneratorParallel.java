package parallel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class ClosureGeneratorParallel {
	public static int cTriplesBefore = 0;
	public static int cTriplesAfter = 0;
	public static int totalFiles = 0;
	public static long totalTime = 0;
	public static long timeRC, timeSC, timeTC;
	public static Map<String, String> filesProblem = new HashMap<String, String>();
	public static String cPredicate = null;
	public static Map<String, Set<String>> datasets = new HashMap<String, Set<String>>();

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
	
	public static Map<String, Set<String>> getTCp(Map<String, Set<String>> ret) throws Exception {
		for (Map.Entry<String, Set<String>> entry : ret.entrySet()) {
			String keyplus = entry.getKey();
			Set<String> value = entry.getValue();
			for (String key : ret.keySet()) {
				if (value.contains(key)) {
					String [] elem = ret.get(key).toArray(new String[ret.get(key).size()]);
					IntStream.range(0, elem.length).parallel().forEach(i -> {
					//for (String elem : ret.get(key))
						ret.get(keyplus).add(elem[i]);
					});
				}
			}
				
		}

		return ret;
	}

	public static Map<String, Set<String>> getRC(Map<String, Set<String>> ret) throws Exception {
		Set<String> setKeys = new HashSet<String>();

		for (String key : ret.keySet()) {
			setKeys.add(key);
		}

		for (String key : setKeys) {
			ret.get(key).add(key);
			Set<String> objs = ret.get(key);
			for (String obj : objs) {
				if (!ret.containsKey(obj)) {
					Set<String> value = new HashSet<String>();
					value.add(obj);
					ret.put(obj, value);
				}
			}
		}

		return ret;
	}

	public static Map<String, Set<String>> getRCp(Map<String, Set<String>> ret) throws Exception {
		Set<String> setKeys = new HashSet<String>();

		for (String key : ret.keySet()) {
			setKeys.add(key);
		}

		for (String key : setKeys) {
			ret.get(key).add(key);
			//Set<String> objs = ret.get(key);
			String [] objs = ret.get(key).toArray(new String[ret.get(key).size()]);
			IntStream.range(0, objs.length).parallel().forEach(i -> {
			//for (String obj : objs) {
				if (!ret.containsKey(objs[i])) {
					Set<String> value = new HashSet<String>();
					value.add(objs[i]);
					ret.put(objs[i], value);
				}
			//}
			});	
		}

		return ret;
	}
	
	public static Map<String, Set<String>> getSC(Map<String, Set<String>> ret) throws Exception {
		Set<String> setKeys = new HashSet<String>();

		for (String key : ret.keySet()) {
			setKeys.add(key);
		}

		for (String key : setKeys) {
			// ret.get(key).add(key);
			Set<String> objs = ret.get(key);
			for (String obj : objs) {
				if (!ret.containsKey(obj)) {
					Set<String> value = new HashSet<String>();
					value.add(key);
					ret.put(obj, value);
				} else {
					ret.get(obj).add(key);
				}
			}
		}

		return ret;
	}

	public static Map<String, Set<String>> getSCp(Map<String, Set<String>> ret) throws Exception {
		Set<String> setKeys = new HashSet<String>();

		for (String key : ret.keySet()) {
			setKeys.add(key);
		}

		for (String key : setKeys) {
			// ret.get(key).add(key);
			//Set<String> objs = ret.get(key);
			//for (String obj : objs) {
			String [] objs = ret.get(key).toArray(new String[ret.get(key).size()]);
			IntStream.range(0, objs.length).parallel().forEach(i -> {
				if (!ret.containsKey(objs[i])) {
					Set<String> value = new HashSet<String>();
					value.add(key);
					ret.put(objs[i], value);
				} else {
					ret.get(objs[i]).add(key);
				}
			//}
			});		
		}

		return ret;
	}
	
	public static Map<String, Set<String>> mixFiles(Set<File> setFiles) throws Exception {
		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();
		String line = null;
		String fileName = null;
		// Set<String> triples = new HashSet<String>();
		for (File f : setFiles) {
			fileName = f.getName().replaceAll(".nt", "");
			String sDs[] = fileName.split("---");
			if (sDs.length < 2) {
				// filesProblem.put(f.getName(), "File not in the standard
				// Ds1---Ds2");
				// throw new Exception("File not in the standard Ds1---Ds2");
				sDs = getDataSetDomain(f);
			}
			sDs[0] = f.getName() + "\t" + sDs[0];
			sDs[1] = f.getName() + "\t" + sDs[1];
			if (!datasets.containsKey(sDs[0]))
				datasets.put(sDs[0], new HashSet<String>());
			if (!datasets.containsKey(sDs[1]))
				datasets.put(sDs[1], new HashSet<String>());

			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				while (((line = br.readLine()) != null)) {
					// triples.add(line);
					String uriS = line.substring(1, line.indexOf('>'));
					String uriO = line.substring(line.lastIndexOf('<') + 1, line.lastIndexOf('>'));
					if (ret.containsKey(uriS)) {
						ret.get(uriS).add(uriO);
					} else {
						Set<String> obj = new HashSet<String>();
						obj.add(uriO);
						ret.put(uriS, obj);
					}
					datasets.get(sDs[0]).add(uriS);
					datasets.get(sDs[1]).add(uriO);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private static String[] getDataSetDomain(File f) throws IOException {
		String uriS = "ErrorSubject";
		String uriO = "ErrorObject";
		BufferedReader brTest = new BufferedReader(new FileReader(f));
		try {
			String line = brTest.readLine();
			uriS = line.substring(1, line.indexOf('>'));
			uriO = line.substring(line.lastIndexOf('<') + 1, line.lastIndexOf('>'));
			uriS = uriS.split("/")[2];
			uriO = uriO.split("/")[2];
			brTest.close();
		} catch (Exception e) {
			if (brTest != null)
				brTest.close();
		}
		String[] ret = { uriS, uriO };
		return ret;
	}

	public static File generateFile(Set<String> triples, String fileName) {
		File ret = new File(fileName);
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			for (String line : triples) {
				writer.println(line);
			}
			writer.close();
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

	public static Map<String, Set<String>> getClosure(Map<String, Set<String>> ret) throws Exception {

		// Map<String, Set<String>> ret = getAllRelations(file);
		// System.out.println("Input: " + ret);
		long start = System.currentTimeMillis();
		//ret = getRC(ret);
		ret = getRCp(ret);
		timeRC += System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		//ret = getSC(ret);
		ret = getSCp(ret);
		timeSC += System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		//ret = getTC(ret);
		ret = getTCp(ret);
		timeTC += System.currentTimeMillis() - start;
		return ret;
	}
}
