package computersimulator.io;

import computersimulator.components.Word;
import computersimulator.cpu.InputOutputController;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author george
 */
public class CardReader implements IIODevice {
    
    private BufferedReader br;
    
    public CardReader(){
        try {
            FileReader reader=new FileReader("cardreader.txt");
            br = new BufferedReader(reader);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReadFilebyJava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Input reads a word
     * @return 
     */
    public Word input(){
        String strLine="";
        try {
            strLine = br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(CardReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(strLine);
        
        String firstTwentyChars=strLine.substring(0, 20);
        
        Word result = Word.WordFromBinaryString(firstTwentyChars);
        
        return new Word(0);
    }
    
    /**
     * Writes a word
     * @param value 
     */
    public void output(Word value){
        System.out.println("ERROR: This IO device does not support writing.");
    }
    
    /**
     * Check Status of Device
     * @return status
     */
    public int checkStatus(){
        int res = 0;
        try {
            res = (br.ready() ? InputOutputController.STATUS_READY : InputOutputController.STATUS_BUSY);
        } catch (IOException ex) {
            Logger.getLogger(CardReader.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return res;
    }   
    
}
