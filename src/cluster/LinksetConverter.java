package util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LinksetConverter {

	private static Set<String> triples = new HashSet<String>();

	public static void main(String args[]) throws IOException {
		Set<String> rTriples = new HashSet<String>();
		File dir = new File(args[0]);
		Set<File> files = getFiles(dir);
		//files.stream().forEach(file -> {
		for(File file : files){
			try {
				Set<String> domains = getAllDomains(file);
				for (String domain : domains) {
					for (String triple : triples) {
						String uriO = triple.substring(triple.lastIndexOf('<') + 1, triple.lastIndexOf('>'));
						String domainObj = getDomainName(uriO);
						
						if(domain.equals(domainObj)){
							rTriples.add(triple);
						}
					}
					triples.removeAll(rTriples);
					String fileName = file.getName() + "---" + domain + ".nt";
					writeFile(rTriples, fileName);
					rTriples.clear();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void writeFile(Set<String> triples, String fileName) {
		try {
			PrintWriter writer = new PrintWriter("nLustre/" + fileName, "UTF-8");
			triples.stream().forEach(triple -> {
				writer.println(triple);
			});
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static Set<String> getAllDomains(File file) throws IOException, URISyntaxException {
		Set<String> ret = new HashSet<String>();
		String triple = null;
		BufferedReader br = new BufferedReader(new FileReader(file));
		while ((triple = br.readLine()) != null) {
			String uriS = triple.substring(1, triple.indexOf('>'));
			String uriO = triple.substring(triple.lastIndexOf('<') + 1, triple.lastIndexOf('>'));
			String domain = getDomainName(uriO);
			ret.add(domain);
			triples.add(triple);
		}
		br.close();

		return ret;
	}

	private static Set<File> getFiles(File dir) throws IOException {
		Set<File> setFiles = null;
		if (dir.isDirectory()) {
			setFiles = Files.walk(Paths.get(dir.getPath())).filter(Files::isRegularFile).map(Path::toFile)
					.collect(Collectors.toSet());
		}
		return setFiles;
	}

	public static String getDomainName(String url){
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String domain = uri.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}
}
