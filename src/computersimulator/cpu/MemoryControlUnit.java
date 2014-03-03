package computersimulator.cpu;

import computersimulator.components.*;

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
        initializeMemoryToZero(); // Upon powering up, set all elements of memory to zero
        
        cache = new Cache(this);
        
        memoryAddressRegister = new Unit(13);
        memoryBufferRegister = new Word();
        
        resetState();
    }
    
    /**
     * Clock cycle. This is the main function which causes the Memory
     * Unit to do work. This serves as a publicly accessible method, but delegates
     * to the fetch/store controller.
     */
    @Override
    public void clockCycle(){
        cache.clockCycle();
        switch(state){                                   
            case MemoryControlUnit.STATE_PRE_STORE:                
                this.cacheStoreAddressOperation();
                break;
                
            case MemoryControlUnit.STATE_PRE_FETCH:                                                     
                this.cacheFetchAddressOperation();
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
        setMBR(res);
    }
    
    /**
     * Set the Memory Buffer Register (used in store)
     * @param dataWord The value to store
     */
    public void setMBR(Word dataWord){
        this.state = MemoryControlUnit.STATE_WAITING;
        this.memoryBufferRegister = dataWord; 
    }
    
    /**
     *
     * @return memoryBufferRegister
     */
    public Word getMBR(){
        return this.memoryBufferRegister;
    }
    
    /**
     * Set the Memory Access Register (used in get/store)
     * @param addressUnit The addressRaw to get/store
     */    
    public void setMAR(Unit addressUnit){
        this.state = MemoryControlUnit.STATE_WAITING;
        this.memoryAddressRegister = addressUnit;          
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
            case MemoryControlUnit.STATE_NONE:
                return false;        
            case MemoryControlUnit.STATE_WAITING:
                System.out.println("Memory error. Likely forgot to signal which operation.");
            default:
                return true;
        }    
    }
    
    public void signalFetch(){      
       this.state = MemoryControlUnit.STATE_PRE_FETCH;
       //@TODO : attempt cache read and clear the flag if successful
    }
    
    public void signalStore(){
       this.state = MemoryControlUnit.STATE_PRE_STORE;
       //@TODO : attempt cache write and clear the flag if successful
       
    }
    
       
    
    /**
     * Calculates relative addressRaw for memory location from MAR
     * @TODO: 8191 words are addressable via MAR despite only 2048 exist. (see pg 16)... means we need virtual memory?
     * @return Array{bankIndex,cellIndex}
     */
    public int[] calculateActualMemoryLocation(Unit address) {
        // Load the addressRaw in MAR
        int addressRaw = address.getUnsignedValue();
                
        // Decode the Address in MAR      
        
        // Stripe across memory banks to simulate efficient read/write by data line
        int bankIndex = (int)(addressRaw % MemoryControlUnit.BANK_SIZE);
        int cellIndex = (int)Math.floor(addressRaw /MemoryControlUnit.BANK_SIZE);
               
        System.out.println("Calculated Memory Address: "+addressRaw+" as Bank: "+bankIndex+", Cell: "+cellIndex);
        
        if(bankIndex > MemoryControlUnit.BANK_SIZE){
            //throw new Exception("Memory index["+bankIndex+"]["+cellIndex+"] out of bounds. (Memory Size: ["+MemoryControlUnit.BANK_SIZE+"]["+MemoryControlUnit.BANK_CELLS+"])");
            System.out.println("Memory index["+bankIndex+"]["+cellIndex+"] out of bounds. (Memory Size: ["+MemoryControlUnit.BANK_SIZE+"]["+MemoryControlUnit.BANK_CELLS+"])");
            return null; //@TODO Switch back to exception
        }
        
        // Return the result index array
        int[] result = {bankIndex,cellIndex};        
        return result;
    }
    
    /**
     * Engineering console function to read directly from memory
     * @param address
     * @return Word memory value
     */
    public Word engineerFetchByMemoryLocation(Unit address){       
        Word value = cache.engineerFetchWord(address);
        System.out.println("ENGINEER: Fetch Addr: "+address.getUnsignedValue()+"  ---  Value: "+value);        
        
        return value;
    }

    /**
     * Engineering console function to write directly to memory
     * @param address
     * @param value
     */
    public void engineerSetMemoryLocation(Unit address, Word value){
        cache.engineerStoreWord(address, value);
        System.out.println("ENGINEER: Set Addr: "+address.getUnsignedValue()+" to  Value: "+value);        
    }       
    
    private void cacheFetchAddressOperation(){
        Word result = cache.fetchWord(memoryAddressRegister);
        if(result!=null){
            this.memoryBufferRegister = result;
            this.resetState();
            System.out.println("-- Fetch MAR("+this.memoryAddressRegister.getUnsignedValue()+"): "+this.memoryBufferRegister);
        } // else cache miss, try next time        
    }    
    
    private void cacheStoreAddressOperation(){
        Boolean result = cache.storeWord(memoryAddressRegister,memoryBufferRegister);
        if(result==true){            
            this.resetState();
            System.out.println("-- Memory Set - MAR("+this.memoryAddressRegister.getUnsignedValue()+") to "+this.memoryBufferRegister);
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
    
    public Integer[] getCacheBlockStart(Unit address, int count){
        int[] addr = this.calculateActualMemoryLocation(address);

        int blockID = (int)Math.floor(addr[1] / count);
        
        Integer[] result = new Integer[2];
        result[0] = addr[0];
        result[1] = blockID;
        
        return result;
        
    }
    
    public Word[] getCacheBlock(Integer[] blockStart, int count){
    
        Word[] results = new Word[count];
        
        int j=0;
        for(int i=blockStart[1];i<blockStart[1]+count;i++){
            results[j] = this.memory[blockStart[0]][i];
            //System.out.println("Copied("+j+"): "+results[j]);
            j++;
        }
        
        return results;
    }
    
    public void writeCacheBlock(Word[] block, Integer[] blockStart){
        int j=0;
        for(int i=blockStart[1];i<blockStart[1]+block.length;i++){
            this.memory[blockStart[0]][i] = block[j++];
        }
    }
    
}
