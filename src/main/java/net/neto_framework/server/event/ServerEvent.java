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

package net.neto_framework.server.event;

import net.neto_framework.server.Server;

/**
 * A base class for all sever events.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerEvent {
    
    /**
     * Running instance of server.
     */
    private final Server server;
    
    /**
     * Event Type.
     */
    private final ServerEvents eventType;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param eventType Which {@link ServerEvents Event} this represents.
     */
    public ServerEvent(Server server, ServerEvents eventType) {
        this.server = server;
        this.eventType = eventType;
    }

    /**
     * @return Running instance of {@link net.neto_framework.server.Server Server}.
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * @return Which {@link ServerEvents Event} this represents.
     */
    public ServerEvents getEventType() {
        return this.eventType;
    }
    
    /**
    * An enum that contains all registered server events.
    * 
    * @author BleedObsidian (Jesse Prescott)
    */
   public enum ServerEvents {
       
        /**
         * {@link net.neto_framework.server.event.events.ReceivePacketEvent ReceivePacketEvent}.
         */
       RECEIVE_PACKET,
       
       /**
        * {@link net.neto_framework.server.event.events.ClientConnectEvent ClientConnectEvent}.
        */
       CLIENT_CONNECT,
       
       /**
        * {@link net.neto_framework.server.event.events.ClientFailedToConnectEvent 
        * ClientFailedToConnectEvent}.
        */
       CLIENT_FAILED_TO_CONNECT,
       
       /**
        * {@link net.neto_framework.server.event.events.ClientDisconnectEvent 
        * ClientDisconnectEvent}.
        */
       CLIENT_DISCONNECT,
       
       /**
        * {@link net.neto_framework.server.event.events.PacketExceptionEvent PacketExceptionEvent}.
        */
       PACKET_EXCEPTION;
   }
}
