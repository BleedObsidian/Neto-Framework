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

import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.ServerEvent;
import net.neto_framework.server.event.ServerEvents;

/**
 * An event that is fired when the server receives an invalid packet id or
 * packet.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerInvalidPacket extends ServerEvent {
    
    /**
     * Packet ID.
     */
    private final int id;
    
    /**
     * PacketException thrown.
     */
    private final PacketException exception;

    /**
     * New ServerInvalidPacket Event.
     * 
     * @param server Server.
     * @param id Received packet ID.
     * @param exception PacketException.
     */
    public ServerInvalidPacket(Server server, int id, PacketException exception) {
        super(server, ServerEvents.SERVER_INVALID_PACKET);

        this.id = id;
        this.exception = exception;
    }

    /**
     * @return Received ID of packet.
     */
    public int getReceivedID() {
        return this.id;
    }

    /**
     * @return PacketException.
     */
    public PacketException getPacketException() {
        return this.exception;
    }
}
