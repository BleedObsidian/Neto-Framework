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

package net.neto_framework;

import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;

/**
 * An interface used along with registering a packet that will be called when that specific packet
 * is received on the server.
 *
 * @param <T> The packet class this handler is receiving.
 * @author Jesse Prescott (BleedObsidian)
 */
public interface ServerPacketHandler<T extends Packet> {
    
    /**
     * Called when the specified packet is received.
     * 
     * @param server The running instance of Server.
     * @param client The ClientConnection that sent the packet.
     * @param packet Packet.
     */
    public void onReceivePacket(Server server, ClientConnection client, T packet);
}
