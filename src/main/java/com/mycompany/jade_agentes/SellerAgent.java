package com.mycompany.jade_agentes;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class SellerAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": Iniciando y registrando servicio de venta.");

        // Creamos la descripción del agente y su servicio
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID()); // AID = Agent Identifier

        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling"); // Tipo de servicio
        sd.setName(getLocalName() + "-bookstore"); // Nombre del servicio

        dfd.addServices(sd);

        try {
            DFService.register(this, dfd); // Registramos el servicio en el DF
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this); // Se da de baja al morir
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println(getLocalName() + ": Finalizando.");
    }
}
