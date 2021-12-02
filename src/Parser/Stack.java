package Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Une simple implementation de la structure de la pile en utilisant l'interface ArrayList();
 */

public class Stack {

    private List<String> stack = new ArrayList<String>();
    private String head;

    /**
     * Empiler un element dans la pile
     * @param element
     */
    public void push(String element){
        stack.add(element);
        this.head = element;
    }

    /**
     * Dépiler le sommet de la pile
     */
    public void pop(){

        int lastIndex = stack.size() - 1;
        if(lastIndex <= 0){
            stack.clear();
            head = null;
        } else {
            stack.remove(lastIndex);
            head = stack.get(lastIndex-1);
        }

    }
    public String getHead(){
        return head;
    }
    public  List<String> getStack(){
        return stack;
    }

}
