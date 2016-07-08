
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MyTCPellet {
	
	public static int cTriplesBefore = 0;
	public static int cTriplesAfter = 0;
	public static int totalFiles = 0;
	public static Map<String, String> filesProblem = new HashMap<String, String>();
	
	public static void closure(Property target, File input, File output) {

		Reasoner reasoner = PelletReasonerFactory.theInstance().create();
		OntModel ontModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		InfModel infModel = ModelFactory.createInfModel(reasoner, ontModel);

		RDFDataMgr.read(infModel, input.toURI().toString());

		//System.out.println("Model = "+input+", size = " + ontModel.size());
		cTriplesBefore += ontModel.size();

		infModel.validate();

		StmtIterator it = infModel.listStatements();

		Model modelout = ModelFactory.createDefaultModel();

		while (it.hasNext()) {
			Statement st = it.next();
			if (st.getPredicate().equals(target)) {
				// save only target predicates
				// System.out.println(st);
				modelout.add(st);
			}
		}

		// System.out.println("Inferred model size = " + modelout.size());

		infModel.enterCriticalSection(Lock.READ);

		try {
			//System.out.println("Model = "+input+", size = " + modelout.size());
			cTriplesAfter += modelout.size();
			RDFDataMgr.write(new FileOutputStream(output), modelout, Lang.NT);
			// System.out.println("Model generated at "+output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			infModel.leaveCriticalSection();
		}

	}

	/*
	 * Generates the closure for all files in a directory resulting in a new
	 * directory with the new files.
	 * 
	 * @param fPath represents the path of the dump / triple files.
	 */
	public static void generateAllFilesPellet(File fPath) {
		try {
			if (fPath.isDirectory()) {
				List<File> filesInFolder = Files.walk(Paths.get(fPath.getPath())).filter(Files::isRegularFile)
						.map(Path::toFile).collect(Collectors.toList());

				File[] fdir = filesInFolder.stream().toArray(File[]::new);
				File fOut = new File("closurePellet");
				if (!fOut.exists())
					fOut.mkdirs();

				//IntStream.range(0, fdir.length).parallel().forEach(id -> {
				for (File file : fdir) {
					try {
						totalFiles++;
						//System.out.println("File: " + fdir[id].getName());
						File output = new File("closurePellet\\" + file.getName());
						MyTCPellet.closure(OWL.sameAs, file, output);
					} catch (Exception e) {
						filesProblem.put(file.getName(),e.getMessage());
						e.printStackTrace();
					}
				//});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
