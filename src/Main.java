import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import parallel.ClosureGeneratorParallel;

public class Main {
	static long triples = 0;
	static long timeMixFiles, timeClosures, timeErrorDetection;
	public static void main(String[] args) throws IOException {
		final long start = System.currentTimeMillis();
		File dir = new File("sameAs/sample");
		//File dir = new File("sameAs/s1000");
		//File dir = new File("sameAs/s500000");
		//File dir = new File("sameAs/s100000");
		//File dir = new File("sameAs/s1000000");
		//printNTriples(dir);
		String predicate = "http://www.w3.org/2002/07/owl#sameAs";
		tClosure(predicate, dir);
		//final long totalTime = System.currentTimeMillis() - start;
		final String fileName = "summarizarion_" + dir.getName().split("\\\\")[0] + ".txt";
		Set<String> txtContent = new HashSet<String>();
		txtContent.add("TotalTime(MixFiles): " + timeMixFiles + " ms");
		
		timeClosures = ClosureGenerator.timeRC+ClosureGenerator.timeSC+ClosureGenerator.timeTC;
		txtContent.add("TotalTime(Closures): " + timeClosures + " ms");
		txtContent.add("TotalTime(RC): " + ClosureGenerator.timeRC + " ms");
		txtContent.add("TotalTime(SC): " + ClosureGenerator.timeSC + " ms");
		txtContent.add("TotalTime(TC): " + ClosureGenerator.timeTC + " ms");
		txtContent.add("TotalTime(ErrorDetection): " + timeErrorDetection + " ms");
		final long totalTime = timeMixFiles + timeErrorDetection+timeClosures;
		
		txtContent.add("TotalTime: " + totalTime + " ms");
		txtContent.add("Triples: " + triples);
		ClosureGenerator.generateFile(txtContent, fileName);
	}

	private static void tClosure(String predicate, File dir) {
		try {
			String sDir = dir.getName().split("\\\\")[0];
			ClosureGenerator.cPredicate = predicate;
			Map<String, Set<String>> result = null;
			Set<File> setFiles = new HashSet<File>();
			File[] files = getFiles(dir).stream().toArray(File[]::new);
			for (int i = 0; i < files.length; i++) {
				for (int j = i + 1; j < files.length; j++) {
					setFiles.add(files[i]);
					setFiles.add(files[j]);
					try {
						long start = System.currentTimeMillis();
						File f = ClosureGenerator.mixFiles(setFiles);
						timeMixFiles += System.currentTimeMillis() - start;
						
						//start = System.currentTimeMillis();
						result = ClosureGenerator.getClosure(f);
						//timeClosures += System.currentTimeMillis() - start;
						
						//ClosureGenerator.generateFile(result, output);
						start = System.currentTimeMillis();
						ErrorDetection.analyse(result);
						timeErrorDetection += System.currentTimeMillis() - start;
					} catch (Exception e) {
						e.printStackTrace();
					}
					setFiles.clear();
				}
				triples += ErrorDetection.getTriples(files[i].getAbsolutePath());
			}
			ErrorDetection.createReport(ErrorDetection.lstReport, sDir);
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
	
	private static void printNTriples(File dir) throws IOException
	{
		File[] files = getFiles(dir).stream().toArray(File[]::new);
		for (int i = 0; i < files.length; i++) {
			triples += ErrorDetection.getTriples(files[i].getAbsolutePath());
		}
		System.out.println("Dir: " + dir.getName() + " has " + triples + " triples.");
	}
}
