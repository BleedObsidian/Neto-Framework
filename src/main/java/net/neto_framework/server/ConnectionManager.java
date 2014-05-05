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

package net.neto_framework.server;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * A manager to take care of all connections via tcp/udp.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ConnectionManager {
    private int idPool = 0;

    private volatile HashMap<Integer, Socket> tcpConnections = new HashMap<Integer, Socket>();
    private volatile HashMap<Integer, InetAddress> udpConnections = new HashMap<Integer, InetAddress>();

    /**
     * Add given TCP connection into pool.
     * 
     * @param socket
     *            Socket of new TCP connection.
     * @return Unique connection ID.
     */
    public int addTcpConnection(Socket socket) {
        this.tcpConnections.put(this.idPool++, socket);
        System.out.println("TCP Connection Added");
        return this.idPool;
    }

    /**
     * Add given UDP connection into pool.
     * 
     * @param address
     *            InetAddress of new UDP connection.
     * @return Unique connection ID.
     */
    public int addUdpConnection(InetAddress address) {
        this.udpConnections.put(this.idPool++, address);
        System.out.println("UDP Connection Added");
        return this.idPool;
    }

    /**
     * Remove TCP connection.
     * 
     * @param id
     *            Unique ID of connection.
     */
    public void removeTcpConnection(int id) {
        this.tcpConnections.remove(id);
    }

    /**
     * Remove UDP connection.
     * 
     * @param id
     *            Unique ID of connection.
     */
    public void removeUdpConnection(int id) {
        this.udpConnections.remove(id);
    }

    /**
     * If TCP connection pool contains a connection with the given ID.
     * 
     * @param id
     *            ID.
     * @return If connection exists.
     */
    public boolean hasTcpConnection(int id) {
        return this.tcpConnections.containsKey(id) ? true : false;
    }

    /**
     * If UDP connection pool contains a connection with the given ID.
     * 
     * @param id
     *            ID.
     * @return If connection exists.
     */
    public boolean hasUdpConnection(int id) {
        return this.udpConnections.containsKey(id) ? true : false;
    }

    /**
     * Get TCP socket for connection with given ID.
     * 
     * @param id
     *            ID.
     * @return TCP Socket.
     */
    public Socket getTcpConnection(int id) {
        return this.tcpConnections.get(id);
    }

    /**
     * Get InetAdress for connection with given ID.
     * 
     * @param id
     *            ID.
     * @return UDP InetAddress.
     */
    public InetAddress getUdpConnection(int id) {
        return this.udpConnections.get(id);
    }
}
