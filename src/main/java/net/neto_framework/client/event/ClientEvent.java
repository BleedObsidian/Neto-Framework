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
     * Event Type.
     */
    private final ClientEvents eventType;

    /**
     * New ClientEvent.
     * 
     * @param client Client.
     * @param eventType Which {@link ClientEvents Event} this represents.
     */
    public ClientEvent(Client client, ClientEvents eventType) {
        this.client = client;
        this.eventType = eventType;
    }

    /**
     * @return Client.
     */
    public Client getClient() {
        return this.client;
    }

    /**
     * @return Which {@link ServerEvents Event} this represents.
     */
    public ClientEvents getEventType() {
        return this.eventType;
    }
    
    /**
     * An enum that contains all registered client events.
     * 
     * @author BleedObsidian (Jesse Prescott)
     */
    public enum ClientEvents {
        
        /**
         * {@link net.neto_framework.client.event.events.ReceivePacketEvent ReceivePacketEvent}.
         */
        RECEIVE_PACKET,
        
        /**
         * {@link net.neto_framework.client.event.events.DisconnectEvent DisconnectEvent}.
         */
        DISCONNECT,
        
        /**
         * {@link net.neto_framework.client.event.events.PacketExceptionEvent PacketExceptionEvent}.
         */
        PACKET_EXCEPTION;
    }

}
