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

import net.neto_framework.server.event.events.ClientConnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.event.events.PacketExceptionEvent;
import net.neto_framework.server.event.events.ReceivePacketEvent;

/**
 * An abstract class that allows actions to be taken when specified events are called on the server.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public abstract class ServerEventListener {
    
    /**
     * Fired when a packet is received client side.
     * 
     * @param event {@link net.neto_framework.server.event.events.ReceivePacketEvent 
     *              ReceivePacketEvent}.
     */
    public void onReceivePacket(ReceivePacketEvent event) {
        
    }
    
    /**
     * Fired when a new client successfully connects to the server.
     * 
     * @param event {@link net.neto_framework.server.event.events.ClientConnectEvent 
     *              ClientConnectEvent}.
     */
    public void onClientConnect(ClientConnectEvent event) {
    }
    
    /**
     * Fired when a new client tried to connect but failed (TCP Only). Reasons include IOExceptions
     * or invalid handshakes.
     * 
     * @param event {@link net.neto_framework.server.event.events.ClientFailedToConnectEvent
     *              ClientFailedToConnectEvent}.
     */
    public void onClientFailedToConnect(ClientFailedToConnectEvent event){
    }
    
    /**
     * Fired when a new client is disconnected from the server. Reasons include IOExceptions,
     * receiving disconnect packet and client not responding to keep alive.
     * 
     * @param event {@link net.neto_framework.server.event.events.ClientDisconnectEvent 
     *              ClientDisconnectEvent}.
     */
    public void onClientDisconnect(ClientDisconnectEvent event) {
    }
    
    /**
     * Fired when a packet exception is raised. This is usually followed by a client
     * disconnect event firing.
     * 
     * @param event {@link net.neto_framework.server.event.events.PacketExceptionEvent
     *              PacketExceptionEvent}
     */
    public void onPacketException(PacketExceptionEvent event) {
        
    }
}
