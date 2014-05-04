package computersimulator.cpu;

import computersimulator.components.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MemoryControlUnit - MemoryControlUnit implements a single port memory. 
 * All memory elements need to be set to zero on power up.
 In one cycle, it should accept an addressRaw from the MAR. It should then
 accept a value in the MBR to be stored in memory on the next cycle or 
 place a value in the MBR that is read from memory on the next cycle.
 */
public class MemoryControlUnit implements IClockCycle {
    
    // Memory 2d Array 8 banks of 256 words each = 2048 addresses
    private final Word[][] memory;    
    private final static int BANK_SIZE = 8;
    private final static int BANK_CELLS = 256;
    
    
    private final static Boolean ENABLE_CACHE = false;  
    
    // MAR	13 bits	Memory Address Register: holds the addressRaw of the word to be fetched from memory
    private Unit memoryAddressRegister;
    // MBR	20 bits	Memory Buffer Register: holds the word just fetched from or stored into memory
    private Word memoryBufferRegister;
    
    private Cache cache;    
    
    // state is used by the fetch/store controller to determine the current operation
    private int state;
    private final static int STATE_NONE = 0;    
    private final static int STATE_PRE_STORE = 1;
    private final static int STATE_PRE_FETCH = 2;
    private final static int STATE_WAITING = 3;
    

    public MemoryControlUnit() {
        memory = new Word[MemoryControlUnit.BANK_SIZE][MemoryControlUnit.BANK_CELLS];             
        this.resetMemory();
    }
    
    
    public final void resetMemory(){
        initializeMemoryToZero(); // Upon powering up, set all elements of memory to zero
        cache = new Cache(this);
        
        memoryAddressRegister = new Unit(13);
        memoryBufferRegister = new Word();
        
        this.resetState();
        
    }
    
    /**
     * Clock cycle. This is the main function which causes the Memory
     * Unit to do work. This serves as a publicly accessible method, but delegates
     * to the fetch/store controller.
     * @throws computersimulator.components.MachineFaultException
     */
    @Override
    public void clockCycle() throws MachineFaultException{
        cache.clockCycle();
        switch(state){                                   
            case MemoryControlUnit.STATE_PRE_STORE: 
                if(MemoryControlUnit.ENABLE_CACHE){
                    this.cacheStoreAddressOperation();
                } else {
                    this.storeAddressInMemoryOperation();
                }
                break;
                
            case MemoryControlUnit.STATE_PRE_FETCH:                                                     
                if(MemoryControlUnit.ENABLE_CACHE){
                    this.cacheFetchAddressOperation();
                } else {
                    this.fetchAddressOperation();
                }
                break;
                            
            case MemoryControlUnit.STATE_NONE:
            default: // no memory action requested            
                
                break;
        }
    }   
    

    /**
     * Set the Memory Buffer Register (used in store)
     * @param dataUnit The value to store (converted to Word)
     */
    public void setMBR(Unit dataUnit){
        Word res = new Word();
        res.setValueBinary(dataUnit.getBinaryString());
        this.setMBR(res);
    }
    
    /**
     * Set the Memory Buffer Register (used in store)
     * @param dataWord The value to store
     */
    public void setMBR(Word dataWord){
        this.state = MemoryControlUnit.STATE_WAITING;
        this.memoryBufferRegister = Word.cloneWord(dataWord);
        
    }
    
    /**
     *
     * @return memoryBufferRegister
     */
    public Word getMBR(){
        return Word.cloneWord(memoryBufferRegister);
    }
    
    /**
     * Set the Memory Access Register (used in get/store)
     * @param addressUnit The addressRaw to get/store
     */    
    public void setMAR(Unit addressUnit){
        this.state = MemoryControlUnit.STATE_WAITING;
        this.memoryAddressRegister = Unit.cloneUnit(addressUnit);
    }   

    /**
     *
     * @return memoryAddressRegister
     */
    public Unit getMAR(){
        return Unit.cloneUnit(memoryAddressRegister);
    }
    
    
    /**
     * Check to see if we're mid-operation this clock cycle
     * @return true/false 
     */
    public boolean isBusy(){
        switch(state){          
            case MemoryControlUnit.STATE_NONE:
                return false;        
            case MemoryControlUnit.STATE_WAITING: 
                Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "Memory error. Likely forgot to signal which operation.");
            default:
                return true;
        }    
    }
    
    public void signalFetch(){      
       this.state = MemoryControlUnit.STATE_PRE_FETCH;
    }
    
    public void signalStore(){
       this.state = MemoryControlUnit.STATE_PRE_STORE;       
    }
    
       
    
    /**
     * Calculates relative addressRaw for memory location from MAR
     * @param address
     * @throws computersimulator.components.MachineFaultException
     * @TODO: 8191 words are addressable via MAR despite only 2048 exist. (see pg 16)... means we need virtual memory?
     * @return Array{bankIndex,cellIndex}
     */
    public int[] calculateActualMemoryLocation(Unit address) throws MachineFaultException {
        // Load the addressRaw in MAR
        int addressRaw = address.getUnsignedValue();
                
        // Decode the Address in MAR      
        
        // Stripe across memory banks to simulate efficient read/write by data line
        int bankIndex = (int)(addressRaw % MemoryControlUnit.BANK_SIZE);
        int cellIndex = (int)Math.floor(addressRaw /MemoryControlUnit.BANK_SIZE);
               
        Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "Calculated Memory Address: "+addressRaw+" as Bank: "+bankIndex+", Cell: "+cellIndex);
        
        if(bankIndex > MemoryControlUnit.BANK_SIZE){
            Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "Memory index["+bankIndex+"]["+cellIndex+"] out of bounds. (Memory Size: ["+MemoryControlUnit.BANK_SIZE+"]["+MemoryControlUnit.BANK_CELLS+"])");
            throw new MachineFaultException(MachineFaultException.ILLEGAL_MEMORY_ADDRESS);       
        }
        
        // Return the result index array
        int[] result = {bankIndex,cellIndex};        
        return result;
    }
    
    /**
     * Engineering console function to read directly from memory
     * @param address
     * @return Word memory value
     * @throws computersimulator.components.MachineFaultException
     */
    public Word engineerFetchByMemoryLocation(Unit address) throws MachineFaultException{       
        Word value;
        if(MemoryControlUnit.ENABLE_CACHE){
            value = cache.engineerFetchWord(address);
        } else {
            int[] addr = this.calculateActualMemoryLocation(address);
            value = new Word(this.memory[addr[0]][addr[1]]);        
        }
        Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "ENGINEER: Fetch Addr: "+address.getUnsignedValue()+"  ---  Value: "+value);        
        
        return value;
    }

    /**
     * Engineering console function to write directly to memory
     * @param address
     * @param value
     * @throws computersimulator.components.MachineFaultException
     */
    public void engineerSetMemoryLocation(Unit address, Word value) throws MachineFaultException{
        if(MemoryControlUnit.ENABLE_CACHE){
            cache.engineerStoreWord(address, value);
        } else {
            int[] addr = this.calculateActualMemoryLocation(address);    
            this.memory[addr[0]][addr[1]] = new Word(value);
        }
        Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "ENGINEER: Set Addr: "+address.getUnsignedValue()+" to  Value: "+value);        
    }       
    
    /**
     * Proxy to cache in order to fetch an address
     * @throws MachineFaultException 
     */
    private void cacheFetchAddressOperation() throws MachineFaultException{
        Word result = cache.fetchWord(memoryAddressRegister);
        if(result!=null){
            this.memoryBufferRegister = result;
            this.resetState();
            Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "-- Fetch MAR("+this.memoryAddressRegister.getUnsignedValue()+"): "+this.memoryBufferRegister);
        } // else cache miss, try next time        
    }    
    
    /**
     * Proxy to cache in order to store a word
     * @throws MachineFaultException 
     */
    private void cacheStoreAddressOperation() throws MachineFaultException{
        Boolean result = cache.storeWord(memoryAddressRegister,memoryBufferRegister);
        if(result==true){            
            this.resetState();
            Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "-- Memory Set - MAR("+this.memoryAddressRegister.getUnsignedValue()+") to "+this.memoryBufferRegister);
        } // else cache miss, try next time        
    }  
    
    
    /**
     * Reset state
     */    
    private void resetState(){   
        this.state = MemoryControlUnit.STATE_NONE;
    }    

    /**
     * Initialize memory banks to zero filled words. 
     */   
    private void initializeMemoryToZero(){
        for (Word[] bank : this.memory) {
            for (int i = 0; i < bank.length; i++) {
                bank[i] = new Word(0); // set to zero
            }
        }
    }
    
    /**
     * Get the start address of a block based off the address and count
     * @param address
     * @param count
     * @return location
     * @throws MachineFaultException 
     */
    public Integer[] getCacheBlockStart(Unit address, int count) throws MachineFaultException{
        int[] addr = this.calculateActualMemoryLocation(address);

        int blockID = (int)Math.floor(addr[1] / count);
        
        Integer[] result = new Integer[2];
        result[0] = addr[0];
        result[1] = blockID;
        
        return result;
        
    }
    
    /**
     * Get an actual cache block from the blockStart position
     * @param blockStart
     * @param count
     * @return cache block
     */
    public Word[] getCacheBlock(Integer[] blockStart, int count){
    
        Word[] results = new Word[count];
        
        int j=0;
        for(int i=blockStart[1];i<blockStart[1]+count;i++){
            results[j] = new Word(this.memory[blockStart[0]][i]);
            j++;
        }
        
        return results;
    }
    
    /**
     * Write a cache block to memory using a block location
     * @param block
     * @param blockStart 
     */
    public void writeCacheBlock(Word[] block, Integer[] blockStart){
        int j=0;
        for(int i=blockStart[1];i<blockStart[1]+block.length;i++){
            this.memory[blockStart[0]][i] = new Word(block[j]);
            j++;
        }
    }
    
    
    /**
     * fetchAddressOperation - This fetches an addressRaw specified by MAR, and
 puts the contents of that memory location into MBR. 
     * Private because it is called by clockCycle.
     */    
    private void fetchAddressOperation(){
        try {
            // Load and Decode the Address in MAR
            int[] addr = this.calculateActualMemoryLocation(this.memoryAddressRegister);
            int bankIndex = addr[0]; 
            int cellIndex = addr[1]; 
        
            // Copy the contents of that memory location into the MBR            
            this.memoryBufferRegister = new Word(this.memory[bankIndex][cellIndex]);            
            Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "-- Fetch MAR("+this.memoryAddressRegister.getUnsignedValue()+"): "+this.memoryBufferRegister);
            this.resetState();
        } catch(Exception e){
            //@TODO: Handle bad addressRaw (virtual memory?)
            Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "-- Bad Address: "+this.memoryAddressRegister+" -> "+e.getMessage());
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
            int[] addr = this.calculateActualMemoryLocation(this.memoryAddressRegister);
            int bankIndex = addr[0]; 
            int cellIndex = addr[1];        

            //Copy the value from MDR to Memory                
            this.memory[bankIndex][cellIndex] = new Word(this.memoryBufferRegister);
            Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "-- Memory Set - MAR("+this.memoryAddressRegister.getUnsignedValue()+") to "+this.memoryBufferRegister);
            this.resetState();
        } catch(Exception e){
            //@TODO: Handle bad addressRaw (virtual memory?)
            Logger.getLogger(MemoryControlUnit.class.getName()).log(Level.FINER, "-- Bad Address: "+this.memoryAddressRegister+" -> "+e.getMessage());
        }                
    }    
    
}
