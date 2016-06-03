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
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.server.event.ServerEvent;
import net.neto_framework.server.event.ServerEventListener;
import net.neto_framework.server.event.events.ClientConnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;

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
     * All registered {@link net.neto_framework.client.event.ClientEventListener 
     * ClientEventListener}.
     */
    private final ArrayList<ClientEventListener> clientEventListeners = new ArrayList<>();

    /**
     * Register listener.
     * 
     * @param listener {@link net.neto_framework.server.event.ServerEventListener
     *                 ServerEventListener}.
     */
    public void registerServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.add(listener);
    }

    /**
     * Register listener.
     * 
     * @param listener {@link net.neto_framework.client.event.ClientEventListener
     *                 ClientEventListener}.
     */
    public void registerClientEventListener(ClientEventListener listener) {
        this.clientEventListeners.add(listener);
    }

    /**
     * Unregister listener.
     * 
     * @param listener {@link net.neto_framework.server.event.ServerEventListener
     *                 ServerEventListener}.
     */
    public void unregisterServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.remove(listener);
    }

    /**
     * Unregister listener.
     * 
     * @param listener {@link net.neto_framework.client.event.ClientEventListener
     *                 ClientEventListener}.
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
        case RECEIVE_PACKET:
            this.serverEventListeners.stream().forEach((listener) -> {
                listener.onReceivePacket(
                        (net.neto_framework.server.event.events.ReceivePacketEvent) event);
            });
            break;
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
                listener.onPacketException((net.neto_framework.server.event.events.
                        PacketExceptionEvent) event);
            });
            break;
        }
    }

    /**
     * Call client event.
     * 
     * @param event {@link net.neto_framework.client.event.ClientEvent ClientEvent}.
     */
    public void callEvent(ClientEvent event) {
        switch (event.getEvent()) {
        case RECEIVE_PACKET:
            this.clientEventListeners.stream().forEach((listener) -> {
                listener.onReceivePacket(
                        (net.neto_framework.client.event.events.ReceivePacketEvent) event);
            });
            break;
        case DISCONNECT:
            this.clientEventListeners.stream().forEach((listener) -> {
                listener.onDisconnect((DisconnectEvent) event);
        });
            break;
        case PACKET_EXCEPTION:
            this.clientEventListeners.stream().forEach((listener) -> {
                listener.onPacketException((net.neto_framework.client.event.events.
                        PacketExceptionEvent) event);
        });
            break;
        }
    }
}
