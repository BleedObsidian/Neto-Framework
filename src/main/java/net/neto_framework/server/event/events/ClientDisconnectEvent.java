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
import net.neto_framework.server.Server;
import net.neto_framework.server.event.ServerEvent;

/**
 * An event that is fired when a client is disconnected from the server. Reasons include
 * IOExceptions, receiving disconnect packet or client not responding to keep alive.
 * <p>
 * This contains the reason why the client was disconnected, the UUID of the client and if
 * applicable the exception that caused it.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class ClientDisconnectEvent extends ServerEvent {

    /**
     * The reason the client was disconnected from the server.
     */
    private final ClientDisconnectReason reason;

    /**
     * The UUID of the client that was disconnected.
     */
    private final UUID uuid;

    /**
     * The exception that caused the client to be disconnected. This will be null if the reason is
     * not {@link ClientDisconnectReason#EXCEPTION EXCEPTION}.
     */
    private final Exception exception;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param reason The {@link ClientDisconnectReason ClientDisconnectReason} that the client was
     *               disconnected.
     * @param uuid The {@link java.util.UUID UUID} of the client that was disconnected.
     * @param exception The exception that caused the client to be disconnected.
     */
    public ClientDisconnectEvent(Server server, ClientDisconnectReason reason, UUID uuid,
            Exception exception) {
        super(server, ServerEvents.CLIENT_DISCONNECT);

        this.reason = reason;
        this.uuid = uuid;
        this.exception = exception;
    }

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param reason The {@link ClientDisconnectReason ClientDisconnectReason} that the client was
     *               disconnected.
     * @param uuid The {@link java.util.UUID UUID} of the client that was disconnected.
     */
    public ClientDisconnectEvent(Server server, ClientDisconnectReason reason, UUID uuid) {
        super(server, ServerEvents.CLIENT_DISCONNECT);

        this.reason = reason;
        this.uuid = uuid;
        this.exception = null;
    }

    /**
     * @return The {@link ClientDisconnectReason ClientDisconnectReason}.
     */
    public ClientDisconnectReason getReason() {
        return this.reason;
    }

    /**
     * @return The UUID that was given to the {@link net.neto_framework.server.ClientConnection
     *         ClientConnection}.
     */
    public UUID getClientUUID() {
        return this.uuid;
    }

    /**
     * @return The exception that caused the client to be disconnected. This is null if the reason
     * is not {@link ClientDisconnectReason#EXCEPTION EXCEPTION}
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * An enum of the possible reasons that a client was disconnected from the server. This is used
     * in the {@link net.neto_framework.server.event.events.ClientDisconnectEvent 
     * ClientDisconnectEvent}.
     *
     * @author BleedObsidian (Jesse Prescott)
     */
    public enum ClientDisconnectReason {

        /**
         * Client was disconnected due to an exception. This can be caused by the client not sending
         * a disconnect packet before closing the connection. The exception can be found in the 
         * {@link net.neto_framework.server.event.events.ClientDisconnectEvent 
         * ClientDisconnectEvent}.
         */
        EXCEPTION,
        
        /**
         * Client sent a disconnect packet. This is sent when a client chooses to disconnect 
         * cleanly.
         */
        DISCONNECT_PACKET,
        
        /**
         * Client did not respond to keep alive in the given time frame. This is common for a UDP
         * connection if a disconnect packet was not received.
         */
        KEEP_ALIVE;
    }

}
