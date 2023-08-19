
package Agents;

import Library.LibraryInterface;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Hashtable;

public class LibraryAdmin extends Agent {

    public static Hashtable appointmentRec;
    // The GUI by means of which the user can add books in the catalogue
    private LibraryInterface myGui;

    // Put agent initializations here
    protected void setup() {

        System.out.println("[*] Agent - Book " + getAID().getName() + " was created.");
        appointmentRec = new Hashtable();

        // Create and show the GUI
        myGui = new LibraryInterface(this);
        myGui.showGui();

        // Register the rec book info
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Appointment-book");
        sd.setName("Appointmen portfolio JADE");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        
        addBehaviour(new OfferRequestsServer());

        
        addBehaviour(new BookingAppointmentServer());
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        myGui.dispose();
        // Printout a dismissal message
        System.out.println("[*] Book [Agent] " + getAID().getName() + " has finished.");
    }

    /**
     * This is invoked by the GUI when the user adds a new book for appointment
     * adding
     */
    public void updateRecBook(final String appointment_Type, final int solt_Number, final String token_option, final String date) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                appointmentRec.put(appointment_Type, new Tokensystem(new Integer(solt_Number), token_option, date));
                System.out.println("New Appointment booking added to " + getAID().getName() + " appointment Type : " + appointment_Type + " Token : "
                        + solt_Number + " , date : " + date + " token option : " + token_option);
            }
        });
    }

    private class OfferRequestsServer extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String appointmentType = msg.getContent();
                ACLMessage answer = msg.createReply();

                Tokensystem token = (Tokensystem) appointmentRec.get(appointmentType);
                if (token != null) {
                    // The requested appointment is available for booking. Reply with the slot
                    answer.setPerformative(ACLMessage.PROPOSE);
                    answer.setContent(String.valueOf(token.getSlot_number()));
                } else {
                    // The requested product is NOT available for booking.
                    answer.setPerformative(ACLMessage.REFUSE);
                    answer.setContent("Not Available");
                }
                myAgent.send(answer);
            } else {
                block();
            }
        }

    }  // End of inner class OfferRequestsServer

    private class BookingAppointmentServer extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String appointmentType = msg.getContent();
                ACLMessage answer = msg.createReply();

                Tokensystem token = (Tokensystem) appointmentRec.remove(appointmentType);
                if (token != null) {
                    answer.setPerformative(ACLMessage.INFORM);
                    System.out.println("The Appointment " + appointmentType + " was assign to agent " + msg.getSender().getName());
                } else {
                    // The requested booking has been allocated 
                    answer.setPerformative(ACLMessage.FAILURE);
                    answer.setContent("Not Available");
                }
                myAgent.send(answer);
            } else {
                block();
            }
        }

    }  // End of inner class OfferRequestsServer

}
