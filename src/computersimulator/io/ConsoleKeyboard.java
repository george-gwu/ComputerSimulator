package computersimulator.io;

import computersimulator.components.Word;
import computersimulator.cpu.InputOutputController;
import java.util.ArrayList;

/**
 *
 * @author george
 */
public class ConsoleKeyboard implements IIODevice {
    
    private ArrayList<Integer> buffer;
    private Word tempInput;
    
    /**
     * Console Keyboard Constructor
     */
    public ConsoleKeyboard(){
        buffer = new ArrayList<>();
        tempInput=null;
        
    }
    
    /**
     * Input reads a word
     * @return Word
     */
    @Override
    public Word input(){        
        Word result = tempInput;        
        tempInput = null;
        return result;        
    }
    
    /**
     * Writes a word
     * @param value 
     */
    @Override
    public void output(Word value){
        System.out.println("ERROR: This IO device does not support writing.");
    }
    
    /**
     * Check Status of Device
     * @return status=Busy if no value in temp
     */
    @Override
    public int checkStatus(){
        return (tempInput==null ? InputOutputController.STATUS_BUSY : InputOutputController.STATUS_READY);        
    }  
    
    
    public void buttonPress(int keyCode){        
        System.out.println("[IO]: Key Press - "+keyCode);
        if(keyCode==13){ // Key Press was Enter
            String temp = "";
            for(int key : buffer){
                temp+=(char)key;                
            }
            tempInput = new Word(Integer.parseInt(temp));
            buffer.clear();
            System.out.println("[IO]: Received Value - "+tempInput);
        } else if(keyCode==8){ // Key Press was Backspace
            if(buffer.size()>0){
                buffer.remove(buffer.size()-1);
            }
        } else if(keyCode>=48 && keyCode <= 57){ // Key Press was Digit
            buffer.add(keyCode);
        }
        
    }
    
}
