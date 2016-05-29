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
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.PacketReceiver;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.event.events.ClientConnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent.ClientDisconnectReason;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.event.events.PacketExceptionEvent;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A connection handler that accepts TCP/UDP connections on a separate thread.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnectionHandler extends Thread {

    /**
     * Running instance of Server.
     */
    private final Server server;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     */
    public ServerConnectionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            if (this.server.getProtocol() == Protocol.TCP) {
                Socket socket;
                try {
                    socket = this.server.getTcpSocket().accept();
                } catch (IOException e) {
                    ClientFailedToConnectEvent event = new ClientFailedToConnectEvent(this.server,
                                    new ConnectionException("I/O Error when accepting a TCP" + 
                                            " connection.", e));
                    this.server.getEventHandler().callEvent(event);
                    continue;
                }
                    
                Connection connection = new Connection(socket);
                byte[] magicStringBuffer = Connection.MAGIC_STRING.getBytes();

                byte[] buffer = new byte[Connection.MAGIC_STRING.getBytes().length];
                try {
                    connection.receive(buffer);
                } catch (IOException e) {
                    ClientFailedToConnectEvent event = new ClientFailedToConnectEvent(
                            this.server, new ConnectionException("I/O Error when reading TCP " +
                                    "handshake.", e));
                    this.server.getEventHandler().callEvent(event);
                    
                    try {
                        socket.close();
                    } catch (IOException e2) { } //TODO: Log
                    continue;
                }

                if (!new String(buffer).equals(Connection.MAGIC_STRING)) {
                    ClientFailedToConnectEvent event = new ClientFailedToConnectEvent(
                            this.server, new ConnectionException("Invalid Handshake:" + 
                                    " Wrong magic string received."));
                    this.server.getEventHandler().callEvent(event);
                    
                    try {
                        socket.close();
                    } catch (IOException e2) { } //TODO: Log
                    continue;
                }

                try {
                    connection.getTCPSocket().getOutputStream().write(magicStringBuffer);
                } catch (IOException e) {
                    ClientFailedToConnectEvent event = new ClientFailedToConnectEvent(
                            this.server, new ConnectionException("I/O Error when sending TCP " +
                                    "handshake.", e));
                    this.server.getEventHandler().callEvent(event);
                    
                    try {
                        socket.close();
                    } catch (IOException e2) { } //TODO: Log
                    continue;
                }

                ClientConnection clientConnection = this.server.getConnectionManager().
                        addClientConnection(this.server, socket);
                this.server.getEventHandler().callEvent(new ClientConnectEvent(this.server,
                        clientConnection));
            } else if (this.server.getProtocol() == Protocol.UDP) {
                byte[] data = new byte[65508];
                DatagramPacket dataPacket = new DatagramPacket(data, data.length);

                try {
                    this.server.getUdpSocket().receive(dataPacket);
                    
                    Connection connection = new Connection(this.server.getUdpSocket(),
                            dataPacket.getAddress(), dataPacket.getPort());
                    
                    connection.setUdpDataInputStream(new ByteArrayInputStream(data));
                    String uuid = connection.receiveString();

                    if (uuid.equals(Connection.MAGIC_STRING)) {
                        ClientConnection clientConnection = this.server.getConnectionManager().
                                addClientConnection(this.server, connection);

                        connection.sendString(clientConnection.getUUID().toString());
                        byte[] sendResponseData = connection.getUdpData();

                        DatagramPacket sendResponsePacket = new DatagramPacket(sendResponseData,
                                sendResponseData.length, dataPacket.getAddress(), 
                                dataPacket.getPort());

                        this.server.getUdpSocket().send(sendResponsePacket);
                        
                        this.server.getEventHandler().callEvent(new ClientConnectEvent(this.server,
                            clientConnection));
                    } else {
                        UUID clientId = UUID.fromString(uuid);
                        
                        if (this.server.getConnectionManager().hasClientConnection(clientId)) {
                            ClientConnection client = this.server.getConnectionManager().
                                    getClientConnection(clientId);
                            client.getConnection().setUdpDataInputStream(
                                    new ByteArrayInputStream(data));
                            client.getConnection().receiveString();
                            int packetId = client.getConnection().receiveInteger();

                            if (this.server.getPacketManager().hasPacket(packetId)) {
                                this.server.getPacketManager().receive(packetId,
                                        client.getConnection(), PacketReceiver.SERVER);
                            } else {
                                PacketException exception = new PacketException("Unkown packet"
                                        + " received.");
                                PacketExceptionEvent packetEvent = new PacketExceptionEvent(
                                        this.server, exception, client.getUUID());
                                this.server.getEventHandler().callEvent(packetEvent);
                                
                                client.disconnect();
                                ClientDisconnectEvent event = new ClientDisconnectEvent(this.server,
                                        ClientDisconnectReason.EXCEPTION, client.getUUID(),
                                        exception);
                                this.server.getEventHandler().callEvent(event);
                            }
                        } else {
                            PacketException exception = new PacketException("Unkown packet "
                                    + "structure received from unkown client.");
                            PacketExceptionEvent event = new PacketExceptionEvent(this.server,
                                    exception);
                            this.server.getEventHandler().callEvent(event);
                        }
                    }
                } catch (IOException e) {
                    PacketException exception = new PacketException("Failed to read packet.", e);
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                }
            }
        }
    }
}
