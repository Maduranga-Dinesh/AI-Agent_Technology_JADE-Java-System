
package Agents;


public class Tokensystem {

    private int slot_number;
    private String toekn_option;
    private String date;

    public Tokensystem(int slot_number, String toekn_option, String date) {
        this.slot_number = slot_number;
        this.toekn_option = toekn_option;
        this.date = date;
    }
    
    

    public int getSlot_number() {
        return slot_number;
    }

    public void setSlot_number(int slot_number) {
        this.slot_number = slot_number;
    }

    public String getToekn_option() {
        return toekn_option;
    }

    public void setToekn_option(String toekn_option) {
        this.toekn_option = toekn_option;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
