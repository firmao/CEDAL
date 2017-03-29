import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.crypto.Data;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class LinkLion2_p1 {

	static Map<String, String> mEndPointError = new HashMap<String, String>();
	public static void main(String[] args) throws IOException {
		/*
		 * For each triple in dump file or Endpoint: 
		 * 		If object typeof datatype:
		 * 			Count +1 for subject URI
		 */
		Map<String, Integer> mSubjCount = new HashMap<String, Integer>();
		Set<String> setEndPoints = getEndPoints();
		setEndPoints.forEach(endPoint -> {
			Set<String> setTriples = getTriples(endPoint);
			setTriples.forEach(triple -> {
				String[] sTriple = triple.split("\t", -1);
				String subj = sTriple[0];
				String pred = sTriple[1];
				String obj = sTriple[2];
				//boolean bIsDataType = isDataType(obj); // Jena.
				boolean bIsDataType = isDataTypeLiteral(obj); // Andre
				if (bIsDataType) {
					if(mSubjCount.containsKey(subj)){			
						Integer value = mSubjCount.get(subj);
						mSubjCount.put(subj, value+1);
					}else{
						mSubjCount.put(subj, 1);
					}
					addDB(mSubjCount);
				}
			});
		});

	}

	private static Set<String> getTriples(String endPoint) {
		Set<String> setReturn = new HashSet<String>();
		String sparqlQueryString = "SELECT DISTINCT * WHERE { ?s ?p ?o } limit 10";

		Query query = QueryFactory.create(sparqlQueryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, query);

		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				String subj = soln.get("?s").toString();
				String pred = soln.get("?p").toString();
				String obj = soln.get("?o").toString();
				String line = subj + "\t" + pred + "\t" + obj;
				setReturn.add(line);
				//result.add(soln.get("?strLabel").toString());
				// System.out.println(soln.get("?strLabel"));
			}
		} catch (Exception e) {
			mEndPointError.put(endPoint, e.getMessage());
			//e.printStackTrace();
		}

		finally {
			qexec.close();
		}
		
		return setReturn;
	}

	private static Set<String> getEndPoints() throws IOException {
		return Files.lines(Paths.get("GoodEndPoints.txt")).collect(Collectors.toSet());
	}

	private static void addDB(Map<String, Integer> mSubjCount) {
		mSubjCount.forEach((uriSubj,count) -> {
			String dataset = getDataset(uriSubj);
			DBUtil.insert(dataset, uriSubj, count);
		});
	}

	private static String getDataset(String uriSubj) {
		return uriSubj.split("/")[2];
	}

	// if is not an URI then is a DataType.
	public static boolean isDataType(String uri) {

		OntDocumentManager mgr = new OntDocumentManager();
		mgr.setProcessImports(false);
		OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_MEM);
		s.setDocumentManager(mgr);
		OntModel ontModel = ModelFactory.createOntologyModel(s);
		// from http://www.programcreek.com/java-api-examples/index.php?source_dir=szeke-master/src/main/java/edu/isi/karma/modeling/ontology/OntologyHandler.java

		DatatypeProperty dp = ontModel.getDatatypeProperty(uri);
		if (dp != null)
			return true;

		return false;
	}
	
	/*
	 *  if is not an URI then is a DataType.
	 */
	public static boolean isDataTypeLiteral(String uri){
		return !uri.startsWith("http");
	}

}
