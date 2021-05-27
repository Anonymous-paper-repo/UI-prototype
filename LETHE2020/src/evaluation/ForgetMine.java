package  evaluation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.man.cs.lethe.forgetting.IOWLForgetter;
import uk.ac.man.cs.lethe.forgetting.ShqTBoxForgetter;


import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;


public class ForgetMine {
    public static String filename = "";
    public static int RoleSize = 0;
    public static int ConceptSize = 0;
    public static int LogicalAxiomSize = 0;
    public static int FConceptSize = 0;
    public static int FRoleSize = 0;
    public static int FLogicAxiomSize = 0;
    public static int isExtra = 0;
    public static int runtime = 0;
    public static double SuccessRate = 0;
    public static long memory = 0;
    public static boolean flag = false;



    public ForgetMine() {

    }

    public static void Evaluation(File file, String logpath) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
        OWLOntologyManager manager= OWLManager.createOWLOntologyManager();
        IRI iri = IRI.create(file);
        random_select rs = new random_select();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(iri),
                new OWLOntologyLoaderConfiguration().setLoadAnnotationAxioms(true));
        Set<OWLClass> names = ontology.getClassesInSignature();
        Set<OWLEntity> name = new HashSet<>(names);
        LogicalAxiomSize=ontology.getLogicalAxiomCount();
        ConceptSize=names.size();
        RoleSize=ontology.getObjectPropertiesInSignature().size();
        int success_num=0;
        runtime = 0;
        memory = 0;
        isExtra = 0 ;
        int kk=0;
        for (int i=1;i<=100;i++) {
            System.gc();
            System.out.println(i);
            Runtime r = Runtime.getRuntime();
            long mem1 = r.freeMemory();
            flag= false;
            Callable<Void> task =new Callable<Void>() {
                public Void call() throws Exception{
                    forget_one(name,ontology);
                    return null;
                }
            };
            ExecutorService executorService= Executors.newSingleThreadScheduledExecutor();
            Future<Void> future=executorService.submit(task);
            try {
                future.get(100,TimeUnit.SECONDS);
                if(!flag) {
                	success_num +=1;
                } else {
                	isExtra +=1;
                }
            } catch(TimeoutException e) {
                kk++;
                isExtra +=1;
                continue;

            }
            long mem2 = r.freeMemory();
            memory += mem1- mem2;


        }
        memory = memory/10;
        SuccessRate = success_num;
        runtime = runtime/10;
        String log = filename+","+LogicalAxiomSize+","+ConceptSize+","+RoleSize+","+runtime+","+SuccessRate+","+isExtra+","+FLogicAxiomSize+","+FConceptSize+","+FRoleSize+"\n";
        WriteFile.writeFile(logpath,log);

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
        FConceptSize = resultontology.getClassesInSignature().size();
        FRoleSize = resultontology.getObjectPropertiesInSignature().size();
        FLogicAxiomSize = resultontology.getLogicalAxiomCount();
        Set<OWLClass> conceptn = resultontology.getClassesInSignature();
        for (OWLClass concept : conceptn) {
        	if (concept.getIRI().getShortForm().startsWith("_D")){
        		System.out.println(concept.getIRI().getShortForm());
        		flag = true;
        		break;
        	}
        }
        


        return resultontology;
    }

    public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
        String logpath = "C:\\Users\\Yue_X\\Desktop\\LETHELog.txt";
        String features = "OntologyName, AxiomSize, ConceptSize, RoleSize, Runtime, SuccessRate, isExtra, FAxiomSize, FConceptSize, FRoleSize\n";
        WriteFile.writeFile(logpath,features);
        String ontologyPath = "C:\\Users\\Yue_X\\Desktop\\SQ_ontology";
        File ontopath = new File(ontologyPath);
        File[] files = ontopath.listFiles();
        int i = 1;
        for (File file :files){
            System.out.println(i++);
            filename = file.getName();
            System.out.println(filename);
            if (!file.isDirectory() && filename.endsWith("xml")){
                Evaluation(file,logpath);
                file.delete();
            }
        }


    }

}
