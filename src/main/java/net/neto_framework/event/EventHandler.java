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
import net.neto_framework.client.event.events.ClientInvalidPacket;
import net.neto_framework.client.event.events.ClientPacketException;
import net.neto_framework.client.event.events.ClientReceiveInvalidHandshake;
import net.neto_framework.client.event.events.ClientServerConnect;
import net.neto_framework.client.event.events.ClientServerDisconnect;
import net.neto_framework.server.event.ServerEvent;
import net.neto_framework.server.event.ServerEventListener;
import net.neto_framework.server.event.events.ServerClientConnect;
import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerInvalidPacket;
import net.neto_framework.server.event.events.ServerPacketException;
import net.neto_framework.server.event.events.ServerReceiveInvalidHandshake;
import net.neto_framework.server.event.events.ServerStart;
import net.neto_framework.server.event.events.ServerStop;

/**
 * Used to call and manage events for servers and clients.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class EventHandler {
    
    /**
     * All registered ServerEvent listeners.
     */
    private final ArrayList<ServerEventListener> serverEventListeners = new ArrayList<ServerEventListener>();
    
    /**
     * All registered ClientEvent listeners.
     */
    private final ArrayList<ClientEventListener> clientEventListeners = new ArrayList<ClientEventListener>();

    /**
     * Register ServerEventListener.
     * 
     * @param listener
     *            ServerEventListener.
     */
    public void registerServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.add(listener);
    }

    /**
     * Register ClientEventListener.
     * 
     * @param listener
     *            ClientEventListener.
     */
    public void registerClientEventListener(ClientEventListener listener) {
        this.clientEventListeners.add(listener);
    }

    /**
     * Unregister ServerEventListener.
     * 
     * @param listener
     *            ServerEventListener.
     */
    public void unregisterServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.remove(listener);
    }

    /**
     * Unregister ClientEventListener.
     * 
     * @param listener
     *            ClientEventListener.
     */
    public void unregisterClientEventListener(ClientEventListener listener) {
        this.clientEventListeners.remove(listener);
    }

    /**
     * Call server event.
     * 
     * @param event
     *            ServerEvent.
     */
    public void callEvent(ServerEvent event) {
        switch (event.getEvent()) {
        case SERVER_FAILED_TO_ACCEPT_CONNECTION:
            for (ServerEventListener listener : this.serverEventListeners) {
                listener.onServerFailedToAcceptConnection((ServerFailedToAcceptConnection) event);
            }
            break;
        case SERVER_RECEIVE_INVALID_HANDSHAKE:
            for (ServerEventListener listener : this.serverEventListeners) {
                listener.onServerReceiveInvalidHandshake((ServerReceiveInvalidHandshake) event);
            }
            break;
        case SERVER_CLIENT_CONNECT:
            for (ServerEventListener listener : this.serverEventListeners) {
                listener.onServerClientConnect((ServerClientConnect) event);
            }
            break;
        case SERVER_START:
            for (ServerEventListener listener : this.serverEventListeners) {
                listener.onServerStart((ServerStart) event);
            }
            break;
        case SERVER_STOP:
            for (ServerEventListener listener : this.serverEventListeners) {
                listener.onServerStop((ServerStop) event);
            }
            break;
        case SERVER_INVALID_PACKET:
            for (ServerEventListener listener : this.serverEventListeners) {
                listener.onServerInvalidPacket((ServerInvalidPacket) event);
            }
            break;
        case SERVER_PACKET_EXCEPTION:
            for (ServerEventListener listener : this.serverEventListeners) {
                listener.onServerPacketException((ServerPacketException) event);
            }
            break;
        }
    }

    /**
     * Call client event.
     * 
     * @param event
     *            ClientEvent.
     */
    public void callEvent(ClientEvent event) {
        switch (event.getEvent()) {
        case CLIENT_SERVER_CONNECT:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientServerConnect((ClientServerConnect) event);
            }
            break;
        case CLIENT_SERVER_DISCONNECT:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientServerDisconnect((ClientServerDisconnect) event);
            }
            break;
        case CLIENT_RECEIVE_INVALID_HANDSHAKE:
            for (ClientEventListener listener : this.clientEventListeners) {
                listener.onClientReceiveInvalidHandshake((ClientReceiveInvalidHandshake) event);
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