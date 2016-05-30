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

package net.neto_framework.client.event;

import net.neto_framework.client.Client;

/**
 * A base class for all client events.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ClientEvent {
    
    /**
     * Client.
     */
    private final Client client;
    
    /**
     * ClientEvents.
     */
    private final ClientEvents event;

    /**
     * New ClientEvent.
     * 
     * @param client Client.
     * @param event ClientEvents.
     */
    public ClientEvent(Client client, ClientEvents event) {
        this.client = client;
        this.event = event;
    }

    /**
     * @return Client.
     */
    public Client getClient() {
        return this.client;
    }

    /**
     * @return ClientEvents.
     */
    public ClientEvents getEvent() {
        return this.event;
    }
    
    /**
     * An enum that contains all registered client events.
     * 
     * @author BleedObsidian (Jesse Prescott)
     */
    public enum ClientEvents {
        /**
         * Fired when the client is disconnected from the server.
         */
        DISCONNECT,
        
        /**
         * Fired when a packet exception is raised in the client.
         */
        PACKET_EXCEPTION;
    }

}
