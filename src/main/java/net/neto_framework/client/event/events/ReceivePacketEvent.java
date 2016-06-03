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

package net.neto_framework.client.event.events;

import net.neto_framework.Packet;
import net.neto_framework.client.Client;
import net.neto_framework.client.ServerConnection;
import net.neto_framework.client.event.ClientEvent;

/**
 * An event that is fired when the client receives a packet.
 * <p>
 * This event contains the {@link net.neto_framework.Packet Packet} that was received and the {@link
 * net.neto_framework.client.ServerConnection ServerConnection} that sent it.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ReceivePacketEvent extends ClientEvent {
    
    /**
     * The {@link net.neto_framework.client.ServerConnection ServerConnection} that sent the packet.
     */
    private final ServerConnection serverConnection;
    
    /**
     * The {@link net.neto_framework.Packet Packet} that was received.
     */
    private final Packet packet;

    /**
     * @param client Running instance of {@link net.neto_framework.client.Client Client}.
     * @param serverConnection The {@link net.neto_framework.client.ServerConnection
     *                         ServerConnection} that sent the packet.
     * @param packet The {@link net.neto_framework.Packet Packet} that was received.
     */
    public ReceivePacketEvent(Client client, ServerConnection serverConnection, Packet packet) {
        super(client, ClientEvents.RECEIVE_PACKET);
        
        this.serverConnection = serverConnection;
        this.packet = packet;
    }

    /**
     * @return The {@link net.neto_framework.client.ServerConnection ServerConnection} that sent the packet.
     */
    public ServerConnection getServerConnection() {
        return this.serverConnection;
    }
    
    /**
     * @return The {@link net.neto_framework.Packet Packet} that was received.
     */
    public Packet getPacket() {
        return this.packet;
    }
}
