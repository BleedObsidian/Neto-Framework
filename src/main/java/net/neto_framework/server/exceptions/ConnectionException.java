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

package net.neto_framework.server.exceptions;

/**
 * An exception thrown when failing to accept a socket connection or when
 * failing to receiving a handshake packet.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ConnectionException extends Exception {
    
    /**
     * SerialVersionUID.
     */
    private static final long serialVersionUID = 4404027049082607879L;

    /**
     * New ConnectionException with cause exception.
     * 
     * @param message
     *            Reason for exception.
     * @param exception
     *            Cause exception.
     */
    public ConnectionException(String message, Exception exception) {
        super(message, exception);
    }
}