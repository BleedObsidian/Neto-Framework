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
import net.neto_framework.PacketReceiver;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
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
     * Connection.
     */
    private final Connection connection;
    
    /**
     * If the client is currently connected.
     */
    private boolean isConnected;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param uuid UUID of client.
     * @param connection {@link net.neto_framework.Connection Connection}.
     */
    public ClientConnection(Server server, UUID uuid, Connection connection) {
        this.server = server;
        this.uuid = uuid;
        this.connection = connection;
        this.isConnected = true;
    }

    @Override
    public void run() {
        while (this.server.isRunning() && this.server.getProtocol() == Protocol.TCP &&
                this.isConnected) {
            int packetId;
            try {
                packetId = this.connection.receiveInteger();
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
                
            if (this.server.getPacketManager().hasPacket(packetId)) {
                try {
                    this.server.getPacketManager().receive(packetId, 
                            this.connection, PacketReceiver.SERVER);
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
     * @param packet Packet.
     * @throws IOException If fails to send packet.
     */
    public void sendPacket(Packet packet) throws IOException {
        if(this.server.getProtocol() == Protocol.TCP) {
            this.connection.sendInteger(packet.getID());
            packet.send(this.connection);
        } else {
            this.connection.sendString(this.uuid.toString());
            this.connection.sendInteger(packet.getID());
            packet.send(this.connection);
            byte[] data = this.connection.getUdpData();
            
            DatagramPacket dataPacket = new DatagramPacket(data, data.length,
                    this.connection.getAddress(), this.connection.getPort());
            this.server.getUdpSocket().send(dataPacket);
        }
    }
    
    /**
     * Disconnect client from the server.
     */
    public void disconnect() {
        if(this.server.getProtocol() == Protocol.TCP) {
            this.isConnected = false;
            this.server.getConnectionManager().removeClientConnection(this.uuid);
                
            try {
                this.connection.getTCPSocket().close();
            } catch (IOException e) { } //TODO: Log
        } else {
            this.isConnected = false;
            this.server.getConnectionManager().removeClientConnection(this.uuid);
        }
        
        //TODO: Send disconnect packet to client.
    }

    /**
     * @return UUID.
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * @return Connection.
     */
    public Connection getConnection() {
        return this.connection;
    }
}
