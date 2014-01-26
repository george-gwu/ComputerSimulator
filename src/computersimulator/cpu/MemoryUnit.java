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
    
    // state is used by the fetch/store controller to determine the current operation
    private int state;
    private final static int STATE_NONE = 0;    
    private final static int STATE_STORE = 1;    
    private final static int STATE_FETCH = 2;    
    

    public MemoryUnit() {
        memory = new Word[8][256];     
        initializeMemoryToZero(); // Upon powering up, set all elements of memory to zero
        
        resetRegisters();
    }
    
    /**
     * Set the Memory Buffer Register (used in store)
     * @param dataWord The value to store
     * @return TRUE/FALSE if successful
     */
    public boolean setMBR(Word dataWord){
        switch(state){            
            case MemoryUnit.STATE_FETCH:     
            case MemoryUnit.STATE_STORE:
                return false; // We're currently busy, set fails.
                                
            case MemoryUnit.STATE_NONE:
            default:
                
                this.memoryBufferRegister = dataWord;
                return true;                              
        }        
    }
    
    public Word getMBR(){
        return this.memoryBufferRegister;
    }
    
    /**
     * Set the Memory Access Register (used in get/store)
     * @param addressUnit The address to get/store
     * @return TRUE/FALSE if successful
     */    
    public boolean setMAR(Unit addressUnit){
        switch(state){            
            case MemoryUnit.STATE_FETCH:     
            case MemoryUnit.STATE_STORE:
                return false; // We're currently busy, set fails.
                                
            case MemoryUnit.STATE_NONE:
            default:
                
                this.memoryAddressRegister = addressUnit;
                return true;                              
        }         
        
    }
    
    public Unit getMAR(){
        return this.memoryAddressRegister;
    }
    
    /**
     * Clock cycle. This is the main function which causes the Memory
     * Unit to do work. This serves as a publicly accessible method, but delegates
     * to the fetch/store controller.
     */
    public void clockCycle(){
        this.fetchStoreController();                
    }
    
    private void fetchStoreController(){
        switch(state){            
            case MemoryUnit.STATE_FETCH:
                this.resetState(); // We had a chance to pick up the result            
                break;
            
            case MemoryUnit.STATE_STORE:
                this.resetState();  // We had a chance to pick up the result                              
                break;
                
            case MemoryUnit.STATE_NONE:
            default:                
                if(this.memoryAddressRegister!= null){
                    if(this.getMBR() == null){  // This is a fetch request
                        this.state = MemoryUnit.STATE_FETCH;                        
                        this.fetchAddressOperation();
                    } else { // This is a store request
                        this.state = MemoryUnit.STATE_STORE;
                        this.storeAddressInMemoryOperation();                        
                    }
                } // else no memory action requested
                break;
        }
    }
    
    /**
     * fetchAddressOperation - This fetches an address specified by MAR, and
     * puts the contents of that memory location into MBR. 
     * Private because it is called by clockCycle.
     */    
    private void fetchAddressOperation(){
        /*
            Fetch(address)
            Load the address into MAR.
            Decode the address in MAR.
            Copy the contents of that memory location into the MDR    
        */
    }
    
    /**
     * storeAddressInMemoryOperation - This stores a MBR value into memory at
     * the location specified by MAR.
     * Private because it is called by clockCycle.
     */
    private void storeAddressInMemoryOperation(){
        /*
            Store(address, value)
            Load the address into MAR.
            Load the value into MDR.
            Decode the address in MAR.
            Store the contents of MDR into that memory location.        
        */        
    }
    
    


    /**
     * Reset registers to null
     */    
    private void resetRegisters(){        
        this.memoryAddressRegister = null;
        this.memoryBufferRegister = null;
        this.resetState();
    }
    
    /**
     * Reset state
     */    
    private void resetState(){   
        this.state = MemoryUnit.STATE_NONE;
    }    

    /**
     * Initialize memory banks to zero filled words. 
     * NOTE: Final because it is called in the constructor.
     */   
    private void initializeMemoryToZero(){
        for (Word[] bank : memory) {
            for (int i = 0; i < bank.length; i++) {
                bank[i] = new Word(0); // set to zero
            }
        }
        
    }
    
}
