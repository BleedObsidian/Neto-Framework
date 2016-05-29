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
import java.util.Map.Entry;

/**
 * Handles and manages all packets.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class PacketManager {
    
    /**
     * HashMap of all registered packets.
     */
    private final HashMap<Integer, Packet> packets = new HashMap<>();
    
    /**
     * The pool used to assign unique packet id's.
     */
    private int idPool = 1;

    /**
     * Register packet.
     * 
     * @param packet Packet.
     */
    public void registerPacket(Packet packet) {
        this.packets.put(this.idPool, packet);
        this.idPool++;
    }

    /**
     * Unregister packet.
     * 
     * @param packet Packet.
     */
    public void unregisterPacket(Packet packet) {
        for(Entry<Integer, Packet> registeredPacket : this.packets.entrySet()) {
            if(registeredPacket.getValue().getClass() == packet.getClass()) {
                this.packets.remove(registeredPacket.getKey());
                return;
            }
        }
    }

    /**
     * Receive packet.
     * 
     * @param id Packet ID.
     * @param connection Connection.
     * @param receiver PacketReceiver.
     * @throws IOException If fails to receive packet.
     */
    public void receive(int id, Connection connection, PacketReceiver receiver) throws IOException {
        try {
            Packet packet = this.packets.get(id).getClass().newInstance();
            packet.receive(connection);
            packet.onReceive(packet, receiver);
        } catch (InstantiationException e) {
            throw new RuntimeException("Packet " + id + " class has a constructor.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Packet " + id + " illegal access.", e);
        }
    }
    
    /**
     * Get the ID for a given packet.
     * 
     * @param packet {@link net.neto_framework.Packet Packet}.
     * @return The ID of the packet. (Returns 0 if unknown/unregistered packet).
     */
    public int getIdOfpacket(Packet packet) {
        for(Entry<Integer, Packet> registeredPacket : this.packets.entrySet()) {
            if(registeredPacket.getValue().getClass() == packet.getClass()) {
                return registeredPacket.getKey();
            }
        }
        
        return 0;
    }

    /**
     * @param id Packet ID.
     * @return If has packet with given ID.
     */
    public boolean hasPacket(int id) {
        return this.packets.containsKey(id);
    }
}
