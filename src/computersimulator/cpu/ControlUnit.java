package computersimulator.cpu;

import computersimulator.components.Unit;
import computersimulator.components.Word;

/**
 * It is the task of the control unit to fetch the next instruction from 
 *   memory to be executed, decode it (i.e., determine what is to be done), and 
 *   execute it by issuing the appropriate command to the ALU, memory , and the 
 *   I/O controllers.
 * @author george
 */
public class ControlUnit {

    // PC	13 bits	Program Counter: address of next instruction to be executed
    private Unit programCounter;
    
    //IR	20 bits	Instruction Register: holds the instruction to be executed
    private Word instructionRegister;
    
    //MSR	20 bits	Machine Status Register: certain bits record the status of the health of the machine
    private Word machineStatusRegister;

    //MFR	4 bits	Machine Fault Register: contains the ID code if a machine fault after it occurs
    private Unit machineFaultRegister;
    
    //X1…X3	13 bits	Index Register: contains a 13-bit base address that supports base register addressing of memory.
    private Unit[] xRegisters = new Unit[4];
    
    
    public ControlUnit() {
        this.instructionRegister = new Word();
        this.programCounter = new Unit(13);
        this.machineStatusRegister = new Word();
        this.machineFaultRegister = new Unit(4);
        
        for(int x=0;x<4;x++){
            this.xRegisters[x] = new Unit(13);
        }
                  
        
    }
    
    
        
      /**
         * 
fetch the next instruction from memory to be executed,
decode it (i.e., determine what is to be done), and
execute it by issuing the appropriate command to the ALU, memory , and the I/O controllers.

* These three fundamental steps are repeated over and over until we reach the 
* last instruction in the program, typically something called HALT, STOP, or QUIT. 
* This is commonly referred to as the instruction cycle. Thus, in order to understand 
* the behavior of the CU we must first investigate the characteristics of machine language instructions.
         */
    
    
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
        
        Unit rfi1 = example.decomposeByOffset(6,7);
        System.out.println("RFI1: "+rfi1);        
        
        Unit xfi1 = example.decomposeByOffset(8,9);
        System.out.println("XFI1: "+xfi1);        
      
        Unit index = example.decomposeByOffset(10);
        System.out.println("INDEX: "+index);        

        Unit trace = example.decomposeByOffset(11);
        System.out.println("TRACE: "+trace);        
        
        Unit address = example.decomposeByOffset(12,19);
        System.out.println("ADDR: "+address);        
        
    }
    

    
    
}
