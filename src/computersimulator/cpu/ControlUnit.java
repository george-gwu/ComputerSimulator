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
    
    // used to control the instruction cycle
    private int state;
    private static final int STATE_NONE=0;
    private static final int STATE_FETCH_INSTRUCTION=1;
    private static final int STATE_DECODE_INSTRUCTION=2;
    private static final int STATE_EXECUTE_INSTRUCTION=3;
    
    
    private static final int OPCODE_LDR=1;
    
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
                this.microState=1;
                break;
                
                
            case 1:
                // Set up for next major state
                this.microState=null;
                this.state=ControlUnit.STATE_EXECUTE_INSTRUCTION;
                
                break;
        }    

    }
    
    /**
     * execute instruction by issuing the appropriate command to the ALU, memory, and the I/O controllers
     */
    private void executeInstructionRegister() throws Exception {
        // Only two micro states, execute(0), and increment PC/return to fetch.
        switch(this.microState){            
            case 0: 
                int opcode = this.instructionRegisterDecoded.get("opcode").getValue();
                System.out.println("--EXECUTING OPCODE: "+ opcode);
                switch(opcode){
                    case ControlUnit.OPCODE_LDR:
                        this.executeOpcodeLDR();
                        
                        break;
                        
                    default: // Unhandle opcode. Crash!
                        throw new Exception("Unhandled Opcode: "+opcode);                        
                    
                }
                this.microState++;
                break;
                
            default:
                // Micro-N: c(pc) + 1 -> PC  --- Increment PC
                System.out.println("Micro-Final: c(pc) + 1 -> PC (Increment PC)");
                                
                this.getPC().add(new Unit(13, 1));
                System.out.println("-- PC: "+this.getPC());
                
                this.microState = null;
                this.state = ControlUnit.STATE_NONE;
                
                break;
                
        }        
    }
   
    
    private Unit calculateEffectiveAddress(HashMap<String,Unit> irDecoded){
     
        if(irDecoded.get("xfiI").getValue()==0 && irDecoded.get("rfiI").getValue()==0){            
            
            return irDecoded.get("address");
            
        } else if(irDecoded.get("index").getValue()==0 && irDecoded.get("xfiI").getValue()>=1 && irDecoded.get("xfiI").getValue()<=3){
            int contentsOfX = irDecoded.get("xfiI").getValue();
            Unit addr = irDecoded.get("address");
            Word contentsOfAddr = this.memory.engineerFetchByMemoryLocation(addr);
            
            return new Unit(13, (contentsOfX + contentsOfAddr.getValue()));            
            
        } else if(irDecoded.get("index").getValue()==1 && irDecoded.get("xfiI").getValue()==0){
            Unit addr = irDecoded.get("address");
            Word contentsOfAddr = this.memory.engineerFetchByMemoryLocation(addr);
            Word contentsOfContents = this.memory.engineerFetchByMemoryLocation(contentsOfAddr);
            
            return new Unit(13, contentsOfContents.getValue());                                    
            
        } else if(irDecoded.get("index").getValue()==1 && irDecoded.get("xfiI").getValue()>=1 && irDecoded.get("xfiI").getValue()<=3){
            int contentsOfX = irDecoded.get("xfiI").getValue();
            Unit addr = irDecoded.get("address");
            Word contentsOfAddr = this.memory.engineerFetchByMemoryLocation(addr);
            
            Unit location = new Unit(13, (contentsOfX + contentsOfAddr.getValue()));
            Word contentsOfLocation = this.memory.engineerFetchByMemoryLocation(location);
            return new Unit(13, contentsOfLocation.getValue());
            
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
    
    
    public void decodeTest(){
          /* 
        Consider the LDR instruction which we did in class:
        LDR r, x, address [,I]
        A particular example is:	LDR, 3, 0, 52, I
        which says “load R3 from address 54 indirect with no indexing”  
        Let location 52 contain 100, and location 100 contain 1023
        The format in binary looks like this:
        000001 11 00 1 0 00110100
        */        
        Word example = Word.WordFromBinaryString("000001 11 00 1 0 00110100");
        System.out.println("Raw Word Before Decomposition: "+example+"\n");              
        
        Unit opcode = example.decomposeByOffset(0,5);
        System.out.println("OPCODE: "+opcode);                
        
        Unit rfiI = example.decomposeByOffset(6,7);
        System.out.println("rfiI: "+rfiI);        
        
        Unit xfiI = example.decomposeByOffset(8,9);
        System.out.println("xfiI: "+xfiI);        
      
        Unit index = example.decomposeByOffset(10);
        System.out.println("INDEX: "+index);        

        Unit trace = example.decomposeByOffset(11);
        System.out.println("TRACE: "+trace);        
        
        Unit address = example.decomposeByOffset(12,19);
        System.out.println("ADDR: "+address);        
        
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
     * @TODO: Currently there is a for loop in here, this will be replaced by the
     * microinstruction call functionality. For now this is just temporary.
     */
    private void executeOpcodeLDR(){
        
        for(int i=0; i<2;i++){
            switch(i){
                case 0:
                  // Micro-5: Compute EA                                
                  System.out.println("Micro-5: Compute EA    ");
                  Unit effectiveAddress = this.calculateEffectiveAddress(this.instructionRegisterDecoded);                
                  System.out.println("-- Loading Effective Address: "+effectiveAddress);

                  // Micro-6: MAR<-EA
                  System.out.println("Micro-6: MAR<-EA");
                  memory.setMAR(effectiveAddress);
                  this.microState=2;                

                  break;
              case 1:
                  // Micro-7: M(MAR) -> MBR
                  System.out.println("Micro-7: M(MAR) -> MBR");
                  // do nothing, done by memory
                  this.microState=3;
                  break;

              case 2:
                  // Micro-8: MDR -> RF(RFI)   
                  System.out.println("Micro-8: MDR -> RF(RFI)");
                  int RFI = this.instructionRegisterDecoded.get("rfiI").getValue();
                  System.out.println("-- rfI: "+RFI);
                  this.xRegisters[RFI] = this.memory.getMBR();
                  System.out.println("-- Value of EA: "+this.memory.getMBR());
                  break;
            }
        }
            
                                
    }
    
}
