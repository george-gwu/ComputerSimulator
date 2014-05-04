package computersimulator.io;

import computersimulator.components.Word;
import computersimulator.cpu.InputOutputController;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author george
 */
public class CardReader implements IIODevice {
    
    private ArrayList<String> data;
    private int iterator;
    
    /**
     * Card Reader Constructor - Reads in a text file to an array for access via IO controller
     * @param filename
     */
    public CardReader(String filename){
        data = new ArrayList<>();
        iterator=0;
        
        try {
            FileReader reader=new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);
            
            String strLine;
            while((strLine=br.readLine())!=null){
                data.add(strLine);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(CardReader.class.getName()).log(Level.SEVERE, "IO Exception in CardReader: "+ex);
        }
    }
    
    /**
     * Input reads a word
     * @return Word
     */
    @Override
    public Word input(){
        
        String strLine = data.get(iterator++);
               
        Logger.getLogger(CardReader.class.getName()).log(Level.FINEST, strLine);
        
        String firstTwentyChars=strLine.substring(0, 20);
        
        Word result = Word.WordFromBinaryString(firstTwentyChars);
        
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
     * @return status
     */
    @Override
    public int checkStatus(){
        int res = (this.iterator<this.data.size() ? InputOutputController.STATUS_READY : InputOutputController.STATUS_DONE);
        
        return res;
    }   
    
}
