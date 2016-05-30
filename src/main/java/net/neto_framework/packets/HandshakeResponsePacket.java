/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neto_framework.packets;

import java.io.IOException;
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.client.Client;
import net.neto_framework.client.ServerConnection;
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.DisconnectEvent.DisconnectReason;
import net.neto_framework.client.event.events.PacketExceptionEvent;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;

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
    
    @Override
    public void onServerReceive(Server server, ClientConnection client, Packet packet) {
    }

    @Override
    public void onClientReceive(Client client, ServerConnection connection, Packet packet) {
        if(this.value.equals(HandshakePacket.MAGIC_STRING)) {
            client.setUUID(UUID.fromString(this.uuid));
        } else {
            PacketException exception = new PacketException("Incorrect magic string received.");
            PacketExceptionEvent packetEvent = new PacketExceptionEvent(client, exception);
            client.getEventHandler().callEvent(packetEvent);
            
            client.disconnect();
            DisconnectEvent event = new DisconnectEvent(client, DisconnectReason.EXCEPTION, 
                    exception);
            client.getEventHandler().callEvent(event);
        }
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
