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
 * A disconnect packet can be sent to/from either server or client. This packet is sent before the
 * client/server closes connection, allowing the client/server to end the connection cleanly.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class DisconnectPacket implements Packet {
    
    @Override
    public void send(Connection connection) throws IOException {
    }

    @Override
    public void receive(Connection connection) throws IOException {
    }

    @Override
    public int getId() {
        return -3;
    }
}
