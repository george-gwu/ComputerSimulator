package computersimulator.cpu;

import computersimulator.components.HaltSystemException;
import computersimulator.components.Unit;
import computersimulator.components.Word;
import java.util.HashMap;

/**
 * It is the task of the control unit to fetch the next instruction from 
 *   memory to be executed, decode it (i.e., determine what is to be done), and 
 *   execute it by issuing the appropriate command to the ALU, memory , and the 
 *   I/O controllers.
 * 
 * @TODO We need some way to ensure that registers don't get converted to different size units. I accidentally cast PC to 4 and no error happened immediately.
 * 
 * @author george
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
    private static final int STATE_NONE=0;
    private static final int STATE_FETCH_INSTRUCTION=1;
    private static final int STATE_DECODE_INSTRUCTION=2;
    private static final int STATE_EXECUTE_INSTRUCTION=3;
    
    
    private static final int MICROSTATE_EXECUTE_COMPLETE=999;
    
    private static final int OPCODE_HLT=0;
    private static final int OPCODE_LDR=1;
    private static final int OPCODE_STR=2;
    private static final int OPCODE_LDA=3;
    private static final int OPCODE_TRAP=30;
    private static final int OPCODE_LDX=41;
    private static final int OPCODE_STX=42;
    private static final int OPCODE_AMR=4;
    private static final int OPCODE_SMR=5;
    private static final int OPCODE_AIR=6;
    private static final int OPCODE_SIR=7;
    
    // Engineer: used to control micro step, defined per state
    private Integer microState = null;
        
    // memory reference
    private MemoryControlUnit memory;

    
    
    public ControlUnit(MemoryControlUnit mem) {
        this.instructionRegister = new Word();
        this.programCounter = new Unit(13);
        this.machineStatusRegister = new Word();
        this.machineFaultRegister = new Unit(4);
        this.state = ControlUnit.STATE_NONE;
        this.memory = mem;
        
        for(int x=0;x<3;x++){
            this.indexRegisters[x] = new Unit(13);
        }        
        
        for(int x=0;x<4;x++){
            this.gpRegisters[x] = new Word();
        }                         
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

    public Word[] getGpRegisters() {
        return gpRegisters;
    }        

    public Unit getPC() {
        return this.programCounter;
    }

    public void setPC(Unit programCounter) {
        this.programCounter = programCounter;
    }

    public Word getIR() {
        return instructionRegister;
    }

    public void setIR(Word instructionRegister) {
        this.instructionRegister = instructionRegister;
    }    
    
    /**
     * Clock cycle. This is the main function which causes the ControlUnit to do work.
     *  This serves as a publicly accessible method, but calls the instruction cycle.
     * @throws java.lang.Exception
     */
    @Override
    public void clockCycle() throws Exception {
        // Used to run microcycles without causing a full clock cycle
        boolean runningMicroCycles=true;
        //do {
          System.out.println("Micro!");
          
          this.instructionCycle();

          if(this.blocked == true){
              // A microcycle signaled it is blocking.
              runningMicroCycles=false;
              this.blocked=false;
          }
        //} while(runningMicroCycles);                              
        
    }  
    
    /**
     * Used internally to signal that a microcycle needs a full clock cycle
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
            default:
                this.state = ControlUnit.STATE_FETCH_INSTRUCTION;
                break;     
        }        
    }
    
    /**
     * fetch the next instruction from memory to be executed
     */
    private void fetchNextInstructionFromMemory(){
        
        switch(this.microState){            
            case 0:
                System.out.println("Micro-0: PC -> MAR");
                // Micro-0: PC -> MAR
                Unit pc = this.getPC();
                System.out.println("-- PC: "+pc);
                this.memory.setMAR(pc);
                this.microState=1;  
                this.signalBlockingMicroFunction();
                break;         
                
                // unwritten step: clock cycle causes memory to pull PC
            
            case 1:
                System.out.println("Micro-1: MDR -> IR");
                // Micro-1: MDR -> IR                
                this.setIR(this.memory.getMBR());              
                System.out.println("-- IR: "+this.memory.getMBR());
                this.microState=2;              

                // Set up for next major state
                this.microState=null;
                this.state=ControlUnit.STATE_DECODE_INSTRUCTION;
                break;
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
       decoded.put("rfi",    IR.decomposeByOffset(6, 7  ));
       decoded.put("xfi",    IR.decomposeByOffset(8, 9  ));
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
            System.out.println("Micro-4: Decode IR");
            this.instructionRegisterDecoded = this.decodeInstructionRegister(this.getIR());     
            System.out.println("-- IR Decoded: "+this.instructionRegisterDecoded);
                        
            int opcode = this.instructionRegisterDecoded.get("opcode").getValue();            
            if(opcode == ControlUnit.OPCODE_AIR || opcode ==ControlUnit.OPCODE_SIR){
                // These instructions don't require EA calculation. Skip ahead.
                this.microState=null;
                this.state=ControlUnit.STATE_EXECUTE_INSTRUCTION;            
            } else { // Every other instruction does. We'll progress through eaState and microState now.
                if(this.instructionRegisterDecoded.get("xfi").getValue()==0 && this.instructionRegisterDecoded.get("rfi").getValue()==0){                        
                    this.eaState = ControlUnit.EA_DIRECT;
                } else if(this.instructionRegisterDecoded.get("index").getValue()==0 && this.instructionRegisterDecoded.get("xfi").getValue()>=1 && this.instructionRegisterDecoded.get("xfi").getValue()<=3){
                    this.eaState = ControlUnit.EA_REGISTER_INDIRECT;                    
                } else if(this.instructionRegisterDecoded.get("index").getValue()==1 && this.instructionRegisterDecoded.get("xfi").getValue()==0){
                    this.eaState = ControlUnit.EA_INDEXED;
                } else if(this.instructionRegisterDecoded.get("index").getValue()==1 && this.instructionRegisterDecoded.get("xfi").getValue()>=1 && this.instructionRegisterDecoded.get("xfi").getValue()<=3){
                    this.eaState = ControlUnit.EA_INDEXED_OFFSET;
                }                                
                this.microState++;
            }         
        } else { //microState >= 1 & we're computing EA
            System.out.println("Micro-5."+this.microState+": Compute Effective Address (Type: "+this.eaState+")");            
            switch(this.eaState){
                case ControlUnit.EA_DIRECT: //EA <- ADDR                    
                    System.out.println("Absolute/Direct:" + this.instructionRegisterDecoded.get("address"));
                    this.effectiveAddress = this.instructionRegisterDecoded.get("address");                    
                    break;
                case ControlUnit.EA_REGISTER_INDIRECT: //EA <- c(Xi) + c(ADDR)
                    switch(this.microState){
                        case 1:
                            Unit addr = this.instructionRegisterDecoded.get("address");
                            this.memory.setMAR(addr);                            
                            this.microState++;
                            break;
                        case 2:
                            int contentsOfX = this.instructionRegisterDecoded.get("xfi").getValue();
                            Word contentsOfAddr = this.memory.getMBR();
                            this.effectiveAddress = new Unit(13, (contentsOfX + contentsOfAddr.getValue()));
                            System.out.println("Register Indirect + Offset ("+contentsOfX+" + "+contentsOfAddr.getValue()+"): "+this.effectiveAddress);
                            break;                            
                    }                           
                    break;
                case ControlUnit.EA_INDEXED: //EA <- c(c(ADDR))                         
                    switch(this.microState){
                        case 1: // Set ADDR onto MAR
                            Unit addr = this.instructionRegisterDecoded.get("address");
                            this.memory.setMAR(addr);                            
                            this.microState++;
                            break;
                        case 2: // c(ADDR) from MBR, set to MAR
                            Word contentsOfAddr = this.memory.getMBR();
                            this.memory.setMAR(new Unit(13, contentsOfAddr.getValue()));
                            this.microState++;
                            break;
                        case 3: // c(c(ADDR)) from MBR
                            Word contentsOfContents = this.memory.getMBR();
                            this.effectiveAddress =  new Unit(13, (contentsOfContents.getValue()));
                            System.out.println("Indexed - c(c(ADDR)) =  c(c("+this.instructionRegisterDecoded.get("address").getValue()+")) = "+this.effectiveAddress);                            
                            break;
                    }                           
                    break;                    
                case ControlUnit.EA_INDEXED_OFFSET: //EA <- c(c(Xi) + c(ADDR))
                    switch(this.microState){
                        case 1:
                            Unit addr = this.instructionRegisterDecoded.get("address");
                            this.memory.setMAR(addr);                            
                            this.microState++;
                            break;
                        case 2: // c(ADDR) from MBR, get X, add X + C(ADDR) to MAR
                            Word contentsOfAddr = this.memory.getMBR();
                            int contentsOfX = this.instructionRegisterDecoded.get("xfi").getValue();                            
                            Unit location = new Unit(13, (contentsOfX + contentsOfAddr.getValue()));
                            this.memory.setMAR(location);
                            this.microState++;    
                            break;
                        case 3:
                            Word contentsOfLocation = this.memory.getMBR();
                            this.effectiveAddress = new Unit(13, contentsOfLocation.getValue());
                            System.out.println("Indexed + Offset --> "+this.effectiveAddress);                                
                            break;
                    }                      
                    break;
                default:
                    // Unhandled address mode
            }            
            if(this.effectiveAddress != null){ // EA Calculated. Completed!
                System.out.println("-- Effective Address Calculated: "+this.effectiveAddress);                     
                this.microState=null;
                this.state=ControlUnit.STATE_EXECUTE_INSTRUCTION;                    
            }
        }
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
            int opcode = this.instructionRegisterDecoded.get("opcode").getValue();
            System.out.println("--EXECUTING OPCODE: "+ opcode);
            switch(opcode){
                case ControlUnit.OPCODE_HLT:
                    this.executeOpcodeHLT();                    
                    break;                    
                case ControlUnit.OPCODE_LDR:        //DONE
                    this.executeOpcodeLDR();
                    break;
                case ControlUnit.OPCODE_STR:        //DONE
                    this.executeOpcodeSTR();
                    break;
                case ControlUnit.OPCODE_LDA:        //DONE
                    this.executeOpcodeLDA();
                    break;    
                case ControlUnit.OPCODE_LDX:        //DONE
                    this.executeOpcodeLDX();
                    break;
                case ControlUnit.OPCODE_STX:        //DONE
                    this.executeOpcodeSTX();
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
                default: // Unhandle opcode. Crash!
                    throw new Exception("Unhandled Opcode: "+opcode);                        
            }            
            this.microState++; 
        } else { // MICROSTATE_EXECUTE_COMPLETE
            // Micro-N: c(PC) + 1 -> PC  --- Increment PC
            System.out.println("Micro-Final: c(PC) + 1 -> PC (Increment PC)");

            this.getPC().add(new Unit(13, 1));
            System.out.println("-- PC: "+this.getPC());

            this.microState = null;
            this.state = ControlUnit.STATE_NONE;       
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
                System.out.println("Micro-6: MAR <- EA");
                memory.setMAR(this.effectiveAddress);  
                this.signalBlockingMicroFunction();
                break;
                
            case 1:
                // Micro-7: MBR <- M(MAR)
                System.out.println("Micro-7: MBR <- M(MAR)");
                // do nothing, done by memory
                break;

            case 2:
                // Micro-8: RF(RFI) <- MBR   
                System.out.println("Micro-8: RF(RFI) <- MBR");
                int RFI = this.instructionRegisterDecoded.get("rfi").getValue();
                this.gpRegisters[RFI] = this.memory.getMBR();

                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("COMPLETED INSTRUCTION: LDR - rfi["+RFI+"] is now: "+ this.memory.getMBR());
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
                //Signal completion
                this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;

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
              System.out.println("Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);         
              
              // Micro-7: MBR <- RF(RFI)
              System.out.println("Micro-7: MBR <- RF(RFI)");
              int RFI = this.instructionRegisterDecoded.get("rfi").getValue();
              memory.setMBR(this.gpRegisters[RFI]);
              this.signalBlockingMicroFunction();
            break;
                
            case 1:   
              System.out.println("Micro-8: M(MAR) <- MBR");
              // do nothing, done by memory in this clock cycle    
              
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              System.out.println("COMPLETED INSTRUCTION: STR - M(MAR): "+ this.memory.engineerFetchByMemoryLocation(this.effectiveAddress));
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              
              //Signal Completion
              this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;
            break;
        }
    }
    
    /**
     * Execute Load Register with Address
     */
    private void executeOpcodeLDA() {
        switch(this.microState){
                
            case 0:
                // Micro-6: MAR <- EA
                System.out.println("Micro-6: MAR <- EA");
                memory.setMAR(this.effectiveAddress);    
                this.signalBlockingMicroFunction();
                break;
                
            case 1:
                // Micro-7: MBR <- M(MAR)
                System.out.println("Micro-7: MBR <- M(MAR)");
                // do nothing, done by memory
            break;
                
            case 2:
                // Micro-8: RF(RFI) <- MBR   
                System.out.println("Micro-8: RF(RFI) <- MBR");
                int RFI = this.instructionRegisterDecoded.get("rfi").getValue();
                this.gpRegisters[RFI] = this.memory.getMBR();

                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("COMPLETED INSTRUCTION: LDA - rfi["+RFI+"] is now: "+ this.memory.getMBR());
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
                //Signal completion
                this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;

            break;            
        }
    }
    
    /**
     * Execute Load Index Register from Memory
     */
    private void executeOpcodeLDX() {
        switch(this.microState){            
            case 0:
              // Micro-6: MAR <- EA
              System.out.println("Micro-6: MAR<-EA");
              memory.setMAR(this.effectiveAddress);
              this.signalBlockingMicroFunction();
            break;
                
            case 1:
              // Micro-7: MBR <- M(MAR)
              System.out.println("Micro-7: MBR <- M(MAR)");
              // do nothing, done by memory
            break;

            case 2:
              // Micro 8: c(XFI) <- MBR              
              // Maps XFI 1-3 to Array Index 0-2
              int XFI = (int)(this.instructionRegisterDecoded.get("xfi").getValue()-1);
              this.indexRegisters[XFI] = this.memory.getMBR();  
              
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              System.out.println("COMPLETED INSTRUCTION: LDX - M(MAR): "+ this.memory.engineerFetchByMemoryLocation(this.effectiveAddress));
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
              //Signal Completion
              this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;
            break;
        }
    }
    
    /**
     * Execute Store Index Register to Memory
     */
    private void executeOpcodeSTX() {
        switch(this.microState){
            case 0:
              // Micro-6: MAR <- EA
              System.out.println("Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);
              this.signalBlockingMicroFunction();
            break;
                
            case 1:
              // Micro 7: MBR <- c(XFI)
              System.out.println("Micro 7: MBR <- c(XFI)");
              // Maps XFI 1-3 to Array Index 0-2
              int XFI = (int)(this.instructionRegisterDecoded.get("xfi").getValue()-1);
              memory.setMBR(this.indexRegisters[XFI]);
            break;
                
            case 2:
              // Micro 8: M(MAR) <- MBR
              System.out.println("Micro 8: M(MAR) <- MBR");
              // do nothing, done by memory
                
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              System.out.println("COMPLETED INSTRUCTION: STX - M(MAR): "+ this.memory.engineerFetchByMemoryLocation(this.effectiveAddress));
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
              //Signal Completion
              this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;
            break;
        }
    }
    
    /**
     * Execute Add Memory to Register
     */
    private void executeOpcodeAMR() {
        switch(this.microState){
            
            case 0:
              // Micro-6: MAR <- EA
              System.out.println("Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);
              this.signalBlockingMicroFunction();
            break;
                
            case 1:
              // Micro-7: MBR <- M(MAR)
              System.out.println("Micro-7: MBR <- M(MAR)");
              // do nothing, done by memory
            break;
                
            case 3:
              // Micro-8: OP1 <- MBR
              
            break;
                
            case 4:
              // Micro-9: OP2 <- RF(RFI)
                
            break;
                
            case 5:
              // Micro-10: CTRL <- OPCODE
                
            break;
                
            case 6:
              // Micro-11: RES <- c(OP1) + c(OP2)
                
            break;
                
            case 7:
              // Micro-12: RF(RFI) <- RES
                
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              System.out.println("COMPLETED INSTRUCTION: AMR - M(MAR): "+ this.memory.engineerFetchByMemoryLocation(this.effectiveAddress));
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
              //Signal Completion
              this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;
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
              System.out.println("Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);
              this.signalBlockingMicroFunction();
            break;
                
            case 1:
              // Micro-7: MBR <- M(MAR)
              System.out.println("Micro-7: MBR <- M(MAR)");
              // do nothing, done by memory
            break;
                
            case 3:
              // Micro-8: OP1 <- MBR
              
            break;
                
            case 4:
              // Micro-9: OP2 <- RF(RFI)
                
            break;
                
            case 5:
              // Micro-10: CTRL <- OPCODE
                
            break;
                
            case 6:
              // Micro-11: RES <- c(OP1) - c(OP2)
                
            break;
                
            case 7:
              // Micro-12: RF(RFI) <- RES
                
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
              System.out.println("COMPLETED INSTRUCTION: SMR - M(MAR): "+ this.memory.engineerFetchByMemoryLocation(this.effectiveAddress));
              System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
              //Signal Completion
              this.microState=ControlUnit.MICROSTATE_EXECUTE_COMPLETE;
            break;          
        }
    }
    
    /**
     * Execute Add Immediate to Register
     */
    private void executeOpcodeAIR() {
        switch(this.microState){
            
        }
    }
    
    /**
     * Execute Subtract Immediate from Register
     */
    private void executeOpcodeSIR() {
        switch(this.microState){
            
        }
    }
    
    
    /**
     * Stop the machine
     */
    private void executeOpcodeHLT() throws HaltSystemException {
        throw new HaltSystemException();
    }    
    
}
