package evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;


import concepts.AtomicConcept;
import convertion.Converter;
import inference.DefinerIntroducer;
import roles.AtomicRole;

import forgetting.Fame;
import forgetting.Forgetter;
import formula.Formula;

public class forget_mine {
	
	public static int runtime=0;

	
	public forget_mine() {
		
	}
	
	public void evaluation(File file) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();

		IRI iri = IRI.create(file);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(iri),
				new OWLOntologyLoaderConfiguration().setLoadAnnotationAxioms(true));
		Converter ct=new Converter();

		Fame fame=new Fame();
		random_select rs=new random_select();
		
		
		
		
		List<AtomicRole> roleList = ct.getRolesInSignature_ShortForm(ontology);
		List<AtomicConcept> conceptList = ct.getConceptsInSignature_ShortForm(ontology);
		int Axioms_num=ontology.getLogicalAxiomCount();
		int Concept_num=conceptList.size();
		int Role_num=roleList.size();
		int success_num=0;
		runtime=0;
		int kk=0;
		for (int i=1;i<=100;i++) {
			System.out.println(i);
			Forgetter.flag=false;
			DefinerIntroducer.definer_map.clear();
			DefinerIntroducer.definer_set.clear();
			DefinerIntroducer.definer_flag_map.clear();
			AtomicConcept.setDefiner_index(1);

			
			Callable<Void> task =new Callable<Void>() {
				public Void call() throws Exception{
					forget_one(conceptList,roleList,ontology);
					return null;
				}
			};
			ExecutorService executorService=Executors.newSingleThreadScheduledExecutor();
			Future<Void> future=executorService.submit(task);
			try {
				future.get(3000,TimeUnit.SECONDS);
				success_num++;
			} catch(TimeoutException e) {
				kk++;
				
			}

			
		}

		System.out.println("Average runtime is :"+runtime/success_num+" ms");
		System.out.println("timeout rate is: "+kk+"/100");
		System.out.println("Success rate is: "+success_num+"/100");
	}
	
	public static void forget_one(List<AtomicConcept> conceptList,List<AtomicRole> roleList,OWLOntology ontology) throws OWLOntologyCreationException, CloneNotSupportedException {

		random_select rs=new random_select();
		Converter ct=new Converter();
		Fame fame=new Fame();
		int c_percent = 10;   // Set the value of this variable to determine the ratio of concept names to be forgotten.
		int r_percent = 0;    // Set the value of this variable to determine the ratio of role names to be forgotten.
		Set<AtomicConcept> c_sig=rs.getrandomList(conceptList, c_percent);   
		/*
		Set<AtomicConcept> c_sig=rs.getrandomList(conceptList, c_percent);   
		Set<AtomicConcept>  c_sig = new HashSet<>();
		String s1 = "CAO_0000017,CAO_0000314,CAO_0000024,CAO_0000178,TemporalInterval,CAO_0000158,CAO_0000168,CAO_0000053,CAO_0000154,CAO_0000055,CAO_0000164,CAO_0000170,CHEBI_23367,CAO_0000172,CAO_0000061,ScatteredTemporalRegion,NCBITaxon_1224,IAO_0000027,CAO_0000306";
		for(String sub_s : s1.split(",")) {
			c_sig.add(new AtomicConcept(sub_s));
		}
		*/
		Set<AtomicRole> r_sig=rs.getrandomList(roleList, r_percent);
		long startTime;
		long endTime;
		
		try {
			startTime = System.currentTimeMillis();
			List<Formula> formula_list = ct.OntologyConverter_ShortForm(ontology);
			List<Formula> result_list = fame.FameCR(c_sig, r_sig, formula_list);
			endTime = System.currentTimeMillis();
			runtime=(int) (runtime+endTime-startTime);


			//System.out.println();		
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
}
