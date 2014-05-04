package computersimulator.cpu;

import computersimulator.components.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It is the task of the control unit to fetch the next instruction from 
 *   memory to be executed, decode it (i.e., determine what is to be done), and 
 *   execute it by issuing the appropriate command to the ALU, memory, and the 
 *   I/O controllers.
 * 
 */
public class ControlUnit implements IClockCycle {

    // PC	13 bits	Program Counter: address of next instruction to be executed
    private Unit programCounter;
    
    //IR	20 bits	Instruction Register: holds the instruction to be executed
    private Word instructionRegister;
    
    private HashMap<String,Unit> instructionRegisterDecoded;
    
    //MSR	20 bits	Machine Status Register: certain bits record the status of the health of the machine
    private Word machineStatusRegister;

    //MFR	4 bits	Machine Fault Register: contains the ID code if a machine fault after it occurs
    private Unit machineFaultRegister;
    
    //X1…X3	13 bits	Index Register: contains a 13-bit base address that supports base register addressing of memory.
    private Unit[] indexRegisters = new Unit[3];
    
    //R1…R3	20 bits General Purpose Registers (GPRs) – each 20 bits in length
    private Word[] gpRegisters = new Word[4];    
    
    //CC	4 bits	Condition Code: set when arithmetic/logical operations are executed; 
    //          it has four 1-bit elements: overflow, underflow, division by zero, equal-or-not. 
    //          OVERFLOW[0], UNDERFLOW[1], DIVZERO[2], EQUALORNOT[3]
    private Unit conditionCode;    
    
    
    /**************************************
     * All the variables below are internal and used to maintain state of the control unit
     *************************************/
    
    // Effective Address   ENGINEER Console: Used to hold EA temporarily in microcycles
    private Unit effectiveAddress;
    
    // Engineer: Internal flag to signal that a blocking operation occurred (memory read) forcing a clock cycle
    private boolean blocked = false;
    
    // used to control state of EA
    private int eaState;
    private static final int EA_DIRECT=0;
    private static final int EA_REGISTER_INDIRECT=1;
    private static final int EA_INDEXED=2;
    private static final int EA_INDEXED_OFFSET=3;
     
    // used to control the instruction cycle
    private int state;
    public static final int STATE_NONE=0;
    public static final int STATE_FETCH_INSTRUCTION=1;
    public static final int STATE_DECODE_INSTRUCTION=2;
    public static final int STATE_EXECUTE_INSTRUCTION=3;
    public static final int STATE_MACHINE_FAULT=4;
            
    //used for Condition Code Register
    public final static int CONDITION_REGISTER_OVERFLOW = 0;
    public final static int CONDITION_REGISTER_UNDERFLOW = 1;
    public final static int CONDITION_REGISTER_DIVZERO = 2;
    public final static int CONDITION_REGISTER_EQUALORNOT = 3;

    
    private static final int MICROSTATE_EXECUTE_COMPLETE=999;
    
    private static final int OPCODE_HLT=0;
    private static final int OPCODE_LDR=1;
    private static final int OPCODE_STR=2;
    private static final int OPCODE_LDA=3;
    private static final int OPCODE_AMR=4;
    private static final int OPCODE_SMR=5;
    private static final int OPCODE_AIR=6;
    private static final int OPCODE_SIR=7;
    private static final int OPCODE_JZ=10;
    private static final int OPCODE_JNE=11;
    private static final int OPCODE_JCC=12;
    private static final int OPCODE_JMP=13;
    private static final int OPCODE_JSR=14;
    private static final int OPCODE_RFS=15;
    private static final int OPCODE_SOB=16;
    private static final int OPCODE_JGE=17;
    private static final int OPCODE_MLT=20;
    private static final int OPCODE_DVD=21;
    private static final int OPCODE_TRR=22;
    private static final int OPCODE_AND=23;
    private static final int OPCODE_ORR=24;
    private static final int OPCODE_NOT=25;
    private static final int OPCODE_TRAP=30;    
    private static final int OPCODE_SRC=31;
    private static final int OPCODE_RRC=32;
    private static final int OPCODE_LDX=41;
    private static final int OPCODE_STX=42;
    private static final int OPCODE_INX=43;
    private static final int OPCODE_IN=61;
    private static final int OPCODE_OUT=62;
    private static final int OPCODE_CHK=63;
    
    // Engineer: used to control micro step, defined per state
    private Integer microState = null;
        
    // memory reference
    private MemoryControlUnit memory;
    
    // ALU Reference
    private ArithmeticLogicUnit alu;   
    
    // IO Reference
    private InputOutputController ioController;
    
    // nextPC	13 bits	Next Program Counter: Interal Register Used to signal program counter was adjusted by instruction
    private Unit nextProgramCounter;
    
    public ControlUnit(MemoryControlUnit mem, ArithmeticLogicUnit aluRef) {
        this.instructionRegister = new Word();
        this.programCounter = new Unit(13);
        this.machineStatusRegister = new Word();
        this.machineFaultRegister = new Unit(4);
        this.state = ControlUnit.STATE_NONE;
        this.memory = mem;        
        this.alu=aluRef;
        this.conditionCode = new Unit(4);  
        this.clearConditions();        
        
        for(int x=0;x<3;x++){
            this.indexRegisters[x] = new Unit(13);
        }        
        
        for(int x=0;x<4;x++){
            this.gpRegisters[x] = new Word();
        }                         
    }
    
    public InputOutputController getIOController() {
        return ioController;
    }
    
    public void setIOController(InputOutputController io){
        this.ioController = io;
    }

    /**
     *
     * @return current State
     */
    public int getState() {
        return state;
    }
   
    /**
     *
     * @param ConditionRegister
     * @return Condition Code for Register ID
     */
    public int getConditionCode(int ConditionRegister) {
        Integer[] raw = this.conditionCode.getBinaryArray();        
        return raw[ConditionRegister];
    }

    /**
     * Used for GUI
     * @return ConditionCodeRegister Unit(4) - 
     */
    public Unit getConditionCodeRegister() {
        return conditionCode;
    }    
    
    /**
     * Set a Condition Flag
     * Usage:  this.setCondition(ArithmeticLogicUnit.CONDITION_REGISTER_OVERFLOW);
     * @param ConditionRegister (see static variables)
     */
    public void setCondition(int ConditionRegister){
        Integer[] raw = this.conditionCode.getBinaryArray();
        raw[ConditionRegister] = 1;
        
        StringBuilder ret = new StringBuilder();
        for (Integer el : raw) {
            ret.append(el);
        }
        this.conditionCode.setValueBinary(ret.toString());        
    }
    

    
    /**
     * Unset a Condition Flag
     * Usage:  this.unsetCondition(ArithmeticLogicUnit.CONDITION_REGISTER_OVERFLOW);
     * @param ConditionRegister (see static variables)
     */
    public void unsetCondition(int ConditionRegister){
        Integer[] raw = this.conditionCode.getBinaryArray();
        raw[ConditionRegister] = 0;
        
        StringBuilder ret = new StringBuilder();
        for (Integer el : raw) {
            ret.append(el);
        }
        this.conditionCode.setValueBinary(ret.toString());         
    }
    
    /**
     * Clear any previously set condition codes
     */
    public final void clearConditions(){
        this.conditionCode.setValueBinary("0000");
    }    
    
    public Unit getProgramCounter() {
        return programCounter;
    }

    public Word getInstructionRegister() {
        return instructionRegister;
    }

    public Word getMachineStatusRegister() {
        return machineStatusRegister;
    }

    public Unit getMachineFaultRegister() {
        return machineFaultRegister;
    }

    public Unit[] getIndexRegisters() {
        return indexRegisters;
    }
     /**
     *Use to set Index Register IX
     * @param ixid IndexRegisters Id(1~3)
     * @param IndexRegister data
     */
    public void setIndexRegister(int ixid,Unit IndexRegister)
    {
        if(ixid<4&&ixid>0){ // IX1-3, stored internally at 0-2
            this.indexRegisters[ixid-1] = Unit.cloneUnit(IndexRegister);
        }
    }
    
    /**
     * Returns a translated index register value (1-3) becomes (0-2)
     * @param ixid IndexRegisters Id(1~3)
     * @return Unit value
     */
    public Unit getIndexRegister(int ixid){
        if(ixid<4&&ixid>0){ // IX1-3, stored internally at 0-2
            return Unit.cloneUnit(this.indexRegisters[ixid-1]);
        } else {
            return null;
        }
    }

    public Word[] getGeneralPurposeRegisters() {
        return gpRegisters;
    }  
     /**
     *Use to set General Purpose Register
     * @param RFI GeneralPurposeRegisterValuesId(0~3)
     * @param GeneralPurposeRegisterValue initial data
     */
    public void setGeneralPurposeRegister(int RFI,Word GeneralPurposeRegisterValue){
        if(RFI<4&&RFI>=0) // GPR 0-3
        {
            this.gpRegisters[RFI]=Word.cloneWord(GeneralPurposeRegisterValue);
        }
    }
    
    /**
     * 
     * @param RFI
     * @return Word R(RFI)
     */
    public Word getGeneralPurposeRegister(int RFI){
        return this.gpRegisters[RFI];
    }

    public void setProgramCounter(Unit programCounter) {
        this.programCounter = new Unit(13,programCounter.getUnsignedValue());
    }

    public Word getIR() {
        return instructionRegister;
    }

    public void setIR(Word instructionRegister) {
        this.instructionRegister = instructionRegister;
    }
    
    public void setMFR(Unit id) {
        this.machineFaultRegister = id;
    }
    
    /**
     * Clock cycle. This is the main function which causes the ControlUnit to do work.
     *  This serves as a publicly accessible method, but calls the instruction cycle.
     * @throws java.lang.Exception
     */
    @Override
    public void clockCycle() throws Exception {        
        this.instructionCycle();                           
    }  
    
    /**
     * Used internally to signal that a micro-cycle needs a full clock cycle
     */
    private void signalBlockingMicroFunction(){
        this.blocked=true;
    }    
    
    /**       
    * These fundamental steps are repeated over and over until we reach the 
    * last instruction in the program, typically something called HALT, STOP, or QUIT. 
    */      
    private void instructionCycle() throws Exception {
        if(microState==null){
            microState=0;
        }
        this.blocked=false;             
        
        switch(this.state){
            case ControlUnit.STATE_FETCH_INSTRUCTION: // takes 2 cycles
                this.fetchNextInstructionFromMemory();
                break;
            case ControlUnit.STATE_DECODE_INSTRUCTION:
                this.decodeInstructionRegister();
                break;
            case ControlUnit.STATE_EXECUTE_INSTRUCTION:
                this.executeInstructionRegister();
                break;
            case ControlUnit.STATE_MACHINE_FAULT:
                this.handleMachineFault();
                break;
            default:
                this.state = ControlUnit.STATE_FETCH_INSTRUCTION;
                break;     
        }        
    }
    
     /**
     * Handles Machine Faults
     * a machine fault occurs, the processor saves the current PC and MSR contents 
     * to the locations(4,5) then fetches the address from Location 
     * 1 (Machine Fault) into the PC which becomes the next instruction to be 
     * executed. This address will be the first instruction of a routine which 
     * handles the trap or machine fault. 
     */
    void handleMachineFault() {
        this.blocked=false; 
        switch(this.microState){            
            case 0: // save pc                
                Unit pc = this.getProgramCounter();
                Unit pcPlusOne = new Unit(13, pc.getUnsignedValue()+1);
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "[FAULT] Micro-0: 4->MAR, PC({0}) -> MBR", pcPlusOne.getUnsignedValue());                
                this.memory.setMAR(new Unit(13,4));     
                this.memory.setMBR(pcPlusOne);
                this.memory.signalStore();               
                this.microState++; // no break in case it was cached                                
            case 1: // wait for save pc
                if(!this.memory.isBusy()){ // block until memory read is ready
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "[FAULT] Micro-1: PC -> M(4)");
                    this.microState++; 
                    // bleed through to case 2
                } else {
                    this.signalBlockingMicroFunction();
                    break;
                }                
            case 2: // save msr
                Word msr = this.getMachineStatusRegister();
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "[FAULT] Micro-2: 5->MAR, MSR({0}) -> MBR", msr);                
                this.memory.setMAR(new Unit(13,5));     
                this.memory.setMBR(msr);
                this.memory.signalStore();               
                this.microState++; // no break in case it was cached                    
                break;
            case 3: // wait for save msr
                if(!this.memory.isBusy()){ // block until memory read is ready
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "[FAULT] Micro-3: MSR -> M(5)");
                    this.microState++; 
                    // bleed through to case 4
                } else {
                    this.signalBlockingMicroFunction();
                    break;
                }                       
                break;
            case 4: // fetch machine fault address
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "[FAULT] Micro-4: 1->MAR (Fetch)");
                this.memory.setMAR(new Unit(13,1));                     
                this.memory.signalFetch();               
                this.microState++; // no break in case it was cached                     
                break;
            case 5: // transfer execution to machine fault addr
                if(!this.memory.isBusy()){ // block until memory read is ready
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "[FAULT] Micro-5: M(1) -> PC [{0}]", this.memory.getMBR().getUnsignedValue());
                    this.setProgramCounter(this.memory.getMBR());                                  
                    // Set up for next major state
                    this.microState=null;
                    this.state=ControlUnit.STATE_FETCH_INSTRUCTION;
                    break;
                } else {
                    this.signalBlockingMicroFunction();
                }

        }        
    }
    
    public void signalMachineFault(int faultID){
        this.setMFR(new Unit(4,faultID));
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "[FAULT]: Machine Fault Occurred! [{0}]", faultID);
        
        this.state=ControlUnit.STATE_MACHINE_FAULT;
        this.microState=0;
    }     
    
    /**
     * fetch the next instruction from memory to be executed
     */
    private void fetchNextInstructionFromMemory(){
        
        switch(this.microState){            
            case 0:
                this.nextProgramCounter=null;
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-0: PC -> MAR");
                // Micro-0: PC -> MAR
                Unit pc = this.getProgramCounter();
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "-- PC: {0}", pc);
                this.memory.setMAR(pc);               
                this.memory.signalFetch();               
                this.microState++; // no break in case it was cached
                
            case 1:
                if(!this.memory.isBusy()){ // block until memory read is ready
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-1: MDR -> IR");
                    // Micro-1: MDR -> IR                
                    this.setIR(this.memory.getMBR());              
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "-- IR: {0}", this.memory.getMBR());
                    this.microState=2;              

                    // Set up for next major state
                    this.microState=null;
                    this.state=ControlUnit.STATE_DECODE_INSTRUCTION;
                    break;
                } else {
                    this.signalBlockingMicroFunction();
                }
        }
    }
    
    /**
     *
     * @param IR Instruction Register 
     * @return HashMap of IR
     */
    private HashMap<String,Unit> decodeInstructionRegister(Word IR){
       HashMap<String,Unit> decoded = new HashMap();
       
       decoded.put("opcode",  IR.decomposeByOffset(0, 5  ));       
       decoded.put("xfi",    IR.decomposeByOffset(6,  7  ));
       decoded.put("rfi",    IR.decomposeByOffset(8,  9  ));
       decoded.put("index",   IR.decomposeByOffset(10    ));
       decoded.put("trace",   IR.decomposeByOffset(11    ));
       decoded.put("address", IR.decomposeByOffset(12, 19));
       
       return decoded;
   }      
    
    /**
     * decode instruction (i.e., determine what is to be done)
     * also calculates effective address if instruction requires it
     */
    private void decodeInstructionRegister(){        
        if(this.microState == 0){// Micro-4: Decode IR
            this.effectiveAddress=null;
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-4: Decode IR");
            this.instructionRegisterDecoded = this.decodeInstructionRegister(this.getIR());     
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "-- IR Decoded: {0}", this.instructionRegisterDecoded);
                        
            int opcode = this.instructionRegisterDecoded.get("opcode").getUnsignedValue();
            
            
            switch(opcode){
                case ControlUnit.OPCODE_INX:
                case ControlUnit.OPCODE_AIR:
                case ControlUnit.OPCODE_SIR:
                case ControlUnit.OPCODE_MLT:
                case ControlUnit.OPCODE_DVD:
                case ControlUnit.OPCODE_TRR:
                case ControlUnit.OPCODE_AND:
                case ControlUnit.OPCODE_ORR:
                case ControlUnit.OPCODE_NOT:                     
                case ControlUnit.OPCODE_SRC:
                case ControlUnit.OPCODE_RRC:
                case ControlUnit.OPCODE_IN:
                case ControlUnit.OPCODE_OUT:
                case ControlUnit.OPCODE_CHK:
                case ControlUnit.OPCODE_HLT:
                case ControlUnit.OPCODE_TRAP:
                    // These instructions don't require EA calculation. Skip ahead.
                    this.microState=null;
                    this.state=ControlUnit.STATE_EXECUTE_INSTRUCTION;                                
                    this.effectiveAddress=null;
                    break;
                default:// Every other instruction does. We'll progress through eaState and microState now.
                    if(this.instructionRegisterDecoded.get("index").getUnsignedValue()==0 && this.instructionRegisterDecoded.get("xfi").getUnsignedValue()==0){                        
                        this.eaState = ControlUnit.EA_DIRECT;
                    } else if(this.instructionRegisterDecoded.get("index").getUnsignedValue()==0 && this.instructionRegisterDecoded.get("xfi").getUnsignedValue()>=1 && this.instructionRegisterDecoded.get("xfi").getUnsignedValue()<=3){
                        this.eaState = ControlUnit.EA_REGISTER_INDIRECT;                    
                    } else if(this.instructionRegisterDecoded.get("index").getUnsignedValue()==1 && this.instructionRegisterDecoded.get("xfi").getUnsignedValue()==0){
                        this.eaState = ControlUnit.EA_INDEXED;
                    } else if(this.instructionRegisterDecoded.get("index").getUnsignedValue()==1 && this.instructionRegisterDecoded.get("xfi").getUnsignedValue()>=1 && this.instructionRegisterDecoded.get("xfi").getUnsignedValue()<=3){
                        this.eaState = ControlUnit.EA_INDEXED_OFFSET;
                    }                                
                    this.microState++;    
                    
                    break;
            }      
        } else { //microState >= 1 & we're computing EA
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-5.{0}: Compute Effective Address (Type: {1})", new Object[]{this.microState, this.eaState});            
            switch(this.eaState){
                case ControlUnit.EA_DIRECT: //EA <- ADDR                    
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Absolute/Direct:{0}", this.instructionRegisterDecoded.get("address"));
                    this.effectiveAddress = new Unit(13,this.instructionRegisterDecoded.get("address").getUnsignedValue());                    
                    break;
                case ControlUnit.EA_REGISTER_INDIRECT: //EA <- c(Xi) + ADDR
                    switch(this.microState){
                        case 1:
                            Unit addr = this.instructionRegisterDecoded.get("address");  
                            int contentsOfX = this.getIndexRegister(this.instructionRegisterDecoded.get("xfi").getUnsignedValue()).getUnsignedValue(); //read Xi here  
                            this.effectiveAddress = new Unit(13, (contentsOfX + addr.getUnsignedValue()));
                            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Register Indirect + Offset ({0} + {1}): {2}", new Object[]{contentsOfX, addr.getUnsignedValue(), this.effectiveAddress});
                            break;                            
                    }                           
                    break;
                case ControlUnit.EA_INDEXED: //EA <- c(ADDR)                         
                    switch(this.microState){
                        case 1: // Set ADDR onto MAR
                            Unit addr = new Unit(13,this.instructionRegisterDecoded.get("address").getUnsignedValue());
                            this.memory.setMAR(addr);  
                            this.memory.signalFetch();
                            this.microState++; // no break in case it was cached
                        case 2: // c(ADDR) from MBR, set to MAR
                            if(!this.memory.isBusy()){ // block until memory read is ready
                                Word contentsOfAddr = this.memory.getMBR();
                                this.effectiveAddress =  new Unit(13, (contentsOfAddr.getUnsignedValue()));
                                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Indexed - c(ADDR) =  c({0}) = {1}", new Object[]{this.instructionRegisterDecoded.get("address").getUnsignedValue(), this.effectiveAddress});                            
                            } else {
                                this.signalBlockingMicroFunction();
                            }
                            break;
                    }                           
                    break;                    
                case ControlUnit.EA_INDEXED_OFFSET: //EA <- c(c(Xi) + ADDR)
                    switch(this.microState){
                        case 1:
                            Unit addr = this.instructionRegisterDecoded.get("address");
                            int contentsOfX = this.getIndexRegister(this.instructionRegisterDecoded.get("xfi").getUnsignedValue()).getUnsignedValue();    //read Xi here                        
                            Unit location = new Unit(13, (contentsOfX + addr.getUnsignedValue()));
                            this.memory.setMAR(location);
                            this.memory.signalFetch();
                            this.microState++; // no break in case it was cached
                        case 2:
                            if(!this.memory.isBusy()){ // block until memory read is ready
                                Word contentsOfLocation = this.memory.getMBR();
                                this.effectiveAddress = new Unit(13, contentsOfLocation.getUnsignedValue());
                                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Indexed + Offset --> {0}", this.effectiveAddress);                                
                            } else {
                                this.signalBlockingMicroFunction();
                            }
                            break;
                    }                      
                    break;
                default:
                    // Unhandled address mode
            }            
            if(this.effectiveAddress != null){ // EA Calculated. Completed!
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "-- Effective Address Calculated: {0}", this.effectiveAddress);                     
                this.microState=null;
                this.state=ControlUnit.STATE_EXECUTE_INSTRUCTION;                    
            }
        }
    }
    
    private void signalMicroStateExecutionComplete(){
        this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;
    }
    
    /**
     * execute instruction by issuing the appropriate command to the ALU, memory, and the I/O controllers
     * Each instruction will receive the microstate at 0 and is responsible for taking action.
     * When the instruction completes, it sets the microState to MICROSTATE_EXECUTE_COMPLETE
     */
    private void executeInstructionRegister() throws Exception {
        if(microState==null){
            microState=0;
        }
        /* This delegates the microstate to the instruction, but does handle a special
            microstate (999) to signal completion. */
        if(this.microState < ControlUnit.MICROSTATE_EXECUTE_COMPLETE){
            int opcode = this.instructionRegisterDecoded.get("opcode").getUnsignedValue();
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "--EXECUTING OPCODE: {0}, MicroState: {1}", new Object[]{opcode, microState});
            switch(opcode){
                case ControlUnit.OPCODE_HLT:
                    this.executeOpcodeHLT();                    
                    break;                    
                case ControlUnit.OPCODE_LDR:
                    this.executeOpcodeLDR();
                    break;
                case ControlUnit.OPCODE_STR:
                    this.executeOpcodeSTR();
                    break;
                case ControlUnit.OPCODE_LDA:
                    this.executeOpcodeLDA();
                    break;    
                case ControlUnit.OPCODE_LDX:
                    this.executeOpcodeLDX();
                    break;
                case ControlUnit.OPCODE_STX:
                    this.executeOpcodeSTX();
                    break;
                case ControlUnit.OPCODE_INX:
                    this.executeOpcodeINX();
                    break;
                case ControlUnit.OPCODE_AMR:
                    this.executeOpcodeAMR();
                    break;
                case ControlUnit.OPCODE_SMR:
                    this.executeOpcodeSMR();
                    break;
                case ControlUnit.OPCODE_AIR:
                    this.executeOpcodeAIR();
                    break;
                case ControlUnit.OPCODE_SIR:
                    this.executeOpcodeSIR();
                    break;
                case ControlUnit.OPCODE_JMP:  
                    this.executeOpcodeJMP();
                    break;
                case ControlUnit.OPCODE_JZ:  
                    this.executeOpcodeJZ();
                    break;
                case ControlUnit.OPCODE_JNE: 
                    this.executeOpcodeJNE();
                    break;
                case ControlUnit.OPCODE_JGE:  
                    this.executeOpcodeJGE();
                    break;                    
                case ControlUnit.OPCODE_SOB: 
                    this.executeOpcodeSOB();
                    break;
                case ControlUnit.OPCODE_JCC:
                    this.executeOpcodeJCC();
                    break;
                case ControlUnit.OPCODE_RFS:
                    this.executeOpcodeRFS();
                    break;
                case ControlUnit.OPCODE_JSR:
                    this.executeOpcodeJSR();
                    break;
                case ControlUnit.OPCODE_SRC:
                    this.executeOpcodeSRC();
                    break;
                case ControlUnit.OPCODE_RRC:
                    this.executeOpcodeRRC();
                    break;     
                case ControlUnit.OPCODE_ORR:
                    this.executeOpcodeORR();
                    break;
                case ControlUnit.OPCODE_NOT:
                    this.executeOpcodeNOT();
                    break;
                 case ControlUnit.OPCODE_TRR:
                    this.executeOpcodeTRR();
                    break;
                case ControlUnit.OPCODE_AND:
                    this.executeOpcodeAND();
                    break; 
                case ControlUnit.OPCODE_MLT:
                    this.executeOpcodeMLT();
                    break;
                case ControlUnit.OPCODE_DVD:
                    this.executeOpcodeDVD();
                    break;
                case ControlUnit.OPCODE_TRAP:
                    this.executeOpcodeTRAP();
                    break;
                case ControlUnit.OPCODE_IN:
                    this.executeOpcodeIN();
                    break;
                case ControlUnit.OPCODE_OUT:
                    this.executeOpcodeOUT();
                    break;
                case ControlUnit.OPCODE_CHK:
                    this.executeOpcodeCHK();
                    break;
                default: // Illegal opcode. Crash!
                    throw new MachineFaultException(MachineFaultException.ILLEGAL_OPCODE);
            }            
            if(!this.blocked){ // if not blocked, move ahead
                if(opcode!=ControlUnit.OPCODE_TRAP){ // trap increments its own state
                    this.microState++; 
                }
            }
        } else { // MICROSTATE_EXECUTE_COMPLETE            
            if(this.nextProgramCounter==null){
                // Micro-N: c(PC) + 1 -> PC  --- Increment PC
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-Final: c(PC) + 1 -> PC (Increment PC)");
                this.getProgramCounter().setValue(this.getProgramCounter().getUnsignedValue() + 1);                 
            } else { 
                // Micro-N PC <- tempPC (internal to our simulator)
                this.getProgramCounter().setValue(this.nextProgramCounter.getUnsignedValue());
            }
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "-- PC: {0}", this.getProgramCounter());
            this.state = ControlUnit.STATE_NONE;     
            this.microState = null;
            this.signalBlockingMicroFunction();            

        }        
    }        
      
    /***************** OPCODE IMPLEMENTATIONS BELOW ******************/
    
    /**
     * Execute Load Data Into Register
     */
    private void executeOpcodeLDR(){
        switch(this.microState){            

            case 0:
                // Micro-6: MAR <- EA
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: MAR <- EA");
                memory.setMAR(this.effectiveAddress);  
                memory.signalFetch();
                this.microState++; // no break in case it was cached
                
            default:
                // Micro-7: MBR <- M(MAR)
                // Micro-8: RF(RFI) <- MBR   
                if(!this.memory.isBusy()){                
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: MBR <- M(MAR)");
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: RF(RFI) <- MBR");
                    int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
                    this.setGeneralPurposeRegister(RFI, this.memory.getMBR());

                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " LDR - rfi[{0}] is now: {1}", new Object[]{RFI, this.memory.getMBR()});
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                    this.signalMicroStateExecutionComplete();
                } else {
                    this.signalBlockingMicroFunction();
                }
                
                break;            
            
        }
            
                                
    }
    
    /**
     * Execute Store Register to Memory
     */
    private void executeOpcodeSTR() {
        switch(this.microState){    
            
            case 0:
              // Micro-6: MAR <- EA
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);         
              
              // Micro-7: MBR <- RF(RFI)
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: MBR <- RF(RFI)");
              int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
           
              memory.setMBR(this.getGeneralPurposeRegister(RFI));
              memory.signalStore();
              this.microState++; // no break in case it was cached
                
            default:
                if(!memory.isBusy()){
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: M(MAR) <- MBR");
                    // do nothing, done by memory in this clock cycle   
           
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    try {
                        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " STR - M({0}) is now {1}", new Object[]{this.effectiveAddress.getUnsignedValue(), this.memory.engineerFetchByMemoryLocation(this.effectiveAddress)});
                    } catch (MachineFaultException ex) {
                        Logger.getLogger(ControlUnit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                    this.signalMicroStateExecutionComplete();
                } else {
                    this.signalBlockingMicroFunction();
                }
            break;
        }
    }
    
    /**
     * Execute Load Register with Address
     * Load Register with Address, r = 0..3
     * r <− EA or r <− c(EA), if I bit set
     */
    private void executeOpcodeLDA() {
       
                // Micro-6: RF(RFI) <- EA
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: RF(RFI) <- EA");
                int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
                this.setGeneralPurposeRegister(RFI, new Word(this.effectiveAddress.getUnsignedValue()));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " LDA - rfi[{0}] is now: {1}", new Object[]{RFI, this.getGeneralPurposeRegister(RFI)});
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                this.signalMicroStateExecutionComplete();             

    }
    
    /**
     * Execute Load Index Register from Memory
     * LDX x, X’ address[,I]
     * Load Index Register from Memory, X’ = 1..3 
     * X(X') <- c(EA)
     */
    private void executeOpcodeLDX() {
        switch(this.microState){            
            case 0:
              // Micro-6: MAR <- EA
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: MAR<-EA");
              memory.setMAR(this.effectiveAddress);
              memory.signalFetch();  
              this.microState++; // no break in case it was cached
                
            default:
                if(!memory.isBusy()){
                    // Micro-7: MBR <- M(MAR)
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: MBR <- M(MAR)");
                    // Micro 8: c(XFI) <- MBR                                  
                    int XFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
                    this.setIndexRegister(XFI, new Unit(13,this.memory.getMBR().getSignedValue()));

                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " LDX - X({0}) is now {1}", new Object[]{XFI, this.getIndexRegister(XFI)});
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                    this.signalMicroStateExecutionComplete();
                } else {
                    this.signalBlockingMicroFunction();
                }
            break;
        }
    }
    
    /**
     * Execute Store Index Register to Memory
     * STX x, X’, address[,I]
     * X’ = 1..3,  EA <- c(X’), C(EA) <- c(X’), if I-bit set
     */
    private void executeOpcodeSTX() {
        switch(this.microState){
            case 0:
              // Micro-6: MAR <- EA
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);              
            break;
                
            case 1:
              // Micro 7: MBR <- c(XFI)
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro 7: MBR <- c(XFI)");
              int XFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
            
              memory.setMBR(new Word(this.getIndexRegister(XFI).getSignedValue()));
              memory.signalStore(); 
              this.microState++; // no break in case it was cached
                
            default:
              if(!memory.isBusy()){
                    // Micro 8: M(MAR) <- MBR
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro 8: M(MAR) <- MBR");
                    // do nothing, done by memory

                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    try {
                        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " STX - M({0}): {1}", new Object[]{this.effectiveAddress.getUnsignedValue(), this.memory.engineerFetchByMemoryLocation(this.effectiveAddress)});
                    } catch (MachineFaultException ex) {
                        Logger.getLogger(ControlUnit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                    this.signalMicroStateExecutionComplete();
              } else {
                  this.signalBlockingMicroFunction();
              }
            break;
        }
    }
    
    /**
     * Increment Index Register 
     */
    private void executeOpcodeINX() {
        // Micro 6: c(XFI) = c(XFI) + 1
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro 6: c(XFI) = c(XFI) + 1");
        int XFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();

        this.setIndexRegister(XFI, new Unit(13,this.getIndexRegister(XFI).getSignedValue() + 1));

        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " INX - X({0}) is now: {1}", new Object[]{XFI, this.getIndexRegister(XFI).getSignedValue()});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        this.signalMicroStateExecutionComplete();
    }    
    
    /**
     * Execute Add Memory to Register
     */
    private void executeOpcodeAMR() {
        switch(this.microState){
            
            case 0:
              // Micro-6: MAR <- EA
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);
              memory.signalFetch();  
              this.microState++; // no break in case it was cached
                
            case 1:              
                if(!memory.isBusy()){
                    // Micro-7: MBR <- M(MAR)
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: MBR <- M(MAR)");
                    // Micro-8: OP1 <- MBR
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: OP2 <- MBR");
                    alu.setOperand2(this.memory.getMBR());  // This might be possible to run in cycle 1                    
                } else {
                    this.signalBlockingMicroFunction();
                }                
            break;
                
            case 2:
              // Micro-9: OP1 <- RF(RFI)
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-9: OP2 <- RF(RFI)");
              int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
              alu.setOperand1(this.getGeneralPurposeRegister(RFI));
            break;
                
            case 3:
              // Micro-10: CTRL <- OPCODE
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-10: CTRL <- OPCODE");  
              alu.setControl(ArithmeticLogicUnit.CONTROL_ADD);
              alu.signalReadyToStartComputation();
            break;
                
            case 4:
              // Micro-11: RES <- c(OP1) + c(OP2)
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-11: RES <- c(OP1) + c(OP2)");
              // Do nothing. (occurs automatically one clock cycle after signaled ready to compute)
            break;
                
            case 5:
              // Micro-12: RF(RFI) <- RES
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-12: RF(RFI) <- RES");
              RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue(); 
              this.setGeneralPurposeRegister(RFI, new Word(alu.getResult()));
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " AMR - RF({0}): {1}", new Object[]{RFI, this.getGeneralPurposeRegister(RFI)});
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
              this.signalMicroStateExecutionComplete();
            break;          
        }
    }
    
    /**
     * Execute Subtract Memory from Register
     */
    private void executeOpcodeSMR() {
        switch(this.microState){
            
            case 0:
              // Micro-6: MAR <- EA
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);
              memory.signalFetch();
              this.microState++; // no break in case it was cached
                
            case 1:
                if(!memory.isBusy()){
                    // Micro-7: MBR <- M(MAR)
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: MBR <- M(MAR)");
                    // Micro-8: OP1 <- MBR
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: OP2 <- MBR");
                    alu.setOperand2(this.memory.getMBR());                  
                } else {
                    this.signalBlockingMicroFunction();
                } 
                break;
            case 2:                                     
            
              // Micro-9: OP2 <- RF(RFI)
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-9: OP1 <- RF(RFI)");
              int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
              alu.setOperand1(this.getGeneralPurposeRegister(RFI));
            break;
                
            case 3:
              // Micro-10: CTRL <- OPCODE
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-10: CTRL <- OPCODE");  
              alu.setControl(ArithmeticLogicUnit.CONTROL_SUBTRACT);
              alu.signalReadyToStartComputation();
            break;
                
            case 4:
              // Micro-11: RES <- c(OP1) - c(OP2)
              // Do nothing. (occurs automatically one clock cycle after signaled ready to compute)
            break;
                
            case 5:
              // Micro-12: RF(RFI) <- RES
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-12: RF(RFI) <- RES");
              RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
              
              this.setGeneralPurposeRegister(RFI, new Word(alu.getResult()));
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " SMR - RF({0}): {1}", new Object[]{RFI, this.getGeneralPurposeRegister(RFI)});
              Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
                
              this.signalMicroStateExecutionComplete();
            break;          
        }
    }
    
    /**
     * Execute Add Immediate to Register
     */
    private void executeOpcodeAIR() {
        switch(this.microState){
            case 0:
                // Micro-6: OP1 <- RF(RFI)                
                int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();                
                alu.setOperand1(this.getGeneralPurposeRegister(RFI));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: OP1 <- RF(RFI) - {0}", alu.getOperand1());
            break;
                        
            case 1:
                // Micro-7: OP2 <- Immed   (Immed is stored in ADDR)                
                alu.setOperand2(this.instructionRegisterDecoded.get("address"));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: OP2 <- Immed - {0}", alu.getOperand2());
            break;
                
            case 2:
                // Micro-8: CTRL <- OPCODE
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: CTRL <- OPCODE");  
                alu.setControl(ArithmeticLogicUnit.CONTROL_ADD);
                alu.signalReadyToStartComputation();
            break;
                
            case 3:
                // Micro-9: RES <- c(OP1) + c(OP2)
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-9: RES <- c(OP1) + c(OP2)");
                // Do nothing. (occurs automatically one clock cycle after signaled ready to compute)
            break;
                
            case 4:
                // Micro-10: RF(RFI) <- RES
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-10: RF(RFI) <- RES - {0}", alu.getResult());
                RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
               
                this.setGeneralPurposeRegister(RFI, new Word(alu.getResult()));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " AIR - RF({0}): {1}", new Object[]{RFI, this.getGeneralPurposeRegister(RFI)});
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
                
                this.signalMicroStateExecutionComplete();
            break;          
        }
    }
    
    /**
     * Execute Subtract Immediate from Register
     */
    private void executeOpcodeSIR() {
        switch(this.microState){
            case 0:
                // Micro-6: OP1 <- RF(RFI)                
                int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
                alu.setOperand1(this.getGeneralPurposeRegister(RFI));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: OP1 <- RF(RFI) - {0}", alu.getOperand1());
            break;
                        
            case 1:
                // Micro-7: OP2 <- Immed  (Immed is stored in ADDR)                
                alu.setOperand2(this.instructionRegisterDecoded.get("address"));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: OP2 <- Immed - {0}", alu.getOperand2());
            break;
                
            case 2:
                // Micro-8: CTRL <- OPCODE
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: CTRL <- OPCODE");  
                alu.setControl(ArithmeticLogicUnit.CONTROL_SUBTRACT);
                alu.signalReadyToStartComputation();
            break;
                
            case 3:
                // Micro-9: RES <- c(OP1) - c(OP2)
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-9: RES <- c(OP1) - c(OP2)");
                // Do nothing. (occurs automatically one clock cycle after signaled ready to compute)
            break;
                
            case 4:
                // Micro-10: RF(RFI) <- RES
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-10: RF(RFI) <- RES - {0}", alu.getResult());
                RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
               
                this.setGeneralPurposeRegister(RFI, new Word(alu.getResult()));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " SIR - RF({0}): {1}", new Object[]{RFI, this.getGeneralPurposeRegister(RFI)});
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
                
                this.signalMicroStateExecutionComplete();
            break;          
        }
        
    }

    /**
     * Execute Unconditional Jump to Address
     * PC <- EA, if I bit not set; PC <− c(EA), if I bit set
     * Note: r is ignored in this instruction
     * Test pased by Fan based on 001101 00 01 1 0 01111011
     * 
     */
    private void executeOpcodeJMP(){
        
        this.nextProgramCounter=new Unit(13,this.effectiveAddress.getUnsignedValue());
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: PC <- EA - {0}", this.nextProgramCounter);
            this.signalMicroStateExecutionComplete();
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JMP - IND={0}: {1}", new Object[]{this.instructionRegisterDecoded.get("index").getUnsignedValue(), this.nextProgramCounter});
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
     
    }
    
    /**
     * Jump If Zero:
     *  If c(r) = 0, then PC <− EA or c(EA), if I bit set;
     *  Else PC <- PC+1
     * Test pased by Fan based on 001010  00  01  1  0  01111011(given by professor)
     */
    private void  executeOpcodeJZ(){        
        int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
        if(this.getGeneralPurposeRegister(RFI).getUnsignedValue()==0)
        { // c(r)==0, jump
         this.nextProgramCounter=new Unit(13,this.effectiveAddress.getUnsignedValue());
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: PC <- EA - {0}", this.nextProgramCounter);              
         this.signalMicroStateExecutionComplete();         
         
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
         Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JZ - R({0}) was Zero -- JUMPING: {1}", new Object[]{RFI, this.nextProgramCounter});
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); 
           
        }
        else
        {
             // not zero->PC++
            this.signalMicroStateExecutionComplete();            
            
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JZ - R({0}) was NOT Zero -- Continuing.", RFI);
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
            
        }
    }
        

  
    
    /**
     * Jump If Not Equal (to Zero) -- really JNZ.
     * If c(r) != 0, then PC <−- EA or c(EA) , if I bit set;
     * Else PC <- PC + 1
     * Test pased by Fan based on 001011 00 01 1 0 01111011(R(0)==0 AND R(0)=1)
     */
    private void executeOpcodeJNE(){
            int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
        if(this.getGeneralPurposeRegister(RFI).getUnsignedValue()!=0)
        { // c(r)!=0, jump
         this.nextProgramCounter=new Unit(13,this.effectiveAddress.getUnsignedValue());
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: PC <- EA - {0}", this.nextProgramCounter);              
         this.signalMicroStateExecutionComplete();        
         
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
         Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JNE - R({0}) was NOT Zero -- JUMPING: {1}", new Object[]{RFI, this.nextProgramCounter});
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); 
           
        }
        else
        {
             // not zero->PC++
            this.signalMicroStateExecutionComplete();            
            
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JNE - R({0}) was  Zero -- Continuing.", RFI);
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
            
        }
    }
    
    
    /**
     * Subtract One And Branch. SOB allows you to support simple loops. 
     * R = 0..3
     * r <− c(r) – 1
     * If c(r) > 0,  PC <- EA; but PC <− c(EA), if I bit set;
     * Else PC <- PC + 1
     * Test pased by Fan based on 010000 00 01 1 0 01111011 (R0=1 or r0=2)
     */
     private void executeOpcodeSOB(){        
        int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
        
        switch(this.microState){
            case 0: // case 0, we decrement c(r)
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6:RF({0})=c({1})-1", new Object[]{RFI, RFI});  
                Word rCurrent = this.getGeneralPurposeRegister(RFI);
                Word rNew = new Word(rCurrent.getUnsignedValue()-1);
                this.setGeneralPurposeRegister(RFI, rNew);
                break;
            case 2:
                if(this.getGeneralPurposeRegister(RFI).getUnsignedValue()>0){ // c(r)>0, jump back
                    this.nextProgramCounter=new Unit(13,this.effectiveAddress.getUnsignedValue());
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: PC <- EA - {0}", this.nextProgramCounter);                                  
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " SOB - R({0}) was {1}, GREATER than Zero after minus 1 -- JUMPING: {2}", new Object[]{RFI, this.getGeneralPurposeRegister(RFI).getUnsignedValue(), this.nextProgramCounter});
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); 
                    this.signalMicroStateExecutionComplete();
                } else {
                    // equal to zero->PC++                    
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " SOB - R({0}) was {1}, NOT GREATER than Zero after minus 1  -- Continuing.", new Object[]{RFI, this.getGeneralPurposeRegister(RFI).getUnsignedValue()});
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");              
                    this.signalMicroStateExecutionComplete();
                }
                
        }
     }
   
    /**
     * 
     * Jump Greater Than or Equal To:
     * If c(r) >= 0, then PC <- EA or c(EA) , if I bit set;
     * Else PC <- PC + 1
     * Test pased by Fan based on 010001 00 01 1 0 01111011
    */
   private void executeOpcodeJGE()
   {
        int RFI = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();
        if(this.getGeneralPurposeRegister(RFI).getSignedValue()>=0)
        { // c(r)>=0, jump
         this.nextProgramCounter=new Unit(13,this.effectiveAddress.getUnsignedValue());
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: PC <- EA - {0}", this.nextProgramCounter);              
         this.signalMicroStateExecutionComplete();
         
         /**
         if (int x < 4)
         *{
         *  this.branchHistory[OPCODE_JGE][x] = {OPCODE_JGE, (x + 1)};
         *}
         */
         
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
         Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JGE - R({0}) was NOT LESS than Zero -- JUMPING: {1}", new Object[]{RFI, this.nextProgramCounter});
         Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); 
           
        }
        else
        {
             // not zero->PC++
            this.signalMicroStateExecutionComplete();
            
            /**
            if (int x > 0)
            *{
            *  this.branchHistory[OPCODE_JGE][x] = {OPCODE_JGE, (x - 1)};
            *}
            */
            
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JGE - R({0}) was LESS Zero -- Continuing.", RFI);
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
            
        }
    } 
   
    
    /**
     * 
     * Jump If Condition Code:
     * If CC = 1, then PC <- EA or c(EA) , if I bit set;
     * Else PC <- PC + 1
    */
  private void executeOpcodeJCC(){
        int CC = this.instructionRegisterDecoded.get("rfi").getUnsignedValue();         //CC replaces RFI for the JCC instruction.
        if(this.getConditionCode(CC)==1){
            this.nextProgramCounter=new Unit(13,this.effectiveAddress.getUnsignedValue());
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: PC <- EA - {0}", this.nextProgramCounter);              
            this.signalMicroStateExecutionComplete();
            
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JCC({0}) - Jumping: {1}", new Object[]{CC, this.nextProgramCounter});
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");              
        } else { // not zero->PC++             CC != 1
            this.signalMicroStateExecutionComplete();
            
            /**
            if (int x > 0)
            *{
            *  this.branchHistory[OPCODE_JCC][x] = {OPCODE_JCC, (x - 1)};
            *}
            */
            
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JCC({0}) - Not Jumping. Value was: {1}", new Object[]{CC, this.getConditionCode(CC)});
            Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
        }
            
    }
 
    /**
     * 
     * Return From Subroutine w/ return code as Immediate portion (optional) 
     * stored in R0’s address field. 
     * R0 <− Immed; PC <− c(R3)
     * IX, I fields are ignored.
     * 
    */    
    private void executeOpcodeRFS(){
      switch(this.microState){
            case 0:
                // R0 <- Immed (Immed is stored in ADDR)        
                this.setGeneralPurposeRegister(0, new Word(this.instructionRegisterDecoded.get("address").getUnsignedValue()));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: R0 <- Immediate");
            break;
                
            case 1:
                // PC <- c(R3)
                this.nextProgramCounter = new Unit(13, this.getGeneralPurposeRegister(3).getUnsignedValue());
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: PC <- c(R3)");
            
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " RFS - Ret: {0} - Jump: {1}", new Object[]{this.getGeneralPurposeRegister(0), this.nextProgramCounter});
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
                
                this.signalMicroStateExecutionComplete();
            break;          
        }
                
    }
    
    /**
     * 
     * Jump and Save Return Address:
     * R3 <- PC + 1, PC <- EA or c(EA) , if I bit set;
     * R0 should contain pointer to arguments. Argument list should end with -17777 value.
    */
    private void executeOpcodeJSR(){
        switch(this.microState){
            case 0:
                //RF(RFI1) <- PC + 1
                this.setGeneralPurposeRegister(3, new Word(this.getProgramCounter().getUnsignedValue()+1));
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: RF(I) <- PC + 1");
            break;
                
            case 1:
                //PC <- EA
                this.nextProgramCounter=new Unit(13,this.effectiveAddress.getUnsignedValue());
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: PC <- EA - {0}", this.nextProgramCounter);
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " JSR - Next: {0}, RET: {1}", new Object[]{this.nextProgramCounter, this.getGeneralPurposeRegister(3)});
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                                 
                this.signalMicroStateExecutionComplete(); 
                break;                
              
        }
    }
    
    /**
     * Shift Register by Count
     * c(r) is shifted left (L/R =1) or right (L/R = 0) either logically (A/L = 1) or arithmetically (A/L = 0)
     * XX, XXX are ignored
     * Count = 0…20
     * If Count = 0, no shift occurs
     */
    private void executeOpcodeSRC(){
        int RFI = this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
        
        int algorithmicLogical = this.getIR().decomposeByIndex(10).getUnsignedValue();
        int leftRight = this.getIR().decomposeByIndex(11).getUnsignedValue();
        int count = this.getIR().decomposeByOffset(15, 19).getUnsignedValue();
        
        // Shift functionality is implemented in Unit
        this.getGeneralPurposeRegister(RFI).shiftByCount(leftRight, count, algorithmicLogical);
        
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " SRC - Shift Register {0} {1} {2} by {3}: {4}", new Object[]{RFI, (leftRight==1) ? "Left" : "Right", (algorithmicLogical==1) ? "Logical" : "Algorithmic", count, this.getGeneralPurposeRegister(RFI)});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                                 
        this.signalMicroStateExecutionComplete();
    }
    
    /**
     * Rotate Register by Count
     * c(r) is rotated left (L/R = 1) or right (L/R =0) either logically (A/L =1)
     * XX, XXX is ignored
     * Count = 0…20
     * If Count = 0, no rotate occurs
     */
    private void executeOpcodeRRC(){
        int RFI = this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
        
        int leftRight = this.getIR().decomposeByIndex(11).getUnsignedValue();
        int count = this.getIR().decomposeByOffset(15, 19).getUnsignedValue();
        
        // Rotate functionality is implemented in Unit
        this.getGeneralPurposeRegister(RFI).rotateByCount(leftRight, count);
        
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " RRC - Rotate Register {0} {1} by {2}: {3}", new Object[]{RFI, (leftRight==1) ? "Left" : "Right", count, this.getGeneralPurposeRegister(RFI)});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");    
        this.signalMicroStateExecutionComplete();
    }
    /*
    Test the Equality of Register and Register

If c(rx) = c(ry), set cc(4) <- 1; else, cc(4) <- 0


    */
    private void executeOpcodeTRR(){
        int rx=this.getIR().decomposeByOffset(6, 7).getUnsignedValue();
        int ry=this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
        if(this.getGeneralPurposeRegister(rx).getBinaryString().equals(this.getGeneralPurposeRegister(ry).getBinaryString())){
            this.setCondition(ControlUnit.CONDITION_REGISTER_EQUALORNOT);
        } else {
            this.unsetCondition(ControlUnit.CONDITION_REGISTER_EQUALORNOT);
        }
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "COMPLETED INSTRUCTION:TRR RF({0}/{1}) and RF({2}/{3}) are {4}", new Object[]{rx, this.getGeneralPurposeRegister(rx), ry, this.getGeneralPurposeRegister(ry), (this.getConditionCode(ControlUnit.CONDITION_REGISTER_EQUALORNOT)==1)?"equal":"not equal"});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
        this.signalMicroStateExecutionComplete();     
    }
    
    /*
    Logical And of Register and Register
    c(rx) <- c(rx) AND c(ry)
    */
    private void executeOpcodeAND(){
        Integer rx = this.getIR().decomposeByOffset(6, 7).getUnsignedValue(); // rx
        Integer ry = this.getIR().decomposeByOffset(8, 9).getUnsignedValue(); // ry
        
        Unit contentsOfRx = this.getGeneralPurposeRegister(rx);
        Unit contentsOfRy = this.getGeneralPurposeRegister(ry);
        
        Unit result = contentsOfRx.logicalAND(contentsOfRy);
        
        this.setGeneralPurposeRegister(rx, new Word(result));
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " AND rx({0}), ry({1}) = {2}", new Object[]{contentsOfRx.getBinaryString(), contentsOfRy.getBinaryString(), result});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
        this.signalMicroStateExecutionComplete();      
 
    }
    
    /**
     * Logical OR of Register and Register
     * c(rx) <- c(rx) OR c(ry)
     */
    private void executeOpcodeORR(){
        Integer rx = this.getIR().decomposeByOffset(6, 7).getUnsignedValue(); // rx
        Integer ry = this.getIR().decomposeByOffset(8, 9).getUnsignedValue(); // ry
        
        Unit contentsOfRx = this.getGeneralPurposeRegister(rx);
        Unit contentsOfRy = this.getGeneralPurposeRegister(ry);
        
        Unit result = contentsOfRx.logicalOR(contentsOfRy);
        
        this.setGeneralPurposeRegister(rx, new Word(result));
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " ORR rx({0}), ry({1}) = {2}", new Object[]{contentsOfRx.getBinaryString(), contentsOfRy.getBinaryString(), result});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
        this.signalMicroStateExecutionComplete();
    }
    
    /**

     * Logical Not of Register To Register
     * C(rx) <- NOT c(rx)
     */
    private void executeOpcodeNOT(){
        
        Integer rx = this.getIR().decomposeByOffset(6, 7).getUnsignedValue();
        Unit contentsOfRx = this.getGeneralPurposeRegister(rx);
        Unit InvertedContentsOfRx = contentsOfRx.logicalNOT();
       
        this.setGeneralPurposeRegister(rx, new Word(InvertedContentsOfRx));
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " NOT rx({0}) = {1}", new Object[]{contentsOfRx.getBinaryString(), InvertedContentsOfRx});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
        this.signalMicroStateExecutionComplete();
        
    }
    
    /**
     * Multiply Register by Register
     * rx, rx+1 <- c(rx) * c(ry)
     */
    private void executeOpcodeMLT() {
        Integer rx,ry;
        switch(this.microState){
            case 0:
                // Micro-6: OP1 <- RF(RFI1)                
                rx=this.getIR().decomposeByOffset(6, 7).getUnsignedValue();
                Unit contentsOfRx=this.getGeneralPurposeRegister(rx);
                alu.setOperand1(contentsOfRx);
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: OP1 <- c(rx) - {0}", alu.getOperand1());
            break;
                        
            case 1:
                // Micro-7: OP2 <- RF(RFI2)   
                ry=this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
                Unit contentsOfRy=this.getGeneralPurposeRegister(ry);
                alu.setOperand2(contentsOfRy);
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: OP2 <- c(ry) - {0}", alu.getOperand2());
            break;
                
            case 2:
                // Micro-8: CTRL <- OPCODE
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: CTRL <- OPCODE");
                alu.setControl(ArithmeticLogicUnit.CONTROL_MULTIPLY);
                alu.signalReadyToStartComputation();
            break;
                
            case 3:
                // Micro-9: RES <- c(OP1) * c(OP2)      
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-9: RES <- c(OP1) * c(OP2)");
                // Do nothing. (occurs automatically one clock cycle after signaled ready to compute)
            break;
                
            case 4:
                // Micro-10: c(RX) <- RES(HI), c(RX+1) <- RES(LOW)
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-10:  c(RX) <- RES(HI), c(RX+1) <- RES(LOW)");
                
                rx=this.getIR().decomposeByOffset(6, 7).getUnsignedValue();              
                Integer rxPlusOne = rx+1;
                if(rx>2){
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "MLT: Invalid Value for RX (0-2).");
                }
                
                Unit result40Bit = alu.getResult();
                String result40String = result40Bit.getBinaryString();
                
                Word highBits = Word.WordFromBinaryString(result40String.substring(0,20));
                Word lowBits = Word.WordFromBinaryString(result40String.substring(20));                               
                
                this.setGeneralPurposeRegister(rx, highBits);
                this.setGeneralPurposeRegister(rxPlusOne, lowBits);
                                
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "COMPLETED INSTRUCTION:MLT = {0} {1} ({2})", new Object[]{highBits.getBinaryString(), lowBits.getBinaryString(), result40Bit});
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
                this.signalMicroStateExecutionComplete();
            break;          
        }  
    }
    
    /**
     * Divide Register by Register
     * rx, rx+1 <- c(rx) / c(ry)
     */
    private void executeOpcodeDVD() {
        Integer rx,ry;
        switch(this.microState){
            case 0:
                // Micro-6: OP1 <- RF(RFI1)                
                rx=this.getIR().decomposeByOffset(6, 7).getUnsignedValue();
                Unit contentsOfRx=this.getGeneralPurposeRegister(rx);
                alu.setOperand1(contentsOfRx);
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-6: OP1 <- c(rx) - {0}", alu.getOperand1());
            break;
                        
            case 1:
                // Micro-7: OP2 <- RF(RFI2)   
                ry=this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
                Unit contentsOfRy=this.getGeneralPurposeRegister(ry);
                alu.setOperand2(contentsOfRy);
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-7: OP2 <- c(ry) - {0}", alu.getOperand2());
            break;
                
            case 2:
                // Micro-8: CTRL <- OPCODE
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-8: CTRL <- OPCODE");
                alu.setControl(ArithmeticLogicUnit.CONTROL_DIVIDE);
                alu.signalReadyToStartComputation();
            break;
                
            case 3:
                // Micro-9: RES <- c(OP1) / c(OP2)      
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-9: RES <- c(OP1) / c(OP2)");
                // Do nothing. (occurs automatically one clock cycle after signaled ready to compute)
            break;
                
            case 4:
                // Micro-10: c(RX) <- RES(quotient), c(RX+1) <- RES(remainder)
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-10:  c(RX) <- RES(QUOTIENT), c(RX+1) <- RES(REMAINDER)");
                
                rx=this.getIR().decomposeByOffset(6, 7).getUnsignedValue();              
                Integer rxPlusOne = rx+1;
                if(rx>2){
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "DVD: Invalid Value for RX (0-2).");
                }
                
                Unit result40Bit = alu.getResult();
                String result40String = result40Bit.getBinaryString();
                
                Word quotient = Word.WordFromBinaryString(result40String.substring(0,20));
                Word remainder = Word.WordFromBinaryString(result40String.substring(20));                               
                
                this.setGeneralPurposeRegister(rx, quotient);
                this.setGeneralPurposeRegister(rxPlusOne, remainder);
                                
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "COMPLETED INSTRUCTION:DVD = {0} r{1}", new Object[]{quotient.getSignedValue(), remainder.getSignedValue()});
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");  
                this.signalMicroStateExecutionComplete();
        }
    }
    
    /**
     * Execute TRAP.
     * Traps to memory address 0, which contains the address of a table in 
     * memory. The table can have a maximum of 16 entries representing 16 
     * routines for user-specified instructions stored elsewhere in memory. Trap
     * code (in Address field) contains an index into the table, e.g. it takes 
     * values 0 – 15. When a TRAP instruction is executed, it goes to the 
     * routine, executes those instructions, and returns to the instruction 
     * after the TRAP instruction. If the value of code is greater than 15, 
     * a machine fault occurs.
     */
    private void executeOpcodeTRAP() throws MachineFaultException {
        
        // Get the TRAP code from the address bits.
        int trapCode = this.getIR().decomposeByOffset(12, 19).getUnsignedValue();        
        
        if (trapCode >= 16){   // TRAP codes range from 0 - 15.
            throw new MachineFaultException(MachineFaultException.ILLEGAL_TRAP_CODE);
        }
        
        switch(this.microState){            
            case 0: // save pc                
                Unit pc = this.getProgramCounter();
                Unit pcPlusOne = new Unit(13, pc.getUnsignedValue()+1);
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-0: 2->MAR, PC({0}) -> MBR", pc.getUnsignedValue());                
                this.memory.setMAR(new Unit(13,2));     
                this.memory.setMBR(pcPlusOne);
                this.memory.signalStore();               
                this.microState++; // no break in case it was cached                                
            case 1: // wait for save pc
                if(!this.memory.isBusy()){ // block until memory read is ready
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-1: PC -> M(2)");
                    this.microState++; 
                    // bleed through to case 2
                } else {
                    this.signalBlockingMicroFunction();
                    break;
                }                
            case 2: // save msr
                Word msr = this.getMachineStatusRegister();
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-2: 3->MAR, MSR({0}) -> MBR", msr);                
                this.memory.setMAR(new Unit(13,3));     
                this.memory.setMBR(msr);
                this.memory.signalStore();               
                this.microState++; // no break in case it was cached                    
                break;
            case 3: // wait for save msr
                if(!this.memory.isBusy()){ // block until memory read is ready
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-3: MSR -> M(3)");
                    this.microState++; 
                    // bleed through to case 4
                } else {
                    this.signalBlockingMicroFunction();
                    break;
                }                       
                break;
            case 4: // fetch machine fault address
                Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-4: 0->MAR (Fetch)");
                this.memory.setMAR(new Unit(13,0));                     
                this.memory.signalFetch();               
                this.microState++; // no break in case it was cached                     
                break;
            case 5: // transfer execution to machine fault addr
                if(!this.memory.isBusy()){ // block until memory read is ready                    
                    Word offset = this.memory.getMBR();
                    int newLoc = offset.getUnsignedValue() + trapCode;
                    this.nextProgramCounter = new Unit(13, newLoc);
                    
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "Micro-5: M(0) [{0}] +TC [{1}] -> PC [{2}]", new Object[]{this.memory.getMBR().getUnsignedValue(), trapCode, newLoc});
                    
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " TRAP {0}", trapCode);
                    Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");                                         
                    this.signalMicroStateExecutionComplete();
                    break;
                } else {
                    this.signalBlockingMicroFunction();
                }

        }            
        
        
        
    }
        
    /**
     * Stop the machine
     */
    private void executeOpcodeHLT() throws HaltSystemException {                
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " HLT");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.signalMicroStateExecutionComplete();
        throw new HaltSystemException();
    }   
    
    /**
     * Input Character To Register from Device, r = 0..3
     */
    private void executeOpcodeIN(){                
        
        int r = this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
        int DEVID = this.getIR().decomposeByOffset(16, 19).getUnsignedValue();
                
        Word received = ioController.input(DEVID);
        if(received==null){
            received = new Word(0);
        }
        this.setGeneralPurposeRegister(r, received);    
        
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " IN - Set R{0} to {1} from device {2}", new Object[]{r, received, DEVID});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.signalMicroStateExecutionComplete();
    }  
    
    /**
     * Output Character to Device from Register, r = 0..3
     */
    private void executeOpcodeOUT(){                
        
        int r = this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
        int DEVID = this.getIR().decomposeByOffset(16, 19).getUnsignedValue();
        
        Word contentsOfR=this.getGeneralPurposeRegister(r);
        
        ioController.output(DEVID, contentsOfR);
        
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " OUT - Pushed {0} to Device: {1}", new Object[]{contentsOfR, DEVID});
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.signalMicroStateExecutionComplete();
    }   
    
    /**
     * Check Device Status to Register, r = 0..3
     * c(r) <- device status
     */
    private void executeOpcodeCHK(){                
        
        int r = this.getIR().decomposeByOffset(8, 9).getUnsignedValue();
        int DEVID = this.getIR().decomposeByOffset(16, 19).getUnsignedValue();
        
        int status = ioController.checkStatus(DEVID);
        this.setGeneralPurposeRegister(r, new Word(status));        
        
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Logger.getLogger(ControlUnit.class.getName()).log(Level.INFO, " CHK  - Status: {0}", status);
        Logger.getLogger(ControlUnit.class.getName()).log(Level.CONFIG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.signalMicroStateExecutionComplete();
    }       
    
}