import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
	static long triples = 0;
	public static void main(String[] args) {
		final long start = System.currentTimeMillis();
		//File dir = new File("sameAs/s8");
		File dir = new File("sameAs/s8_1");
		String predicate = "http://www.w3.org/2002/07/owl#sameAs";
		tClosure(predicate, dir);
		final long totalTime = System.currentTimeMillis() - start;
		final String fileName = "summarizarion_" + dir.getName().split("\\\\")[0] + ".txt";
		Set<String> txtContent = new HashSet<String>();
		txtContent.add("TotalTime: " + totalTime + "ms");
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
						File f = ClosureGenerator.mixFiles(setFiles);
						result = ClosureGenerator.getClosure(f);
						//ClosureGenerator.generateFile(result, output);
						ErrorDetection.analyse(result);
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
}
