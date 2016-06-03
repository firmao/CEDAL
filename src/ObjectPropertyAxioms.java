import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

/**
 * Object Property Axioms using OWL API and HermiT reasoner
 * 
 * @author Saleem
 *
 */
public class ObjectPropertyAxioms {

	public static void main(String[] args) throws OWLOntologyCreationException {

		// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// Let's load an ontology from the web
		// IRI iri =
		// IRI.create("http://swat.cse.lehigh.edu/onto/univ-bench.owl");
		// String ontologyFile = "./ontology/univ-bench-dl.owl";
		String ontologyFile = "./ontology/rdf-schema.owl";
		// OWLOntology onlineOntology = loadOnlineOntology(iri);
		OWLOntology localOntology = loadLocalOntology(ontologyFile);
		// System.out.println( );
		// OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		// Specify the progress monitor via a configuration. We could also
		// specify other setup parameters in the configuration, and different
		// reasoners may accept their own defined parameters this way.
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		// Create a reasoner that will reason over our ontology and its imports
		// closure. Pass in the configuration.
		OWLReasoner reasoner = reasonerFactory.createReasoner(localOntology, config);
		// Ask the reasoner to do all the necessary work now
		reasoner.precomputeInferences();
		// We can determine if the ontology is actually consistent (in this
		// case, it should be).
		// boolean consistent = reasoner.isConsistent();
		// System.out.println("Consistent: " + consistent);
		// System.out.println("\n");
		// OWLDataFactory fac = manager.getOWLDataFactory();

		// Node<OWLObjectPropertyExpression> bottomNode=
		// reasoner.getBottomObjectPropertyNode();
		for (OWLClass cls : localOntology.getClassesInSignature()) {
			System.out.println(cls + " has equivalent classes " + cls.getEquivalentClasses(localOntology));
			// Set<OWLClass> classes = getEquivalentClasses(reasoner,cls);
			// System.out.println(cls+" has equivalent classes :"+classes);

		}
		for (OWLObjectPropertyExpression prop : localOntology.getObjectPropertiesInSignature()) {

			// if(!getInverseObjectProperties(reasoner,prop).isEmpty())
			// System.out.println(prop+" has inverses
			// :"+getInverseObjectProperties(reasoner,prop));
			if (!getEquivalentObjectProperties(reasoner, prop).isEmpty())
				System.out
						.println(prop + " has equivalent properties :" + getEquivalentObjectProperties(reasoner, prop));
			if (!getDisjointObjectProperties(reasoner, prop).isEmpty())
				System.out.println(prop + " has disjoint properties :" + getDisjointObjectProperties(reasoner, prop));
			if (!getSubObjectProperties(reasoner, prop).isEmpty())
				System.out.println(prop + " has sub properties :" + getSubObjectProperties(reasoner, prop));

			// if(!prop.getInverses(localOntology).isEmpty())
			// {
			// System.out.print(prop.toString());
			// System.out.println(" has ontology inverses :" +
			// prop.getInverses(localOntology));
			// }
			// if(!prop.getSubProperties(localOntology).isEmpty())
			// {
			// System.out.print(prop.toString());
			// System.out.println(" has ontology subproperties :" +
			// prop.getSubProperties(localOntology));
			// }
			// if(! prop.getEquivalentProperties(localOntology).isEmpty())
			// {
			// System.out.println(prop.toString());
			// System.out.println("ontology equilant props:" +
			// prop.getEquivalentProperties(localOntology));
			// }
			//
			// if(! prop.getDisjointProperties(localOntology).isEmpty())
			// {
			// System.out.println(prop.toString());
			// System.out.println("has disjoint props:" +
			// prop.getDisjointProperties(localOntology));
			// }
			//
			 if(isSymmetric(prop,localOntology))
			 System.out.println(prop +" is symmetric");
			 if(isReflexive(prop,localOntology))
			 System.out.println(prop +" is Reflexive");

		}
	}

	public static Set<OWLClass> getEquivalentClasses(OWLReasoner reasoner, OWLClass cls) {
		Set<OWLClass> equivalentClasses = new HashSet<OWLClass>();
		if (!reasoner.getEquivalentClasses(cls).isSingleton()) {
			System.out.println(cls + "equivalanet " + reasoner.getEquivalentClasses(cls));
			Iterator<OWLClass> itr = reasoner.getEquivalentClasses(cls).getEntities().iterator();
			while (itr.hasNext()) {
				OWLClass c = itr.next();
				if (!c.isAnonymous() && !cls.equals(c)) {
					// System.out.println(prop+ " is equivalent to " + p);
					equivalentClasses.add(c);
				}
			}
		}
		return equivalentClasses;
	}

	public static Set<OWLObjectPropertyExpression> getSubObjectProperties(OWLReasoner reasoner,
			OWLObjectPropertyExpression prop) {
		Set<OWLObjectPropertyExpression> subProps = new HashSet<OWLObjectPropertyExpression>();
		// if(!reasoner.getSubObjectProperties(prop,
		// true).getFlattened().isEmpty())
		Iterator<OWLObjectPropertyExpression> itr = reasoner.getSubObjectProperties(prop, true).getFlattened()
				.iterator();
		while (itr.hasNext()) {
			OWLObjectPropertyExpression propertyExpression = itr.next();
			OWLObjectProperty p = propertyExpression.getNamedProperty();
			if (!propertyExpression.isAnonymous() && !propertyExpression.isBottomEntity()) {
				// System.out.println(prop+ " is equivalent to " + p);
				subProps.add(p);
			}
		}

		return subProps;
	}

	public static boolean isReflexive(OWLObjectPropertyExpression prop, OWLOntology localOntology) {
		
		if (prop.isReflexive(localOntology))
			return true;
		// System.out.println(prop + " is reflexive" );
		return false;
	}

	public static boolean isSymmetric(OWLObjectPropertyExpression prop, OWLOntology localOntology) {
		if (prop.isSymmetric(localOntology))
			return true;
		// System.out.println(prop + " is symetric" );
		return false;
	}

	public static Set<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLReasoner reasoner,
			OWLObjectPropertyExpression prop) {
		Set<OWLObjectPropertyExpression> disjointProps = new HashSet<OWLObjectPropertyExpression>();
		if (!reasoner.getDisjointObjectProperties(prop).isSingleton()) {
			Iterator<Node<OWLObjectPropertyExpression>> itr = reasoner.getDisjointObjectProperties(prop).iterator();
			while (itr.hasNext()) {
				Node<OWLObjectPropertyExpression> propertyExpression = itr.next();
				Set<OWLObjectPropertyExpression> p = propertyExpression.getEntities();
				// System.out.println(prop+ " is disjoint to " + p);
				disjointProps.addAll(p);
			}
		}
		return disjointProps;
	}

	public static Set<OWLObjectProperty> getEquivalentObjectProperties(OWLReasoner reasoner,
			OWLObjectPropertyExpression prop) {
		Set<OWLObjectProperty> equivalentProps = new HashSet<OWLObjectProperty>();
		if (!reasoner.getEquivalentObjectProperties(prop).isSingleton()) {
			Iterator<OWLObjectPropertyExpression> itr = reasoner.getEquivalentObjectProperties(prop).getEntities()
					.iterator();
			while (itr.hasNext()) {
				OWLObjectPropertyExpression propertyExpression = itr.next();
				OWLObjectProperty p = propertyExpression.getNamedProperty();
				if (!propertyExpression.isAnonymous() && !prop.equals(p)) {
					// System.out.println(prop+ " is equivalent to " + p);
					equivalentProps.add(p);
				}
			}
		}
		return equivalentProps;
	}

	public static Set<OWLObjectProperty> getInverseObjectProperties(OWLReasoner reasoner,
			OWLObjectPropertyExpression prop) {
		Set<OWLObjectProperty> inverses = new HashSet<OWLObjectProperty>();
		if (!reasoner.getInverseObjectProperties(prop).isSingleton()) {
			Iterator<OWLObjectPropertyExpression> itr = reasoner.getInverseObjectProperties(prop).getEntities()
					.iterator();
			while (itr.hasNext()) {
				OWLObjectPropertyExpression propertyExpression = itr.next();
				if (!propertyExpression.isAnonymous() && !prop.equals(propertyExpression.getNamedProperty())) {
					// System.out.println(prop+ " has inverse " +
					// propertyExpression.getNamedProperty());
					inverses.add(propertyExpression.getNamedProperty());
				}
			}
		}
		return inverses;
	}

	/**
	 * Load ontology from a local fine
	 * 
	 * @param ontologyFile
	 *            Location of the local ontology file
	 * @return OWLOntology Owl ontology
	 * @throws OWLOntologyCreationException
	 */
	public static OWLOntology loadLocalOntology(String ontologyFile) throws OWLOntologyCreationException {
		File file = new File(ontologyFile);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology localOntology = manager.loadOntologyFromOntologyDocument(file);
		System.out.println("Loaded ontology: " + localOntology);
		// We can always obtain the location where an ontology was loaded from
		// IRI documentIRI = manager.getOntologyDocumentIRI(localOntology);
		// System.out.println(" from: " + documentIRI);
		// OWLDataFactory factory = manager.getOWLDataFactory();
		// factory.get
		return localOntology;
	}

	/**
	 * Load ontology from Web using online IRI
	 * 
	 * @param ontologyIRI
	 *            IRI of the online ontology
	 * @return OwlOntology Owl ontology
	 * @throws OWLOntologyCreationException
	 */
	public static OWLOntology loadOnlineOntology(IRI ontologyIRI) throws OWLOntologyCreationException {
		// Get hold of an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology Ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
		System.out.println("Loaded ontology: " + Ontology);
		return Ontology;
	}

}
