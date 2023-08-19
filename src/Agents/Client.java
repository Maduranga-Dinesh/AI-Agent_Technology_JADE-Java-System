
package Agents;

import static Agents.LibraryAdmin.appointmentRec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Client extends Agent {

    // The name of the appointment 
    private String allocateAppointment;
    private String allocateDate;
    private String LibraryAdmin;
    
    private AID[] AdminAgents;
    // Time to request product to agents
    private static final int REQUEST_TIME = 30000;

    private String AS_SOON = "YES";

    // Put agent initializations here
    protected void setup() {
        // Print a welcome message
        System.out.println("[*] Agent - Client " + getAID().getName() + " was created.");

        // Get the name of the appointment type as a start-up argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            allocateAppointment = (String) args[0];
            allocateDate = (String) args[1];
            try {
                if (args.length > 2) {
                    String my_book = (String) args[2];
                    if (my_book != null && !my_book.isEmpty()) {
                        LibraryAdmin = my_book;
                    }
                }
            } catch (Exception e) {
                System.out.println("[*] Couldn't Find Allocating Book's Name");
            }

            System.out.println("Appointment required - " + allocateAppointment + " for date - " + allocateDate + " (Book - " + LibraryAdmin + ")");
            
            addBehaviour(new TickerBehaviour(this, REQUEST_TIME) {
                protected void onTick() {
                    System.out.println("Trying to allocate " + allocateAppointment);
                    // Update the list of seller agents
                    DFAgentDescription agentDesc = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("Appointment-book");
                    agentDesc.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, agentDesc);
                        System.out.println("Book assign: (Appointment Type : " + allocateAppointment + " )");
                        AdminAgents = new AID[result.length];
                        if (result.length > 0) {
                            for (int i = 0; i < result.length; ++i) {
                                Tokensystem tracker = (Tokensystem) appointmentRec.get(allocateAppointment);
                                if (tracker != null) {
                                    String my_date = tracker.getDate();
                                    if (my_date.equalsIgnoreCase(allocateDate)) {
                                        if (LibraryAdmin == null || LibraryAdmin.isEmpty()) {
                                            AdminAgents[i] = result[i].getName();
                                            System.out.println("User : " + AdminAgents[i]);
                                            System.out.println("Found book Agent By :" + AdminAgents[i].getName());
                                        } else {
                                            System.out.println("Requested Admin : " + LibraryAdmin);
                                            String found_book = ("" + result[i].getName()).split("@")[0];
                                            System.out.println("Found book : " + found_book);
                                            if (LibraryAdmin != null && LibraryAdmin.equalsIgnoreCase(found_book) && tracker.getToekn_option().equalsIgnoreCase(AS_SOON)) {
                                                System.out.println(" Book found, But, not allowed to Immediate service");
                                            } else {
                                                AdminAgents[i] = result[i].getName();
                                                System.out.println("User : " + AdminAgents[i]);
                                                System.out.println("Found Book Agent By :" + AdminAgents[i].getName());
                                            }

                                        }

                                    } else {
                                        System.out.println("Appointment Type found, but different dates");
                                    }
                                } else {
                                    System.out.println("No resulting value found");
                                }

                            }
                        } else {
                            System.out.println("No Results found");
                        }
                        System.out.println("-----------");
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }

                    // Perform the request
                    myAgent.addBehaviour(new BookingRequest());
                }
            });
        } else {
            // Make the agent terminate
            System.out.println("The requested book is not available.");
            doDelete();
        }
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Client Agent " + getAID().getName() + " has finished.");
    }

  
    private class BookingRequest extends Behaviour {

        private AID selectedDoctor; 
        private String selectedSlot;  
        private int repliesCnt = 0; 
        private MessageTemplate mt; 
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < AdminAgents.length; ++i) {
                        cfp.addReceiver(AdminAgents[i]);
                    }
                    cfp.setContent(allocateAppointment);
                    cfp.setConversationId("Appointment-Commerce");
                    cfp.setReplyWith("cfp " + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Appointment-Commerce"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            String selected_slot = reply.getContent();
                            if (selectedDoctor == null || !selected_slot.equalsIgnoreCase(selectedSlot)) {
                                // This is the best option at present
                                Tokensystem tracker = (Tokensystem) appointmentRec.get(allocateAppointment);
                                if (tracker != null) {
                                    String my_date = tracker.getDate();
                                    if (my_date.equalsIgnoreCase(allocateDate)) {
                                        selectedSlot = selected_slot;
                                        selectedDoctor = reply.getSender();
                                        System.out.println("Define parameters : " + reply.getAllUserDefinedParameters());
                                    } else {
                                        System.out.println("Appointment Type found, but different dates");
                                    }
                                } else {
                                    System.out.println("No resulting value found");
                                }

                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= AdminAgents.length) {
                            // We received all replies
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    
                    ACLMessage booking = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    booking.addReceiver(selectedDoctor);
                    booking.setContent(allocateAppointment);
                    booking.setConversationId("Appointment-Commerce");
                    booking.setReplyWith("Booking " + System.currentTimeMillis());
                    myAgent.send(booking);
                   
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Appointment-Commerce"), MessageTemplate.MatchInReplyTo(booking.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receive the booking reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // booking reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // allocating successful. We can terminate
                            System.out.println(allocateAppointment + " has been allocated to agent " + reply.getSender().getName());
                            System.out.println("Slot = " + selectedSlot);
                            myAgent.doDelete();
                        } else {
                            System.out.println("Error: Request slot already allocated");
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            if (step == 2 && selectedDoctor == null) {
                System.out.println("Error: " + allocateAppointment + " is not for booking.");
            }
            return ((step == 2 && selectedDoctor == null) || step == 4);
        }
    }  // End of inner class RequestPerformer

}
