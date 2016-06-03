/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neto_framework.packets;

import java.io.IOException;
import net.neto_framework.Connection;
import net.neto_framework.Packet;

/**
 * The handshake response packet is sent from server -> client after the server has received a
 * handshake.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class HandshakeResponsePacket implements Packet {
    
    /**
     * The actual magic string value that was received.
     */
    private String value;
    
    /**
     * UUID assigned by server.
     */
    private String uuid;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(HandshakePacket.MAGIC_STRING);
        connection.sendString(uuid);
    }
    
    @Override
    public void receive(Connection connection) throws IOException {
        this.value = connection.receiveString();
        this.uuid = connection.receiveString();
    }
    
    /**
     * @return The actual magic string value that was received.
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * @param uuid UUID of client.
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    
    /**
     * @return UUID given by server.
     */
    public String getUUID() {
        return this.uuid;
    }

    @Override
    public int getId() {
        return -2;
    }
}
