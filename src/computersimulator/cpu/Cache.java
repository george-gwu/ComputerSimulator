
package computersimulator.cpu;

import computersimulator.components.MachineFaultException;
import computersimulator.components.Unit;
import computersimulator.components.Word;
import java.util.HashMap;

/**
 * Fully associative cache with a least recently used replacement policy
 * @author george
 */
public class Cache implements IClockCycle {
    
    private final static int BLOCK_SIZE = 32;
    private final static int CACHE_SIZE = 8;
        
    private final Word[][] cache;
    
    private String[] tags;    
    private boolean[] dirty;
    private Long[] lastUsed;
    private HashMap<String,Integer[]> location;
    
    
    private MemoryControlUnit memory;
    
    
    public Cache(MemoryControlUnit mem){
        cache = new Word[Cache.CACHE_SIZE][Cache.BLOCK_SIZE];            
        
        tags = new String[Cache.CACHE_SIZE];
        dirty = new boolean[Cache.CACHE_SIZE];
        lastUsed = new Long[Cache.CACHE_SIZE];
        location = new HashMap();
        
        //initialize cache table rows
        for(int i=0; i<Cache.CACHE_SIZE;i++){
            tags[i]=null;
            dirty[i]=false;
            lastUsed[i]=null;
        }
        
        this.memory=mem;
    }
    
     /**
     * Clock cycle. This is the main function which causes the Cache
     * Unit to do work. 
     */
    @Override
    public void clockCycle(){
     
    }

    /**
     * Primary interface for memory fetch, will return a word if it is a cache hit
     * @param address
     * @return
     * @throws computersimulator.components.MachineFaultException
     */
    public Word fetchWord(Unit address) throws MachineFaultException{        
        Integer[] block = calculateBlockFromAddress(address);
        String tag = this.calculateTagFromBlockID(block);
        System.out.println("[Cache]: Read requested for M("+address.getUnsignedValue()+") -> Block: "+tag);
        if(this.isBlockAvailable(tag)){  // CACHE HIT!
            System.out.println("[Cache]: HIT during fetch ("+tag+")");
            int blockID = this.getBlockLocation(tag);            
            
            
            int[] rawMemoryAddress = memory.calculateActualMemoryLocation(address);           
            int cacheAddress = rawMemoryAddress[1] % Cache.BLOCK_SIZE; 
            
            lastUsed[blockID] = System.currentTimeMillis();
            
            return this.cache[blockID][cacheAddress];
            
        } else { // CACHE miss, do fetch for next cycle
            System.out.println("[Cache]: MISS during fetch ("+tag+")");            
            
            this.fetchBlock(block);            
            
            return null; // queued (try again next cycle)
        }                
    }
    
  /**
     * Primary interface for memory fetch, will return a word if it is a cache hit
     * @param address
     * @return
     * @throws computersimulator.components.MachineFaultException
     */
    public Word engineerFetchWord(Unit address) throws MachineFaultException{        
        Integer[] block = calculateBlockFromAddress(address);
        String tag = this.calculateTagFromBlockID(block);
        
        if(!this.isBlockAvailable(tag)){  
            this.fetchBlock(block);   
        }
        
        int blockID = this.getBlockLocation(tag);
        lastUsed[blockID] = System.currentTimeMillis();

        int[] rawMemoryAddress = memory.calculateActualMemoryLocation(address);           
        int cacheAddress = rawMemoryAddress[1] % Cache.BLOCK_SIZE; 

        return this.cache[blockID][cacheAddress];

    }

    /**
     * Attempts to store a word if it is in cache, otherwise returns false but pulls into cache
     * @param address
     * @param value
     * @return true/false on success
     * @throws computersimulator.components.MachineFaultException
     */
    public Boolean storeWord(Unit address, Word value) throws MachineFaultException{        
      Integer[] block = calculateBlockFromAddress(address);
      String tag = this.calculateTagFromBlockID(block);
      System.out.println("[Cache]: Store requested for M("+address.getUnsignedValue()+") -> Block: "+tag);
        if(this.isBlockAvailable(tag)){  // CACHE HIT!
            System.out.println("[Cache]: HIT during store ("+tag+")");
            int blockID = this.getBlockLocation(tag);            
            
            int[] rawMemoryAddress = memory.calculateActualMemoryLocation(address);           
            int cacheAddress = rawMemoryAddress[1] % Cache.BLOCK_SIZE;           
            
            lastUsed[blockID] = System.currentTimeMillis();
            dirty[blockID] = true;
            
            this.cache[blockID][cacheAddress]=value;
            
            
            return true;
            
        } else { // CACHE miss, do fetch for next cycle
            System.out.println("[Cache]: MISS during store ("+tag+")");            
            
            this.fetchBlock(block);            
            
            return false; // queued (try again next cycle)
        } 
    }

    /**
     * Stores a word in cache, use for debugging, bypassing clock cycles
     * @param address
     * @param value
     * @throws computersimulator.components.MachineFaultException
     */
    public void engineerStoreWord(Unit address, Word value) throws MachineFaultException{        
      Integer[] block = calculateBlockFromAddress(address);
      String tag = this.calculateTagFromBlockID(block);
      System.out.println("[Cache]: Store requested for M("+address.getUnsignedValue()+") -> Block: "+tag);
        if(!this.isBlockAvailable(tag)){             
            this.fetchBlock(block);            
        }
        int blockID = this.getBlockLocation(tag);            
        
        int[] rawMemoryAddress = memory.calculateActualMemoryLocation(address);           
        int cacheAddress = rawMemoryAddress[1] % Cache.BLOCK_SIZE; 

        lastUsed[blockID] = System.currentTimeMillis();
        dirty[blockID] = true;

        this.cache[blockID][cacheAddress]=value;
            
    }    
    
    /**
     * Test if word is in cache
     * @param address
     * @return
     * @throws computersimulator.components.MachineFaultException
     */
    public Boolean testWord(Unit address) throws MachineFaultException{
        String tag = this.calculateTagFromBlockID(calculateBlockFromAddress(address));
        return this.isBlockAvailable(tag);
    }
    
    /**
     * Fetch a block from memory and store it into the least recently used location
     * @param block 
     */
    private void fetchBlock(Integer[] block){
        int freeBlockID = this.getFreeBlock();
        
        Word[] newBlock = memory.getCacheBlock(block, Cache.BLOCK_SIZE);
        
        cache[freeBlockID] = newBlock;
        lastUsed[freeBlockID] = System.currentTimeMillis();
        dirty[freeBlockID] = false;
        tags[freeBlockID] = calculateTagFromBlockID(block);
        location.put(tags[freeBlockID], block);        
        
        System.out.println("[Cache]: Fetched Block "+tags[freeBlockID]+" into location: "+freeBlockID);
        
    }
    
    /**
     * Get an unused block, or if none exists, trigger a write-back
     * @return blockID
     */
    private Integer getFreeBlock(){
        for(int i=0;i<Cache.CACHE_SIZE;i++){
            if(lastUsed[i]==null){ // unused block is free
                return i;
            }
        }
        
        Integer oldest=0;
        for(int j=1;j<Cache.CACHE_SIZE;j++){
            if(lastUsed[j] < lastUsed[oldest]){
                oldest = j;
            }
        }
        
        this.cleanBlock(oldest);
        
        return oldest;
        
    }   
    
    /**
     * Cleans a block from cache and triggers a write-back if needed
     * @param blockID 
     */
    private void cleanBlock(Integer blockID){
        System.out.println("[Cache]: Freeing Cache Block "+blockID);
        if(dirty[blockID]){
            memory.writeCacheBlock(cache[blockID], location.get(tags[blockID]));            
            dirty[blockID]=false;
            System.out.println("[Cache]: Wrote back to memory because it was dirty.");
        } 
        lastUsed[blockID] = null;
        location.put(tags[blockID], null); 
        tags[blockID] = null;
        
    }
    
    
    /**
     * Check if a tag is in cache
     * @param tag
     * @return
     */
    private boolean isBlockAvailable(String tag){
        return (this.getBlockLocation(tag)!=null);
    }
    
    /**
     * Locate tag in cache
     * @param tag
     * @return
     */
    private Integer getBlockLocation(String tag){
        Integer loc = null;
        for(int i=0;i<Cache.CACHE_SIZE;i++){
            String cacheTag = tags[i];
            if(cacheTag!=null && cacheTag.equals(tag)){
                loc=i;
            }
        }
        return loc;
    }
    
    /**
     * Test whether an address is in cache already
     * @param address
     * @return 
     * @throws computersimulator.components.MachineFaultException 
     */    
    public boolean isWordAvailable(Unit address) throws MachineFaultException{
        String tag = this.calculateTagFromBlockID(calculateBlockFromAddress(address));
                
        return isBlockAvailable(tag);
    } 
    
    
    private Integer[] calculateBlockFromAddress(Unit address) throws MachineFaultException{
        /*        
            FETCH(34)  ——  M[1][4]  —— cache block 1,0
            FETCH(27)  ——  M[2][3]  —— cache block 2,0
            FETCH(35)  ——  M[2][4]  —— cache block 2,0
        */
        return memory.getCacheBlockStart(address, Cache.BLOCK_SIZE);
    }
    
    /**
     * Calculates a tag... for now just comma delimited for debugging ease
     * @param blockLocation
     * @return 
     */
    private String calculateTagFromBlockID(Integer[] blockLocation){               
        return blockLocation[0]+","+blockLocation[1];       
    }

    
}
