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

import java.util.UUID;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.ServerEvent;

/**
 * An event that is fired when a packet exception occurs. This is usually followed by a client
 * disconnect event firing.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class PacketExceptionEvent extends ServerEvent {
    
    /**
     * The {@link net.neto_framework.exceptions.PacketException PacketException} that was thrown.
     */
    private final PacketException exception;
    
    /**
     * The UUID of the client that was involved in the event. (May be null).
     */
    private final UUID uuid;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server
     *               Server}.
     * @param exception The {@link net.neto_framework.exceptions.PacketException PacketException}
     *                  that was thrown.
     * @param uuid The UUID of the client that was involved in the event.
     */
    public PacketExceptionEvent(Server server, PacketException exception, UUID uuid) {
        super(server, ServerEvents.PACKET_EXCEPTION);

        this.exception = exception;
        this.uuid = uuid;
    }
    
    /**
     * @param server Running instance of {@link net.neto_framework.server.Server
     *               Server}.
     * @param exception The {@link net.neto_framework.exceptions.PacketException PacketException}
     *                  that was thrown.
     */
    public PacketExceptionEvent(Server server, PacketException exception) {
        super(server, ServerEvents.PACKET_EXCEPTION);

        this.exception = exception;
        this.uuid = null;
    }

    /**
     * @return The {@link net.neto_framework.exceptions.PacketException PacketException} that was
     *         thrown.
     */
    public PacketException getException() {
        return this.exception;
    }
    
    /**
     * @return The UUID of the client that was involved in the event. (May be null)
     */
    public UUID getUUID() {
        return this.uuid;
    }
}
