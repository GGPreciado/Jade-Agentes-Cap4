package com.mycompany.jade_agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Vector;
import java.util.Date;

public class BookBuyerAgent extends Agent {

    // The list of known seller agents
    private Vector sellerAgents;

    // The GUI to interact with the user
    private BookBuyerGui myGui;

    /**
     * Agent initializations
     */
    @Override
    protected void setup() {
        // Printout a welcome message
        System.out.println("Buyer-agent " + getAID().getName() + " is ready.");
        
        // Behaviour para saludar al agente Seller
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(getLocalName() + " recibió: " + msg.getContent());

                    // Responder al AgenteB
                    ACLMessage newMsg = new ACLMessage(ACLMessage.INFORM);
                    newMsg.addReceiver(new AID("AgenteBookSeller", AID.ISLOCALNAME));
                    newMsg.setContent("AgenteA recibió: \"" + msg.getContent() + "\" y responde a AgenteB.");
                    send(newMsg);

                    System.out.println(getLocalName() + " envió un mensaje a AgenteB.");
                } else {
                    block();
                }
            }
        });
        
        
        
        // Update the list of seller agents every minute
//        addBehaviour(new TickerBehaviour(this, 60000) {
//            protected void onTick() {
//                // Update the list of seller agents
//                DFAgentDescription template = new DFAgentDescription();
//                ServiceDescription sd = new ServiceDescription();
//                sd.setType("Book-selling");
//                template.addServices(sd);
//                try {
//                    DFAgentDescription[] result = DFService.search(myAgent,
//                            template);
//                    sellerAgents.clear();
//                    for (int i = 0; i < result.length; ++i) {
//                        sellerAgents.addElement(result[i].getName());
//                    }
//                } catch (FIPAException fe) {
//                    fe.printStackTrace();
//                }
//            }
//        });
//        // Show the GUI to interact with the user
//        myGui = new BookBuyerGuiImpl();
//        myGui.setAgent(this);
//        myGui.show();
    }

    /**
     * Agent clean-up
     */
    @Override
    protected void takeDown() {
        // Dispose the GUI if it is there
//        if (myGui != null) {
//            myGui.dispose();
//        }
        // Printout a dismissal message
        System.out.println("Buyer-agent " + getAID().getName() + "terminated.");
    }

    /**
     * This method is called by the GUI when the user inserts a new book to buy
     *
     * @param title The title of the book to buy
     * @param maxPrice The maximum acceptable price to buy the book
     * @param deadline The deadline by which to buy the book
     */
    public void purchase(String title, int maxPrice, Date deadline) {
        addBehaviour(new PurchaseManager(this, title, maxPrice,
                deadline));
    }

    private class PurchaseManager extends TickerBehaviour {

        private String title;
        private int maxPrice;
        private long deadline, initTime, deltaT;

        private PurchaseManager(Agent a, String t, int mp, Date d) {
            super(a, 60000); // tick every minute
            title = t;
            maxPrice = mp;
            deadline = d.getTime();
            initTime = System.currentTimeMillis();
            deltaT = deadline - initTime;
        }

        public void onTick() {
            long currentTime = System.currentTimeMillis();
            if (currentTime > deadline) {
                // Deadline expired
//                myGui.notifyUser("Cannot buy book " + title);
                stop();
            } else {
                // Compute the currently acceptable price and start a negotiation
                long elapsedTime = currentTime - initTime;
                int acceptablePrice = (int) (maxPrice * (elapsedTime / deltaT));
                myAgent.addBehaviour(new BookNegotiator(title,
                        acceptablePrice, this));
            }
        }
    }

    /**
     * Inner class BookNegotiator. This is the behaviour used by Book-buyer
     * agents to actually negotiate with seller agents the purchase of a book.
     */
    private class BookNegotiator extends Behaviour {

        private String title;
        private int maxPrice;
        private PurchaseManager manager;
        private AID bestSeller; // The seller agent who provides the best offer
        private int bestPrice; // The best offered price
        private int repliesCnt = 0; // The counter of replies from seller agents

        private MessageTemplate mt; // The template to receive replies
        private int step = 0;

        public BookNegotiator(String t, int p, PurchaseManager m) {
            super(null);
            title = t;
            maxPrice = p;
            manager = m;
        }

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.size(); ++i) {
                        cfp.addReceiver((AID) sellerAgents.get(i));
                    }
                    cfp.setContent(title);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value 
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(
                            MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                // This is the best offer at present
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.size()) {
                            // We received all replies
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    if (bestSeller != null && bestPrice <= maxPrice) {
                        // Send the purchase order to the seller that provided the best offer
                        ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        order.addReceiver(bestSeller);
                        order.setContent(title);
                        order.setConversationId("book-trade");
                        order.setReplyWith("order" + System.currentTimeMillis());
                        myAgent.send(order);
                        // Prepare the template to get the purchase order reply
                        mt = MessageTemplate.and(
                                MessageTemplate.MatchConversationId("book-trade"),
                                MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                        step = 3;
                    } else {
                        // If we received no acceptable proposals, terminate
                        step = 4;
                    }
                    break;
                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful. We can terminate
                            myGui.notifyUser("Book " + title
                                    + " successfully purchased. Price = " + bestPrice
                            );
                            manager.stop();
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            return step == 4;
        }
    } // End of inner class BookNegotiator

}
