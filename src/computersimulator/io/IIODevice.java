
package computersimulator.io;

import computersimulator.components.Word;

/**
 * This is the primary interface IO devices.
 */
public interface IIODevice {
    

    /**
     * Input reads a word
     * @return 
     */
    public Word input();
    
    /**
     * Writes a word
     * @param value 
     */
    public void output(Word value);
    
    /**
     * Check Status of Device
     * @return status
     */
    public int checkStatus();
}
