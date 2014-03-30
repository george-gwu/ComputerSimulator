package computersimulator.cpu;

import computersimulator.components.Word;
import computersimulator.io.*;

/**
 * InputOutputController - I/O operations communicate with peripherals attached to the computer system. 
 * We need to simulate a card reader by reading a file from disk. We also need to simulate a GUI with a 
 * console printer and a pane that simulates a console keyboard.
 * @author george
 */
public class InputOutputController implements IClockCycle {
    
    private CardReader cardReader;
    private ConsoleKeyboard consoleKeyboard;
    private ConsolePrinter consolePrinter;
    
    public static final int DEVICE_CONSOLEKEYBOARD=0;
    public static final int DEVICE_CONSOLEPRINTER=1;
    public static final int DEVICE_CARDREADER=2;
    //3-16	Console Registers, switches, etc
    
    public final static int STATUS_READY = 0;
    public final static int STATUS_BUSY = 1;
    public final static int STATUS_DONE = 2; // EOF

    public InputOutputController() {
        consolePrinter = new ConsolePrinter();
        consoleKeyboard = new ConsoleKeyboard();
        this.resetIOController();
    }
    
    public final void resetIOController(){
        cardReader  = new CardReader();
    }
    
    public IIODevice getDevice(int DEVID){
        switch(DEVID){
            case DEVICE_CARDREADER:
                return cardReader;
            case DEVICE_CONSOLEKEYBOARD:
                return consoleKeyboard;
            case DEVICE_CONSOLEPRINTER:
                return consolePrinter;
            default:
                return null;
        }
    }
    
    
    public Word input(int DEVID){
        IIODevice device = this.getDevice(DEVID);
        if(device!=null){
            return device.input();
        } else {
            return new Word(0);
        }        
    }
    
    public void output(int DEVID, Word value){            
        IIODevice device = this.getDevice(DEVID);
        if(device!=null){
            device.output(value);
        }          
    }
    
    /**
     * Check Status of Device
     * @param DEVID
     * @return status
     */
    public int checkStatus(int DEVID){        
        IIODevice device = this.getDevice(DEVID);
        if(device!=null){
            return device.checkStatus();
        } else {
            return 0;
        }            
    }
    
    
    /**
     * Clock cycle. This is the main function which causes the IOController to do work.
     *  This serves as a publicly accessible method, but delegates to other methods.
     */
    public void clockCycle(){
        // @TODO: Stubbed until Part 3
    }  
    
}
