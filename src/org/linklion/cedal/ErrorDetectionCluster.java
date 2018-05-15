package cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

import util.CleanLinkSets;

public class ErrorDetectionCluster {

	public static Set<String> lstReport = new HashSet<String>();

	public static void analyse(Map<String, Set<String>> result) throws Exception {
		int i = 0;
		Map<Integer, Set<String>> clusters = new HashMap<Integer, Set<String>>();
		for (Set<String> value : result.values()) {
			if (!clusters.containsValue(value)) {
				clusters.put(i++, value);
			}
		}
		for (Set<String> value : clusters.values()) {
			getAnalysis(value);
		}
		printDatasetNoErrors();
	}

	public static void createReport(Set<String> pLstReport, String dir) throws IOException {
		String fileName = "erroReport_" + dir + "_parallel.csv";
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.println("File" + "\t" + "Dataset" + "\t" + "ClusterError" + "\t" + "Errors" + "\t" + "Score");
		for (String line : pLstReport) {
			writer.println(line);
		}
		writer.close();
		TSVReader.analisysTop(fileName);
		CleanLinkSets.cleanLinksets(new File(fileName), new File(dir), dir + "_Fixed");
	}
	
	public static void createReport_old(Set<String> pLstReport, String dir) throws IOException {
		String fileName = "erroReport_" + dir + "_parallel.csv";
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.println("File" + "\t" + "Dataset" + "\t" + "ClusterError");
		for (String line : pLstReport) {
			writer.println(line);
		}
		writer.close();
	}

	public static void getAnalysis(Set<String> cluster) throws IOException {
		Map<String, String> clusterDataset = new HashMap<String, String>();
		for (String resource : cluster) {
			clusterDataset.put(resource, getDataset(resource));
		}
		printErrors(clusterDataset);
	}

	private static String getDataset(String resource) {
		String dsName = null;
		for (Map.Entry<String, Set<String>> entry : ClosureGeneratorCluster.datasets.entrySet()) {
			if (entry.getValue().contains(resource))
				return entry.getKey();
		}
		return dsName;
	}

	private static void printErrors(Map<String, String> map) throws IOException {
		Map<String, ArrayList<String>> reverseMap = new HashMap(map.entrySet().stream()
				.collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
				.collect(Collectors.toMap(item -> item.get(0).getValue(),
						item -> new ArrayList<>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));

		// System.out.println(reverseMap);

		for (Map.Entry<String, ArrayList<String>> entry : reverseMap.entrySet()) {
			List<String> lstValues = entry.getValue();
			String key = entry.getKey();
			if (lstValues.size() > 1) {
				int score = (lstValues.size() * (lstValues.size() - 1)) / 2;
				String line = key + "\t" + reverseMap.get(key) + "\t" + lstValues.size() + "\t" + score;
				try{
				lstReport.add(line);
				
				//ClosureGeneratorCluster.datasets.remove(reverseMap.get(key));
				String [] str = key.split("\t");
				String fName = str[0];
				String dataset = str[1];
				ClosureGeneratorCluster.allGoodFiles.remove(fName);
				ClosureGeneratorCluster.allGoodDatasets.remove(dataset);
				
				}catch(Exception ex){}
			}
		}
		try{
			printDatasetNoErrors();
		}catch(Exception e){}
	}

	private static void printDatasetNoErrors() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("GoodDFilesAndDataSets.txt", "UTF-8");
		
		int numGoodFiles = ClosureGeneratorCluster.allGoodFiles.size();
		int numGoodDatasets = ClosureGeneratorCluster.allGoodDatasets.size();
		
		writer.println("Number of good Linkset files: " + numGoodFiles);
		writer.println("Number of good Datasets: " + numGoodDatasets);
		writer.println("#######");
		writer.println("Good Linkset files: " + ClosureGeneratorCluster.allGoodFiles);
		writer.println("#######");
		writer.println("good Datasets: " + ClosureGeneratorCluster.allGoodDatasets);
		
		writer.close();
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
