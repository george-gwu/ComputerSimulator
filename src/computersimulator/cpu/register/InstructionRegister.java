/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package computersimulator.cpu.register;

/**
 *
 * @author pawel
 */
public class InstructionRegister extends Register {
    public InstructionRegister(String name, int length) {
        super.setName(name);
        super.setLength(length);
    }
}
