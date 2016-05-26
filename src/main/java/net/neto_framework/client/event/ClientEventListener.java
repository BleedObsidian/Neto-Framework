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

import net.neto_framework.client.event.events.ClientInvalidPacket;
import net.neto_framework.client.event.events.ClientPacketException;
import net.neto_framework.client.event.events.ClientFailedToConnectToServer;
import net.neto_framework.client.event.events.ClientServerConnect;
import net.neto_framework.client.event.events.ClientServerDisconnect;

/**
 * An abstract class that allows actions to be taken when specified events are
 * called on the client.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public abstract class ClientEventListener {
    /**
     * Fired when the client successfully connects to a server.
     * 
     * @param event ClientServerConnect.
     */
    public void onClientServerConnect(ClientServerConnect event) {

    }
    
    /**
     * Fired when the client fails to connect to the server. Reasons include
     * I/O errors and Invalid Handshakes.
     * 
     * @param event ClientFailedToConnectToServer event.
     */
    public void onClientFailedToConnectToServer(
            ClientFailedToConnectToServer event) {

    }

    /**
     * Fired when the client disconnects from a server.
     * 
     * @param event ClientServerConnect.
     */
    public void onClientServerDisconnect(ClientServerDisconnect event) {

    }

    /**
     * Fired when the client receives an invalid packet or packet id.
     * 
     * @param event ClientInvalidPacket.
     */
    public void onClientInvalidPacket(ClientInvalidPacket event) {

    }

    /**
     * Fired when the client has a problem receiving or sending a packet.
     * 
     * @param event ClientPacketException.
     */
    public void onClientPacketException(ClientPacketException event) {

    }
}
