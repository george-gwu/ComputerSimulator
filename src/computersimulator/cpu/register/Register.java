package computersimulator.cpu.register;

/**
 *
 * @author pawel
 */
abstract class Register {
    private String name;
    private int length;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
}
