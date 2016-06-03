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

import net.neto_framework.server.Server;
import net.neto_framework.server.event.ServerEvent;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * Fired when a new client tried to connect but failed (TCP Only). Reasons can include IOExceptions
 * or invalid handshakes.
 * <p>
 * This contains the {@link net.neto_framework.server.exceptions.ConnectionException
 * ConnectionException} that was thrown.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ClientFailedToConnectEvent extends ServerEvent {
    
    /**
     * The ConnectionException that was thrown.
     */
    private final ConnectionException exception;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param exception The {@link net.neto_framework.server.exceptions.ConnectionException
     *                  ConnectionException} that was thrown.
     */
    public ClientFailedToConnectEvent(Server server, 
            ConnectionException exception) {
        super(server, ServerEvents.CLIENT_FAILED_TO_CONNECT);

        this.exception = exception;
    }

    /**
     * @return The {@link net.neto_framework.server.exceptions.ConnectionException 
     *         ConnectionException} that was thrown.
     */
    public ConnectionException getException() {
        return this.exception;
    }
}
