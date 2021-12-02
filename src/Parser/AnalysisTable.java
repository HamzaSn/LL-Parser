package Parser;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cette classe d'objet est responsable du calcul des premiers et suivants des non terminaux d'un grammaire, de la construction du tableau d'analyse
 * et de l'affichage et l'ecriture du rapport de l'analyse.
 */

public class AnalysisTable {


    public InputReader IR = new InputReader("./input/config.txt", "./input/input.txt");
    private final List<String> terminals;
    private final List<String> nonTerminals;
    private final HashMap<String, List<String>> productions;
    private final String axiom;
    public HashMap<String, List<String>> firsts = new HashMap<>();
    protected HashMap<String, HashSet<String>> finalFirsts = new HashMap<>();
    protected HashMap<String, List<String>> follows = new HashMap<>();
    protected HashMap<String, HashSet<String>> finalFollows = new HashMap<>();
    protected HashMap<String, HashMap<String, List<String>>> analysisTable;
    protected HashMap<String, HashMap<String, Integer>> prodTable = new HashMap<>();
    protected HashMap<Integer, String> prodArray = new HashMap<>();
    protected StringBuilder analysisTablePrinting = new StringBuilder();
    private int prodNum = 1;


    /**
     * Constructeur de la classe charge les valeur de la configuration et construit les structure
     * de données de base pour le fonctionnement des autre fonction. (prodTable et prodArray)
     * @throws IOException
     */
    public AnalysisTable() throws IOException {

        terminals = IR.terminals;
        nonTerminals = IR.nonTerminals;
        productions = IR.productions;
        axiom = IR.axiom;

        AtomicInteger count = new AtomicInteger(0);
        productions.forEach((k, v) -> {
            prodTable.putIfAbsent(k, new HashMap<>());
            v.forEach((elem) -> {
                prodTable.get(k).put(elem, count.incrementAndGet());
                prodArray.put(count.get(), elem);

            });
        });


    }


    /**
     * Calculer le premier d'un symbole (1er argument) et affecter le resultat a l'ensemble
     * des premiers du non terminal (2emme argument).
     * La fonction implemente les regles generale du calcul de l'ensemble des premiers
     * exemple : A -> a B | &     B -> A C       C -> c
     * premier(a) = a
     * premier(B) = permier(A)\{&} ∪ premier(C)
     *
     * NB : la fonction retourne vrai dans le cas ou l'ensemble des premiers du symbole
     * passé en parametre (token) contient le character marqueur de transition epsilon "&"
     * @param token
     * @param nonTerminal
     * @return boolean
     */
    public boolean calculateFirst(String token, String nonTerminal) {
        boolean epsilon = false;
        if (token.equals("&")) { // si l'element est epsilon 
            epsilon = true; // mettre a jour la valeur epsilon
        } else { // si ce n'est pas epsilon

            // premier(A) = premier(B) ∪ premier(A) est equivalent a premier(A) = premier(B).
            // On fera le calcul que pour les cas ou le symbole qu'on souhaite calculer l'ensemble des premier est different 
            // du non terminal dont les resultat seront affecté.
            if (!token.equals(nonTerminal)) { // si token != nonTerminal
                firsts.putIfAbsent(nonTerminal, new ArrayList<>());
                if (terminals.contains(token)) { // si token est un terminal : premier(a) = a
                    firsts.get(nonTerminal).add(token); // ajouter token au premiers du nonTerminal du parametre
                } else { // si c'est un non terminal
                    List<String> prod = productions.get(token); // on cherche les regles de production de cet non terminal
                    production_loop :
                    for (String p : prod) { // pour chaque production
                        if (p.equals("&")) { // si production est epsilon
                            epsilon = true; 
                        } else { // si la production n'est pas egal a epsilon
                            String[] split = p.split(" "); 
                            int lastIndex = split.length - 1;
                            element_loop :
                            for (String s : split) { // pour chaque element de la production
                                if (s.equals(split[lastIndex])) { // si il s'agit du dernier element de la production

                                // Note : calculateFirst retourne vrai si epsilon appartient a l'ensenmble des premien d'un element
                                // mais aussi affecte le resultat a l'ensemble des premiers du non terminal

                                    if (calculateFirst(s, nonTerminal)) { // si cet dernier element contient epsilon 
                                        firsts.get(nonTerminal).add("&"); // epsilon rentre dans l'ensemble des premiers du symbole courant
                                    } else { // si dernier element ne contient pas epsilon (les premiers ont été deja affecté )
                                        break element_loop; // on arrete la boucle pour les elements
                                        // puisque c'est le dernier element break n'est pas necessaire mais c'est pour la comprehension
                                    }
                                } else { // si ce n'est pas le dernier element
                                    // on calcule les premiers de cet element, on les affecte a l'ensemble des premiers du non terminal
                                    if (!calculateFirst(s, nonTerminal)) { // si cet element ne contient pas epsilon
                                        break element_loop; // on arrete la boucle des element et on passe a la production suivante si existe
                                }
                            }

                            }

                        }

                    }

                }
            }


        }
        return epsilon;
    }



    /**
     * calcul des premiers de tout les non terminaux.
     * @return HashMap<String, HashSet<String>>
     */
    public HashMap<String, HashSet<String>> calculateFirsts() {
        nonTerminals_loop:
        for (String nonTerminal : nonTerminals) { // pour chaque non terminal
            firsts.putIfAbsent(nonTerminal, new ArrayList<>());
            List<String> currentProd = productions.get(nonTerminal);
            production_loop:
            for (String production : currentProd) { // pour chaque production
                String[] split = production.split(" ");
                int lastIndex = split.length - 1;
                elements_loop:
                for (String s : split) { // pour chaque elements dans la production
                    if (s.equals("&")) { // si symbole = epsilon
                        firsts.get(nonTerminal).add("&"); // epsilon rentre dans l'ensemble de premier
                        continue production_loop; // passer a la production suivante
                    } else { // si symbole != epsilon

                        
                        if (!s.equals(nonTerminal)) { // si symbole est un terminal
                            if (s.equals(split[lastIndex])) { // s'il s'agit du dernier element de la production en cours
                               
                                if (calculateFirst(s, nonTerminal)) { // si ce dernier element contient  epsilon
                                    firsts.get(nonTerminal).add("&"); // epsilon rentre dans l'ensemble des premier du non terminal
                                } 
                            } else { // si l'element n'est pas le dernier dans la production
                                // si les premiers de cet element ne contient pas epsilon
                                if (!calculateFirst(s, nonTerminal)) { // on calcule les premiers de cet element sans inculre epsilon dans l'ensemble
                                    continue production_loop; // on passe a la production suivante
                                }
                            }
                        }
                    }
                }
            }
        }

        // finalFirsts n'aura que les valuers unique pour chaque non terminal
        firsts.forEach(((k, v) -> {
            finalFirsts.put(k, new HashSet<>(v));
        }));


        return finalFirsts;
    }


    /**
     * calculer les suivants d'un non terminal
     * @param nonTerminal
     */
    public void calculateFollow(String nonTerminal) {
        if (firsts.isEmpty()) { // si le calcul des premier n'a pas été encore réalisé
            calculateFirsts(); // on calcule l'ensemble des premiers de tout les non terminaux
        }
        follows.putIfAbsent(nonTerminal, new ArrayList<>());
        productions.forEach((name, production) -> { // pour chaque ensembe de regle production 
            production_loop:
            for (String prod : production) { // pour chaque production
                String[] split = prod.split(" ");
                split_loop:
                for (int i = 0; i < split.length; i++) { // pour chaque element de la regle de production
                    if (split[i].equals(nonTerminal)) { // si l'element courant est egale au non terminal qu'on cherche a calculer les suivants
                        if ((i + 1) < split.length && terminals.contains(split[i + 1])) { // si l'element admet un terminal en position suivante
                            follows.get(nonTerminal).add(split[i + 1]); // on ajoute ce terminal a l'ensemble des suivant du non terminal
                        } else if ((i + 1) < split.length && nonTerminals.contains(split[i + 1])) { // si l'element a la position suivante est un non terminal
                            follows.get(nonTerminal).addAll(firsts.get(split[i + 1])); // les premiers du non terminal au position i+1 seront ajouter au non terminal a la position i
                            if (firsts.get(split[i + 1]).contains("&")) { // si les premiers de ce dernier non terminal contiennent epsilon
                                while (follows.get(nonTerminal).contains("&")) { // tant que epsilon existe 
                                    follows.get(nonTerminal).remove("&"); // on elimine l'epsilon de l'ensemble des suivant
                                }
                                if ((i + 2) >= split.length && !name.equals(nonTerminal)) { // si l'element a la position i+2 n'existe pas 
                                                                                            // et si le non terminal n'est pas egale a la partie droite de la regle 
                                    calculateFollow(name); // on calcule les suivant du nom de la regle la partie gauche de la production 
                                    follows.get(nonTerminal).addAll(follows.get(name)); // suivant du nom de la regle rentre dans les suivants du non terminal du parametre
                                    // par exemple :  A -> B C |  C - > c   |   C -> &
                                    // suivants(B) = premier(C)/{&} ∪ suivants(A)
                                } else { // si l'element i+2 existe
                                while(i < split.length && firsts.get(split[i]).contains("&")) { // tant qu'il existe un element suivant
                                    follows.get(nonTerminal).addAll(firsts.get(split[i + 1])); // premiers de l'element suivant rentre dans l'ensemble des premier du non terminal
                                    while (follows.get(nonTerminal).contains("&")) { // on elimine les epsilons tant qu'ils existent
                                        follows.get(nonTerminal).remove("&");
                                    }
                                    if((i+1) == (split.length-1) && firsts.get(split[i+1]).contains("&")){ // s'il s'agit du dernier element et contant un epsilon
                                        calculateFollow(name); // on calcule les suivant du nom de la regle la partie gauche de la production
                                        follows.get(nonTerminal).addAll(follows.get(name)); // suivant du nom de la regle rentre dans les suivants du non terminal du parametre


                                    } else { // si ce n'est pas le dernier element on incemente i pour passer a l'element suivant
                                        i++;
                                    }
                                } // fin tant que il ya un element suivant contenant epsilon


                                }
                                // on passe a la production suivante puisque on a parcouri tout les element de la production courante
                                continue production_loop;
                            } // fin si & ∈ premier(split[i+1])

                        } else if (i + 1 >= split.length) { // sinon si l'index courant est en fin de la regle de production
                            if (!name.equals(nonTerminal)) { // si le terminal n'est pas egale au nom de la regle (partie gauche)
                                follows.putIfAbsent(name, new ArrayList<>());
                                if (follows.get(name).isEmpty()) { // si les suivants de la partie gauche n'est pas encore calculé
                                    calculateFollow(name); // calculer les suivants de ce dernier
                                    follows.get(nonTerminal).addAll(follows.get(name)); // et ajouter les suivants au non terminal du parametre
                                    // A -> A B C
                                    // si epsilon ∈ premier(C)
                                    // suivant(B) = premier(C)/{&} ∪ suivants(A)

                                } else { // si les suivant ont été deja calculé
                                    follows.get(nonTerminal).addAll(follows.get(name)); // on ajoute ceux ci a l'ensemble des suivant du non terminal du parametre
                                }

                                continue production_loop;
                            } // fin si nom de production n'est pas egale au non terminal

                        } // fin si i+1 >= nombre des elements
                    } // fin si element courant egale au non terminal qu'on souhaite chercher les suivants
                } // fin pour chaque element de chaque production
            } // fin pour chaque production dans l'ensemble des production pour chaque non terminal
        }); // fin pour chaque ensemble de regles de production
        // finalFollows n'aura que les valuers unique pour chaque non terminal
        finalFollows.put(nonTerminal, new HashSet<>(follows.get(nonTerminal)));

    }


    /**
     * calculer les suivants de tous les non terminaux
     */
    public void calculateFollows() {
        // l'ensemble des suivants de l'axiom du grammaire obtient le marqueur de fin "$" en plus
        // de ces suivants
        // pour ce faire, on cherche l'index de l'axiom dans la list des non terminaux
        int index = nonTerminals.indexOf(axiom);
        // on ajoute l'axiom a la structure de données qui va contenir les suivants de chaque non terminaux
        follows.put(axiom, new ArrayList<>());
        // on ajoute "$"
        follows.get(axiom).add("$");
        // et on calcule les suivant de ce non terminal (axiom)
        calculateFollow(axiom);
        // puis on defini le reste des regle de production
        List<String> remainingTerminals = new ArrayList<>(nonTerminals);
        remainingTerminals.remove(index);

        for (String nonTerminal : remainingTerminals) { // pour chaque non terminal dans le reste des non terminaux

            calculateFollow(nonTerminal); // on calcule les suivants de chaqu'un de ceux-ci

        }

    }


    /**
     * determine si un terminal peut etre dérivé d'un non terminal
     * @param terminal
     * @param nonTerminal
     * @return boolean : vrai si on peut dérivé terminal a partir du non terminal, faux sinon.
     */
    public boolean isChild(String terminal, String nonTerminal) {
        boolean success = false;
        List<String> production = productions.get(nonTerminal); //selectionner les productions du non terminal
        production_loop:
        for (String prod : production) { // pour chaque production
            String[] split = prod.split(" ");
            if (split[0].equals("&")) { // si production est epsilon
                success = false; // terminal ne peut pas etre dérivé de cette production
                // on passe a la production suivante
            } else { // si production n'est pas epsilon
                split_loop:
                for (String s : split) { // pour chaque element de la production en cours
                    if (terminals.contains(s)) { // si element est un terminal
                        if (s.equals(terminal)) { // si element = terminal du parametre
                            success = true; // terminal (parametre) peut etre dérivé de non terminal (parametre)
                            break production_loop; // on arrete la recherche
                        }
                    } else { // si element est un non terminal
                        // on s'interessent aux premiers de l'element
                        if (finalFirsts.get(s).contains(terminal)) { // si l'ensemble des premiers de l'elements
                                                                     // contient le terminal qu'on cherche
                            success = true;
                        } else if(firsts.get(s).contains("&")){ // sinon si l'element contient epsilon
                            continue split_loop; // on passe  a l'element suivant
                        } else { // sinon on passe a la production suivante
                            continue production_loop;
                        }
                    } // fin si element est un non terminal
                } // fin pour chaque element de la production en cours
            } // fin si production nest pas epsilon

        } // fin pour chaque production

        return success;
    }


    /**
     * retourne le numéro correspandant a la regle nonTerminal -> terminal
     * retourne -1 si la regle n'existe pas.
     * @param prodId
     * @return int
     */
    public int findProductionById(String prodId) {
        // resultat intialisé a -1
        AtomicInteger result = new AtomicInteger(-1);
        // id de production est une chaine de charactere contenant le non terminal et le terminal
        // qu'on souhaite dériver séparé par virgule ","
        // on sépare les deux parametres
        String nonTerminal = prodId.split(",")[0];
        String terminal = prodId.split(",")[1];
        // on selectionne les productions du non terminal
        // Note : prodTable est un HashMap avec les noms des non-terminaux comme clé , et avec
        // un autre HashMap pour les valeurs ayant les regles de production comme clé
        // et les numéros des regles comme valeurs de la sturcture.
        // pour avoir le numéro d'une regle le synatx sera
        // prodTable.get(nonTerminal).get(production) et ca retourne le numéro de cette regle

        // premiere etape est de selectionner les production du non terminal du parametre
        HashMap<String, Integer> currentProductions = prodTable.get(nonTerminal);

        // creation d'un tableau statique pour contenir les production
        String[] arrayProd = new String[currentProductions.size()];

        AtomicInteger i = new AtomicInteger(0);
        currentProductions.forEach((prod, num) -> {
            arrayProd[i.getAndIncrement()] = prod;
        });

        prod_loop:
        for (String prod : arrayProd) { // pour chaque production du non terminal
            String[] split = prod.split(" ");
            split_loop:
            for (String token : split) { // pour chaque element de la production en cours
                if (split[0].equals("&")) { // si element est epsilon
                    if (terminal.equals("&")) { // et terminal qu'on cherche est aussi epsilon
                        result.set(currentProductions.get(prod)); // on affecte le numéro de la regle au resultat
                        // note : currentproductions.get(prod) est equivalent a l'expression
                        // prodTable.get(nonTerminal).get(prod)
                        // car currentProduction = prodTable.get(nonTerminal)
                        break prod_loop; // on arrete lorceque on trouve la premiere regle
                                        // qui satisfait les conditions

                    } else { // si l'element en cours  est epsilon mais terminal de parametre n'est pas epsilon
                        continue prod_loop; // on continue les production
                    }

                } // fin si element est epsilon

                // si element n'est pas epsilon ( on n'a pas besoins de sinon car si l'element est epsilon
                // ca va soit arreter la boucle soit passer a la production suivante

                if (terminals.contains(token)) { // si element est un terminal
                    if (token.equals(terminal)) { // si c'est egale a ce qu'on cherche
                        result.set(currentProductions.get(prod)); // on affecte le numéro de production au resultat
                        break prod_loop; // et on arrete la boucle
                    } else { // si element est un terminal mais n'est pas egle au terminal qu'on cherche
                        continue prod_loop; // on passe a la production suivante
                    }
                } else { // si element est un non terminal
                    // on utilise la fonction isChild pour voir si on peut dérivé
                    // le terminal qu'on cherche du non terminal present au debut de la regle
                    if (isChild(terminal, token)) { // si c'est dérivable
                        result.set(currentProductions.get(prod)); // on affecte le numéro de la regle courante au resultat
                        break prod_loop; // et on arrete la boucle des productions
                    } else if(firsts.get(token).contains("&")) { // si le terminal qu'on cherche n'est pas dérivable
                                                                // du non terminal present dans la regle
                                                                // et si l'ensembles des premier de ce non terminals
                                                                // contient epsilon
                        continue split_loop; // on donne la main au symbole suivant
                    } else { // si les premiers de l'element ne contient pas epsilon
                        continue prod_loop; // on passe a la production suivante
                    }
                } // fin si element est un non terminal
            } // fin pour chaque elements
        } // fin pour chaque production du non terminal (parametre)


        return result.get();
    }


    /**
     *  Construction de la table d'analyse syntaxique.
     *  La table d'analyse est un HashMap dont les valeurs sont eux meme un HashMap identifié
     *  par une chaine de charactere, et contenant une list<String> ,
     *  les clés du HashMap de la table d'analyse (analysisTable) sont les non terminaux de la grammaire
     *  representant les ligne de la tableau, les clés du Hashmap inclus dans le tableau d'analyse
     *  sont les terminaux de la grammaire plus le symbole marqueur de fin "$",
     *  en fin pour chaque terminal on trouve une List<String> contenant le/les numéro des
     *  régles a appliquer
     */
    public void constructAnalysisTable() {

        // instanciation de la structure analysisTable
        analysisTable = new HashMap<>();
        nonTerminal_loop:
        for (String nonTerminal : nonTerminals) { // pour chaque non terminal
            analysisTable.putIfAbsent(nonTerminal, new HashMap<>());
            terminal_loop:
            for (String terminal : terminals) { // pour chaque terminal
                analysisTable.get(nonTerminal).putIfAbsent(terminal, new ArrayList<>());
                String prodId = nonTerminal + "," + terminal; // construction de l'id
                int result = findProductionById(prodId); // recherche du numéro de la regle
                // note : pour faire le premier balayage de la construction du tableau
                // ce n'est pas neccessaire de verifier si le symbole terminal appartient
                // a l'ensemble des premiers du non terminal car dans le cas ou un terminal n'appartient
                //  pas au premiers du non terminal courant le resultat de findProductionById va etre -1
                //  et le resultat sera ignoré et donc passer au terminal suivant
                if (result != -1) { // si resultat n'est pas egale a -1
                    // on affecte a l'entré courante du tableau ( i-eme ligne , j-eme colonne )
                    // le resultat de findProductionById
                    analysisTable.get(nonTerminal).get(terminal).add(result + "");

                } else { // si resultat est egale a -1 ( regle inexistante )
                    continue terminal_loop; // on passe au terminal suivant
                }


            } // fin pour chaque terminal
            for(String terminal : terminals){
                // deuxieme balayage (Suivant)
                if (finalFollows.get(nonTerminal).contains(terminal)) { // si l'ensemble des suivants du non terminal
                    // contient le terminal courant
                    if (finalFirsts.get(nonTerminal).contains("&")) { // et si & ∈ premiers(nonTerminal)
                        String prodId2 = nonTerminal + "," + "&"; // construction de l'id de production
                        int syncResult = findProductionById(prodId2);
                        // et en fin on affecte le resultat a l'entré correspandante
                        analysisTable.get(nonTerminal).get(terminal).add(syncResult + "");
                    } else { // si & ∉ premiers(nonTerminal)
                        // on affecte le terme "synch" a l'entrée correspandante du tableau
                        analysisTable.get(nonTerminal).get(terminal).add("synch");
                    }
                }
            }



            // apres avoir fini tout les terminals du grammaire on refait le deuxieme balayage (par rapport au suivants)
            // pour la derniere colonne du tableau qui va contenir le symbole marqueur de fin "$"
            analysisTable.get(nonTerminal).putIfAbsent("$", new ArrayList<>());
            // on refait les meme procedures du deuxieme balayage pour "$"
            if (finalFollows.get(nonTerminal).contains("$")) {
                if (finalFirsts.get(nonTerminal).contains("&")) {
                    String prodId2 = nonTerminal + "," + "&";
                    int syncResult = findProductionById(prodId2);
                    if(syncResult != -1){
                        analysisTable.get(nonTerminal).get("$").add(syncResult + "");
                    } else {
                        analysisTable.get(nonTerminal).get("$").add( "synch");
                    }

                } else {
                    analysisTable.get(nonTerminal).get("$").add("synch");
                }
            }


        } // fin pour chaque non terminal

    }


    /**
     * verifie si la grammaire donné est une grammaire LL(1)
     * si une entrée du tableau d'analyse sytaxique admet deux ou plus de regles
     * a appliquer n'est pas une grammaire LL(1).
     * @return boolean : true si la grammaire est LL(1), false sinon
     */
    public boolean isLL1() {

        // on initialise le resultat a true;
        // un grammaire est dit LL(1) si aucune entrée dans le tableau d'analyse
        // ne contient plus qu'une regle a appliquer
        AtomicBoolean result = new AtomicBoolean(true);
        analysisTable.forEach((nonTerminal, set) -> { // pour chaque ligne
            set.forEach((terminal, resultats) -> { // pour chaque colonne
                if (resultats.size() > 1) { // si les resultat d'une entré a une longeur supérieur a 1
                    result.set(false); // la grammaire n'est pas LL(1)
                    // Un grammaire est LL(1) jusqu'a preuve du contraire
                }
            }); // fin pour chaque colonne
        }); // fin pour chaque ligne
        return result.get();
    }


    /**
     * Affichage du tableau de l'analyse syntaxique avec une mise en format
     */
    public void showAnalysisTable() {
        List<String> terminalsDollar = terminals;
        terminalsDollar.add("$");
        System.out.println(String.format("%n %n %60s %n %n", "Non-recursive Analysis Table"));
        for (String terminal : terminalsDollar) {
            System.out.println(String.format("%20s", terminal));
        }
        System.out.println(String.format("%n%s", "--".repeat((terminalsDollar.size()) * 10)));
        for (String nonTerminal : nonTerminals) {
            System.out.println(String.format("%n%s", nonTerminal));
            int n = nonTerminal.length();
            int dist = 20 - n;
            int i = 0;
            for (String terminal : terminalsDollar) {
                if (i == 0) {
                    System.out.println(String.format("%" + dist + "s", analysisTable.get(nonTerminal).get(terminal).toString()));
                    i++;
                } else {
                    System.out.println(String.format("%20s", analysisTable.get(nonTerminal).get(terminal).toString()));

                }
            }
        }
        System.out.println(String.format("%n%s", "--".repeat((terminalsDollar.size()) * 10)));
    }


    /**
     * Methode pour l'affichage en console du résultat complet de l'analyse
     * @throws IOException
     */
    public void showAnalysis() throws IOException {
        File x = new File("output.txt");
        x.createNewFile();
        FileReader temporaryFile = new FileReader("output.txt");
        writeAnalysis("output.txt");
        BufferedReader br = new BufferedReader(temporaryFile);
        String currentLine = br.readLine();
        while (currentLine != null){
            System.out.println(currentLine);
            currentLine = br.readLine();
        }
        br.close();
        x.delete();
    }


    /**
     * Permet de creer un nouveau ficher text contenant le rapport de l'analyse du mot données
     * suivant la grammaire de la configuration
     * @param fileName
     * @throws IOException
     */
    public void writeAnalysis(String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        BufferedWriter bufferWriter = new BufferedWriter(writer);
        String title = String.format("%90s","Analyse Syntaxique prédictive non récursive");
        bufferWriter.append(title);
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append("Axiom  : " + axiom);
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append("Terminaux  : " + terminals.toString());
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append("Non-terminaux  : " + nonTerminals.toString());
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append("Regles de productions : ");
        bufferWriter.newLine();
        bufferWriter.newLine();
        productions.forEach((k, v) -> {
            v.forEach((elem) -> {
                try {
                    bufferWriter.append("(" + prodNum + ")   " + k + " -> " + elem);
                    bufferWriter.newLine();
                    prodNum = prodNum + 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append("Calcul de premiers : ");
        bufferWriter.newLine();
        bufferWriter.newLine();
        finalFirsts.forEach((k, v) -> {
            try {
                bufferWriter.append("premier(" + k + ") = " + v.toString());
                bufferWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append("Calcul de suivants : ");
        bufferWriter.newLine();
        bufferWriter.newLine();
        finalFollows.forEach((k, v) -> {
            try {
                bufferWriter.append("suivants(" + k + ") = " + v.toString());
                bufferWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        List<String> terminalsDollar = terminals;
        terminalsDollar.add("$");
        analysisTablePrinting.append(String.format("%n %n %80s %n %n", "Non-recursive Analysis Table"));
        for (String terminal : terminalsDollar) {
            analysisTablePrinting.append(String.format("%20s", terminal));
        }
        analysisTablePrinting.append(String.format("%n%s", "--".repeat((terminalsDollar.size()) * 10)));
        for (String nonTerminal : nonTerminals) {
            analysisTablePrinting.append(String.format("%n%s", nonTerminal));
            int n = nonTerminal.length();
            int dist = 20 - n;
            int i = 0;
            for (String terminal : terminalsDollar) {
                if (i == 0) {
                    analysisTablePrinting.append(String.format("%" + dist + "s", analysisTable.get(nonTerminal).get(terminal).toString()));
                    i++;
                } else {
                    analysisTablePrinting.append(String.format("%20s", analysisTable.get(nonTerminal).get(terminal).toString()));
                }
            }
        }
        analysisTablePrinting.append(String.format("%n%s", "--".repeat((terminalsDollar.size()) * 10)));
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append(analysisTablePrinting);
        if (isLL1()) {
            bufferWriter.newLine();
            bufferWriter.newLine();
            bufferWriter.append("=>  Grammaire LL(1) ");
            bufferWriter.newLine();
        } else {
            bufferWriter.newLine();
            bufferWriter.newLine();
            bufferWriter.append("=>  Grammaire n'est pas LL(1) ");
            bufferWriter.newLine();
        }
        NonRecursiveSyntaxAnalyser NRSA = new NonRecursiveSyntaxAnalyser();
        NRSA.analyse();
        bufferWriter.newLine();
        bufferWriter.newLine();
        String word = NRSA.IR.elements.toString();
        word = word.replace("[", "");
        word = word.replace(",", "");
        word = word.replace("]", "");
        bufferWriter.append("Comportement de l'analyseur suite a l'analyse du mot : " + word);
        bufferWriter.newLine();
        bufferWriter.newLine();
        bufferWriter.append(NRSA.output);
        bufferWriter.close();
    }


}
