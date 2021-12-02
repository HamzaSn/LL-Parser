package Parser;

import java.io.*;
import java.util.*;

public class InputReader {



    protected File configFile;
    protected File inputFile;
    protected List<String> elements = new ArrayList<>();
    protected List<String> terminals = new ArrayList<>();
    protected List<String> nonTerminals = new ArrayList<>();
    protected HashMap<String,List<String>> productions =  new HashMap<>();
    protected String axiom ;
    protected String[] elementsAnalysis;
    private BufferedReader bufferReaderConfig;
    private BufferedReader bufferReaderInput;


    /**
     * Le constructeur de la classe InputReader prend les chemins (path) des deux fichiers text
     * config.txt contenant une grammaire défini (terminaux, non terminaux et régles
     * de production , l'axiom et assumé la premiere regle de production)
     * et et input.txt qui contient le mot a analyser.
     * Ce constructeur lance les procedures liées a la lecture et la structuration des parametres
     * du grammaire.
     * @param configPath
     * @param inputPath
     * @throws IOException
     */
    public InputReader(String configPath, String inputPath) throws IOException {
        configFile = new File(configPath);
        inputFile = new File(inputPath);
        readInput();
        bufferReaderConfig = new BufferedReader(new FileReader(configFile));
        if(readTerminals()){
            if(readNonterminals()){
                if (readProduction()){
                    //readInput();
                    int n = elements.size();
                    elementsAnalysis = new String[n+1];
                    for(int i = 0 ; i < n ; i++){
                        elementsAnalysis[i] = elements.get(i);
                    }
                    elementsAnalysis[n] = "$";
                }
            }
        }
    }


    /**
     * verifie la syntaxe de la définition des terminaux de la grammaire
     * en cas de validation, les non terminaux seront ajouter a leurs structure de données relative
     * @return true si success, false sinon.
     * @throws IOException
     */
    private boolean readTerminals() throws IOException {
        boolean success = false;
        bufferReaderConfig = new BufferedReader(new FileReader(configFile));
        String currentLine = bufferReaderConfig.readLine();
        if(!currentLine.equals("terminals")){
            System.err.println("cannot read terminals");
        } else {
            currentLine = bufferReaderConfig.readLine();
            terminals.addAll(List.of(currentLine.split(" ")));
            success = true;
        }
        return success;
    }


    /**
     * verifie la syntaxe de la définition des non-terminaux de la grammaire
     * en cas de validation, les non terminaux seront ajouter a leurs structure de données relative
     * @return true si success, false sinon.
     * @throws IOException
     */
    public boolean readNonterminals() throws IOException {
        boolean success = false;
        String currentLine = bufferReaderConfig.readLine();
        if (!currentLine.equals("non terminals")) {
            System.err.println("cannot read non terminals");
        } else {
            currentLine = bufferReaderConfig.readLine();
            nonTerminals.addAll(List.of(currentLine.split(" ")));
            success = true;
        }
        return  success;
    }


    /**
     * verifie la syntaxe de la définition des regles de productions de la grammaire
     * en cas de validation, les non terminaux seront ajouter a leurs structure de données relative
     * @return true si success, false sinon.
     * @throws IOException
     */
    public boolean readProduction() throws IOException {
        boolean success = false;
        String currentLine = bufferReaderConfig.readLine();
        if (!currentLine.equals("productions")) {
            System.err.println("cannot read productions");
        } else {
            currentLine = bufferReaderConfig.readLine();
            int k = 0;
            while (!(currentLine == null)) {
                String name = List.of(currentLine.split(" -> ")).get(0);
                String prod = List.of(currentLine.split(" -> ")).get(1);

                if (!nonTerminals.contains(name)) {
                    System.err.println("Non terminal " + name + " is not declared");
                    success = false;
                    break;
                } else {
                    if(k==0) {
                        axiom = name;
                        k++;
                    }
                    productions.putIfAbsent(name, new ArrayList<String>());
                    List<String> elementsSplit = List.of(prod.split(" "));
                    boolean intruder = false;
                    for(String s : elementsSplit){
                        if((terminals.contains(s) || nonTerminals.contains(s)) || s.equals("&")){
                            continue;
                        } else {
                            intruder = true;
                            System.err.println("unexpected token "+ s);
                            success = false;
                            break;
                        }
                    }
                    if(!intruder){
                        productions.get(name).add(prod);
                        success = true;
                        currentLine = bufferReaderConfig.readLine();
                    } else {
                        break;
                    }


                }
            }
        }

        return success;
    }


    /**
     * lecture de l'entrée (input)
     * @return
     * @throws IOException
     */
    public void readInput() throws IOException {
        bufferReaderInput = new BufferedReader(new FileReader(inputFile));
        String input = bufferReaderInput.readLine();
        this.elements = List.of(input.split(" "));
    }


    /**
     * retire le premier element de l'entrée, correspandant au symbole courant au cours
     * de l'analyse.
     * la methode ne fonctionne plus lorceque l'analyse atteind le marqueur de la fin de la chaine "$".
     * @return true si un element a été retiré, false sinon.
     */
    public boolean removeElement(){
        boolean success = false;
        int n = this.elementsAnalysis.length;
        if(!(n < 1)){
            String[] newArr = new String[n-1];
            success = true;
            for (int i = n - 2 ; i >= 0  ; i--){
                newArr[i] = this.elementsAnalysis[i+1];
            }
            this.elementsAnalysis = newArr;
        }

        return success;
    }


    /**
     * retourne les elements du mot entrée (elements) qui ne seront pas affacté au cours de l'analyse
     * @return
     */
    public List<String> getElements(){

        return elements;
    }


    /**
     * retourne les elements du mot entrée (AnalysisElements) qui sera reduit
     * a chaque fois l'analyseur accepte un symbole.
     * @return
     */
    public String[] getElementsAnalysis(){

        return elementsAnalysis;
    }

}
