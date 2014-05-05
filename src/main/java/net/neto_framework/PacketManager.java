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

import java.io.IOException;
import java.util.HashMap;

/**
 * Handles and manages all packets.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class PacketManager {
    private final HashMap<Integer, Packet> packets = new HashMap<Integer, Packet>();

    /**
     * Add packet.
     * 
     * @param packet
     *            Packet.
     */
    public void addPacket(Packet packet) {
        this.packets.put(packet.getID(), packet);
    }

    /**
     * Remove packet.
     * 
     * @param packet
     *            Packet.
     */
    public void removePacket(Packet packet) {
        this.packets.remove(packet.getID());
    }

    /**
     * Receive packet.
     * 
     * @param id
     *            Packet ID.
     * @param connection
     *            Connection.
     * @throws InstantiationException
     *             If fails to create packet.
     * @throws IllegalAccessException
     *             If fails to create packet.
     * @throws IOException
     *             If fails to receive packet.
     */
    public void receive(int id, Connection connection)
            throws InstantiationException, IllegalAccessException, IOException {
        Packet packet = this.packets.get(id).getClass().newInstance();
        packet.receive(connection);
        packet.onReceive(packet);
    }

    /**
     * @param id
     *            Packet ID.
     * @return If has packet with given ID.
     */
    public boolean hasPacket(int id) {
        return this.packets.containsKey(id);
    }
}
