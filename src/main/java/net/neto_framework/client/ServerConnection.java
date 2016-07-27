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

package net.neto_framework.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Base64;
import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.Protocol;
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.DisconnectEvent.DisconnectReason;
import net.neto_framework.client.event.events.PacketExceptionEvent;
import net.neto_framework.exceptions.PacketException;

/**
 * A connection thread to handle the server connection.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnection implements Runnable {
    
    /**
     * Running instance of {@link net.neto_framework.client.Client Client}.
     */
    private final Client client;
    
    /**
     * Server TCP Connection.
     */
    private final Connection tcpConnection;
    
    /**
     * Server UDP Connection.
     */
    private final Connection udpConnection;

    /**
     * New ServerConnection.
     * 
     * @param client Running instance of {@link net.neto_framework.client.Client Client}.
     * @param tcpConnection TCP {@link net.neto_framework.Connection Connection}.
     * @param udpConnection UDP {@link net.neto_framework.Connection Connection}.
     */
    public ServerConnection(Client client, Connection tcpConnection, Connection udpConnection) {
        this.client = client;
        this.tcpConnection = tcpConnection;
        this.udpConnection = udpConnection;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Neto-Framework Client TCP Handler");
        
        while (this.client.isConnected()) {
            try {
                int packetId = this.tcpConnection.receiveInteger();
                long timestamp = 0;
                String uuidString;
                
                if(this.client.getUUID() != null) {
                    uuidString = this.tcpConnection.receiveString();
                    timestamp = this.tcpConnection.receiveLong();
                    
                    if(!this.client.getUUID().toString().equals(uuidString)) {
                        if(packetId != 0 && !uuidString.equals("")) {
                            PacketException exception = new PacketException("UUID does not match.");
                            PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.client,
                                    exception);
                            this.client.getEventHandler().callEvent(packetEvent);

                            this.client.disconnect();
                            DisconnectEvent event = new DisconnectEvent(this.client,
                                    DisconnectReason.EXCEPTION, exception);
                            this.client.getEventHandler().callEvent(event);
                            break;
                        } else {
                            this.client.disconnect();
                            DisconnectEvent event = new DisconnectEvent(this.client,
                                    DisconnectReason.EXCEPTION, new IOException("Connection "
                                            + "closed."));
                            this.client.getEventHandler().callEvent(event);
                            break;
                        }
                    }
                }
                
                if(this.client.getPacketManager().hasPacket(packetId)) {
                    if(this.client.getUUID() != null &&
                            (System.currentTimeMillis() - timestamp) > Connection.REPLAY_WINDOW) {
                        this.client.getPacketManager().receive(this.client, packetId, this,
                                Protocol.TCP, true);
                    } else {
                        this.client.getPacketManager().receive(this.client, packetId, this,
                                Protocol.TCP, false);
                    }
                } else {
                    PacketException exception = new PacketException("Unkown packet received.");
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.client,
                            exception);
                    this.client.getEventHandler().callEvent(packetEvent);

                    this.client.disconnect();
                    DisconnectEvent event = new DisconnectEvent(this.client,
                            DisconnectReason.EXCEPTION, exception);
                    this.client.getEventHandler().callEvent(event);
                    break;
                }
            } catch (IOException e) {
                if(!this.client.getTcpSocket().isClosed()) {
                    PacketException exception = new PacketException(
                            "Failed to receive packet.", e);
                    this.client.getEventHandler().callEvent(new PacketExceptionEvent(this.client,
                            exception));
                    
                    this.client.disconnect();
                    DisconnectEvent event = new DisconnectEvent(this.client,
                            DisconnectReason.EXCEPTION, exception);
                    this.client.getEventHandler().callEvent(event);
                    break;
                }
                
                break;
            }
        }
    }

    /**
     * Send server packet.
     * 
     * @param packet The {@link net.neto_framework.Packet Packet} to send.
     * @param protocol What {@link net.neto_framework.Protocol Protocol} to use when sending.
     * @throws IOException If fails to send packet.
     */
    public synchronized void sendPacket(Packet packet, Protocol protocol) throws IOException {
        if(this.client.getPacketManager().hasPacket(packet.getId())) {
            if(protocol == Protocol.TCP) {
                this.tcpConnection.sendInteger(packet.getId());

                if(this.client.getUUID() != null) {
                    this.tcpConnection.sendString(this.client.getUUID().toString());
                    this.tcpConnection.sendLong(System.currentTimeMillis());
                }

                packet.send(this.tcpConnection);
            } else {
                this.udpConnection.sendInteger(packet.getId());
                this.udpConnection.sendString(this.client.getUUID().toString());
                this.udpConnection.sendLong(System.currentTimeMillis());
                packet.send(this.udpConnection);
                byte[] data = this.udpConnection.getUdpData();
                data = Base64.getEncoder().withoutPadding().encode(data);
                
                DatagramPacket dataPacket = new DatagramPacket(data, data.length,
                        this.udpConnection.getAddress(), this.udpConnection.getPort());
                this.client.getUdpSocket().send(dataPacket);
            }
        } else {
            throw new RuntimeException("Attempt to send unregistered packet.");
        }
    }
    
    /**
     * Enable encryption. (Used during handshake process).
     */
    public void enableEncryption() {
        this.tcpConnection.enableEncryption(this.client.getSecretKey(),
                this.client.getIvParameterSpec());
        this.udpConnection.enableEncryption(this.client.getSecretKey(),
                this.client.getIvParameterSpec());
    }
    
    /**
     * @return TCP {@link net.neto_framework.Connection Connection} to server.
     */
    public Connection getTCPConnection() {
        return this.tcpConnection;
    }
    
    /**
     * @return UDP {@link net.neto_framework.Connection Connection} to server.
     */
    public Connection getUDPConnection() {
        return this.udpConnection;
    }
}
