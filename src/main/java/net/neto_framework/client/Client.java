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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.PacketManager;
import net.neto_framework.Protocol;
import net.neto_framework.address.SocketAddress;
import net.neto_framework.client.event.events.PacketExceptionEvent;
import net.neto_framework.client.exceptions.ClientConnectException;
import net.neto_framework.event.EventHandler;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.packets.DisconnectPacket;
import net.neto_framework.packets.HandshakePacket;
import net.neto_framework.packets.HandshakeResponsePacket;

/**
 * A client handler that can connect to a TCP or UDP server.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Client {

    /**
     * The {@link net.neto_framework.PacketManager PacketManager}.
     */
    private final PacketManager packetManager;
            
    /**
     * The {@link net.neto_framework.event.EventHandler EventHandler}.
     */
    private final EventHandler eventHandler;

    /**
     * The {@link net.neto_framework.address.SocketAddress} to connect to.
     */
    private final SocketAddress address;

    /**
     * TCP Socket.
     */
    private Socket tcpSocket;
    
    /**
     * UDP Socket.
     */
    private DatagramSocket udpSocket;

    /**
     * {@link net.neto_framework.client.ServerConnection ServerConnection}.
     */
    private ServerConnection serverConnection;

    /**
     * If client is connected to a Server.
     */
    private volatile boolean isConnected;
    
    /**
     * UUID given by server.
     */
    private UUID uuid;

    /**
     * @param address The {@link net.neto_framework.address.SocketAddress SocketAddress} to connect
     *                to.
     */
    public Client(SocketAddress address) {
        this.packetManager = new PacketManager();
        this.packetManager.registerPacket(HandshakePacket.class);
        this.packetManager.registerPacket(HandshakeResponsePacket.class);
        this.packetManager.registerPacket(DisconnectPacket.class);
        
        this.eventHandler = new EventHandler();
        this.address = address;
    }

    /**
     * Attempt to connect to server.
     * 
     * @throws net.neto_framework.client.exceptions.ClientConnectException If failed.
     */
    public void connect() throws ClientConnectException {
        if (!this.isConnected) {
            try {
                this.tcpSocket = new Socket(this.address.getInetAddress(), 
                        this.address.getPort());
                this.isConnected = true;
            } catch (IOException e) {
                throw new ClientConnectException("Failed to connect to given SocketAddress.", e);
            }

            try {
                this.udpSocket = new DatagramSocket();
            } catch (SocketException e) {
                this.disconnect();
                throw new ClientConnectException("Failed to create UDP socket.", e);
            }

            Connection tcpConnection = new Connection(this.tcpSocket);
            Connection udpConnection = new Connection(this.udpSocket,
                    this.address.getInetAddress(), this.address.getPort());

            this.serverConnection = new ServerConnection(this, tcpConnection, udpConnection);
            this.isConnected = true;
            (new Thread(this.serverConnection)).start();
            
            try {
                HandshakePacket packet = new HandshakePacket();
                packet.setUdpPort(this.udpSocket.getLocalPort());
                this.serverConnection.sendPacket(packet, Protocol.TCP);
            } catch (IOException e) {
                this.disconnect();
                throw new ClientConnectException("Failed to send handshake packet.", e);
            }
            
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("Neto-Framework Client UDP Handler");
                    
                    while(Client.this.isConnected) {
                        byte[] data = new byte[65508];
                        DatagramPacket dataPacket = new DatagramPacket(data, data.length);

                        try {
                            Client.this.udpSocket.receive(dataPacket);

                            Connection connection = new Connection(udpSocket,
                                    dataPacket.getAddress(), dataPacket.getPort());

                            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                            connection.setUdpDataInputStream(inputStream);
                            int packetId = connection.receiveInteger();
                            UUID uuid = UUID.fromString(connection.receiveString());

                            if(!Client.this.packetManager.hasPacket(packetId)) {
                                PacketException exception = new PacketException("Unkown packet"
                                        + " received.");
                                PacketExceptionEvent event = new PacketExceptionEvent(Client.this,
                                        exception);
                                Client.this.eventHandler.callEvent(event);
                            }

                            if(Client.this.uuid.equals(uuid)) {
                                Client.this.serverConnection.getUDPConnection().
                                        setUdpDataInputStream(inputStream);
                                Client.this.packetManager.receive(Client.this, packetId,
                                        Client.this.serverConnection, Protocol.UDP);
                            } else {
                                PacketException exception = new PacketException("UUID does not "
                                        + "match on received packet.");
                                PacketExceptionEvent event = new PacketExceptionEvent(Client.this,
                                        exception);
                                Client.this.eventHandler.callEvent(event);
                            }
                        } catch (IOException e) {
                            if(!Client.this.udpSocket.isClosed()) {
                                PacketException exception = new PacketException("Failed to read "
                                        + "packet.", e);
                                PacketExceptionEvent event = new PacketExceptionEvent(Client.this,
                                        exception);
                                Client.this.eventHandler.callEvent(event);
                            } else {
                                break;
                            }
                        }
                    }
                }
            })).start();
        }
    }

    /**
     * Disconnect from the server.
     * 
     * @param sendDisconnectPacket If true, sends a disconnect packet to the server before closing.
     */
    public void disconnect(boolean sendDisconnectPacket) {
        if (this.isConnected) {
            if(sendDisconnectPacket) {
                try {
                    this.serverConnection.sendPacket(new DisconnectPacket(), Protocol.TCP);
                } catch (IOException e) {} //TODO: log
            }
            
            this.isConnected = false;
            
            try {
                this.tcpSocket.close();
            } catch (IOException e) {} //TODO: Log

            if(this.udpSocket != null) {
                this.udpSocket.close();
            }
        }
    }
    
    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        this.disconnect(true);
    }

    /**
     * @return {@link net.neto_framework.PacketManager PacketManager}.
     */
    public PacketManager getPacketManager() {
        return this.packetManager;
    }

    /**
     * @return {@link net.neto_framework.event.EventHandler EventHandler}.
     */
    public EventHandler getEventHandler() {
        return this.eventHandler;
    }

    /**
     * @return {@link net.neto_framework.address.SocketAddress} the client is/will connect to.
     */
    public SocketAddress getAddress() {
        return this.address;
    }

    /**
     * @return TCP Socket. (Null if not using TCP as protocol or if the client
     *         is not connected.)
     */
    public Socket getTcpSocket() {
        return this.tcpSocket;
    }

    /**
     * @return UDP Socket. (Null if not using UDP as protocol or if the client
     *         is not connected.)
     */
    public DatagramSocket getUdpSocket() {
        return this.udpSocket;
    }

    /**
     * @return {@link net.neto_framework.client.ServerConnection ServerConnection}.
     */
    public ServerConnection getServerConnection() {
        return this.serverConnection;
    }

    /**
     * @return If client is currently connected to a server.
     */
    public boolean isConnected() {
        return this.isConnected;
    }
    
    /**
     * @return UUID given by server.
     */
    public UUID getUUID() {
        return this.uuid;
    }
    
    /**
     * @param uuid UUID.
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
