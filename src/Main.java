import Parser.NonRecursiveSyntaxAnalyser;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.err.println("Compilateur : ");
        NonRecursiveSyntaxAnalyser NRSA = new NonRecursiveSyntaxAnalyser();
        try {
            NRSA.analyse();
            NRSA.AT.showAnalysis();
        } catch (Exception e){
            System.err.println("Java : ");
            e.printStackTrace();
        }


    }
}
