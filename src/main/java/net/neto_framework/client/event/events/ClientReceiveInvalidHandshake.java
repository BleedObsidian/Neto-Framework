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

package net.neto_framework.client.event.events;

import java.net.InetAddress;

import net.neto_framework.client.Client;
import net.neto_framework.client.event.ClientEvent;
import net.neto_framework.client.event.ClientEvents;

/**
 * An event that is fired when the client receives an invalid handshake packet.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ClientReceiveInvalidHandshake extends ClientEvent {
    
    /**
     * InetAddress of InvalidHandshake.
     */
    private final InetAddress address;
    
    /**
     * Data within invalid handshake.
     */
    private final byte[] data;

    /**
     * New ClientReceiveInvalidHandshake event.
     * 
     * @param client
     *            Client.
     * @param address
     *            Address of handshake origin.
     * @param data
     *            Data received instead of magic number.
     */
    public ClientReceiveInvalidHandshake(Client client, InetAddress address,
            byte[] data) {
        super(client, ClientEvents.CLIENT_RECEIVE_INVALID_HANDSHAKE);

        this.address = address;
        this.data = data;
    }

    /**
     * @return Address of invalid handshake packet origin.
     */
    public InetAddress getAddress() {
        return this.address;
    }

    /**
     * @return Invalid data received in handshake packet.
     */
    public byte[] getData() {
        return this.data;
    }
}
