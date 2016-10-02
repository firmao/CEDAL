package parallel;

import java.io.IOException;
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

public class ErrorDetectionParallel {

	public static Set<String> lstReport = new HashSet<String>();

	public static void analyse(Map<String, Set<String>> result) throws Exception {
		Map<Integer, Set<String>> clusters = new HashMap<Integer, Set<String>>();

		// for (Set<String> value : result.values()) {
		try {
			Object[] arrValues = result.values().toArray();
			IntStream.range(0, arrValues.length).parallel().forEach(id -> {
				Set<String> value = (Set<String>) arrValues[id];
				if (!clusters.containsValue(value)) {
					clusters.put(id, value);
					try {
						getAnalysis(value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createReport(Set<String> pLstReport, String dir) throws IOException {
		PrintWriter writer = new PrintWriter("erroReport_" + dir + "_parallel.csv", "UTF-8");
		writer.println("File" + "\t" + "Dataset" + "\t" + "ClusterError");
		for (String elem : pLstReport) {
			writer.println(elem);
		}
		writer.close();
	}

	private static void getAnalysis(Set<String> cluster) throws IOException {
		Map<String, String> clusterDataset = new HashMap<String, String>();
		for (String elem : cluster) {
			clusterDataset.put(elem, getDataset(elem));
		}
		printErrors(clusterDataset);
	}

	private static String getDataset(String elem) {
		String dsName = null;
		for (Map.Entry<String, Set<String>> entry : ClosureGeneratorParallel.datasets.entrySet()) {
			if (entry.getValue().contains(elem))
				return entry.getKey();
		}
		return dsName;
	}

	private static void printErrors(Map<String, String> map) throws IOException {
		Map<String, ArrayList<String>> reverseMap = new HashMap<>(map.entrySet().stream()
				.collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
				.collect(Collectors.toMap(item -> item.get(0).getValue(),
						item -> new ArrayList<>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));

		// System.out.println(reverseMap);

		for (Map.Entry<String, ArrayList<String>> entry : reverseMap.entrySet()) {
			List<String> lstValues = entry.getValue();
			String key = entry.getKey();
			if (lstValues.size() > 1) {
				String line = key + "\t" + reverseMap.get(key);
				lstReport.add(line);
			}
		}
	}

	public static long getTriples(String fileName) {
		// String fileName = "sameAs/" + key.split("\t")[0];
		long lineCount = 0;
		try {
			final Path path = Paths.get(fileName);
			lineCount = Files.lines(path).count();
		} catch (Exception e) {
			//e.printStackTrace();
		}

		return lineCount;
	}

}
