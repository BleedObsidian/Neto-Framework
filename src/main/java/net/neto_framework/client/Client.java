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
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import net.neto_framework.Connection;
import net.neto_framework.PacketManager;
import net.neto_framework.Protocol;
import net.neto_framework.address.SocketAddress;
import net.neto_framework.client.event.events.ClientReceiveInvalidHandshake;
import net.neto_framework.client.event.events.ClientServerConnect;
import net.neto_framework.client.event.events.ClientServerDisconnect;
import net.neto_framework.client.exceptions.ClientException;
import net.neto_framework.event.EventHandler;

/**
 * A client handler that can connect to a TCP or UDP server.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Client {

    /**
     * Client PacketManager.
     */
    private final PacketManager packetManager;
            
    /**
     * Client EventHandler.
     */
    private final EventHandler eventHandler;

    /**
     * SocketAddress of server.
     */
    private final SocketAddress address;
    
    /**
     * Protocol in use.
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
     * ServerConnection of Client.
     */
    private ServerConnection connection;

    /**
     * If Client is connected to a Server.
     */
    private boolean isConnected;

    /**
     * New client that connects to a given server address and protocol.
     * 
     * @param packetManager
     *            PacketManager.
     * @param address
     *            Server SocketAddress.
     * @param protocol
     *            Protocol.
     */
    public Client(PacketManager packetManager, SocketAddress address,
            Protocol protocol) {
        this.packetManager = packetManager;
        this.eventHandler = new EventHandler();
        this.address = address;
        this.protocol = protocol;
    }

    /**
     * Attempt to connect to server.
     * 
     * @throws ClientException
     *             If fails to connect to server.
     */
    public void connect() throws ClientException {
        if (!this.isConnected) {
            if (this.protocol == Protocol.TCP) {
                try {
                    this.tcpSocket = new Socket(this.address.getInetAddress(),
                            this.address.getPort());
                    this.isConnected = true;
                } catch (IOException e) {
                    throw new ClientException(
                            "Failed to connect to given SocketAddress", e);
                }

                try {
                    this.tcpSocket.getOutputStream().write(
                            Connection.MAGIC_STRING.getBytes());

                    byte[] magicStringBuffer = new byte[Connection.MAGIC_STRING
                            .getBytes().length];
                    this.tcpSocket.getInputStream().read(magicStringBuffer);
                    String sentMagicString = new String(magicStringBuffer);

                    if (sentMagicString.equals(Connection.MAGIC_STRING)) {
                        this.connection = new ServerConnection(this,
                                new Connection(this.tcpSocket));
                        this.isConnected = true;
                        (new Thread(this.connection)).start();
                    } else {
                        ClientReceiveInvalidHandshake event = new ClientReceiveInvalidHandshake(
                                this, this.tcpSocket.getInetAddress(),
                                magicStringBuffer);
                        this.eventHandler.callEvent(event);
                        throw new ClientException(
                                "Received invalid handshake from server",
                                new Exception());
                    }
                } catch (IOException e) {
                    throw new ClientException(
                            "Failed to send/receive handshake packet", e);
                }
            } else if (this.protocol == Protocol.UDP) {
                try {
                    this.udpSocket = new DatagramSocket();
                } catch (SocketException e) {
                    throw new ClientException("Failed to create UDP socket", e);
                }

                try {
                    byte[] buffer = Connection.MAGIC_STRING.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer,
                            buffer.length, this.address.getInetAddress(),
                            this.address.getPort());

                    this.udpSocket.send(packet);

                    byte[] magicStringBuffer = new byte[Connection.MAGIC_STRING
                            .getBytes().length];
                    DatagramPacket idPacket = new DatagramPacket(
                            magicStringBuffer, magicStringBuffer.length);
                    this.udpSocket.receive(idPacket);
                    String sentMagicString = new String(idPacket.getData());

                    if (sentMagicString.equals(Connection.MAGIC_STRING)) {
                        this.connection = new ServerConnection(this,
                                new Connection(this.udpSocket,
                                        this.udpSocket.getInetAddress(),
                                        this.udpSocket.getPort()));
                        this.isConnected = true;
                        (new Thread(this.connection)).start();
                    } else {
                        ClientReceiveInvalidHandshake event = new ClientReceiveInvalidHandshake(
                                this, this.udpSocket.getInetAddress(),
                                idPacket.getData());
                        this.eventHandler.callEvent(event);
                        throw new ClientException(
                                "Received invalid handshake from server",
                                new Exception());
                    }
                } catch (IOException e) {
                    throw new ClientException(
                            "Failed to send/receive handshake packet", e);
                }
            }

            this.eventHandler.callEvent(new ClientServerConnect(this,
                    this.connection));
        }
    }

    /**
     * Attempt to close connection.
     * 
     * @throws ClientException
     *             If fails to close connection.
     */
    public void disconnect() throws ClientException {
        if (this.isConnected) {
            if (this.protocol == Protocol.TCP) {
                try {
                    this.tcpSocket.close();
                    this.isConnected = false;
                } catch (IOException e) {
                    throw new ClientException(
                            "Failed to close TCP connection.", e);
                }
            } else if (this.protocol == Protocol.UDP) {
                this.udpSocket.close();
                this.isConnected = false;
            }

            this.eventHandler.callEvent(new ClientServerDisconnect(this,
                    this.connection));
        }
    }

    /**
     * @return PacketManager.
     */
    public synchronized PacketManager getPacketManager() {
        return this.packetManager;
    }

    /**
     * @return EventHandler.
     */
    public synchronized EventHandler getEventHandler() {
        return this.eventHandler;
    }

    /**
     * @return Address that server is binding to.
     */
    public SocketAddress getAddress() {
        return this.address;
    }

    /**
     * @return Protocol the server is using.
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
     * @return Server Connection.
     */
    public synchronized ServerConnection getServerConnection() {
        return this.connection;
    }

    /**
     * @return If client is currently connected to a server.
     */
    public synchronized boolean isConnected() {
        return this.isConnected;
    }
}
