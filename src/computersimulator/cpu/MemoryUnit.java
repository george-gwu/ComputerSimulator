package computersimulator.cpu;

import computersimulator.components.Word;

/**
 * MemoryUnit - MemoryUnit should implement a single port memory. 
 * All memory elements need to be set to zero on power up.
 * In one cycle, it should accept an address from the MAR. It should then
 * accept a value in the MBR to be stored in memory on the next cycle or 
 * place a value in the MBR that is read from memory on the next cycle.
 * 
 * Memory should be a 2d array consisting of 8 banks of 256 words each. = 2048 words
 * 8k words max. (pg 16) means we need virtual memory?
 * 
 * @author george
 */
public class MemoryUnit {
    
    private Word[][] memory;

    public MemoryUnit() {
        memory = new Word[8][256];     
        try {
            initializeMemoryToZero(); // Upon powering up, set all elements of memory to zero
        } catch(Exception e){} // silence initialization errors since 0 is valid                
    }
    
    public final void initializeMemoryToZero(){
        for (Word[] bank : memory) {
            for (int i = 0; i < bank.length; i++) {
                bank[i] = new Word(0); // set to zero
            }
        }
        
    }
    
}
