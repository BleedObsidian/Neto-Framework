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
 * The handshake packet is the first packet send from client -> server.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class HandshakePacket implements Packet {
    
    /**
     * The magic string that is sent to ensure the same framework is being used.
     */
    public static final String MAGIC_STRING = "sjd7dHS92jS92L02";
    
    /**
     * The actual value that was received.
     */
    private String value;
    
    /**
     * The UDP port the client is listening on.
     */
    private int udpPort;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(HandshakePacket.MAGIC_STRING);
        connection.sendInteger(this.udpPort);
    }
    
    @Override
    public void receive(Connection connection) throws IOException {
        this.value = connection.receiveString();
        this.udpPort = connection.receiveInteger();
    }
    
    /**
     * @return The actual magic string value that was received.
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * @return The UDP port the client is listening on..
     */
    public int getUdpPort() {
        return this.udpPort;
    }
    
    /**
     * @param port The UDP port the client is listening on.
     */
    public void setUdpPort(int port) {
        this.udpPort = port;
    }

    @Override
    public int getId() {
        return -1;
    }
}
