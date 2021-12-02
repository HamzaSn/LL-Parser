import Parser.NonRecursiveSyntaxAnalyser;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.err.println("Compilateur : ");
        NonRecursiveSyntaxAnalyser NRSA = new NonRecursiveSyntaxAnalyser();
        try {
            NRSA.analyse();
            // Au cas ou vous souhaitez afficher l'analyse en console sans creation de fichier
            // commenter l'appel a la fonction writeAnalysis() et décommente la ligne suivante :
            // NRSA.AT.showAnalysis();
             NRSA.AT.writeAnalysis("./output/output.txt");
        } catch (Exception e){
            System.err.println("Java : ");
            e.printStackTrace();
        }


    }
}
