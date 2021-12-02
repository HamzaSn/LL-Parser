package Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Analyseur Syntaxique prédictive non récursif (Structure de la pile)
 * Analyse descendante.
 * - contient les methodes lexAnalyse() et analyse() qui dependent
 *   de la classe InputReader pour les parametre du grammaire
 *   de la classe AnalysisTable pour les valeur du tableau d'analyse
 */
public class NonRecursiveSyntaxAnalyser {

    public Stack S = new Stack();
    public Analysis AT = new Analysis();
    public InputReader IR = AT.IR;

    public String output = "";
    private String Errors = "";
    private int numberOfErrors = 0;

    public NonRecursiveSyntaxAnalyser() throws IOException {
    }


    /**
     * IR (InputReader) contient toute les informations par rapport
     * a la grammaire de configuration utilisé pour l'analyse.
     * La methode lexAnalyse() verifie si tout symbole de l'input appartient a
     * l'ensemble des terminaux de la grammaire pui affiche dans la console des
     * messages d'erreur.
     * @return false si l'analyse lexicale de l'input a détécté des erreurs léxicale,
     * et true sinon.
     */
    public boolean lexAnalyse() {
        if (IR.terminals.containsAll(IR.elements)) {
            return true;
        } else {
            IR.elements.forEach((elem) -> {
                if (!IR.terminals.contains(elem)) {
                    System.err.println("Unexpected token '" + elem + "'");
                }
            });

            return false;
        }


    }

    /**
     * accepter un symbole de l'entrée.
     * pour préserver les elements de l'input de l'utilisateur tout au long
     * de l'execution IR (inputReader) contient une stucture de données indepandante
     * appelé elementsAnalysis dont on utilisera pour montrer les résultat de l'analyse.
     * accept() enleve le premier element du tableau contenant les symbole de
     * l'entrée et retoure
     * @return
     */
    private boolean accept() {
        return IR.removeElement();
    }

    /**
     * l'analyse de l'input est dépandant des parametres de la configuration
     * l'analyse comporte 3 phase principale :
     * - instatiation de la classe Analysis, calucl de premier et suivants
     * - construction de la table d'analyse syntaxique
     * - detailler le comportement de l'analyseur non récusif
     * L'analyse syntaxique se déclanche que lorceque l'analyse lexicale ne revele pas d'erreurs
     * @return true si le mot et lu sans erreur, false sinon
     * @throws IOException
     */
    public boolean analyse() throws IOException {
        boolean success = false;
        if (lexAnalyse()) { // if Lexical Parsing success
            AT.calculateFirsts();
            AT.calculateFollows();
            AT.constructAnalysisTable();
            S.push("$");
            S.push(IR.axiom);
            output += String.format("pile : %-30s entrée : %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()));
            List<String> tokens = new ArrayList<>(IR.getElements());
            tokens.add("$");
            tokens_loop:
            for (String token : tokens) {
                pile_loop:
                while (!S.getHead().equals("$")) {
                    if (IR.terminals.contains(S.getHead())) { // if terminal
                        if (token.equals(S.getHead())) {
                            accept();
                            S.pop();
                            output += String.format("pile : %-30s entrée : %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()));


                            continue tokens_loop;
                        } else {
                            accept();
                            Errors += "\n" + "Expected " + S.getHead() + " But Got : " + token ;
                            output += String.format("pile : %-30s entrée : %30s      %s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()), "Expected " + S.getHead() + " But Got : " + token);
                            numberOfErrors++;

                            continue tokens_loop;
                        }
                    } else { // if non-terminal

                        if (S.getHead().equals("&")) {
                            S.pop();
                            output += String.format("pile : %-30s entrée : %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()));
                        } else {

                            if (AT.analysisTable.get(S.getHead()).get(token).isEmpty()) {
                                if(IR.getElementsAnalysis().length <= 1 ){
                                    S.pop();
                                    output += String.format("pile : %-30s entrée : %30s %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()), "Entrée vide");
                                    Errors += "Entrée vide \n";
                                    numberOfErrors++;
                                    continue pile_loop;
                                }else {
                                    HashSet<String> x = new HashSet<>(AT.finalFirsts.get(S.getHead()));
                                    x.remove("&");
                                    String expected = "Expected " + x + " But got '" + token + "'" ;
                                    Errors += expected+ "\n";
                                    output += String.format("pile : %-30s entrée : %30s %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()), expected);
                                    numberOfErrors++;
                                    if (accept()) {

                                        continue tokens_loop;

                                    }
                                }

                            } else {

                                String analysisReturn = AT.analysisTable.get(S.getHead()).get(token).get(0);
                                if (analysisReturn.equals("synch")) {
                                    Errors += "Synch ERROR";
                                    numberOfErrors++;
                                    output += String.format("pile : %-30s entrée : %30s %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()), "synch ERROR");
                                    S.pop();
                                    output += String.format("pile : %-30s entrée : %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()));
                                    continue pile_loop;
                                } else if (analysisReturn.equals("")) {
                                    Errors += "null pointer ERROR";
                                    numberOfErrors++;
                                    output += String.format("pile : %-30s entrée : %30s %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()),"null ERROR");
                                    continue tokens_loop;
                                } else {

                                    int productionId = Integer.parseInt(analysisReturn);
                                    String production = AT.prodArray.get(productionId);
                                    List<String> elements = List.of(production.split(" "));
                                    int elementsLength = elements.size();
                                    S.pop();
                                    for (int i = (elementsLength - 1); i >= 0; i--) {
                                        S.push(elements.get(i));
                                    }
                                    output += String.format("pile : %-30s entrée : %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()));

                                }

                            }


                        }
                    }
                }

                if (S.getHead().equals("$") && IR.elementsAnalysis.length != 1) {
                    accept();
                    output += String.format("pile : %-30s entrée : %30s %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()),"Pile vide!");
                    numberOfErrors++;
                    continue tokens_loop;

                } else if (S.getHead().equals("$") && IR.elementsAnalysis.length == 1) {
                    S.pop();
                    accept();
                    output += String.format("pile : %-30s entrée : %30s %n", S.getStack(), Arrays.toString(IR.getElementsAnalysis()));
                }
            }

            if(numberOfErrors==0){
                output += "\n" + "Input parsed with no errors" + "\n" + Errors;
            } else{
                output += "\n" + "Input parsed with " + numberOfErrors + " Errors" + "\n" + Errors;
            }


        } else {
            System.err.println("Parsing error");
            output += "\n" + "parsing Error;";

        }
        if(numberOfErrors==0){
            success = true;
        }

    return success;
    }
}
