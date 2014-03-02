/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package computersimulator.io;
import computersimulator.components.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import computersimulator.cpu.Computer;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author vanlee
 */
public class ReadFilebyJava {

    
    public ReadFilebyJava() 
    {
        
   
        
        
    }
    public void ReadFromFile(Computer computer,Unit addr)
    {
             try {
             FileReader reader=new FileReader("src/computersimulator/io/input.txt");
             BufferedReader br = new BufferedReader(reader);
             String strLine=null;
             while((strLine=br.readLine())!=null)
             {
                 
                 String str=strLine.substring(0, 20);
                 addr.setValue(addr.getUnsignedValue()+1);
                 Word instructions=new Word();
                 instructions.setValueBinary(str);
                 computer.getMemory().engineerSetMemoryLocation(addr, instructions);
             }
       } catch (FileNotFoundException ex) {
            Logger.getLogger(ReadFilebyJava.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReadFilebyJava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
