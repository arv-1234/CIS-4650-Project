Names: Justin Palmer (1102541), Jessica Nguyen (1169812), Arvind Palakkal (1141879)

Acknowledgment: I used C1-Package's SampleScanner code and modified it for my implementation.

**Intro and compilation**
The c2 directory contains the symbol table for checkpoint 2. To run the program you must first open the terminal in the directory c2, after doing so you must then type make into the terminal, this should make all the relevant files needed for the semantic analyzer to run( if any errors arise it most likely has something to do with the makefiles classpath value, it should be set to the correct path but if it is not please set it to the correct one to run). After successfully making you can run the parser with any .cm file by doing the following:

**TO RUN**
- In the makefile, make sure the proper class path is uncommented, this will vary from person to person but make sure we leave it at the one that works on nomachine/socs-server.

-  cd into the <c2 folder> and <enter make> into the terminal.

-  Type in the command, java -cp /usr/share/java/cup.jar:. Main tests/filename.cm [-a,-s]: 

    - java -cp /usr/share/java/cup.jar:. Main <tests/filename.cm -a> (This will print the abstract syntax tree to a file with the extension .abs)

    - java -cp /usr/share/java/cup.jar:. Main <tests/filename.cm> -s (This will print the symbol table to a file with the extension .sym)


- To run the code on the school server type in this command: java -cp /usr/share/java/cup.jar:. Main <Test file name> [-a,-s]

- To run the scanner against any of the test files on the school server: java -cp /usr/share/java/cup.jar:. Scanner  < <tests/filename.cm>


**Files**
- Within the absyn folder contains all the java files that define the objects of the abstract syntax tree.

- cm.cup defines the rules for the parser to follow along with the definition of how the error recovery is handled 
  (Essentially everything needed to define the parser)

- cm.flex defines our scanner

- The test files should also be located within the tests folder in the c2 directory. There are 5 test files. [1.cm - 5.cm]

- ShowTreeVisitor holds the definition for the visitor function and is mostly in charge as to what is printed out in the abstract syntax tree

- SemanticAnalyzer.java holds the definition for our symbol table, type checking, and visit functions (For the symbol table)

**Limitations**


**Test Plan**
- To test the program we created 5 test file 1.cm, 2.cm, 3.cm, 4.cm, 5.cm

- The types of errors that will be present in each file will be mentioned in the file in a comment


**(ALSO NOTE WE HAD TO MANUALLY PLACE java-cup-11b.jar into the project to run our code locally)**