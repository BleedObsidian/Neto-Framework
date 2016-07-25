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
import java.util.Base64;
import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.packets.DisconnectPacket;
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
    private final Connection tcpConnection;
    
    /**
     * A HashMap that can be used to store anything about clients.
     */
    private final HashMap<String, Object> storage;
    
    /**
     * UDP connection.
     */
    private Connection udpConnection;
    
    /**
     * The secret key used for packet encryption.
     */
    private SecretKey secretKey;
    
    /**
     * The IV parameter spec used for ciphering.
     */
    private IvParameterSpec ivParameterSpec;
    
    /**
     * If the client is currently connected.
     */
    private boolean isConnected;
    
    /**
     * A timer used to kick a client if they do not complete the handshake process in enough time.
     */
    private Timer handshakeTimer;
    
    /**
     * If the client and server have completed the handshake process.
     */
    private boolean isHandshakeCompleted = false;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param uuid UUID of client.
     * @param tcpConnection TCP {@link net.neto_framework.Connection Connection}.
     */
    public ClientConnection(Server server, UUID uuid, Connection tcpConnection) {
        this.server = server;
        this.uuid = uuid;
        this.tcpConnection = tcpConnection;
        this.storage = new HashMap<>();
        this.isConnected = true;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Neto-Framework Server Client: " + this.uuid.toString());
        
        while (this.server.isRunning() && this.isConnected) {
            int packetId;
            String uuidString = "";
            
            try {
                packetId = this.tcpConnection.receiveInteger();
                
                if(this.isHandshakeCompleted) {
                    uuidString = this.tcpConnection.receiveString();
                }
            } catch (IOException e) {
                if(!this.tcpConnection.getTCPSocket().isClosed()) {
                    PacketException exception = new PacketException("Failed to read packet. Client"
                            + " most likely closed the connection without sending a disconnect"
                            + " packet.", e);
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.server,
                            exception, this.uuid);
                    this.server.getEventHandler().callEvent(packetEvent);

                    this.disconnect(false);
                    ClientDisconnectEvent event = new ClientDisconnectEvent(this.server, 
                            ClientDisconnectReason.EXCEPTION, this.uuid, exception);
                    this.server.getEventHandler().callEvent(event);
                    break;
                } else {
                    break;
                }
            }
            
            if(this.isHandshakeCompleted && !this.uuid.toString().equals(uuidString)) {
                PacketException exception = new PacketException("UUID does not match.");
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.server, exception,
                        this.uuid);
                this.server.getEventHandler().callEvent(packetEvent);
                
                this.disconnect(false);
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
                    
                    this.disconnect(false);
                    ClientDisconnectEvent event = new ClientDisconnectEvent(this.server, 
                            ClientDisconnectReason.EXCEPTION, this.uuid, exception);
                    this.server.getEventHandler().callEvent(event);
                    break;
                }
            } else {
                PacketException exception = new PacketException("Unkown packet received." +
                        packetId);
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.server, exception,
                        this.uuid);
                this.server.getEventHandler().callEvent(packetEvent);
                
                this.disconnect(false);
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
    public synchronized void sendPacket(Packet packet, Protocol protocol) throws IOException {
        if(this.server.getPacketManager().hasPacket(packet.getId())) {
            if(protocol == Protocol.TCP) {
                this.tcpConnection.sendInteger(packet.getId());
                
                if(this.isHandshakeCompleted) {
                    this.tcpConnection.sendString(this.uuid.toString());
                }
                
                packet.send(this.tcpConnection);
            } else {
                this.udpConnection.sendInteger(packet.getId());
                this.udpConnection.sendString(this.uuid.toString());
                packet.send(this.udpConnection);
                byte[] data = this.udpConnection.getUdpData();
                data = Base64.getEncoder().withoutPadding().encode(data);

                DatagramPacket dataPacket = new DatagramPacket(data, data.length,
                        this.udpConnection.getAddress(), this.udpConnection.getPort());
                this.server.getUdpSocket().send(dataPacket);
            }
        } else {
            throw new RuntimeException("Attempt to send unregistered packet.");
        }
    }
    
    /**
     * Disconnect client from the server.
     * 
     * @param sendDisconnectPacket If true, sends a disconnect packet to the client before closing.
     */
    public synchronized void disconnect(boolean sendDisconnectPacket) {
        if(sendDisconnectPacket) {
            try {
                this.sendPacket(new DisconnectPacket(), Protocol.TCP);
            } catch (IOException e) {} //TODO: Log
        }
        
        try {
            this.tcpConnection.getTCPSocket().close();
        } catch (IOException e) { } //TODO: Log

        this.server.getConnectionManager().removeClientConnection(this.uuid);
        this.isConnected = false;
    }
    
    /**
     * Enable encryption.
     */
    public void enableEncryption() {
        this.tcpConnection.enableEncryption(this.secretKey, this.ivParameterSpec);
        this.udpConnection.enableEncryption(this.secretKey, this.ivParameterSpec);
    }
    
    /**
     * Store data with given key.
     * 
     * @param key A key to identify this data.
     * @param data Data to be stored.
     */
    public void store(String key, Object data) {
        this.storage.put(key, data);
    }
    
    /**
     * Retrieve data with given key.
     * 
     * @param key The key.
     * @return Data (May be null).
     */
    public Object retrieve(String key) {
        return this.storage.get(key);
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
     * @return The secret key used for packet encryption.
     */
    public SecretKey getSecretKey() {
        return this.secretKey;
    }
    
    /**
     * @param secretKey The secret key used for packet encryption.
     */
    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }
    
    /**
     * @return The IV parameter spec used for ciphering.
     */
    public IvParameterSpec getIvParameterSpec() {
        return this.ivParameterSpec;
    }
    
    /**
     * @param ivParameterSpec The IV parameter spec used for ciphering.
     */
    public void setIvParameterSpec(IvParameterSpec ivParameterSpec) {
        this.ivParameterSpec = ivParameterSpec;
    }
    
    /**
     * @return {@link java.util.Timer Timer} for handshake process.
     */
    public Timer getTimer() {
        return this.handshakeTimer;
    }
    
    /**
     * @param timer {@link java.util.Timer Timer} for handshake process.
     */
    public void setTimer(Timer timer) {
        this.handshakeTimer = timer;
    }
    
    /**
     * @return If the client and server have completed the handshake process.
     */
    public boolean isHandshakeCompleted() {
        return this.isHandshakeCompleted;
    }
    
    /**
     * @param value If the client and server have completed the handshake process.
     */
    public void setHandshakeCompleted(boolean value) {
        this.isHandshakeCompleted = value;
    }
    
    /**
     * Disconnect client from the server.
     */
    public void disconnect() {
        this.disconnect(true);
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
