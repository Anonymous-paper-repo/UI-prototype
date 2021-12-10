# UI-prototype

UI-prototype is a practical tool for computing  uniform interpolants (UI) in SHQ-ontologies. The inputs of UI-prototype are a SHQ-ontologies and a set Σ of concept names and role names. The output is a new ontology and its signatures are retricted in the specified set Σ. It is the first tool that can simultaneously forget concept names and role names in SHQ which is a very expressive descirption language. 

## Environment Requirement

1. jdk 1.8
2. An IDE (IDEA or Eclipse).

## Run configuration

1. Download the entire project and unzip it.
2. Click the ‘import project’ in the IDE and select the unzipped folder.
3. Add the jar file in the dependency directory to the classpath.

Run the main method in /src/swing/FameGUI.java. If the program runs normally and the GUI is started, the configuration is successful.


## Data

The ontologies for the evaluation is available in Data.zip. 

The statistics of these ontology is shown below.
The meaning of the metrics in the table from top to bottom are the average number of axioms, concept names and role names contained in each ontology and the average structure complexity of these ontologies.
| Metrics                | Mean  | Min | 25%  | 50%  | 75%   | Max    |
|------------------------|-------|-----|------|------|-------|--------|
| Axioms                 | 5703  | 44  | 706  | 2450 | 6214  | 70116  |
| Number of Concept name | 3209  | 0   | 382  | 913  | 2890  | 69689  |
| Number of Role name    | 88    | 1   | 11   | 34   | 89    | 1390   |
| Structural Complexity  | 13186 | 0   | 1582 | 5393 | 12220 | 169572 |

Structural Complexity is defined by induction.
SC(C) = 1, C is a concept name;
SC(C1 and C2) = SC(C1) + SC(C2), C1 and C2 is concepts;
SC(C1 or C2) = SC(C1) + SC(C2), C1 and C2 is concepts;
SC(not C1) = SC(C1), C1 is concept;
SC(>= mr.C1) = 1 + SC(C1), C1 is concept;
SC(<= nr.C1) = 1 + SC(C1), C1 is concept；

You can download the data.zip and unzip it so that you can use the data to reproduce our experimental results.

## Compute Uniform Interpolant

There are two ways to compute the UI:
1. Run the main method in /src/swing/FameGUI.java or type 'java -jar UI-prototype.jar' in your terminal to call a GUI out. You could load the target ontology by cliking the "Load Ontology" button, and specify the concept/role names you want to forget, click the "Forget" button you will see the uniform interpolant computed by the system. You could save the result locally as an .owl file by clicking the "Save Ontology" button.
2. Using the UI API.
There is a code template showing how to compute the UI.

```java
package evaluation;

import concepts.AtomicConcept;
import convertion.BackConverter;
import convertion.Converter;
import forgetting.Fame;
import formula.Formula;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import roles.AtomicRole;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public class demo {
    public void main (String[] args) throws OWLOntologyCreationException, CloneNotSupportedException, FileNotFoundException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        /* TODO: Input your target ontology path */
        String filePath = "";

        /* TODO: Enter the save path of the UI. */
        String OutPutPath = "";


        File file = new File(filePath);
        IRI iri = IRI.create(file);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(iri),
                new OWLOntologyLoaderConfiguration().setLoadAnnotationAxioms(true));
        Converter converter = new Converter();
        converter.CReset();
        Fame fame = new Fame();
        List<AtomicRole> roleList = converter.getRolesInSignature_ShortForm(ontology);
        List<AtomicConcept> conceptList = converter.getConceptsInSignature_ShortForm(ontology);

        /* roleList and conceptList contain all signatures that occur in the input ontology.
        TODO：Write your code to select the names to be eliminated from these two lists.
        May be you can implement a select function.
        Set<AtomicConcept> c_sig = select(conceptList);
        Set<AtomicRole> r_sig =  select(roleList);
         */
        Set<AtomicConcept> c_sig = null;
        Set<AtomicRole> r_sig = null;
        OWLOntology UIonto = null;

        BackConverter backConverter = new BackConverter();
        List<Formula> formula_list = converter.OntologyConverter_ShortForm(ontology);
        List<Formula> result_list = fame.FameCR(c_sig, r_sig, formula_list);
        UIonto = backConverter.toOWLOntology(result_list);
        File outFile = new File(OutPutPath);
        OutputStream os = new FileOutputStream(outFile);
        manager.saveOntology(UIonto, new OWLXMLOntologyFormat(), os);

    }
}
```

## Compare with LETHE
In /src/evaluation/mainTest.java, you can find the code for the evaluation. We compare LETHE with our prototype, and forget the same concept names on the same ontology, and count the running time, success rate and other metrics. To reproduce the experimental results in our WWW2021 paper, you need to configure the following code in the main method in mainTest.java.

Set the log file path. The experimental data will be saved in this file path.
```java
String logpath ="";
```

Set the save path of UI that computed by LETHE. The output of LETHE will be saved in this file path.
```java
String LETHEPATH = "";
```
Set the save path of UI that computed by our tool. The output of our tool will be saved in this file path.
```java
String MyPATH = "";
```

Set the path of test data. 
```java
String ontologyPath = "";
```

Set the number of signatures the be eliminate. In our experiment, this parameter was set to 10, 30, 50 in turn to compare the scalability of the two tools.
```java
percent =10; // 30， 50
```
After that, run the main method in mainTest.java to start the experiment.
