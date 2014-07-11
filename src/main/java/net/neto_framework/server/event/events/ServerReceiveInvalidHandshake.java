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

package net.neto_framework.server.event.events;

import java.net.InetAddress;

import net.neto_framework.server.Server;
import net.neto_framework.server.event.ServerEvent;
import net.neto_framework.server.event.ServerEvents;

/**
 * An event that is fired when the server receives an invalid handshake packet.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ServerReceiveInvalidHandshake extends ServerEvent {
    
    /**
     * InetAddress of invalid handshake.
     */
    private final InetAddress address;
    
    /**
     * Data within invalid handshake.
     */
    private final byte[] data;

    /**
     * New ServerReceiveInvalidHandshake event.
     * 
     * @param server
     *            Server.
     * @param address
     *            Address of handshake origin.
     * @param data
     *            Data received instead of magic number.
     */
    public ServerReceiveInvalidHandshake(Server server, InetAddress address,
            byte[] data) {
        super(server, ServerEvents.SERVER_RECEIVE_INVALID_HANDSHAKE);

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
