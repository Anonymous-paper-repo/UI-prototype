package evaluation;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import evaluation.random_select;
import evaluation.forget_lethe;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;



public class main_test {
	

	
	public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		forget_lethe fl = new forget_lethe();
		String path = "/Users/yue_x/Desktop/NCBO BioPortal/owlxml/cao.clusters-of-orthologous-groups-cog-analysis-ontology.3.owl.xml";
		File file = new File(path);
		fl.evaluation(file);
	}
}
