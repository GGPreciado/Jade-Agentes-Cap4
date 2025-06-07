/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jade_agentes;

/**
 *
 * @author utente
 */
public interface BookSellerGui {
    void setAgent(BookSellerAgent a);

    void show();

    void hide();

    void notifyUser(String string);

    void dispose();
}
