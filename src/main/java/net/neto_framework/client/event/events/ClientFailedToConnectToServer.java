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
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * Fired when a new client tried to connect but failed. Reasons can include
 * I/O Exceptions or Invalid Handshakes.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ClientFailedToConnectToServer extends ClientEvent {
    
    /**
     * ConnectionException thrown.
     */
    private final ConnectionException exception;

    /**
     * New ClientFailedToConnectToServer event.
     * 
     * @param client Client.
     * @param exception Exception thrown.
     */
    public ClientFailedToConnectToServer(Client client, 
            ConnectionException exception) {
        super(client, ClientEvents.CLIENT_FAILED_TO_CONNECT_TO_SERVER);

        this.exception = exception;
    }

    /**
     * @return ConnectionException thrown.
     */
    public ConnectionException getException() {
        return this.exception;
    }
}