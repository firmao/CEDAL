package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class CleanLinkSets {

	public static void main(String[] args) {
		String fName = "erroReport_nEduc_parallel.csv";
		String sDirLinkset = "nEduc";
		File fErrors = new File(fName);
		File dirLinkset = new File(sDirLinkset);
		String newLinksetDir = "newLinksetDir";
		cleanLinksets(fErrors, dirLinkset, newLinksetDir);
	}

	private static void cleanLinksets(File fErrors, File dirLinkset, String newLinksetDir) {
		File fNewLinksetDir = new File(newLinksetDir);
		fNewLinksetDir.mkdir();
		Map<String, Set<String>> mErrors = readFile(fErrors);
		mErrors.forEach((fName, setURIErrors) -> {
			try {
				Set<String> triples = deleteTriples(fName, setURIErrors, dirLinkset);
				generateFile(triples, fName, fNewLinksetDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	private static void generateFile(Set<String> triples, String fName, File fNewLinksetDir)
			throws FileNotFoundException, UnsupportedEncodingException {
		System.out.println("Generanting file: " + fName);
		PrintWriter writer = new PrintWriter(fNewLinksetDir.getAbsolutePath() + "/" + fName, "UTF-8");

		for (String triple : triples) {
			writer.println(triple);
		}

		writer.close();
		System.out.println("File generated with success: " + fName);
	}

	private static Set<String> deleteTriples(String fName, Set<String> setURIErrors, File dirLinksets) throws IOException {
		Set<String> ret = new HashSet<String>();
		List<String> lstLines = FileUtils.readLines(new File(dirLinksets.getAbsolutePath() + "/" + fName), "UTF-8");
		for (String triple : lstLines) {
			for (String uri : setURIErrors) {
				if (triple.contains(uri)) {
					System.out.println("URI: " + uri);
					ret.remove(triple);
					break;
				}else{
					ret.add(triple);
					//break;
				}
			}
		}
		return ret;
	}

	private static Map<String, Set<String>> readFile(File fErrors) {
		long start = System.currentTimeMillis();
		System.out.println("Loading file with erreous paths...");
		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();
		try {
			List<String> lstLines = FileUtils.readLines(fErrors, "UTF-8");
			for (String line : lstLines) {
				try {
					if (!line.contains(","))
						continue;
					String s[] = line.split("\t");
					String fName = s[0];
					String errorPath[] = s[2].split(",");
					Set<String> sErrorPath = new HashSet<String>();
					for (String uri : errorPath) {
						uri = uri.replaceAll("[\\[\\]]", "");
						sErrorPath.add(uri);
					}
					if (ret.containsKey(fName)) {
						ret.get(fName).addAll(sErrorPath);
					} else {
						ret.put(fName, sErrorPath);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long totalTime = System.currentTimeMillis() - start;
		System.out.println("TotalTime load File Errors: " + totalTime);
		return ret;
	}

}
