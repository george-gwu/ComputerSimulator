package computersimulator.cpu;

import computersimulator.components.Word;

/**
 * InputOutputController - I/O operations communicate with peripherals attached to the computer system. 
 * We need to simulate a card reader by reading a file from disk. We also need to simulate a GUI with a 
 * console printer and a pane that simulates a console keyboard.
 * @author george
 */
public class InputOutputController implements IClockCycle {
    
    /**
     * 
     * DEVID	Device
        0	Console Keyboard
        1	Console Printer
        2	Card Reader
        3-16	Console Registers, switches, etc
     */

    public InputOutputController() {
        
    }
    
    
    public Word input(int DEVID){
        // @TODO: Return Word from Device by DEVID
        return new Word(0);
    }
    
    public void output(int DEVID, Word value){
        //@TODO: Push word to IO device by DEVID
    }
    
    /**
     * Check Status of Device
     * @param DEVID
     * @return status
     */
    public int checkStatus(int DEVID){
        //@TODO: Look up status of DEVID by communicating with device, then return a code
        // codes: 0-none, 1-busy, others?
        return 0;
    }
    
    
    /**
     * Clock cycle. This is the main function which causes the IOController to do work.
     *  This serves as a publicly accessible method, but delegates to other methods.
     */
    public void clockCycle(){
        // @TODO: Stubbed until Part 3
    }  
    
}
