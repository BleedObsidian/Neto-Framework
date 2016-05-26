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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.PacketReceiver;

import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerInvalidPacket;
import net.neto_framework.server.event.events.ServerPacketException;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A connection handler that accepts TCP/UDP connections on a separate thread.
 * (Used internally)
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnectionHandler extends Thread {
    
    /**
     * Server.
     */
    private final Server server;

    /**
     * New ServerConnectionHandler.
     * 
     * @param server
     *            Server that is using this ServerConnectionHandler.
     */
    public ServerConnectionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            if (this.server.getProtocol() == Protocol.TCP) {
                try {
                    Socket socket = this.server.getTcpSocket().accept();
                    this.server.getConnectionManager().addConnection(
                            this.server, socket);
                } catch (IOException e) {
                    ConnectionException exception = new ConnectionException(
                            "I/O Error when trying to accept a TCP socket connection.",
                            e);
                    ServerFailedToAcceptConnection event = new ServerFailedToAcceptConnection(
                            this.server, this.server.getProtocol(), exception);
                    this.server.getEventHandler().callEvent(event);
                }
            } else if (this.server.getProtocol() == Protocol.UDP) {
                byte[] data = new byte[65508];
                DatagramPacket dataPacket = new DatagramPacket(data,
                        data.length);
                
                try {
                    System.out.println("Server: Receive 1");
                    this.server.getUdpSocket().receive(dataPacket);
                    System.out.println("Server: Receive 2");    
                    
                    ByteArrayInputStream inputStream =
                            new ByteArrayInputStream(data);

                    byte[] stringLength = new byte[4];
                    inputStream.read(stringLength);

                    byte[] string = new byte[ByteBuffer.wrap(stringLength).
                            getInt()];
                    inputStream.read(string);
                    String uuid = new String(string).trim();

                    if(uuid.equals(Connection.MAGIC_STRING)) {
                        System.out.println("Server: New connection.");
                        UUID newUuid = this.server.getConnectionManager().
                                addConnection(this.server,
                                        dataPacket.getAddress(), 
                                        dataPacket.getPort());
                        
                        this.server.getConnectionManager().
                                getConnection(newUuid).getConnection().
                                sendString(newUuid.toString());
                        byte[] sendResponseData = this.server.
                                getConnectionManager().getConnection(newUuid).
                                getConnection().getUdpDataOutputStream().
                                toByteArray();
                        
                        DatagramPacket sendResponsePacket = new DatagramPacket(
                                sendResponseData, sendResponseData.length,
                                dataPacket.getAddress(), dataPacket.getPort());
                        
                        this.server.getUdpSocket().send(sendResponsePacket);
                    } else {
                        UUID clientId = UUID.fromString(uuid);
                        if(this.server.getConnectionManager().
                                hasConnection(clientId)) {
                            byte[] packetIdData = new byte[4];
                            inputStream.read(packetIdData);
                            int packetId = ByteBuffer.wrap(packetIdData).
                            getInt();

                            ClientConnection client = this.server.
                                    getConnectionManager().
                                    getConnection(clientId);

                            if(this.server.getPacketManager().
                                    hasPacket(packetId)) {
                                client.getConnection().
                                        setUdpDataInputStream(inputStream);
                                this.server.getPacketManager().receive(
                                        packetId, client.getConnection(),
                                        PacketReceiver.SERVER);
                            } else {
                                PacketException exception = 
                                        new PacketException(
                                                "Invalid packet received.");
                                this.server.getEventHandler().callEvent(
                                        new ServerInvalidPacket(this.server,
                                                packetId, exception));
                            }
                        } else {
                            ConnectionException exception = 
                                    new ConnectionException(
                                            "Unreadable UDP Packet received.",
                                            new Exception());
                            ServerFailedToAcceptConnection event = 
                                    new ServerFailedToAcceptConnection(
                                            this.server, 
                                            this.server.getProtocol(),
                                            exception);
                            this.server.getEventHandler().callEvent(event);
                        }
                    }
                } catch (IOException e) {
                    ConnectionException exception = new ConnectionException(
                            "I/O Error when trying to read a UDP packet.", e);
                    ServerFailedToAcceptConnection event = 
                            new ServerFailedToAcceptConnection(this.server,
                                    this.server.getProtocol(), exception);
                    this.server.getEventHandler().callEvent(event);
                    return;
                } catch (InstantiationException e) {
                    PacketException exception = new PacketException(
                                "Failed to create instance of packet.", e);
                        this.server.getEventHandler().callEvent(
                                new ServerPacketException(this.server,
                                        exception));
                } catch (IllegalAccessException e) {
                    PacketException exception = new PacketException(
                                "Failed to create instance of packet.", e);
                        this.server.getEventHandler().callEvent(
                                new ServerPacketException(this.server,
                                        exception));
                }
            }
        }
    }
}
