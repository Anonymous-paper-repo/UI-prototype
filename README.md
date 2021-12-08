# UI-prototype

## Usage of the Prototype:

To get the prototype work, first you need to make sure you have Java Runtime Environment installed on your machine, and we suggest you using a Java IDE such as Eclipse to run the code.

Download the source code, together with other files, and import it as a Java project into your IDE. 

To run the uniform interpolation method, go to the directory "Swing" and run the main method in FameGUI.java to call a GUI out, where you could load the target ontology by cliking the "Load Ontology" button, and specify the concept/role names you want to forget, click the "Forget" button you will see the uniform interpolant computed by the system. You could save the result locally as an .owl file by clicking the "Save Ontology" button. You can also run this prototype through the executable file 'UI-prototype.jar'.

## Evaluation

The test ontologies is available in Data.zip. 

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
SC(C1 and C2) = SCom(C1) + SCom(C2), C1 and C2 is concepts;
SC(C1 or C2) = SCom(C1) + SCom(C2), C1 and C2 is concepts;
SC(not C1) = SCom(C1), C1 is concept;
SC(>= mr.C1) = 1 + SCom(C1), C1 is concept;
SC(<= nr.C1) = 1 + SCom(C1), C1 is concept；
