package computersimulator.io;

import computersimulator.components.Word;
import computersimulator.cpu.InputOutputController;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author george
 */
public class ConsoleKeyboard implements IIODevice {
    
    private Word buffer;
    
    /**
     * Console Keyboard Constructor
     */
    public ConsoleKeyboard(){
        buffer = null;
        
    }
    
    /**
     * Input reads a word
     * @return Word
     */
    @Override
    public Word input(){        
        Word result = buffer;        
        buffer = null;
        return result;        
    }
    
    /**
     * Writes a word
     * @param value 
     */
    @Override
    public void output(Word value){
        Logger.getLogger(CardReader.class.getName()).log(Level.SEVERE, "ERROR: This IO device does not support writing.");
    }
    
    /**
     * Check Status of Device
     * @return status=Busy if no value in temp
     */
    @Override
    public int checkStatus(){
        return (buffer==null ? InputOutputController.STATUS_BUSY : InputOutputController.STATUS_READY);        
    }  
    
    /**
     * buttonPress is the primary interface for the GUI. It manipulates a keyStroke
     * buffer before pushing it to a temporary holder.
     * @param keyCode 
     */
    public void buttonPress(int keyCode){        
        Logger.getLogger(CardReader.class.getName()).log(Level.CONFIG, "[IO]: Key Press - "+keyCode);
        buffer = new Word(keyCode);        
    }
    
}
