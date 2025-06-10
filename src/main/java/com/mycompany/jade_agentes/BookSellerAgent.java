/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jade_agentes;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.*;

public class BookSellerAgent extends Agent {
    // The catalogue of books available for sale

    private Map catalogue = new HashMap();
    // The GUI to interact with the user
    private BookSellerGui myGui;

    /**
     * Agent initializations
     */
    protected void setup() {
        // Printout a welcome message
        System.out.println("Seller-agent " + getAID().getName() + " is ready.");
        
        // Behaviour para recibir el mensaje del agente Buyer
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(getLocalName() + " recibió: " + msg.getContent());
                } else {
                    block();
                }
            }
        });
        
        
        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Book-selling");
        sd.setName(getLocalName() + "-Book-selling");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

//        Create and show the GUI
//        myGui = new BookSellerGuiImpl();
//        myGui.setAgent(this);
//        myGui.show();
//        // Add the behaviour serving calls for price from buyer agents
//        addBehaviour(new CallForOfferServer());
//        // Add the behaviour serving purchase requests from buyer agents
//        addBehaviour(new PurchaseOrderServer());
    }

    /**
     * Agent clean-up
     */
    protected void takeDown() {
//      Dispose the GUI if it is there
//        if (myGui != null) {
//            myGui.dispose();
//        }
        // Printout a dismissal message
        System.out.println("Seller-agent " + getAID().getName() + "terminating.");
    }

    /**
     * This method is called by the GUI when the user inserts a new book for
     * sale
     *
     * @param title The title of the book for sale
     * @param initPrice The initial price
     * @param minPrice The minimum price
     * @param deadline The deadline by which to sell the book
     */
    public void putForSale(String title, int initPrice, int minPrice, Date deadline) {
        addBehaviour(new PriceManager(this, title, initPrice, minPrice, deadline));
    }

    private class PriceManager extends TickerBehaviour {

        private String title;
        private int initPrice, currentPrice, deltaP;
        private long initTime, deadline, deltaT;

        private PriceManager(Agent a, String t, int ip, int mp, Date d) {
            super(a, 60000);
            title = t;
            initPrice = ip;
            currentPrice = initPrice;
            deltaP = initPrice - mp;
            deadline = d.getTime();
            initTime = System.currentTimeMillis();
        }

        public void onStart() {
            // Insert the book in the catalogue of books available for sale
            catalogue.put(title, this);
            super.onStart();
        }

        public void onTick() {
            long currentTime = System.currentTimeMillis();
            if (currentTime > deadline) {
                // Deadline expired
//                myGui.notifyUser("Cannot sell book " + title);
                catalogue.remove(title);
                stop();
            } else {
                // Compute the current price
                long elapsedTime = currentTime - initTime;
                currentPrice = (int) (initPrice - deltaP * (elapsedTime / deltaT));
            }
        }

        public int getCurrentPrice() {
            return currentPrice;
        }
    }

}
