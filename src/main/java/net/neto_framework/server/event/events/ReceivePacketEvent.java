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

package net.neto_framework.server.event.events;

import net.neto_framework.Packet;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.ServerEvent;

/**
 * An event that is fired when the server receives a packet.
 * <p>
 * This event contains the {@link net.neto_framework.Packet Packet} that was received and the {@link
 * net.neto_framework.server.ClientConnection ClientConnection} that sent it.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ReceivePacketEvent extends ServerEvent {
    
    /**
     * The {@link net.neto_framework.server.ClientConnection ClientConnection} that sent the packet.
     */
    private final ClientConnection clientConnection;
    
    /**
     * The {@link net.neto_framework.Packet Packet} that was received.
     */
    private final Packet packet;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param clientConnection The {@link net.neto_framework.server.ClientConnection
     *                         ClientConnection} that sent the packet.
     * @param packet The {@link net.neto_framework.Packet Packet} that was received.
     */
    public ReceivePacketEvent(Server server, ClientConnection clientConnection, Packet packet) {
        super(server, ServerEvents.RECEIVE_PACKET);
        
        this.clientConnection = clientConnection;
        this.packet = packet;
    }

    /**
     * @return The {@link net.neto_framework.server.ClientConnection ClientConnection} that sent the packet.
     */
    public ClientConnection getClientConnection() {
        return this.clientConnection;
    }
    
    /**
     * @return The {@link net.neto_framework.Packet Packet} that was received.
     */
    public Packet getPacket() {
        return this.packet;
    }
}
