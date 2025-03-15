/*
  Created by: Fei Song
  File Name: Main.java
  To Build: 
  After the Scanner.java, tiny.flex, and tiny.cup have been processed, do:
    javac Main.java
  
  To Run: 
    java -classpath /usr/share/java/cup.jar:. Main gcd.tiny

  where gcd.tiny is an test input file for the tiny language.
*/
   
import java.io.*;
import absyn.*;
import java.nio.file.Path;
import java.nio.file.Paths;
   
class Main {
  //public final static boolean SHOW_TREE = true;
  static public void main(String argv[]) {    
    /* Start the parser */

    boolean printAST = false;
    //boolean printTable = false;

    for(String commandArgs : argv)
    {
      if(commandArgs.equals("-a"))
      {
        printAST = true;
      }

      /* 
      else if(commandArgs.equals("-s"))
      {
        printTable = true;
      }
      */
    }
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);

      Path filePath = Paths.get(argv[0]);
      Path nameOfFile = filePath.getFileName();
      String fileName = nameOfFile.toString();
      fileName = fileName.substring(0, fileName.lastIndexOf("."));

      System.out.println("filename: " + fileName);

      if (printAST && result != null) {
        PrintStream fileOutput = new PrintStream(new FileOutputStream("tests/" + fileName + ".abs"));
        System.setOut(fileOutput);
        System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0); 
      }
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
