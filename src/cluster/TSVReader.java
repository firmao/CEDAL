package cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TSVReader {
	
	public static Set<String> getLines(BufferedReader r) throws IOException{
		String line;
		Set<String> file = new HashSet<String>();
		// load each line and append it to file.
		while ((line=r.readLine())!=null){
		    file.add(line);
		}
		return file;
	}
	
	public static void main(String args[]) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(new File("files/erroReport_all_LinkLion.csv")));
		Map<String, Integer> fileNames = new HashMap<String, Integer>();
		Map<String, Integer> domains = new HashMap<String, Integer>();
		Set<String> paths = new HashSet<String>();
		Set<String> lines = getLines(br);
		
		//lines.parallelStream().forEach(elem ->
		lines.forEach(elem ->
		{
			try {
				String [] elems = elem.split("\\t");
				String fileName = elems[0];
				String domain = elems[1];
				String path = elems[2];
				if (fileNames.containsKey(fileName)){
					int size = fileNames.get(fileName);
					fileNames.put(fileName, (size + 1));
				}else{
					fileNames.put(fileName, 1);
				}
				if (domains.containsKey(domain)){
					int size2 = domains.get(domain);
					domains.put(domain, (size2 + 1));
				}else{
					domains.put(domain,1);
				}
				paths.add(path);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		System.out.println("Files: " + fileNames.keySet().size());
		System.out.println("Domains: " + domains.keySet().size());
		System.out.println("Paths: " + paths.size());
		createFile(fileNames, "File_error.csv", "File");
		createFile(domains, "Domain_error.csv", "Domain");
		//readTSVfile("files/erroReport_all_LinkLion.csv");
	}

	private static void createFile(Map<String, Integer> map, String fileName, String field) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.println(field + "\t" + "Error");
		for(String key : map.keySet()){
			writer.println(key + "\t" + map.get(key));
		}
		writer.close();
	}
	
	public static void analisysTop(String fNameErrorReport) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(new File(fNameErrorReport)));
		Map<String, Integer> fileNames = new HashMap<String, Integer>();
		Map<String, Integer> domains = new HashMap<String, Integer>();
		Set<String> paths = new HashSet<String>();
		Set<String> lines = getLines(br);
		
		//lines.parallelStream().forEach(elem ->
		lines.forEach(elem ->
		{
			try {
				String [] elems = elem.split("\\t");
				String fileName = elems[0];
				String domain = elems[1];
				String path = elems[2];
				if (fileNames.containsKey(fileName)){
					int size = fileNames.get(fileName);
					fileNames.put(fileName, (size + 1));
				}else{
					fileNames.put(fileName, 1);
				}
				if (domains.containsKey(domain)){
					int size2 = domains.get(domain);
					domains.put(domain, (size2 + 1));
				}else{
					domains.put(domain,1);
				}
				paths.add(path);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		System.out.println("Files: " + fileNames.keySet().size());
		System.out.println("Domains: " + domains.keySet().size());
		System.out.println("Paths: " + paths.size());
		createFile(fileNames, fNameErrorReport + "_File_error.csv", "File");
		createFile(domains, fNameErrorReport + "_Domain_error.csv", "Domain");
		//readTSVfile("files/erroReport_all_LinkLion.csv");
	}
}
