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

package net.neto_framework.packets;

import java.io.IOException;
import net.neto_framework.Connection;
import net.neto_framework.Packet;

/**
 * The success packet is sent from server to client and is the final packet involved in the
 * handshake process. This packet contains sensitive information such as the client's UUID. This
 * UUID will be used by the client to authenticate every incoming and outgoing UDP packet.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class SuccessPacket implements Packet {
    
    /**
     * The UUID of the client.
     */
    private String uuid;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(this.uuid);
    }

    @Override
    public void receive(Connection connection) throws IOException {
        this.uuid = connection.receiveString();
    }
    
    /**
     * 
     * @return The UUID of the client.
     */
    public String getUUID() {
        return this.uuid;
    }
    
    /**
     * @param uuid The UUID of the client.
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int getId() {
        return -3;
    }

}
