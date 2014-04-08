
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
        ROM.put(01, "00000000000000101010");       // Machine Fault Handler -> 42
        
        ROM.put(10, "011111 00 10 1 1 000 10100"); //10: SRC(2,20,1,1)  -- reset ECX to 0 (index value)        
        ROM.put(11, "011111 00 11 1 1 000 10100"); //11: SRC(3,20,1,1)  -- reset EDX to 0 (IO Status Ready)        
        ROM.put(12, "000010 00 10 0 0 00000110" ); //12: STR(2,0,6)     -- set M(6) to ECX        
        ROM.put(13, "101001 00 01 0 0 00000110" ); //13: LDX(0, 1, 6)   -- Set X(1) from M(6) (copied from ECX)                
        ROM.put(14, "111101 00 00 000000 0010"  ); //14: L1: IN(0, 2)   -- read word from CardReader to EAX        
        ROM.put(15, "000010 01 00 0 0 01000000" ); //15: STR(0,1,64i1)  -- store EAX to ADDR+X1 (ADDR=64)       
        ROM.put(16, "101011 00 01 0 0 00000000" ); //16: INX(1)         -- X(1)++
        ROM.put(17, "111111 00 00 000000 0010"  ); //17: CHK(0, 2)      -- Check status of Card Reader to EAX        
        ROM.put(18, "010110 00 11 0 0 00000000" ); //18: TRR(0, 3)      -- Test EAX against EDX (IO Status Ready -- not done)        
        ROM.put(19, "001100 00 11 0 0 00001110" ); //19: JCC(3,x, L1)   -- JMP to L1 if EAX=EDX --- L1=14   
        ROM.put(20, "001101 00 00 0 0 01000000" ); //20: JMP(64)        -- else: launch program by transferring control to 64
        /***************** Error Handler ************************************/               
        ROM.put(28, "000001 00 11 0 0 00000010"); // 28: LDR(3, 0, 2)  -- restore PC to EDX (used on 41)
        ROM.put(29, "011111 00 00 1 1 000 10100"); //29: SRC(0,20,1,1)  -- reset EAX to 0
        ROM.put(30, "011111 00 01 1 1 000 10100"); //30: SRC(1,20,1,1)  -- reset EBX to 0
        ROM.put(31, "011111 00 10 1 1 000 10100"); //31: SRC(2,20,1,1)  -- reset ECX to 0
        ROM.put(32, "000110 00 00 0 0 01000101");  //32: AIR(0,72)      -- Set EAX to 69 ('E')
        ROM.put(33, "000110 00 01 0 0 01110010");  //33: AIR(1,105)     -- Set EBX to 114 ('r')
        ROM.put(34, "000110 00 10 0 0 01101111");  //34: AIR(2,13)      -- Set ECX to 111 ('o')
        ROM.put(35, "111110 00 00 000000 0001");   //35: OUT(0,1)       -- Output EAX ('E')
        ROM.put(36, "111110 00 01 000000 0001");   //36: OUT(1,1)       -- Output EBX ('r')
        ROM.put(37, "111110 00 01 000000 0001");   //37: OUT(1,1)       -- Output EBX ('r')
        ROM.put(38, "111110 00 10 000000 0001");   //38: OUT(2,1)       -- Output ECX ('o')
        ROM.put(39, "111110 00 01 000000 0001");   //39: OUT(1,1)       -- Output EBX ('r')
        ROM.put(40, "000010 00 11 0 0 00000110");  // 40: STR(3,0,6)    -- Store EDX to M(6)
        ROM.put(41, "001101 00 00 1 0 00000110");  // 41: JMP(0,6)      -- JMP to c(M(6))
        // Machine error entry point
        ROM.put(42, "000001 00 11 0 0 00000100"); // 42: LDR(3, 0, 4)  -- restore PC to EDX (used on 41)
        ROM.put(43, "011111 00 00 1 1 000 10100"); //43: SRC(0,20,1,1)  -- reset EAX to 0
        ROM.put(44, "000110 00 00 0 0 01001101");  //44: AIR(0,72)      -- Set EAX to 77 ('M')    
        ROM.put(45, "111110 00 00 000000 0001");   //45: OUT(0,1)       -- Output EAX ('M') 
        ROM.put(46, "001101 00 00 0 0 00011101" ); //46: JMP(29)        -- Jump to 29 "MError"
        // Create Trap Table Defaults
        for(int i=47;i<=63;i++){
            ROM.put(i, "001101 00 00 0 0 00011100"); // Set TRAP table locs all to 28 by default
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
