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
    
    /**
     * Card Reader Constructor - Reads in a text file to an array for access via IO controller
     */
    public ConsoleKeyboard(){
        buffer = new ArrayList<>();
        
    }
    
    /**
     * Input reads a word
     * @return Word
     */
    @Override
    public Word input(){
        
        //Word result = Word.WordFromBinaryString(firstTwentyChars);
        
        return new Word(0);
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
     * @return status
     */
    @Override
    public int checkStatus(){
        int res = (buffer.size() > 0 ? InputOutputController.STATUS_READY : InputOutputController.STATUS_BUSY);
        
        return res;
    }  
    
    
    public void buttonPress(int keyCode){
        System.out.println("Key Press: "+keyCode);
        buffer.add(keyCode);
    }
    
}
