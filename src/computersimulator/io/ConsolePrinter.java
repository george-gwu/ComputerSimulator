package computersimulator.io;

import computersimulator.components.Word;
import computersimulator.cpu.InputOutputController;
import javax.swing.JTextArea;


/**
 *
 * @author george
 */
public class ConsolePrinter implements IIODevice {
    
    JTextArea guiDisplay;
    
    /**
     * Console Printer Constructor
     */
    public ConsolePrinter(){
        
        
    }
    
    public void setDisplay(JTextArea disp){
        this.guiDisplay = disp;
    }
    
    /**
     * Input reads a word - Not Available in Printer
     * @return Word
     */
    @Override
    public Word input(){ 
        System.out.println("[IO]: Error - Tried to receive from printer.");
        return null;
    }
    
    /**
     * Writes a character to the screen from a Word value
     * @param value 
     */
    @Override
    public void output(Word value){        
        Integer integerValue = value.getUnsignedValue();
        int intValue = (int)integerValue;       
        guiDisplay.append(String.valueOf((char)intValue));        
    }
    
    /**
     * Check Status of Device
     * @return status  Is it ever busy?
     */
    @Override
    public int checkStatus(){
        return InputOutputController.STATUS_READY;
    }      

    
}
