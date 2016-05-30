/*
    Neto-Framework, a lightweight, event driven network application framework.
    Copyright (C) 2014  BleedObsidian (Jesse Prescott)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.neto_framework.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.packets.HandshakePacket;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent.ClientDisconnectReason;
import net.neto_framework.server.event.events.PacketExceptionEvent;

/**
 * A thread to handle all client connections individually.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ClientConnection implements Runnable {
    
    /**
     * UUID of client.
     */
    private final UUID uuid;

    /**
     * Running instance of Server.
     */
    private final Server server;
    
    /**
     * TCP connection.
     */
    private Connection tcpConnection;
    
    /**
     * UDP connection.
     */
    private Connection udpConnection;
    
    /**
     * If the client is currently connected.
     */
    private boolean isConnected;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param uuid UUID of client.
     * @param tcpConnection TCP {@link net.neto_framework.Connection Connection}.
     */
    public ClientConnection(Server server, UUID uuid, Connection tcpConnection) {
        this.server = server;
        this.uuid = uuid;
        this.tcpConnection = tcpConnection;
        this.isConnected = true;
    }

    @Override
    public void run() {
        while (this.server.isRunning() && this.isConnected) {
            int packetId;
            String uuidString;
            try {
                packetId = this.tcpConnection.receiveInteger();
                uuidString = this.tcpConnection.receiveString();
            } catch (IOException e) {
                PacketException exception = new PacketException("Failed to read packet.", e);
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.server, exception,
                        this.uuid);
                this.server.getEventHandler().callEvent(packetEvent);
                
                this.disconnect();
                ClientDisconnectEvent event = new ClientDisconnectEvent(this.server, 
                        ClientDisconnectReason.EXCEPTION, this.uuid, exception);
                this.server.getEventHandler().callEvent(event);
                break;
            }
            
            if(!this.uuid.toString().equals(uuidString) &&
                    packetId != (new HandshakePacket()).getId()) {
                PacketException exception = new PacketException("UUID does not match.");
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.server, exception,
                        this.uuid);
                this.server.getEventHandler().callEvent(packetEvent);
                
                this.disconnect();
                ClientDisconnectEvent event = new ClientDisconnectEvent(this.server, 
                        ClientDisconnectReason.EXCEPTION, this.uuid, exception);
                this.server.getEventHandler().callEvent(event);
                break;
            }
                
            if (this.server.getPacketManager().hasPacket(packetId)) {
                try {
                    this.server.getPacketManager().receive(this.server, packetId, this,
                            Protocol.TCP);
                } catch (IOException e) {
                    PacketException exception = new PacketException("Failed to read packet.", e);
                    this.server.getEventHandler().callEvent(new PacketExceptionEvent(this.server, 
                                    exception));
                    
                    this.disconnect();
                    ClientDisconnectEvent event = new ClientDisconnectEvent(this.server, 
                            ClientDisconnectReason.EXCEPTION, this.uuid, exception);
                    this.server.getEventHandler().callEvent(event);
                    break;
                }
            } else {
                PacketException exception = new PacketException("Unkown packet received.");
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.server, exception,
                        this.uuid);
                this.server.getEventHandler().callEvent(packetEvent);
                
                this.disconnect();
                ClientDisconnectEvent event = new ClientDisconnectEvent(this.server, 
                        ClientDisconnectReason.EXCEPTION, this.uuid, exception);
                this.server.getEventHandler().callEvent(event);
                break;
            }
        }
    }

    /**
     * Send client packet.
     * 
     * @param packet The {@link net.neto_framework.Packet Packet} to send.
     * @param protocol What {@link net.neto_framework.Protocol Protocol} to use when sending.
     * @throws IOException If fails to send packet.
     */
    public void sendPacket(Packet packet, Protocol protocol) throws IOException {
        if(this.server.getPacketManager().hasPacket(packet.getId())) {
            if(protocol == Protocol.TCP) {
                this.tcpConnection.sendInteger(packet.getId());
                this.tcpConnection.sendString(this.uuid.toString());
                packet.send(this.tcpConnection);
            } else {
                this.udpConnection.sendInteger(packet.getId());
                this.udpConnection.sendString(this.uuid.toString());
                packet.send(this.udpConnection);
                byte[] data = this.udpConnection.getUdpData();

                DatagramPacket dataPacket = new DatagramPacket(data, data.length,
                        this.udpConnection.getAddress(), this.udpConnection.getPort());
                this.server.getUdpSocket().send(dataPacket);
            }
        } else {
            throw new RuntimeException("Attempt to send unregistered packet.");
        }
    }
    
    /**
     * Add the UDP connection of client. This can be used when a handshake packet is received
     * telling the server what port to communicate to the client with.
     * 
     * @param udpConnection UDP {@link net.neto_framework.Connection Connection}.
     */
    public void addUdpConnection(Connection udpConnection) {
        this.udpConnection = udpConnection;
    }
    
    /**
     * Disconnect client from the server.
     */
    public void disconnect() {
        try {
            this.tcpConnection.getTCPSocket().close();
        } catch (IOException e) { } //TODO: Log

        this.server.getConnectionManager().removeClientConnection(this.uuid);
        this.isConnected = false;
        
        //TODO: Send disconnect packet to client.
    }

    /**
     * @return UUID.
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * @return TCP {@link net.neto_framework.Connection Connection}.
     */
    public Connection getTCPConnection() {
        return this.tcpConnection;
    }
    
    /**
     * @return UDP {@link net.neto_framework.Connection Connection}.
     */
    public Connection getUDPConnection() {
        return this.udpConnection;
    }
}
