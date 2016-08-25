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
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.event.events.PacketExceptionEvent;
import net.neto_framework.server.exceptions.ConnectionException;

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
     * A HashMap that can be used to store anything about clients.
     */
    private final HashMap<String, Object> storage;
    
    /**
     * TCP connection.
     */
    private final Connection tcpConnection;
    
    /**
     * UDP connection.
     */
    private Connection udpConnection;
    
    /**
     * If the client is currently connected.
     */
    private boolean isConnected;
    
    /**
     * The UDP port number the client is sending packets from.
     */
    private int clientUdpPort;
    
    /**
     * The secret key used for packet encryption.
     */
    private SecretKey secretKey;
    
    /**
     * The IV parameter spec used for ciphering.
     */
    private IvParameterSpec ivParameterSpec;
    
    /**
     * The random that was sent to the client during handshake hashed in SHA-512.
     */
    private byte[] hashedRandom;
    
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
        
        // Name this thread.
        Thread.currentThread().setName("Neto-Framework Server Client");
        
        // Continuously read TCP packets from client until the server is stopped or they are no
        // longer connected.
        while (this.server.isRunning() && this.isConnected) {
            
            // Define metadata variables.
            int packetId = 0;
            long timestamp = 0;
            
            // Attempt to receive metadata.
            try {
                packetId = this.tcpConnection.receiveInteger();
                timestamp = this.tcpConnection.receiveLong();
            } catch (IOException e) {
                
                // Check if the TCP socket has not been closed.
                if(!this.tcpConnection.getTCPSocket().isClosed()) {
                    
                    // Call a packet exception event.
                    PacketException exception = new PacketException("Failed to read packet. Client"
                            + " most likely closed the connection without sending a disconnect"
                            + " packet.", e);
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(this.server,
                            exception, this.uuid);
                    this.server.getEventHandler().callEvent(packetEvent);
                    this.disconnect(false);
                    
                    // If the client has completed the handshake, call a client disconnect event.
                    if(this.isHandshakeCompleted) {
                        ClientDisconnectEvent event = new ClientDisconnectEvent(this.server, 
                                ClientDisconnectReason.EXCEPTION, this, exception);
                        this.server.getEventHandler().callEvent(event);
                    }
                }
                
                // End the thread.
                break;
            }
            
            // Check to see if the packet arrived within the replay window.
            if((System.currentTimeMillis() - timestamp) > Connection.REPLAY_WINDOW) {
                try {
                    int available = this.tcpConnection.getTCPSocket().getInputStream().available();
                    this.tcpConnection.getTCPSocket().getInputStream().skip(available);
                } catch (IOException e) { }
                
                continue;
            }
            
            // If the client has not completed the handshake process, they should not be able to
            // send any other packets except those of the handshake process.
            if(!this.isHandshakeCompleted && packetId != -1 && packetId != -4) {
                ConnectionException exception = new ConnectionException("A client sent an unkown"
                        + " packet or other before completing the handshake process.");
                ClientFailedToConnectEvent event = new ClientFailedToConnectEvent(this.server,
                exception);
                this.server.getEventHandler().callEvent(event);

                this.disconnect();
                break;
            }
            
            // Check to see if the server knows the given packet.
            if(!this.server.getPacketManager().hasPacket(packetId)) {
                PacketException exception = new PacketException("Unkown TCP packet received.");
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
                
                this.disconnect();
                ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(this.server, 
                        ClientDisconnectReason.EXCEPTION, this, exception);
                this.server.getEventHandler().callEvent(disconnectEvent);
                break;
            }
            
            // Attempt to receive packet data.
            try {
                this.server.getPacketManager().receive(this.server, packetId, this, Protocol.TCP);
            } catch (IOException e) {
                PacketException exception = new PacketException("Failed to read TCP packet"
                        + " data.", e);
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
                this.disconnect();
                    
                // If the client has completed the handshake, call a client disconnect event.
                if(this.isHandshakeCompleted) {
                    ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(this.server, 
                            ClientDisconnectReason.EXCEPTION, this, exception);
                    this.server.getEventHandler().callEvent(disconnectEvent);
                }
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
        
        // Throw an exception if an attempt to send an unregistered packet is made.
        if(!this.server.getPacketManager().hasPacket(packet.getId())) {
            throw new RuntimeException("You can not attempt to send an unregistered packet, please"
                    + " register it with the PacketManager first.");
        }
        
        // If sending the packet over TCP.
        if(protocol == Protocol.TCP) {
            // Send the Packet ID.
            this.tcpConnection.sendInteger(packet.getId());
            
            // Send the current timestamp.
            this.tcpConnection.sendLong(System.currentTimeMillis());
            
            // Send the packet data.
            packet.send(this.tcpConnection);
        }
        
        // If sending the packet over UDP.
        if(protocol == Protocol.UDP) {
            // Send the Packet ID.
            this.udpConnection.sendInteger(packet.getId());
            
            // Send the Client's UUID.
            this.udpConnection.sendString(this.uuid.toString());
            
            // Send the current timestamp.
            this.udpConnection.sendLong(System.currentTimeMillis());
            
            // Send the packet data.
            packet.send(this.udpConnection);
            
            // Get the entire packet's data from stream.
            byte[] data = this.udpConnection.getUdpData();
            
            // Encode the entire packet in Base64.
            data = Base64.getEncoder().withoutPadding().encode(data);
            
            // Craft the raw UDP packet.
            DatagramPacket dataPacket = new DatagramPacket(
                    data,
                    data.length,
                    this.udpConnection.getAddress(),
                    this.udpConnection.getPort());
            
            // Attempt to send the packet.
            this.server.getUdpSocket().send(dataPacket);
        }
    }
    
    /**
     * Disconnect client from the server.
     * 
     * @param sendDisconnectPacket If true, sends a disconnect packet to the client before closing.
     */
    public synchronized void disconnect(boolean sendDisconnectPacket) {
        
        // Send a disconnect packet to the client if desired.
        if(sendDisconnectPacket) {
            try {
                this.sendPacket(new DisconnectPacket(), Protocol.TCP);
            } catch (IOException e) {} //TODO: Log
        }
        
        // Attempt to close the TCP socket cleanly.
        try {
            this.tcpConnection.getTCPSocket().close();
        } catch (IOException e) { } //TODO: Log
        
        // If the client was in the handshake process cancel the handshake timer.
        if(!this.isHandshakeCompleted) {
            this.handshakeTimer.cancel();
        }

        // Tell the connection manager to remve the client.
        this.server.getConnectionManager().removeClientConnection(this.uuid);
        
        // Place the ClientConnection into an unconnected state.
        this.isConnected = false;
    }
    
    /**
     * Disconnect client from the server.
     */
    public void disconnect() {
        this.disconnect(true);
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
     * @return The random that was sent to the client during handshake hashed in SHA-512.
     */
    public byte[] getHashedRandom() {
        return this.hashedRandom;
    }
    
    /**
     * @param hashedRandom The random that was sent to the client during handshake hashed in
     *                     SHA-512.
     */
    public void setHashedRandom(byte[] hashedRandom) {
        this.hashedRandom = hashedRandom;
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
    
    /**
     * @return The UDP port number the client is sending packets from.
     */
    public int getClientUdpPort() {
        return this.clientUdpPort;
    }
    
    /**
     * @param clientUdpPort The UDP port number the client is sending packets from.
     */
    public void setClientUdpPort(int clientUdpPort) {
        this.clientUdpPort = clientUdpPort;
    }
}
