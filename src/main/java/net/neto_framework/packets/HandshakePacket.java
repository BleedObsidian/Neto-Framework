/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neto_framework.packets;

import java.io.IOException;
import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.Protocol;
import net.neto_framework.client.Client;
import net.neto_framework.client.ServerConnection;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent.ClientDisconnectReason;
import net.neto_framework.server.event.events.PacketExceptionEvent;

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
    
    @Override
    public void onServerReceive(Server server, ClientConnection client, Packet packet) {
        if(this.value.equals(HandshakePacket.MAGIC_STRING)) {
            HandshakeResponsePacket response = new HandshakeResponsePacket();
            response.setUUID(client.getUUID().toString());
            
            try {
                client.sendPacket(response, Protocol.TCP);
            } catch (IOException e) {
                PacketException exception = new PacketException("Failed to send handshake "
                        + "reponse.", e);
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(server, exception);
                server.getEventHandler().callEvent(packetEvent);

                client.disconnect();
                ClientDisconnectEvent event = new ClientDisconnectEvent(server,
                        ClientDisconnectReason.EXCEPTION, client.getUUID(), exception);
                server.getEventHandler().callEvent(event);
                return;
            }
            
            Connection udpConnection = new Connection(server.getUdpSocket(),
                    client.getTCPConnection().getTCPSocket().getInetAddress(), this.udpPort);
            client.addUdpConnection(udpConnection);
            
            server.getConnectionManager().onConnectionValidated(client.getUUID());
        } else {
            PacketException exception = new PacketException("Incorrect magic string received.");
            PacketExceptionEvent packetEvent = new PacketExceptionEvent(server, exception);
            server.getEventHandler().callEvent(packetEvent);
            
            client.disconnect();
            ClientDisconnectEvent event = new ClientDisconnectEvent(server,
                    ClientDisconnectReason.EXCEPTION, client.getUUID(), exception);
            server.getEventHandler().callEvent(event);
        }
    }

    @Override
    public void onClientReceive(Client client, ServerConnection connection, Packet packet) {
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
