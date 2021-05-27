package evaluation;
import uk.ac.man.cs.lethe.forgetting.IOWLForgetter;
import uk.ac.man.cs.lethe.forgetting.ShqTBoxForgetter;
import uk.ac.man.cs.lethe.interpolation.ShqTBoxInterpolator;
import evaluation.random_select;



import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Sets;




public class forget_lethe {
	public static int runtime;
	public forget_lethe() {
		
	}
	
	
	public void evaluation(File file) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		random_select rs = new random_select();
		IRI iri = IRI.create(file);
		OWLOntology inputontology = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(iri),
				new OWLOntologyLoaderConfiguration().setLoadAnnotationAxioms(true));
		OWLOntology resultontology;
		Set<OWLClass> names = inputontology.getClassesInSignature();
		Set<OWLEntity> name = new HashSet<>(names);
		runtime = 0;
		int success_num = 0;
		int kk = 0;
		for(int i=1;i<=100;i++) {
			System.out.println(i);
			Callable<Void> task =new Callable<Void>() {
				public Void call() throws Exception{
					forget_one(name,inputontology);
					return null;
				}
			};
			ExecutorService executorService=Executors.newSingleThreadScheduledExecutor();
			Future<Void> future=executorService.submit(task);
			try {
				future.get(3000,TimeUnit.SECONDS);
				success_num+=1;
			} catch(TimeoutException e) {
				kk++;
				continue;
				
			}
		}
		System.out.println("Average runtime is :"+runtime/success_num+" ms");
		System.out.println("timeout rate is: "+kk+"/100");
		System.out.println("Success rate is: "+success_num+"/100");
	}
	
	public static OWLOntology forget_one(Set<OWLEntity> c_names,OWLOntology ontology) {
		
		random_select rs = new random_select();
		int c_percent = 10;
		Set<OWLEntity> names = rs.getrandomSet(c_names,c_percent);
		ShqTBoxForgetter forgetter = new ShqTBoxForgetter();
		long start_time = System.currentTimeMillis();
		OWLOntology resultontology = forgetter.forget(ontology, names);
		long end_time = System.currentTimeMillis();
		runtime = (int) (runtime+end_time-start_time);
		

		return resultontology;
	}

}
