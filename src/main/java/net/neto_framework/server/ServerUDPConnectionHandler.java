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
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
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
        while (this.server.isRunning()) {
            byte[] data = new byte[65508];
            DatagramPacket dataPacket = new DatagramPacket(data, data.length);

            try {
                this.server.getUdpSocket().receive(dataPacket);

                Connection connection = new Connection(this.server.getUdpSocket(),
                        dataPacket.getAddress(), dataPacket.getPort());

                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                connection.setUdpDataInputStream(inputStream);
                int packetId = connection.receiveInteger();
                UUID uuid = UUID.fromString(connection.receiveString());

                if(!this.server.getPacketManager().hasPacket(packetId)) {
                    PacketException exception = new PacketException("Unkown packet received.");
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                }
                
                if(this.server.getConnectionManager().hasClientConnection(uuid)) {
                    ClientConnection client = this.server.getConnectionManager().
                            getClientConnection(uuid);
                    client.getUDPConnection().setUdpDataInputStream(inputStream);
                    this.server.getPacketManager().receive(this.server, packetId, client,
                            Protocol.UDP);
                } else {
                    PacketException exception = new PacketException("Packet received from unkown"
                            + " client.");
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                }
            } catch (IOException e) {
                PacketException exception = new PacketException("Failed to read packet.", e);
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
            }
        }
    }
}
