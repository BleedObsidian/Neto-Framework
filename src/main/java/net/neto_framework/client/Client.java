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
import net.neto_framework.client.exceptions.ClientConnectException;
import net.neto_framework.event.EventHandler;

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
     * The {@link net.neto_framework.Protocol Protocol} to use.
     */
    private final Protocol protocol;

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
    private boolean isConnected;
    
    /**
     * UUID given by server.
     */
    private UUID uuid;

    /**
     * @param packetManager The {@link net.neto_framework.PacketManager PacketManager}.
     * @param address The {@link net.neto_framework.address.SocketAddress SocketAddress} to connect
     *                to.
     * @param protocol {@link net.neto_framework.Protocol Protocol}.
     */
    public Client(PacketManager packetManager, SocketAddress address, Protocol protocol) {
        this.packetManager = packetManager;
        this.eventHandler = new EventHandler();
        this.address = address;
        this.protocol = protocol;
    }

    /**
     * Attempt to connect to server.
     * 
     * @throws net.neto_framework.client.exceptions.ClientConnectException If failed.
     */
    public void connect() throws ClientConnectException {
        if (!this.isConnected) {
            if (this.protocol == Protocol.TCP) {
                try {
                    this.tcpSocket = new Socket(this.address.getInetAddress(), 
                            this.address.getPort());
                    this.isConnected = true;
                } catch (IOException e) {
                    throw new ClientConnectException(
                            "Failed to connect to given SocketAddress", e);
                }

                try {
                    this.tcpSocket.getOutputStream().write(Connection.MAGIC_STRING.getBytes());
                } catch (IOException e) {
                    throw new ClientConnectException(
                            "Failed to send handshake packet.", e);
                }

                byte[] magicStringBuffer = new byte[Connection.MAGIC_STRING.getBytes().length];
                    
                try {
                    this.tcpSocket.getInputStream().read(magicStringBuffer);
                } catch (IOException e) {
                    throw new ClientConnectException(
                            "Failed to receive handshake packet", e);
                }
                    
                String sentMagicString = new String(magicStringBuffer);

                if (sentMagicString.equals(Connection.MAGIC_STRING)) {
                    this.serverConnection = new ServerConnection(this, new Connection(this.tcpSocket));
                    this.isConnected = true;
                    (new Thread(this.serverConnection)).start();
                } else {
                    throw new ClientConnectException("Received invalid handshake from server");
                }
            } else if (this.protocol == Protocol.UDP) {
                try {
                    this.udpSocket = new DatagramSocket();
                } catch (SocketException e) {
                    throw new ClientConnectException("Failed to create UDP socket", e);
                }

                try {
                    Connection connection = new Connection(this.udpSocket,
                            this.address.getInetAddress(), this.address.getPort());
                    connection.sendString(Connection.MAGIC_STRING);
                    
                    byte[] handshake = connection.getUdpData();
                    DatagramPacket handshakePacket = new DatagramPacket(handshake, handshake.length, 
                            this.address.getInetAddress(), this.address.getPort());
                    
                    this.udpSocket.send(handshakePacket);
                    
                    byte[] data = new byte[65508];
                    DatagramPacket receiveHandshakePacket = new DatagramPacket(
                            data, data.length);
                    
                    this.udpSocket.receive(receiveHandshakePacket);
                    
                    connection.setUdpDataInputStream(new ByteArrayInputStream(data));
                    
                    this.uuid = UUID.fromString(connection.receiveString());

                    this.serverConnection = new ServerConnection(this, connection);
                    this.isConnected = true;
                    (new Thread(this.serverConnection)).start();
                    
                } catch (IOException e) {
                    throw new ClientConnectException("Failed to send/receive handshake packet", e);
                }
            }
        }
    }

    /**
     * Attempt to close serverConnection.
     * 
     * @throws net.neto_framework.client.exceptions.ClientConnectException If failed to close
     *         cleanly.
     */
    public void disconnect() throws ClientConnectException {
        if (this.isConnected) {
            if (this.protocol == Protocol.TCP) {
                try {
                    this.tcpSocket.close();
                    this.isConnected = false;
                } catch (IOException e) {
                    throw new ClientConnectException(
                            "Failed to close TCP connection.", e);
                }
            } else if (this.protocol == Protocol.UDP) {
                this.udpSocket.close();
                this.isConnected = false;
            }
        }
    }

    /**
     * @return {@link net.neto_framework.PacketManager PacketManager}.
     */
    public synchronized PacketManager getPacketManager() {
        return this.packetManager;
    }

    /**
     * @return {@link net.neto_framework.event.EventHandler EventHandler}.
     */
    public synchronized EventHandler getEventHandler() {
        return this.eventHandler;
    }

    /**
     * @return {@link net.neto_framework.address.SocketAddress} the client is/will connect to.
     */
    public SocketAddress getAddress() {
        return this.address;
    }

    /**
     * @return {@link net.neto_framework.Protocol Protocol} the client is using.
     */
    public synchronized Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * @return TCP Socket. (Null if not using TCP as protocol or if the client
     *         is not connected.)
     */
    public synchronized Socket getTcpSocket() {
        return this.tcpSocket;
    }

    /**
     * @return UDP Socket. (Null if not using UDP as protocol or if the client
     *         is not connected.)
     */
    public synchronized DatagramSocket getUdpSocket() {
        return this.udpSocket;
    }

    /**
     * @return {@link net.neto_framework.client.ServerConnection ServerConnection}.
     */
    public synchronized ServerConnection getServerConnection() {
        return this.serverConnection;
    }

    /**
     * @return If client is currently connected to a server.
     */
    public synchronized boolean isConnected() {
        return this.isConnected;
    }
    
    /**
     * @return UUID given by server.
     */
    public synchronized UUID getUUID() {
        return this.uuid;
    }
}
