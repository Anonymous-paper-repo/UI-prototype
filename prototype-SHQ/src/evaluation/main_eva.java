package evaluation;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;



//file1 = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/gene-cds.genomic-clinical-decision-support-ontology.1.owl.xml"
//file2 = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/cabro.computer-assisted-brain-injury-rehabilitation-ontology.1.owl.xml"
//file3 = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/emo.enzyme-mechanism-ontology.1.owl.xml"
//file4 = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/mhc.major-histocompatibility-complex-ontology.4.owl.xml"
//file5 = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/peao.plant-experimental-assay-ontology.1.owl.xml"
//file6 = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/roo.radiation-oncology-ontology.3.owl.xml"
//file7 = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/typon.microbial-typing-ontology.11.owl.xml"



public class main_eva {
	public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		forget_mine rt = new forget_mine();
		String path = "/Users/yue_x/Desktop/www/NCBO BioPortal/owlxml/typon.microbial-typing-ontology.11.owl.xml";  // Path of test data(an ontology).
		File file = new File(path);
		rt.evaluation(file);
	}

	

}
