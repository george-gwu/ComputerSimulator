
package computersimulator.components;

/**
 * Special exception for handling a Machine Fault
 */
public class MachineFaultException extends Exception {
            
    // Used to identify the type of Machine Fault
    public final static int ILLEGAL_MEMORY_ADDRESS = 0;
    public final static int ILLEGAL_TRAP_CODE = 1;
    public final static int ILLEGAL_OPCODE = 2;

    private int faultID;
   
    public MachineFaultException(int fault) {
        this.faultID=fault;        
    }
    

    public int getFaultID() {
        return faultID;
    }    
    
}
