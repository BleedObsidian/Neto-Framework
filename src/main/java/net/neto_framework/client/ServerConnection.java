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

import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.PacketReceiver;
import net.neto_framework.client.event.events.ClientPacketException;
import net.neto_framework.exceptions.PacketException;

/**
 * A connection thread to handle the server connection.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnection implements Runnable {
    
    /**
     * Client.
     */
    private final Client client;
    
    /**
     * Server Connection.
     */
    private final Connection connection;

    /**
     * New ServerConnection.
     * 
     * @param client
     *            Client.
     * @param connection
     *            Connection.
     */
    public ServerConnection(Client client, Connection connection) {
        this.client = client;
        this.connection = connection;
    }

    public void run() {
        while (this.client.isConnected()) {
            try {
                int packetID = this.connection.receiveInteger();

                if (this.client.getPacketManager().hasPacket(packetID)) {
                    try {
                        this.client.getPacketManager().receive(packetID,
                                this.connection, PacketReceiver.CLIENT);
                    } catch (InstantiationException e) {
                        PacketException exception = new PacketException(
                                "Failed to create instance of packet.", e);
                        this.client.getEventHandler().callEvent(
                                new ClientPacketException(this.client,
                                        exception));
                    } catch (IllegalAccessException e) {
                        PacketException exception = new PacketException(
                                "Failed to create instance of packet.", e);
                        this.client.getEventHandler().callEvent(
                                new ClientPacketException(this.client,
                                        exception));
                    }
                } else {
                    PacketException exception = new PacketException(
                            "Invalid packet received.");
                    this.client.getEventHandler().callEvent(
                            new ClientPacketException(this.client, exception));
                }
            } catch (IOException e) {
                PacketException exception = new PacketException(
                        "Failed to receive packet ID.", e);
                this.client.getEventHandler().callEvent(
                        new ClientPacketException(this.client, exception));
            }
        }
    }

    /**
     * Send server packet.
     * 
     * @param packet
     *            Packet.
     * @throws IOException
     *             If fails to send packet.
     */
    public void sendPacket(Packet packet) throws IOException {
        this.connection.sendInteger(packet.getID());
        packet.send(this.connection);
    }
}
