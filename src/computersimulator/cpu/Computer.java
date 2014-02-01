
package computersimulator.cpu;

import computersimulator.components.*;

/**
 *
 * @author george
 */
public class Computer implements IClockCycle {
    
    private CentralProcessingUnit cpu;
    private MemoryControlUnit memory;
    private InputOutputController io;   

    public Computer() {        
        memory = new MemoryControlUnit();  
        cpu = new CentralProcessingUnit(memory); // contains ALU,  ControlUnit      
        io = new InputOutputController();
    }   
    
    /**
     * Clock Cycle for Computer
     * @throws Exception 
     */
    @Override
    public final void clockCycle() throws Exception{
            System.out.println("-------- CLOCK CYCLE --------");
            this.cpu.clockCycle();
            this.memory.clockCycle();
            this.io.clockCycle();                
    }

    /**
     *
     * @return CPU
     */
    public CentralProcessingUnit getCpu() {
        return cpu;
    }

    /**
     *
     * @return Memory Instance
     */
    public MemoryControlUnit getMemory() {
        return memory;
    }

    /**
     *
     * @return IO instance
     */
    public InputOutputController getIO() {
        return io;
    }

    /**
     * Since each Register contains a reference to a particular unit/word, 
     * we can't maintain that reference and instead must look it up every time.
     * @param name Name of Register/Variable
     */
    public Unit getComponentValueByName(String name) throws Exception{
        switch(name){
            case "R0":
                return this.getCpu().getControlUnit().getGpRegisters()[0];
            case "R1":
                return this.getCpu().getControlUnit().getGpRegisters()[1];
            case "R2":
                return this.getCpu().getControlUnit().getGpRegisters()[2];                
            case "R3":
                return this.getCpu().getControlUnit().getGpRegisters()[3];                
            case "X1":
                return this.getCpu().getControlUnit().getIndexRegisters()[0];
            case "X2":
                return this.getCpu().getControlUnit().getIndexRegisters()[1];
            case "X3":
                return this.getCpu().getControlUnit().getIndexRegisters()[2];
            case "MAR":
                return this.getMemory().getMAR();
            case "MBR":
                return this.getMemory().getMBR();
            case "PC":
                return this.getCpu().getControlUnit().getProgramCounter();
            case "CC":
                return this.getCpu().getALU().getConditionCode();
            case "IR":
                return this.getCpu().getControlUnit().getInstructionRegister();
            default:
                throw new Exception("Uknown Component");
               
        }                  
    }
    
    
    /*****
     * 
// M(15) <- STR, 3, 0, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 15), Word.WordFromBinaryString("000010 11 00 1 0 00110100"));
                    
        boolean running = true; // @TODO hook this to IPL button
        
        this.memory.engineerSetMemoryLocation(new Unit(13, 15), Word.WordFromBinaryString("000010 11 00 1 0 00110100"));
        cpu.getControlUnit().setPC(new Unit(13,15));
            
        do {
            try {
                this.clockCycle();
            } catch(HaltSystemException eHalt){
                System.out.println("System HALT.");
                running=false;
            } catch(Exception e){
                System.out.println("Error: "+ e);
            }
        } while(running==true);

//        LDR r, x, address [,I]	
//        which says “load R3 from address 52 indirect with no indexing”  
//        Let location 52 contain 100, and location 100 contain 1023
//        The format in binary looks like this:
//        000001 11 00 1 0 00110100
        
        // M(16) <- LDR, 3, 0, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 16), Word.WordFromBinaryString("000001 11 00 1 0 00110100"));
        
//        LDA r, x, address[,I]
//        which says "Load Register with Address"
        // M(17) <- LDA, 3, 0, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 17), Word.WordFromBinaryString("000011 11 00 1 0 00110100"));        
        
//        LDX x, address[,I]
//        which says "Load Index Register from Memory"
//        Let the memory location be 52.
        // M(18) <- LDX, 3, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 18), Word.WordFromBinaryString("101001 00 11 1 0 00110100"));
               
//        STX x, address[,I]
//        which says "Store Index Register to Memory"
//        Let the memory location be 52.

        // M(19) <- STX, 3, 52, I
        this.memory.engineerSetMemoryLocation(new Unit(13, 19), Word.WordFromBinaryString("101010 00 11 1 0 00110100"));
        
        // set PC to 15 for testing, this will increment until no more instructions exist, then crash
        cpu.getControlUnit().setPC(new Unit(13,15));
* 
     */
    
}
