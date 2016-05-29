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

/**
 * An event that is fired when the client is disconnected from the server.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class DisconnectEvent extends ClientEvent {
    
    /**
     * The {@link DisconnectReason DisconnectReason} the client was disconnected.
     */
    private final DisconnectReason reason;
    
    /**
     * The exception that caused the client to be disconnected. This will be null if the reason is
     * not {@link DisconnectReason#EXCEPTION EXCEPTION}.
     */
    private final Exception exception;

    /**
     * @param client Running instance of {@link net.neto_framework.client.Client Client}.
     * @param reason The {@link DisconnectReason DisconnectReason} that the client was disconnected.
     * @param exception The exception that caused the client to be disconnected.
     */
    public DisconnectEvent(Client client, DisconnectReason reason, Exception exception) {
        super(client, ClientEvents.DISCONNECT);

        this.reason = reason;
        this.exception = exception;
    }

    /**
     * @param client Running instance of {@link net.neto_framework.client.Client Client}.
     * @param reason The {@link DisconnectReason DisconnectReason} that the client was disconnected.
     */
    public DisconnectEvent(Client client, DisconnectReason reason) {
        super(client, ClientEvents.DISCONNECT);

        this.reason = reason;
        this.exception = null;
    }
    
    /**
     * @return The {@link DisconnectReason DisconnectReason} that the client was disconnected.
     */
    public DisconnectReason getReason() {
        return this.reason;
    }
    
    /**
     * @return The exception that caused the client to be disconnected.
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * An enum of the possible reasons that a client was disconnected from the server. This is used
     * in the {@link net.neto_framework.client.event.events.DisconnectEvent DisconnectEvent}.
     *
     * @author BleedObsidian (Jesse Prescott)
     */
    public enum DisconnectReason {

        /**
         * Client was disconnected due to an exception. This can be caused by the server not sending
         * a disconnect packet before closing the connection. The exception can be found in the 
         * {@link net.neto_framework.client.event.events.DisconnectEvent DisconnectEvent}.
         */
        EXCEPTION,
        
        /**
         * Client sent a disconnect packet. This is sent when a client chooses to disconnect 
         * cleanly.
         */
        DISCONNECT_PACKET,
        
        /**
         * Client has not received keep alive from server in given time frame. This is common for a 
         * UDP connection if a disconnect packet was not received.
         */
        KEEP_ALIVE;
    }
}
