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
import net.neto_framework.exceptions.PacketException;

/**
 * An event that is fired when the client has a problem receiving or sending a packet.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class PacketExceptionEvent extends ClientEvent {
    
    /**
     * PacketException thrown.
     */
    private final PacketException exception;

    /**
     * @param client Running instance of {@link net.neto_framework.client.Client Client}.
     * @param exception The {@link net.neto_framework.exceptions.PacketException PacketException} 
     *                  that caused the event.
     */
    public PacketExceptionEvent(Client client, PacketException exception) {
        super(client, ClientEvents.PACKET_EXCEPTION);

        this.exception = exception;
    }

    /**
     * @return The {@link net.neto_framework.exceptions.PacketException PacketException} that caused
     *         the event.
     */
    public PacketException getPacketException() {
        return this.exception;
    }
}
