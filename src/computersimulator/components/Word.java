
package computersimulator.components;

/**
 *
 * @author george
 */
public class Word extends Unit {
    
    private final static int WORD_SIZE=20;

    public Word() {
        super(WORD_SIZE);
    }

    public Word(int value) {
        super(WORD_SIZE, value);
    }
    
    
    /** 
     * Creates a Word from a Binary String. This method allows for spacing which is trimmed for readability.
     * @param binaryReadable Binary String
     * @return Word 
     */
    public static Word WordFromBinaryString(String binaryReadable){              
        String binary = binaryReadable.replace(" ", "");
        
        if(binary.length()!=WORD_SIZE){
            try {
                throw new Exception("This isn't a Word. The size should be "+WORD_SIZE+", but instead was: "+Unit.UnitFromBinaryString(binaryReadable));
            }catch(Exception unusedOnlyForNiceError){}
            
        }
        
        int intValue = Integer.parseInt(binary, 2);
        return new Word(intValue);
        
    }    
    
}
