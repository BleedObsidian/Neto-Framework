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

package net.neto_framework.event;

import java.util.ArrayList;
import net.neto_framework.client.event.ClientEvent;
import net.neto_framework.client.event.ClientEventListener;
import net.neto_framework.client.event.events.ClientFailedToConnectToServer;
import net.neto_framework.client.event.events.ClientInvalidPacket;
import net.neto_framework.client.event.events.ClientPacketException;
import net.neto_framework.client.event.events.ClientServerConnect;
import net.neto_framework.client.event.events.ClientServerDisconnect;
import net.neto_framework.server.event.ServerEvent;
import net.neto_framework.server.event.ServerEventListener;
import net.neto_framework.server.event.events.ClientConnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.event.events.PacketExceptionEvent;

/**
 * Used to call and manage events for servers and clients.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class EventHandler {
    
    /**
     * All registered {@link net.neto_framework.server.event.ServerEventListener 
     * ServerEventListener}.
     */
    private final ArrayList<ServerEventListener> serverEventListeners = new ArrayList<>();
    
    /**
     * All registered ClientEvent listeners.
     */
    //TODO: Update javadoc for client events
    private final ArrayList<ClientEventListener> clientEventListeners = new ArrayList<>();

    /**
     * Register ServerEventListener.
     * 
     * @param listener {@link net.neto_framework.server.event.ServerEventListener
     *                 ServerEventListener}.
     */
    public void registerServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.add(listener);
    }

    /**
     * Register ClientEventListener.
     * 
     * @param listener ClientEventListener.
     */
    public void registerClientEventListener(ClientEventListener listener) {
        this.clientEventListeners.add(listener);
    }

    /**
     * Unregister ServerEventListener.
     * 
     * @param listener {@link net.neto_framework.server.event.ServerEventListener
     *                 ServerEventListener}.
     */
    public void unregisterServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.remove(listener);
    }

    /**
     * Unregister ClientEventListener.
     * 
     * @param listener ClientEventListener.
     */
    public void unregisterClientEventListener(ClientEventListener listener) {
        this.clientEventListeners.remove(listener);
    }

    /**
     * Call server event.
     * 
     * @param event {@link net.neto_framework.server.event.ServerEvent ServerEvent}.
     */
    public void callEvent(ServerEvent event) {
        switch (event.getEvent()) {
        case CLIENT_CONNECT:
            this.serverEventListeners.stream().forEach((listener) -> {
                listener.onClientConnect((ClientConnectEvent) event);
            });
            break;
        case CLIENT_FAILED_TO_CONNECT:
            this.serverEventListeners.stream().forEach((listener) -> {
                listener.onClientFailedToConnect((ClientFailedToConnectEvent) event);
            });
            break;
        case CLIENT_DISCONNECT:
            this.serverEventListeners.stream().forEach((listener) -> {
                listener.onClientDisconnect((ClientDisconnectEvent) event);
            });
            break;
        case PACKET_EXCEPTION:
            this.serverEventListeners.stream().forEach((listener) -> {
                listener.onPacketException((PacketExceptionEvent) event);
            });
            break;
        }
    }

    /**
     * Call client event.
     * 
     * @param event ClientEvent.
     */
    public void callEvent(ClientEvent event) {
        switch (event.getEvent()) {
        case CLIENT_SERVER_CONNECT:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientServerConnect((ClientServerConnect) event);
            }
            break;
        case CLIENT_FAILED_TO_CONNECT_TO_SERVER:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientFailedToConnectToServer((ClientFailedToConnectToServer) event);
            }
            break;
        case CLIENT_SERVER_DISCONNECT:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientServerDisconnect((ClientServerDisconnect) event);
            }
            break;
        case CLIENT_INVALID_PACKET:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientInvalidPacket((ClientInvalidPacket) event);
            }
            break;
        case CLIENT_PACKET_EXCEPTION:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientPacketException((ClientPacketException) event);
            }
            break;
        }
    }
}
