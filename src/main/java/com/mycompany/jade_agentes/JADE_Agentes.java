package com.mycompany.jade_agentes;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class JADE_Agentes {

    public static void main(String[] args) {
        // Obtener instancia del runtime JADE
        Runtime rt = Runtime.instance();

        // Configurar el perfil de la plataforma JADE
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Mostrar GUI de JADE opcionalmente

        // Crear el contenedor principal de agentes
        AgentContainer mainContainer = rt.createMainContainer(profile);

        try {
            // Crear y lanzar el agente
            AgentController agentHolaMundo = mainContainer.createNewAgent(
                "AgenteHolaMundo",                 
                "com.mycompany.jade_agentes.HelloWorldAgent",      
                null                     
            );
            AgentController agentComprador = mainContainer.createNewAgent(
                "AgenteBookBuyer",                 
                "com.mycompany.jade_agentes.BookBuyerAgent",      
                null                     
            );
            AgentController agentVendedor = mainContainer.createNewAgent(
                "AgenteBookSeller",                 
                "com.mycompany.jade_agentes.BookSellerAgent",      
                null                     
            );
            agentHolaMundo.start();
            agentComprador.start();
            agentVendedor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
