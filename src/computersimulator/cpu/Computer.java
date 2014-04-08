
package computersimulator.cpu;

import computersimulator.components.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Computer is the primary class used by the simulator. The core business logic
 * lives inside the components of computer. The primary components are CPU, IO,
 * and Memory.  The computer also is the primary controller of the clock cycle,
 * but delegates to the primary components.  Also, because our primary data 
 * type is an object, computer has one setter and getter for each register. This
 * prevents us from accidentally operating on stale references that are no longer
 * valid.
 */
public class Computer implements IClockCycle {
    
    private CentralProcessingUnit cpu;
    private MemoryControlUnit memory;
    private InputOutputController io;   
    
        
    private int runmode = 0;    

    public static final int RUNMODE_MICROSTEP=0;
    public static final int RUNMODE_STEP=1;
    public static final int RUNMODE_RUN=2;    
    

    public Computer() {        
        memory = new MemoryControlUnit();  
        io = new InputOutputController();
        cpu = new CentralProcessingUnit(memory, io); // contains ALU,  ControlUnit      
    }   
    
    /**
     * Clock Cycle for Computer
     * @throws Exception 
     */
    @Override
    public final void clockCycle() throws Exception {
        try {
            this.cpu.clockCycle();
            this.memory.clockCycle();                        
        } catch(MachineFaultException e){
            this.cpu.getControlUnit().signalMachineFault(e.getFaultID());
        }
    }
    
    /** 
     * IPL - You will need a ROM that contains the simple loader. When you press 
     * the IPL button on the console, the ROM contents are read into memory and 
     * control is transferred to the first instruction of the ROM Loader program. 
     * Your ROM Loader should read a boot program from a virtual card reader and 
     * place them into memory in a location you designate. The ROM Loader then 
     * transfers control to the program which executes until completion or error. 
     * 
     * IPL also loads the error handlers into memory.
     */
    public void IPL(){     
        
        this.io.resetIOController();
        this.memory.resetMemory();
        
        /*** Pseudocode for ROM bootloader 
         * Reads a file to memory starting at M(64) to EOF
         * 
         * ADDR=64, X1=0
         * L1: R0 = io.readLine()
         * memory.set(ADDR+X1, R0)
         * X1++
         * checkForMore = io.checkStatus()
         * test checkForMore
         * jcc checkForMore to L1
         * jmp 64
         */               
        HashMap<Integer,String> ROM = new HashMap<>();                                
        /************* Assembly for bootloader **************/
        ROM.put(00, "00000000000000101111");       // Trap Handler Table Start Position -> 47
        ROM.put(01, "00000000000000101011");       // Machine Fault Handler -> 42
        ROM.put( 9, "11001100110011001100");       // SECTION_TAG (Used for finding sections)
        ROM.put(10, "000011 00 10 0 0 00000000");  //10: LDA(2,0,0,0)   -- reset ECX to 0 (index value)          
        ROM.put(11, "000011 00 11 0 0 00000000");  //11: LDA(3,0,0,0)   -- reset EDX to 0 (IO Status Ready)        
        ROM.put(12, "000001 00 01 0 0 00001001");  //12: LDR(1,0,9)     -- LOAD M(9) to EBX (SECTION_TAG)
        ROM.put(13, "000010 00 10 0 0 00000110" ); //13: STR(2,0,6)     -- set M(6) to ECX        
        ROM.put(14, "101001 00 01 0 0 00000110" ); //14: LDX(0, 1, 6)   -- Set X(1) from M(6) (copied from ECX)                
        ROM.put(15, "111101 00 00 000000 0010"  ); //15: L1: IN(0, 2)   -- read word from CardReader to EAX                
        ROM.put(16, "010110 00 01 0 0 00000000" ); //16: TRR(0, 1)      -- Test EAX against EBX (SECTION_TAG)        
        ROM.put(17, "001100 00 11 0 0 00011000" ); //17: JCC(3,x, L2)   -- JMP to L2 if EAX=EBX --- L2=24 
        ROM.put(18, "000010 01 00 0 0 01000000" ); //18: L3: STR(0,1,64i1)  -- store EAX to ADDR+X1 (ADDR=64)       
        ROM.put(19, "101011 00 01 0 0 00000000" ); //19: INX(1)         -- X(1)++
        ROM.put(20, "111111 00 00 000000 0010"  ); //20: CHK(0, 2)      -- Check status of Card Reader to EAX        
        ROM.put(21, "010110 00 11 0 0 00000000" ); //21: TRR(0, 3)      -- Test EAX against EDX (IO Status Ready -- not done)        
        ROM.put(22, "001100 00 11 0 0 00001111" ); //22: JCC(3,x, L1)   -- JMP to L1 if EAX=EDX --- L1=15  
        ROM.put(23, "001101 00 00 0 0 01000000" ); //23: JMP(64)        -- else: launch program by transferring control to 64        
        ROM.put(24, "000011 01 10 0 0 01000000" ); //24: L2: LDA(1,1,64i1)  -- Load ADDR+X1 to ECX
        ROM.put(25, "000010 10 10 0 0 00001000" ); //25: STR(1,2,8)     -- Store ECX to 8+X2
        ROM.put(26, "101011 00 10 0 0 00000000" ); //26: INX(2)         -- X(2)++
        ROM.put(27, "001101 00 00 0 0 00010010" ); //27: JMP(18)        -- JMP(l3)        
        /***************** Error Handler ************************************/               
        ROM.put(32, "000001 00 11 0 0 00000010");  //32: LDR(3, 0, 2)  -- restore PC to EDX (used on 41)        
        ROM.put(33, "000011 00 00 0 0 01000101");  //33: LDA(0,72)      -- Set EAX to 69 ('E')
        ROM.put(34, "000011 00 01 0 0 01110010");  //34: LDA(1,105)     -- Set EBX to 114 ('r')
        ROM.put(35, "000011 00 10 0 0 01101111");  //35: LDA(2,13)      -- Set ECX to 111 ('o')
        ROM.put(36, "111110 00 00 000000 0001");   //36: OUT(0,1)       -- Output EAX ('E')
        ROM.put(37, "111110 00 01 000000 0001");   //37: OUT(1,1)       -- Output EBX ('r')
        ROM.put(38, "111110 00 01 000000 0001");   //38: OUT(1,1)       -- Output EBX ('r')
        ROM.put(39, "111110 00 10 000000 0001");   //39: OUT(2,1)       -- Output ECX ('o')
        ROM.put(40, "111110 00 01 000000 0001");   //40: OUT(1,1)       -- Output EBX ('r')
        ROM.put(41, "000010 00 11 0 0 00000110");  //41: STR(3,0,6)    -- Store EDX to M(6)
        ROM.put(42, "001101 00 00 1 0 00000110");  //42: JMP(0,6)      -- JMP to c(M(6))
        // Machine error entry point
        ROM.put(43, "000001 00 11 0 0 00000100"); // 43: LDR(3, 0, 4)  -- restore PC to EDX (used on 41)        
        ROM.put(44, "000011 00 00 0 0 01001101");  //44: LDA(0,72)      -- Set EAX to 77 ('M')    
        ROM.put(45, "111110 00 00 000000 0001");   //45: OUT(0,1)       -- Output EAX ('M') 
        ROM.put(46, "001101 00 00 0 0 00100001" ); //46: JMP(33)        -- Jump to 33 "MError"
        // Create Trap Table Defaults
        for(int i=47;i<=63;i++){
            ROM.put(i, "001101 00 00 0 0 00100000"); // Set TRAP table locs all to 32 by default
        }
            
        // Read ROM contents into memory
        for (Map.Entry romEntry : ROM.entrySet()) {            
            try {
                this.getMemory().engineerSetMemoryLocation(new Unit(13, (int)romEntry.getKey()), Word.WordFromBinaryString((String)romEntry.getValue()));
            } catch (MachineFaultException ex) {
                Logger.getLogger(Computer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //Transfer Control to ROM Bootloader
        this.getCpu().getControlUnit().setProgramCounter(new Unit(13, 10)); // Start at 10        
        this.cpu.setRunning(true);          
    }
    

    public void setRunmode(int runmode) {
        this.runmode = runmode;
    }    

    public int getRunmode() {
        return runmode;
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
     * @return Unit value
     */
    public Unit getComponentValueByName(String name){
        switch(name){
            case "R0":
                return this.getCpu().getControlUnit().getGeneralPurposeRegister(0);
            case "R1":
                return this.getCpu().getControlUnit().getGeneralPurposeRegister(1);
            case "R2":
                return this.getCpu().getControlUnit().getGeneralPurposeRegister(2);        
            case "R3":
                return this.getCpu().getControlUnit().getGeneralPurposeRegister(3);
            case "X1":
                return this.getCpu().getControlUnit().getIndexRegister(1);
            case "X2":
                return this.getCpu().getControlUnit().getIndexRegister(2);
            case "X3":
                return this.getCpu().getControlUnit().getIndexRegister(3);
            case "MAR":
                return this.getMemory().getMAR();
            case "MBR":
                return this.getMemory().getMBR();
            case "PC":
                return this.getCpu().getControlUnit().getProgramCounter();
            case "CC":
                return this.getCpu().getControlUnit().getConditionCodeRegister();
            case "IR":
                return this.getCpu().getControlUnit().getInstructionRegister();
            default:
                return new Unit(13,0);
               
        }                  
    }
    
    /**
     * Since each Register contains a reference to a particular unit/word, 
     * we can't maintain that reference and instead must look it up every time.
     * @param name Name of Register/Variable
     * @param deposit Unit to Deposit
     */
    public void setComponentValueByName(String name, Unit deposit){
        
        switch(name){
            case "R0":
                this.getCpu().getControlUnit().getGeneralPurposeRegisters()[0].setValueBinary(deposit.getBinaryString());
                break;
            case "R1":
                this.getCpu().getControlUnit().getGeneralPurposeRegisters()[1].setValueBinary(deposit.getBinaryString());
                break;
            case "R2":
                this.getCpu().getControlUnit().getGeneralPurposeRegisters()[2].setValueBinary(deposit.getBinaryString());               
                break;
            case "R3":
                this.getCpu().getControlUnit().getGeneralPurposeRegisters()[3].setValueBinary(deposit.getBinaryString());                
                break;
            case "X1":
                this.getCpu().getControlUnit().getIndexRegisters()[0].setValueBinary(deposit.getBinaryString());
                break;
            case "X2":
                this.getCpu().getControlUnit().getIndexRegisters()[1].setValueBinary(deposit.getBinaryString());
                break;
            case "X3":
                this.getCpu().getControlUnit().getIndexRegisters()[2].setValueBinary(deposit.getBinaryString());
                break;
            case "MAR":                
                Unit depositUnit = new Unit(13);
                depositUnit.setValueBinary(deposit.getBinaryString());
                this.getMemory().setMAR(depositUnit);
                break;
            case "MBR":
                Word depositWord = new Word(deposit);
                this.getMemory().setMBR(depositWord);
                break;
            case "PC":
                this.getCpu().getControlUnit().getProgramCounter().setValueBinary(deposit.getBinaryString());
                break;
            case "CC":
                // This doesn't accept deposits
                break;
            case "IR":                
                this.getCpu().getControlUnit().getInstructionRegister().setValueBinary(deposit.getBinaryString());
                break;
        }                  
    }             
}
