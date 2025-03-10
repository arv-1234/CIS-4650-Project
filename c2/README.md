Names: Justin Palmer (1102541), Jessica Nguyen (1169812), Arvind Palakkal (1141879)

Acknowledgment: I used C1-Package's SampleScanner code and modified it for my implementation.

**Intro and compilation**
In this package the parser for checkpoint 1 is defined. To run the program you must first open the terminal in the directory c1, after doing so you must then type make into the terminal, this should make all the relevant files needed for the parser to run( if any errors arise it most likely has something to do with the makefiles classpath value, it should be set to the correct path but if it is not please set it to the correct one to run). After successfully making you can run the parser with any .cm file by doing the following:

**TO RUN**
- In the makefile, make sure the proper class path is uncommented, this will vary from person to person but make sure we leave it at the one that works on nomachine/socs-server.

-  Type make into the terminal when it's in the respective folder.

-  Type in the command, java -cp /Path/to/java-cup-11b.jar:. Main <Test file name> -a (the -a command will display the syntax tree)

    - If you run the command without the the -a errors messages about errors in the file will be displayed only, not the tree


- To run the code on the school server type in this command: java -cp /usr/share/java/cup.jar:. Main <Test file name> -a

- To run the scanner against any of the test files on the school server: java -cp /usr/share/java/cup.jar:. Scanner  < <Test file name>


**Files**
- Within the absyn folder contains all the java files that define the objects of the abstract syntax tree.

- cm.cup defines the rules for the parser to follow along with the definition of how the error recovery is handled 
  (Essentially everything needed to define the parser)

- cm.flex defines our scanner

- The test files should also be located within the same c1 directory as cm.cup, there are 5 simple test files labled num.cm where num is a number from   1-5

- ShowTreeVisitor holds the definition for the visitor function and is mostly in charge as to what is printed out in the abstract syntax tree

**Limitations**
- Our error recovery does not work very well. That is why our test files are limited. We have to improve on it before moving to c2

- Some errors cause our program from working entriely 

**Test Plan**
- To test the program we created 5 test file 1.cm, 2.cm, 3.cm, 4.cm, 5.cm

- We also used the programs provided by the c1 package

- The types of errors that will be present in each file will be mentioned in the file in a comment

- However as mentioned before our error recovery does not really work properly so the amount of errors it can catch is limited


**(ALSO NOTE WE HAD TO MANUALLY PLACE java-cup-11b.jar into the project to run our code locally)**

java -cp /usr/share/java/cup.jar:. Main <Test file name>