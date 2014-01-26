package computersimulator.cpu;

import computersimulator.components.Unit;
import computersimulator.components.Word;

/**
 * MemoryUnit - MemoryUnit should implement a single port memory. 
 * All memory elements need to be set to zero on power up.
 * In one cycle, it should accept an address from the MAR. It should then
 * accept a value in the MBR to be stored in memory on the next cycle or 
 * place a value in the MBR that is read from memory on the next cycle.
 * 
 * @TODO: 8k words max. (pg 16) means we need virtual memory?
 * 
 * @author george
 */
public class MemoryUnit {
    
    // Memory 2d Array 8 banks of 256 words each
    private Word[][] memory;    
    // MAR	13 bits	Memory Address Register: holds the address of the word to be fetched from memory
    private Unit memoryAddressRegister;
    // MBR	20 bits	Memory Buffer Register: holds the word just fetched from or stored into memory
    private Word memoryBufferRegister;

    public MemoryUnit() {
        memory = new Word[8][256];     
        initializeMemoryToZero(); // Upon powering up, set all elements of memory to zero
        
        this.memoryAddressRegister = new Unit(13);
        this.memoryBufferRegister = new Word();
    }
    
    public void setMBR(Word input){
        this.memoryBufferRegister = input;
    }
    
    public Word getMBR(){
        return this.memoryBufferRegister;
    }
    
    public void setMAR(Unit input){
        this.memoryAddressRegister = input;
    }
    
    public Unit getMAR(){
        return this.memoryAddressRegister;
    }
    
    /*
Fetch(address)
Load the address into MAR.
Decode the address in MAR.
Copy the contents of that memory location into the MDR    
    */
    
    /*
Store(address, value)
Load the address into MAR.
Load the value into MDR.
Decode the address in MAR.
Store the contents of MDR into that memory location.    
    
    */

    /**
     * Initialize memory banks to zero filled words. 
     * NOTE: Final because it is called in the constructor.
     */   
    public final void initializeMemoryToZero(){
        for (Word[] bank : memory) {
            for (int i = 0; i < bank.length; i++) {
                bank[i] = new Word(0); // set to zero
            }
        }
        
    }
    
}
