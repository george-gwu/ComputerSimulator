package computersimulator.cpu;

import computersimulator.components.*;

/**
 * MemoryControlUnit - MemoryControlUnit implements a single port memory. 
 * All memory elements need to be set to zero on power up.
 * In one cycle, it should accept an address from the MAR. It should then
 * accept a value in the MBR to be stored in memory on the next cycle or 
 * place a value in the MBR that is read from memory on the next cycle.
 * 
 * 
 */
public class MemoryControlUnit implements IClockCycle {
    
    // Memory 2d Array 8 banks of 256 words each = 2048 addresses
    private final Word[][] memory;    
    private final static int BANK_SIZE = 8;
    private final static int BANK_CELLS = 256;
    
    // MAR	13 bits	Memory Address Register: holds the address of the word to be fetched from memory
    private Unit memoryAddressRegister;
    // MBR	20 bits	Memory Buffer Register: holds the word just fetched from or stored into memory
    private Word memoryBufferRegister;
    
    
    // state is used by the fetch/store controller to determine the current operation
    private int state;
    private final static int STATE_NONE = 0;    
    private final static int STATE_STORE = 1;    
    private final static int STATE_FETCH = 2;    
    

    public MemoryControlUnit() {
        memory = new Word[MemoryControlUnit.BANK_SIZE][MemoryControlUnit.BANK_CELLS];     
        initializeMemoryToZero(); // Upon powering up, set all elements of memory to zero
        
        resetRegisters();
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
            case MemoryControlUnit.STATE_FETCH:
            case MemoryControlUnit.STATE_STORE:
                this.resetState(); // +1 cycles means we had a chance to pick up the result            
                break;
                            
            case MemoryControlUnit.STATE_NONE:
            default:                
                if(this.memoryAddressRegister!= null){
                    if(this.getMBR() == null){  // This is a fetch request
                        this.state = MemoryControlUnit.STATE_FETCH;                        
                        this.fetchAddressOperation();
                    } else { // This is a store request
                        this.state = MemoryControlUnit.STATE_STORE;
                        this.storeAddressInMemoryOperation();                        
                    }
                } // else no memory action requested
                break;
        }
    }    
    
    /**
     * Set the Memory Buffer Register (used in store)
     * @param dataWord The value to store
     * @return TRUE/FALSE if successful
     */
    public boolean setMBR(Word dataWord){
        switch(state){            
            case MemoryControlUnit.STATE_FETCH:     
            case MemoryControlUnit.STATE_STORE:
                return false; // We're currently busy, set fails.
                                
            case MemoryControlUnit.STATE_NONE:
            default:
                
                this.memoryBufferRegister = dataWord;
                return true;                              
        }        
    }
    
    /**
     *
     * @return memoryBufferRegister
     */
    public Word getMBR(){
        return this.memoryBufferRegister;
    }
    
    /**
     * Clear MBR (for fetch operation)
     */
    public void clearMBR(){
        this.memoryBufferRegister=null;
    }
    
    /**
     * Set the Memory Access Register (used in get/store)
     * @param addressUnit The address to get/store
     * @return TRUE/FALSE if successful
     */    
    public boolean setMAR(Unit addressUnit){
        switch(state){            
            case MemoryControlUnit.STATE_FETCH:     
            case MemoryControlUnit.STATE_STORE:
                return false; // We're currently busy, set fails.
                                
            case MemoryControlUnit.STATE_NONE:
            default:
                
                this.memoryAddressRegister = addressUnit;
                return true;                              
        }         
        
    }   

    /**
     *
     * @return memoryAddressRegister
     */
    public Unit getMAR(){
        return this.memoryAddressRegister;
    }
    
    
    /**
     * Check to see if we're mid-operation this clock cycle
     * @return true/false 
     */
    public boolean isBusy(){
        switch(state){            
            case MemoryControlUnit.STATE_FETCH:     
            case MemoryControlUnit.STATE_STORE:
                return true;
                                
            case MemoryControlUnit.STATE_NONE:
            default:
                return false;                         
        }    
    }
    
       
    
    /**
     * Calculates relative address for memory location from MAR
     * @TODO: 8191 words are addressable via MAR despite only 2048 exist. (see pg 16)... means we need virtual memory?
     * @return Array{bankIndex,cellIndex}
     */
    private int[] calculateMemoryAddressFromMAR() throws Exception {
        // Load the address in MAR
        int address = this.memoryAddressRegister.getValue();
                
        // Decode the Address in MAR
        int bankIndex = (int)Math.floor((address/ MemoryControlUnit.BANK_SIZE));
        int cellIndex = address % MemoryControlUnit.BANK_CELLS;
        
        if(bankIndex > MemoryControlUnit.BANK_SIZE){
            throw new Exception("Memory index["+bankIndex+"]["+cellIndex+"] out of bounds. (Memory Size: ["+MemoryControlUnit.BANK_SIZE+"]["+MemoryControlUnit.BANK_CELLS+"])");
        }

        
        // Return the result index array
        int[] result = {bankIndex,cellIndex};        
        return result;
    }
    
    public Word engineerFetchByMemoryLocation(Unit address){
        // Decode the Address
        int bankIndex = (int)Math.floor((address.getValue() / MemoryControlUnit.BANK_SIZE));
        int cellIndex = address.getValue() % MemoryControlUnit.BANK_CELLS;        
        
        return new Word(this.memory[bankIndex][cellIndex]);
    }
    
    public void engineerSetMemoryLocation(Unit address, Word value){
         // Decode the Address
        int bankIndex = (int)Math.floor((address.getValue() / MemoryControlUnit.BANK_SIZE));
        int cellIndex = address.getValue() % MemoryControlUnit.BANK_CELLS;        
        
        this.memory[bankIndex][cellIndex] = value;
    }
    
    
    /**
     * fetchAddressOperation - This fetches an address specified by MAR, and
     * puts the contents of that memory location into MBR. 
     * Private because it is called by clockCycle.
     */    
    private void fetchAddressOperation(){
        try {
            // Load and Decode the Address in MAR
            int[] addr = this.calculateMemoryAddressFromMAR();
        
            // Copy the contents of that memory location into the MBR            
            this.memoryBufferRegister = new Word(this.memory[addr[0]][addr[1]]);
            System.out.println("-- Fetch MAR("+this.memoryAddressRegister+"): "+this.memoryBufferRegister);
        } catch(Exception e){
            //@TODO: Handle bad address (virtual memory?)
            System.out.println("-- Bad Address: "+this.memoryAddressRegister+" -> "+e.getMessage());
        }
        
        
    }
    
    /**
     * storeAddressInMemoryOperation - This stores a MBR value into memory at
     * the location specified by MAR.
     * Private because it is called by clockCycle.
     */
    private void storeAddressInMemoryOperation(){   
        try {        
            // Load and Decode the Address in MAR
            int[] addr = this.calculateMemoryAddressFromMAR();

            //Copy the value from MDR to Memory                
            this.memory[addr[0]][addr[1]] = new Word(this.memoryBufferRegister);
            System.out.println("-- Set MAR("+this.memoryAddressRegister+"): "+this.memoryBufferRegister);
        } catch(Exception e){
            //@TODO: Handle bad address (virtual memory?)
            System.out.println("-- Bad Address: "+this.memoryAddressRegister+" -> "+e.getMessage());
        }                
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
        this.state = MemoryControlUnit.STATE_NONE;
    }    

    /**
     * Initialize memory banks to zero filled words. 
     * NOTE: Final because it is called in the constructor.
     */   
    private void initializeMemoryToZero(){
        for (Word[] bank : this.memory) {
            for (int i = 0; i < bank.length; i++) {
                bank[i] = new Word(0); // set to zero
            }
        }
    }
    
}
