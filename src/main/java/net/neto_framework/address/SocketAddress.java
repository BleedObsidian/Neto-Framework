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

package net.neto_framework.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.neto_framework.address.exceptions.SocketAddressException;

/**
 * A socket address used to define what address to bind to when creating a new
 * socket.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class SocketAddress {
    
    /**
     * InetAddress of socket.
     */
    private final InetAddress address;
    
    /**
     * Port of socket.
     */
    private final int port;

    /**
     * Use localhost and given port.
     * 
     * @param port Port number. (Max: 65535)
     * @throws net.neto_framework.address.exceptions.SocketAddressException
     */
    public SocketAddress(int port) throws SocketAddressException {
        try {
            this.address = InetAddress.getLocalHost();

            if (port <= 65535) {
                this.port = port;
            } else {
                throw new SocketAddressException("Port number " + port
                        + " out of range. (Max: 65535)");
            }
        } catch (UnknownHostException e) {
            throw new SocketAddressException("Failed to resolve localhost.", e);
        }
    }

    /**
     * Use given host name and port.
     * 
     * @param host Host name. (Can be an IP address or machine name)
     * @param port Port number. (Max: 65535)
     * @throws net.neto_framework.address.exceptions.SocketAddressException
     */
    public SocketAddress(String host, int port) throws SocketAddressException {
        try {
            this.address = InetAddress.getByName(host);

            if (port <= 65535) {
                this.port = port;
            } else {
                throw new SocketAddressException("Port number " + port
                        + " out of range. (Max: 65535)");
            }
        } catch (UnknownHostException e) {
            throw new SocketAddressException(
                    "IP address for the host could not be found.", e);
        }
    }

    /**
     * Use given host name in network byte order and port.
     * 
     * @param address Raw IP address in network byte order .
     * @param port Port number. (Max: 65535)
     * @throws net.neto_framework.address.exceptions.SocketAddressException
     */
    public SocketAddress(byte[] address, int port)
            throws SocketAddressException {
        try {
            this.address = InetAddress.getByAddress(address);

            if (port <= 65535) {
                this.port = port;
            } else {
                throw new SocketAddressException("Port number " + port
                        + " out of range. (Max: 65535)");
            }
        } catch (UnknownHostException e) {
            throw new SocketAddressException("IP address is too long.", e);
        }
    }

    /**
     * @return {@link java.net.InetAddress InetAddress}.
     */
    public InetAddress getInetAddress() {
        return this.address;
    }

    /**
     * @return Port number.
     */
    public int getPort() {
        return this.port;
    }
}
