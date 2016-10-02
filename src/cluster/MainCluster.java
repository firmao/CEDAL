package cluster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainCluster {
	static long triples = 0;
	static Set<String> txtContent = new HashSet<String>();
	static long timeMixFiles, timeClosures, timeErrorDetection;
	static long totalSameAsTriples = 19200114;

	public static void main(String[] args) {
		//File dir = new File("sameAs/sample");
		//File dir = new File("sameAs/s1000");   //10^3
		//File dir = new File("sameAs/s10000");  //10^4
		//File dir = new File("sameAs/s100000"); //10^5
		//File dir = new File("sameAs/s1000000");  //10^6
		File dir = new File("C:\\Users\\andre\\Dropbox\\RunClosuresJar\\sameAs\\s1000");
		System.out.println("7Available Cores = " + Runtime.getRuntime().availableProcessors());
		//totalSameAsTriples = countTriples(dir); // TotalSameAsTriples = 19200114
		System.out.println("TotalSameAsTriples = " + totalSameAsTriples);

		String predicate = "http://www.w3.org/2002/07/owl#sameAs";
		tClosure(predicate, dir);
		// final long totalTime = System.currentTimeMillis() - start;
		final String fileName = "summarizarion_" + dir.getName().split("\\\\")[0] + "_cluster.txt";

		txtContent.add("TotalTime(MixFiles): " + timeMixFiles + " ms");
		txtContent.add("TotalTime(Closures+ErrorDetection): " + timeClosures + " ms");
		txtContent.add("Available Cores = " + Runtime.getRuntime().availableProcessors());

		final long totalTime = timeMixFiles + timeClosures;
		txtContent.add("TotalTime: " + totalTime + " ms");
		txtContent.add("Triples: " + triples);
		ClosureGeneratorCluster.generateFile(txtContent, fileName);
	}

	private static Set<File> getFiles(File dir) throws IOException {
		Set<File> setFiles = null;
		if (dir.isDirectory()) {
			setFiles = Files.walk(Paths.get(dir.getPath())).filter(Files::isRegularFile).map(Path::toFile)
					.collect(Collectors.toSet());
		}
		return setFiles;
	}

	private static long countTriples(File dir) {
		long ret = 0;
		try {
			File[] files = getFiles(dir).stream().toArray(File[]::new);
			for (int i = 0; i < files.length; i++) {
				ret += ErrorDetectionCluster.getTriples(files[i].getAbsolutePath());
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return ret;
	}

	/*
	 * Adjacency Lists
	 */
	private static void tClosure(String predicate, File dir) {
		try {
			String sDir = dir.getName().split("\\\\")[0];
			ClosureGeneratorCluster.cPredicate = predicate;
			Set<File> setFiles = new HashSet<File>();
			File[] files = getFiles(dir).stream().toArray(File[]::new);
			
			System.out.println("Starting add files...");
			// IntStream.range(0, files.length).parallel().forEach(i -> {
			for (int i = 0; i < files.length; i++) {
				setFiles.add(files[i]);
				triples += ErrorDetectionCluster.getTriples(files[i].getAbsolutePath());
			}
			// });
			System.out.println("Finished add files.");
			try {
				System.out.println("Starting Mix files...");
				long start = System.currentTimeMillis();
				Map<String, Set<String>> cluster = ClosureGeneratorCluster.mixFiles(setFiles);
				timeMixFiles += System.currentTimeMillis() - start;
				System.out.println("Finished Mix files...");

				System.out.println("Starting clusters and EDA...");
				start = System.currentTimeMillis();
				ClosureGeneratorCluster.analyseCluster(cluster);
				timeClosures = System.currentTimeMillis() - start;
				System.out.println("Finished cluster and EDA...");
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			System.out.println("Starting create report...");
			ErrorDetectionCluster.createReport(ErrorDetectionCluster.lstReport, sDir);
			System.out.println("Finished create report.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
