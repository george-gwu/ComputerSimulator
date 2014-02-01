package computersimulator.cpu;

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
    private Unit[] xRegisters = new Unit[4];
    
    //R1…R3	20 bits General Purpose Registers (GPRs) – each 20 bits in length
    private Word[] gpRegisters = new Word[4];    
    
    // Effective Address   ENGINEER Console: Used to hold EA temporarily in microcycles
    private Unit effectiveAddress;
    
    // used to control the instruction cycle
    private int state;
    private static final int STATE_NONE=0;
    private static final int STATE_FETCH_INSTRUCTION=1;
    private static final int STATE_DECODE_INSTRUCTION=2;
    private static final int STATE_EXECUTE_INSTRUCTION=3;
    
    
    private static final int MICROSTATE_EXECUTE_COMPLETE=999;
    
    
    private static final int OPCODE_LDR=1;
    private static final int OPCODE_STR=2;
    private static final int OPCODE_LDA=3;
    private static final int OPCODE_LDX=41;
    private static final int OPCODE_STX=42;
    private static final int OPCODE_AMR=4;
    private static final int OPCODE_SMR=5;
    private static final int OPCODE_AIR=6;
    private static final int OPCODE_SIR=7;
    
    // used to control micro step, defined per state
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
        
        for(int x=0;x<4;x++){
            this.xRegisters[x] = new Unit(13);
            this.gpRegisters[x] = new Word();
        }
                  
        
    }
    
    /**
     * Clock cycle. This is the main function which causes the ControlUnit to do work.
     *  This serves as a publicly accessible method, but calls the instruction cycle.
     */
    @Override
    public void clockCycle(){
        try {
            this.instructionCycle();        
        } catch(Exception e){
            System.out.println("Error: "+e);
            System.exit(1); //@TODO: Signal an error
            
        }
    }  
    
    private void instructionCycle() throws Exception {
        /**       
        * These fundamental steps are repeated over and over until we reach the 
        * last instruction in the program, typically something called HALT, STOP, or QUIT. 
        */        
        
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
     * decode instruction (i.e., determine what is to be done)
     */
    private void decodeInstructionRegister(){        
         switch(this.microState){            
            case 0:
                // Micro-4: Decode IR
                System.out.println("Micro-4: Decode IR");
                this.instructionRegisterDecoded = this.decodeInstructionRegister(this.getIR());     
                System.out.println("-- IR Decoded: "+this.instructionRegisterDecoded);
                
                this.microState=null;
                this.state=ControlUnit.STATE_EXECUTE_INSTRUCTION;
                
                break;

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
        } else {   //>=999         
            // Micro-N: c(pc) + 1 -> PC  --- Increment PC
            System.out.println("Micro-Final: c(pc) + 1 -> PC (Increment PC)");

            this.getPC().add(new Unit(13, 1));
            System.out.println("-- PC: "+this.getPC());

            this.microState = null;
            this.state = ControlUnit.STATE_NONE;            
        }        
    }
   
    
    private Unit calculateEffectiveAddress(HashMap<String,Unit> irDecoded){
     
        if(irDecoded.get("xfiI").getValue()==0 && irDecoded.get("rfiI").getValue()==0){                        
            //EA <- ADDR
            System.out.println("Absolute/Direct:" + irDecoded.get("address"));
            return irDecoded.get("address");            
        } else if(irDecoded.get("index").getValue()==0 && irDecoded.get("xfiI").getValue()>=1 && irDecoded.get("xfiI").getValue()<=3){
            int contentsOfX = irDecoded.get("xfiI").getValue();
            Unit addr = irDecoded.get("address");
            Word contentsOfAddr = this.memory.engineerFetchByMemoryLocation(addr);            
            Unit ret = new Unit(13, (contentsOfX + contentsOfAddr.getValue()));
            
            //EA <- c(Xi) + c(ADDR)
            System.out.println("Register Indirect + Offset ("+contentsOfX+" + "+contentsOfAddr.getValue()+"): "+ret);
            return ret;            
            
        } else if(irDecoded.get("index").getValue()==1 && irDecoded.get("xfiI").getValue()==0){
            Unit addr = irDecoded.get("address");
            Word contentsOfAddr = this.memory.engineerFetchByMemoryLocation(addr);
            Word contentsOfContents = this.memory.engineerFetchByMemoryLocation(contentsOfAddr);
            Unit ret = new Unit(13, (contentsOfContents.getValue()));
            
            //EA <- c(c(ADDR))     
            System.out.println("Indexed - c(c(ADDR)) =  c(c("+addr.getValue()+")) = c("+contentsOfAddr.getValue()+") = "+ret);
            return ret;                                    
            
        } else if(irDecoded.get("index").getValue()==1 && irDecoded.get("xfiI").getValue()>=1 && irDecoded.get("xfiI").getValue()<=3){
            int contentsOfX = irDecoded.get("xfiI").getValue();
            Unit addr = irDecoded.get("address");
            Word contentsOfAddr = this.memory.engineerFetchByMemoryLocation(addr);
            
            Unit location = new Unit(13, (contentsOfX + contentsOfAddr.getValue()));
            Word contentsOfLocation = this.memory.engineerFetchByMemoryLocation(location);
            
            Unit ret = new Unit(13, contentsOfLocation.getValue());
            System.out.println("Indexed + Offset ("+contentsOfX+" + "+contentsOfAddr.getValue()+"): "+ret);    
            //EA <- c(c(Xi) + c(ADDR))
            return ret;
            
        } else { // shouldn't end up here, but this should cause a machine fault
            System.out.println("Error");
            return new Unit(13,1);
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
       decoded.put("rfiI",    IR.decomposeByOffset(6, 7  ));
       decoded.put("xfiI",    IR.decomposeByOffset(8, 9  ));
       decoded.put("index",   IR.decomposeByOffset(10    ));
       decoded.put("trace",   IR.decomposeByOffset(11    ));
       decoded.put("address", IR.decomposeByOffset(12, 19));
       
       return decoded;
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

    /***************** OPCODE IMPLEMENTATIONS BELOW ******************/
    
    /**
     * Execute Load Data Into Register
     */
    private void executeOpcodeLDR(){
        switch(this.microState){            
            case 0:
                // Micro-5: Compute EA                                
                System.out.println("Micro-5: Compute EA    ");
                this.effectiveAddress = this.calculateEffectiveAddress(this.instructionRegisterDecoded);                
                System.out.println("-- Loading Effective Address: "+this.effectiveAddress);                            
                break;
                
            case 1:
                // Micro-6: MAR <- EA
                System.out.println("Micro-6: MAR <- EA");
                memory.setMAR(this.effectiveAddress);      
                break;
            case 2:
                // Micro-7: MBR <- M(MAR)
                System.out.println("Micro-7: MBR <- M(MAR)");
                // do nothing, done by memory
                break;

            case 3:
                // Micro-8: RF(RFI) <- MBR   
                System.out.println("Micro-8: RF(RFI) <- MBR");
                int RFI = this.instructionRegisterDecoded.get("rfiI").getValue();
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
              // Micro-5: Compute EA                                
              System.out.println("Micro-5: Compute EA    ");
              this.effectiveAddress = this.calculateEffectiveAddress(this.instructionRegisterDecoded);                
              System.out.println("-- Loading Effective Address: "+this.effectiveAddress);              
            break;
            
            case 1:
              // Micro-6: MAR <- EA
              System.out.println("Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);         
              
              // Micro-7: MBR <- RF(RFI)
              System.out.println("Micro-7: MBR <- RF(RFI)");
              int RFI = this.instructionRegisterDecoded.get("rfiI").getValue();
              memory.setMBR(this.gpRegisters[RFI]);
            break;
                
            case 2:   
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
              // Micro-5: Compute EA                                
              System.out.println("Micro-5: Compute EA    ");
              this.effectiveAddress = this.calculateEffectiveAddress(this.instructionRegisterDecoded);                
              System.out.println("-- Loading Effective Address: "+this.effectiveAddress);              
            break;
                
            case 1:
                // Micro-6: MAR <- EA
                System.out.println("Micro-6: MAR <- EA");
                memory.setMAR(this.effectiveAddress);      
                break;
                
            case 2:
                // Micro-7: MBR <- M(MAR)
                System.out.println("Micro-7: MBR <- M(MAR)");
                // do nothing, done by memory
            break;
                
            case 3:
                // Micro-8: RF(RFI) <- MBR   
                System.out.println("Micro-8: RF(RFI) <- MBR");
                int RFI = this.instructionRegisterDecoded.get("rfiI").getValue();
                this.xRegisters[RFI] = this.memory.getMBR();

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
              // Micro-5: Compute EA                                
              System.out.println("Micro-5: Compute EA    ");
              this.effectiveAddress = this.calculateEffectiveAddress(this.instructionRegisterDecoded);                
              System.out.println("-- Loading Effective Address: "+this.effectiveAddress);              
            break;
            
            case 1:
              // Micro-6: MAR <- EA
              System.out.println("Micro-6: MAR<-EA");
              memory.setMAR(this.effectiveAddress);
            break;
                
            case 2:
              // Micro-7: MBR <- M(MAR)
              System.out.println("Micro-7: MBR <- M(MAR)");
              // do nothing, done by memory
            break;

            case 3:
              // Micro 8: c(XFI) <- MBR
              int XFI = this.instructionRegisterDecoded.get("xfiI").getValue();
              this.xRegisters[XFI] = this.memory.getMBR();  
              
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
              // Micro-5: Compute EA                                
              System.out.println("Micro-5: Compute EA    ");
              this.effectiveAddress = this.calculateEffectiveAddress(this.instructionRegisterDecoded);                
              System.out.println("-- Loading Effective Address: "+this.effectiveAddress);              
            break;
            
            case 1:
              // Micro-6: MAR <- EA
              System.out.println("Micro-6: MAR <- EA");
              memory.setMAR(this.effectiveAddress);
            break;
                
            case 2:
              // Micro 7: MBR <- c(XFI)
              System.out.println("Micro 7: MBR <- c(XFI)");
              int XFI = this.instructionRegisterDecoded.get("xfiI").getValue();
              memory.setMBR(this.xRegisters[XFI]);
            break;
                
            case 3:
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
            
        }
    }
    
    /**
     * Execute Subtract Memory from Register
     */
    private void executeOpcodeSMR() {
        switch(this.microState){
            
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
    
}
