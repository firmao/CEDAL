package parallel;

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

public class MainParallel {
	static long triples = 0;
	static Set<String> txtContent = new HashSet<String>();
	static long timeMixFiles, timeClosures, timeErrorDetection;
	static long totalSameAsTriples = 19200114;

	public static void main(String[] args) {
		//File dir = new File("sameAs/s1000");
		// File dir = new File("sameAs/s10000");
		//File dir = new File("sameAs/s100000");
		File dir = new File("sameAs/s1000000");
		// File dir = new File("C:\\Users\\andre\\Dropbox\\RunClosuresJar\\sameAs\\s1000");
		System.out.println("7Available Cores = " + Runtime.getRuntime().availableProcessors());
		//totalSameAsTriples = countTriples(dir); // TotalSameAsTriples = 19200114
		System.out.println("TotalSameAsTriples = " + totalSameAsTriples);

		String predicate = "http://www.w3.org/2002/07/owl#sameAs";
		tClosure2(predicate, dir);
		// final long totalTime = System.currentTimeMillis() - start;
		final String fileName = "summarizarion_" + dir.getName().split("\\\\")[0] + "_parallel.txt";

		timeClosures = ClosureGeneratorParallel.timeRC + ClosureGeneratorParallel.timeSC
				+ ClosureGeneratorParallel.timeTC;
		txtContent.add("TotalTime(MixFiles): " + timeMixFiles + " ms");
		txtContent.add("TotalTime(Closures): " + timeClosures + " ms");
		txtContent.add("TotalTime(RC): " + ClosureGeneratorParallel.timeRC + " ms");
		txtContent.add("TotalTime(SC): " + ClosureGeneratorParallel.timeSC + " ms");
		txtContent.add("TotalTime(TC): " + ClosureGeneratorParallel.timeTC + " ms");
		txtContent.add("TotalTime(ErrorDetection): " + timeErrorDetection + " ms");
		txtContent.add("Available Cores = " + Runtime.getRuntime().availableProcessors());

		final long totalTime = timeMixFiles + timeErrorDetection + timeClosures;
		txtContent.add("TotalTime: " + totalTime + " ms");
		txtContent.add("Triples: " + triples);
		ClosureGeneratorParallel.generateFile(txtContent, fileName);
	}

	private static void tClosure(String predicate, File dir) {
		try {
			String sDir = dir.getName().split("\\\\")[0];
			ClosureGeneratorParallel.cPredicate = predicate;
			Set<File> setFiles = new HashSet<File>();
			File[] files = getFiles(dir).stream().toArray(File[]::new);
			for (int i = 0; i < files.length; i++) {
				// IntStream.range(0, files.length).parallel().forEach(i -> {
				for (int j = i + 1; j < files.length; j++) {
					setFiles.add(files[i]);
					setFiles.add(files[j]);
					try {
						long start = System.currentTimeMillis();
						Map<String, Set<String>> map = ClosureGeneratorParallel.mixFiles(setFiles);
						timeMixFiles += System.currentTimeMillis() - start;

						// start = System.currentTimeMillis();
						final Map<String, Set<String>> result = ClosureGeneratorParallel.getClosure(map);
						// timeClosures += System.currentTimeMillis() - start;

						start = System.currentTimeMillis();
						ErrorDetectionParallel.analyse(result);
						timeErrorDetection += System.currentTimeMillis() - start;
					} catch (Exception e) {
						e.printStackTrace();
					}
					setFiles.clear();
					System.out.println("File: " + files[i].getName() + " XXX " + files[j].getName());
					System.out.println("Triples:" + triples + " from " + totalSameAsTriples);
				}
				//triples += ErrorDetectionParallel.getTriples(files[i].getAbsolutePath());

				// });
			}
			ErrorDetectionParallel.createReport(ErrorDetectionParallel.lstReport, sDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
				ret += ErrorDetectionParallel.getTriples(files[i].getAbsolutePath());
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return ret;
	}

	private static void tClosure2(String predicate, File dir) {
		try {
			String sDir = dir.getName().split("\\\\")[0];
			ClosureGeneratorParallel.cPredicate = predicate;
			Set<File> setFiles = new HashSet<File>();
			File[] files = getFiles(dir).stream().toArray(File[]::new);
			
			System.out.println("Starting add files...");
			// IntStream.range(0, files.length).parallel().forEach(i -> {
			for (int i = 0; i < files.length; i++) {
				setFiles.add(files[i]);
				triples += ErrorDetectionParallel.getTriples(files[i].getAbsolutePath());
			}
			// });
			System.out.println("Finished add files.");
			try {
				System.out.println("Starting Mix files...");
				long start = System.currentTimeMillis();
				Map<String, Set<String>> map = ClosureGeneratorParallel.mixFiles(setFiles);
				timeMixFiles += System.currentTimeMillis() - start;
				System.out.println("Finished Mix files...");

				System.out.println("Starting closures files...");
				// start = System.currentTimeMillis();
				final Map<String, Set<String>> result = ClosureGeneratorParallel.getClosure(map);
				// timeClosures += System.currentTimeMillis() - start;
				System.out.println("Finished closures files...");

				System.out.println("Starting EDA files...");
				start = System.currentTimeMillis();
				ErrorDetectionParallel.analyse(result);
				timeErrorDetection += System.currentTimeMillis() - start;
				System.out.println("Finished EDA files...");
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			System.out.println("Starting create report...");
			ErrorDetectionParallel.createReport(ErrorDetectionParallel.lstReport, sDir);
			System.out.println("Finished create report.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
