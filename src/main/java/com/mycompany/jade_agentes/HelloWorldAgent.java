/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jade_agentes;

import jade.core.Agent;
import java.util.Iterator;

/**
 *
 * @author utente
 */
public class HelloWorldAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("Hola Mundo. Soy un agente.");
        System.out.println("Mi nombre local es: " + getAID().getLocalName());
        System.out.println("Mi GUID es: " + getAID().getName());
        
        System.out.println("Mis direcciones son: ");
        Iterator it = getAID().getAllAddresses();     
        while (it.hasNext()) {
        System.out.println("- " + it.next());
        }
    }
}
