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
import net.neto_framework.server.event.ServerEvent;
import net.neto_framework.server.event.ServerEventListener;
import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerReceiveInvalidHandshake;

/**
 * Used to call and manage events for servers and clients.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class EventHandler {
    private final ArrayList<ServerEventListener> serverEventListeners = new ArrayList<ServerEventListener>();
    private final ArrayList<ClientEventListener> clientEventListeners = new ArrayList<ClientEventListener>();

    /**
     * Add ServerEventListener.
     * 
     * @param listener
     *            ServerEventListener.
     */
    public void addServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.add(listener);
    }

    /**
     * Add ClientEventListener.
     * 
     * @param listener
     *            ClientEventListener.
     */
    public void addClientEventListener(ClientEventListener listener) {
        this.clientEventListeners.add(listener);
    }

    /**
     * Remove ServerEventListener.
     * 
     * @param listener
     *            ServerEventListener.
     */
    public void removeServerEventListener(ServerEventListener listener) {
        this.serverEventListeners.remove(listener);
    }

    /**
     * Remove ClientEventListener.
     * 
     * @param listener
     *            ClientEventListener.
     */
    public void removeClientEventListener(ClientEventListener listener) {
        this.clientEventListeners.remove(listener);
    }

    /**
     * Call server event.
     * 
     * @param event
     *            - ServerEvent.
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
        }
    }

    /**
     * Call client event.
     * 
     * @param event
     *            - ClientEvent.
     */
    public void callEvent(ClientEvent event) {

    }
}
