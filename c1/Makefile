JAVA=java
JAVAC=javac
JFLEX=jflex
#JFLEX=~/Projects/jflex/bin/jflex
#CLASSPATH=-cp ~/Projects/java-cup-11b.jar:.

#CLASSPATH=-cp /root/CIS4650/CIS-4650-Project/c1/java-cup-11b.jar:.

CLASSPATH=-cp /usr/share/java/cup.jar:. #For the school server and use JFLEX=jflex
CUP=$(JAVA) $(CLASSPATH) java_cup.Main


all: Main.class

Main.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	#$(CUP) -dump -expect 3 cm.cup
	$(CUP) -expect 3 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~

