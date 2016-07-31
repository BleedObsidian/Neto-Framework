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
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent.ClientDisconnectReason;
import net.neto_framework.server.event.events.PacketExceptionEvent;

/**
 * A connection handler that accepts UDP connections on a separate thread.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerUDPConnectionHandler extends Thread {

    /**
     * Running instance of Server.
     */
    private final Server server;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     */
    public ServerUDPConnectionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Neto-Framework Server UDP Handler");
        
        while (this.server.isRunning()) {
            byte[] data = new byte[65508];
            DatagramPacket dataPacket = new DatagramPacket(data, data.length);

            try {
                this.server.getUdpSocket().receive(dataPacket);
                
                // Trim data
                int i = data.length - 1;
                while (i >= 0 && data[i] == 0) {
                    --i;
                }

                data = Arrays.copyOf(data, i + 1);
                
                // Check packet to see if it is a clients hashed success packet.
                boolean isHash = false;
                for(ClientConnection client :
                        this.server.getConnectionManager().getPendingClientConnections()) {
                    if(new String(client.getHashedSuccessPacket()).equals(new String(data))) {
                        this.server.getConnectionManager().onConnectionValidated(client.getUUID());
                        client.setClientUdpPort(dataPacket.getPort());
                        client.setHandshakeCompleted(true);
                        isHash = true;
                        break;
                    }
                }
                
                if(isHash) { continue; }
                
                data = Base64.getDecoder().decode(data);
                Connection connection = new Connection(this.server.getUdpSocket(),
                        dataPacket.getAddress(), dataPacket.getPort());
                ClientConnection client = this.server.getConnectionManager().
                        getClientConnection(dataPacket.getAddress(), dataPacket.getPort());
                
                if(client == null) {
                    PacketException exception = new PacketException(
                            "UDP Packet received from unkown source.");
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                    continue;
                }
                
                connection.enableEncryption(client.getSecretKey(), client.getIvParameterSpec());
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                connection.setUdpDataInputStream(inputStream);
                int packetId = connection.receiveInteger();
                
                if(!this.server.getPacketManager().hasPacket(packetId)) {
                    PacketException exception = new PacketException("Unkown UDP packet received.");
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                    
                    client.disconnect();
                    ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(
                            this.server, ClientDisconnectReason.EXCEPTION, client, exception);
                    this.server.getEventHandler().callEvent(disconnectEvent);
                }
                
                UUID uuid = UUID.fromString(connection.receiveString());
                long timestamp = connection.receiveLong();
                
                if(client.getUUID().equals(uuid)) {
                    client.getUDPConnection().setUdpDataInputStream(inputStream);
                    
                    if((System.currentTimeMillis() - timestamp) > Connection.REPLAY_WINDOW) {
                        this.server.getPacketManager().receive(this.server, packetId, client,
                                Protocol.UDP, true);
                    } else {
                        this.server.getPacketManager().receive(this.server, packetId, client,
                                Protocol.UDP, false);
                    }
                } else {
                    PacketException exception = new PacketException("Client sent invalid UUID along"
                            + " with TCP packet.");
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                    
                    client.disconnect();
                    ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(
                            this.server, ClientDisconnectReason.EXCEPTION, client, exception);
                    this.server.getEventHandler().callEvent(disconnectEvent);
                    continue;
                }
            } catch (IOException e) {
                if(!this.server.getUdpSocket().isClosed()) {
                    PacketException exception = new PacketException("Failed to read UDP packet.",
                            e);
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                }
            }
        }
    }
}
