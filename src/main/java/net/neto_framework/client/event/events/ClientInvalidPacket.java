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

import net.neto_framework.client.Client;
import net.neto_framework.client.event.ClientEvent;
import net.neto_framework.client.event.ClientEvents;
import net.neto_framework.exceptions.PacketException;

/**
 * An event that is fired when the client receives an invalid packet id or
 * packet.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ClientInvalidPacket extends ClientEvent {
    
    /**
     * ID of packet.
     */
    private final int id;
    
    /**
     * PacketException thrown.
     */
    private final PacketException exception;

    /**
     * New ClientInvalidPacket Event.
     * 
     * @param client Client.
     * @param id Received packet ID.
     * @param exception PacketException.
     */
    public ClientInvalidPacket(Client client, int id, PacketException exception) {
        super(client, ClientEvents.CLIENT_INVALID_PACKET);

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
