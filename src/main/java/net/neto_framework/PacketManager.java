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
import java.util.Map;
import net.neto_framework.client.Client;
import net.neto_framework.client.ServerConnection;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;

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
     * HashMap of all registered packets handlers for the server.
     */
    private final HashMap<Integer, ServerPacketHandler> serverPacketHandlers = new HashMap<>();
    
    /**
     * HashMap of all registered packets handlers for the client.
     */
    private final HashMap<Integer, ClientPacketHandler> clientPacketHandlers = new HashMap<>();

    /**
     * Register packet for server.
     * 
     * @param <T> A class that implements the Packet interface.
     * @param packetClass Packet class.
     * @param packetHandler The handler for this packet.
     */
    public <T extends Packet> void registerPacket(Class<T> packetClass,
            ServerPacketHandler<T> packetHandler) {
        
        Packet packet;
        try {
            packet = packetClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Packet class has a constructor.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Packet illegal access.", e);
        }
        
        if(!this.packets.containsKey(packet.getId()) && packet.getId() != 0) {
            this.packets.put(packet.getId(), packet);
            this.serverPacketHandlers.put(packet.getId(), packetHandler);
        } else {
            throw new RuntimeException("Packet ID is already being used or reserved.");
        }
    }
    
    /**
     * Register packet for client.
     * 
     * @param <T> A class that implements the Packet interface.
     * @param packetClass Packet class.
     * @param packetHandler The handler for this packet.
     */
    public <T extends Packet> void registerPacket(Class<T> packetClass,
            ClientPacketHandler<T> packetHandler) {
        
        Packet packet;
        try {
            packet = packetClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Packet class has a constructor.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Packet illegal access.", e);
        }
        
        if(!this.packets.containsKey(packet.getId()) && packet.getId() != 0) {
            this.packets.put(packet.getId(), packet);
            this.clientPacketHandlers.put(packet.getId(), packetHandler);
        } else {
            throw new RuntimeException("Packet ID is already being used or reserved.");
        }
    }
    
    /**
     * Register packet without a handler.
     * 
     * @param <T> A class that implements the Packet interface.
     * @param packetClass Packet class.
     */
    public <T extends Packet> void registerPacket(Class<T> packetClass) {
        Packet packet;
        try {
            packet = packetClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Packet class has a constructor.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Packet illegal access.", e);
        }
        
        if(!this.packets.containsKey(packet.getId()) && packet.getId() != 0) {
            this.packets.put(packet.getId(), packet);
        } else {
            throw new RuntimeException("Packet ID is already being used or reserved.");
        }
    }

    /**
     * Unregister packet.
     * 
     * @param <T> A class that implements the Packet interface.
     * @param packet Packet.
     */
    public <T extends Packet> void unregisterPacket(Class<T> packet) {
        for(Map.Entry<Integer, Packet> entry : this.packets.entrySet()) {
            if(entry.getValue().getClass() == packet) {
                this.packets.remove(entry.getKey());
                this.serverPacketHandlers.remove(entry.getKey());
                this.clientPacketHandlers.remove(entry.getKey());
            }
        }
    }

    /**
     * Receive packet server-side.
     * 
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param id Packet ID.
     * @param client The {@link net.neto_framework.server.ClientConnection ClientConnection} that
     *               the packet is from.
     * @param protocol The {@link net.neto_framework.Protocol Protocol} the packet is in.
     * @param discard Pull the data out of the stream but discard the packet.
     * @throws IOException If fails to receive packet.
     */
    public void receive(Server server, int id, ClientConnection client, Protocol protocol,
            boolean discard)
            throws IOException {
        try {
            Packet packet = this.packets.get(id).getClass().newInstance();
            
            if(protocol == Protocol.TCP) {
                packet.receive(client.getTCPConnection());
            } else {
                packet.receive(client.getUDPConnection());
            }
            
            if(discard) {
                return;
            }
            
            if(this.serverPacketHandlers.get(id) != null) {
                this.serverPacketHandlers.get(id).onReceivePacket(server, client, packet);
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Packet " + id + " class has a constructor.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Packet " + id + " illegal access.", e);
        }
    }
    
    /**
     * Receive packet server-side.
     * 
     * @param client Running instance of {@link net.neto_framework.client.Client Client}.
     * @param id Packet ID.
     * @param serverConnection The {@link net.neto_framework.client.ServerConnection
     *                         ServerConnection}.
     * @param protocol The {@link net.neto_framework.Protocol Protocol} the packet is in.
     * @param discard Pull the data out of the stream but discard the packet.
     * @throws IOException If fails to receive packet.
     */
    public void receive(Client client, int id, ServerConnection serverConnection, Protocol protocol,
            boolean discard)
            throws IOException {
        try {
            Packet packet = this.packets.get(id).getClass().newInstance();
            
            if(protocol == Protocol.TCP) {
                packet.receive(serverConnection.getTCPConnection());
            } else {
                packet.receive(serverConnection.getUDPConnection());
            }
            
            if(discard) {
                return;
            }
            
            if(this.clientPacketHandlers.get(id) != null) {
                this.clientPacketHandlers.get(id).onReceivePacket(client, packet);
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Packet " + id + " class has a constructor.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Packet " + id + " illegal access.", e);
        }
    }

    /**
     * @param id Packet ID.
     * @return If has packet with given ID.
     */
    public boolean hasPacket(int id) {
        return this.packets.containsKey(id);
    }
}
