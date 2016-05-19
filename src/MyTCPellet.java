
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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


/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MyTCPellet {
	
	public static void closure(Property target, File input, File output) {
		
		Reasoner reasoner = PelletReasonerFactory.theInstance().create();
		OntModel ontModel = ModelFactory
				.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		InfModel infModel = ModelFactory.createInfModel(reasoner, ontModel);

		RDFDataMgr.read(infModel, input.toURI().toString());

		System.out.println("Model = "+input+", size = " + ontModel.size());

		infModel.validate();

		StmtIterator it = infModel.listStatements();
		
		Model modelout = ModelFactory.createDefaultModel();
		
		while(it.hasNext()) {
			Statement st = it.next();
			if(st.getPredicate().equals(target)) {
				// save only target predicates
				System.out.println(st);
				modelout.add(st);
			}
		}
		
		System.out.println("Inferred model size = " + modelout.size());
		
		infModel.enterCriticalSection(Lock.READ);
		
		try {
			RDFDataMgr.write(new FileOutputStream(output), 
					modelout, Lang.NT);
			System.out.println("Model generated at "+output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			infModel.leaveCriticalSection();
		}
		
	}


}
