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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * An interface to send and receive data from TCP and UDP.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Connection {
    
    /**
     * A magic string that should be within a handshake packet to verify the
     * connection is also using the Neto-Framework.
     */
    public static String MAGIC_STRING = "1293";
    
    /**
     * Protocol that connection is using.
     */
    private final Protocol protocol;

    /**
     * TCP Socket of connection.
     */
    private final Socket tcpSocket;
    
    /**
     * UDP Socket of connection.
     */
    private final DatagramSocket udpSocket;

    /**
     * InetAdress of connection.
     */
    private final InetAddress address;
    
    /**
     * Port of connection.
     */
    private final int port;
    
    /**
     * The stream to read data from when using UDP.
     */
    private ByteArrayInputStream udpDataInputStream;
    
    /**
     * The stream to write to when using UDP.
     */
    private ByteArrayOutputStream udpDataOutputStream;

    /**
     * New TCP Connection.
     * 
     * @param socket
     *            TCP Socket.
     */
    public Connection(Socket socket) {
        this.protocol = Protocol.TCP;

        this.tcpSocket = socket;
        this.udpSocket = null;

        this.address = null;
        this.port = 0;
    }

    /**
     * New UDP Connection.
     * 
     * @param udpSocket 
     *            UDP Socket.
     * @param address
     *            UDP InetAddress.
     * @param port
     *            Port number.
     */
    public Connection(DatagramSocket udpSocket, InetAddress address, int port) {
        this.protocol = Protocol.UDP;

        this.tcpSocket = null;
        this.udpSocket = udpSocket;

        this.address = address;
        this.port = port;
        
        this.flush();
    }
    
    /**
     * Flush the connection causing the output stream for UDP to reset.
     */
    public void flush() {
        this.udpDataOutputStream = new ByteArrayOutputStream();
    }

    /**
     * Send byte array to connection.
     * 
     * @param data
     *            Byte array data.
     * @throws IOException
     *             If failed to send
     */
    public void send(byte[] data) throws IOException {
        if (this.protocol == Protocol.TCP) {
            this.tcpSocket.getOutputStream().write(data);
        } else {
            this.udpDataOutputStream.write(data);
        }
    }

    /**
     * Receive byte array from connection.
     * 
     * @param buffer
     *            Buffer.
     * @return Byte array data.
     * @throws IOException
     *             If failed to read.
     */
    public byte[] receive(byte[] buffer) throws IOException {
        if (this.protocol == Protocol.TCP) {
            this.tcpSocket.getInputStream().read(buffer);
            return buffer;
        } else {
            this.udpDataInputStream.read(buffer);
            return buffer;
        }
    }

    /**
     * Send short to connection.
     * 
     * @param data
     *            Short.
     * @throws IOException
     *             If fails to send short.
     */
    public void sendShort(short data) throws IOException {
        this.send(ByteBuffer.allocate(2).putShort(data).array());
    }

    /**
     * Receive short from connection.
     * 
     * @return Short.
     * @throws IOException
     *             If fails to receive short.
     */
    public short receiveShort() throws IOException {
        return ByteBuffer.wrap(this.receive(new byte[2])).getShort();
    }

    /**
     * Send integer to connection.
     * 
     * @param data
     *            Integer.
     * @throws IOException
     *             If fails to send integer.
     */
    public void sendInteger(int data) throws IOException {
        this.send(ByteBuffer.allocate(4).putInt(data).array());
    }

    /**
     * Receive integer from connection.
     * 
     * @return Integer.
     * @throws IOException
     *             If fails to receive integer.
     */
    public int receiveInteger() throws IOException {
        return ByteBuffer.wrap(this.receive(new byte[4])).getInt();
    }

    /**
     * Send long to connection.
     * 
     * @param data
     *            Long.
     * @throws IOException
     *             If fails to send long.
     */
    public void sendLong(long data) throws IOException {
        this.send(ByteBuffer.allocate(8).putLong(data).array());
    }

    /**
     * Receive long from connection.
     * 
     * @return Long.
     * @throws IOException
     *             If fails to receive long.
     */
    public long receiveLong() throws IOException {
        return ByteBuffer.wrap(this.receive(new byte[8])).getLong();
    }

    /**
     * Send float to connection.
     * 
     * @param data
     *            Float.
     * @throws IOException
     *             If fails to send float.
     */
    public void sendFloat(float data) throws IOException {
        this.send(ByteBuffer.allocate(4).putFloat(data).array());
    }

    /**
     * Receive float from connection.
     * 
     * @return Float.
     * @throws IOException
     *             If fails to receive float.
     */
    public float receiveFloat() throws IOException {
        return ByteBuffer.wrap(this.receive(new byte[4])).getFloat();
    }

    /**
     * Send double to connection.
     * 
     * @param data
     *            Double.
     * @throws IOException
     *             If fails to send double.
     */
    public void sendDouble(double data) throws IOException {
        this.send(ByteBuffer.allocate(8).putDouble(data).array());
    }

    /**
     * Receive double from connection.
     * 
     * @return Double.
     * @throws IOException
     *             If fails to receive double.
     */
    public double receiveDouble() throws IOException {
        return ByteBuffer.wrap(this.receive(new byte[8])).getDouble();
    }

    /**
     * Send boolean to connection.
     * 
     * @param data
     *            Boolean.
     * @throws IOException
     *             If failes to send boolean.
     */
    public void sendBoolean(boolean data) throws IOException {
        this.send(new byte[] { (byte) (data ? 0x01 : 0x00) });
    }

    /**
     * Receive boolean from connection.
     * 
     * @return Boolean.
     * @throws IOException
     *             If fails to receive boolean.
     */
    public boolean receiveBoolean() throws IOException {
        return (this.receive(new byte[1])[0] != 0);
    }

    /**
     * Send char to connection.
     * 
     * @param data
     *            Char.
     * @throws IOException
     *             If fails to send char.
     */
    public void sendChar(char data) throws IOException {
        this.send(ByteBuffer.allocate(2).putChar(data).array());
    }

    /**
     * Receive char from connection.
     * 
     * @return Char.
     * @throws IOException
     *             If fails to receive char.
     */
    public char receiveChar() throws IOException {
        return ByteBuffer.wrap(this.receive(new byte[2])).getChar();
    }

    /**
     * Send string to connection.
     * 
     * @param string
     *            String.
     * @throws IOException
     *             If failed to send.
     */
    public void sendString(String string) throws IOException {
        this.sendInteger(string.getBytes().length);
        this.send(string.getBytes());
    }

    /**
     * Receive string from connection.
     * 
     * @return Trimmed string.
     * @throws IOException
     *             If fails to receive string.
     */
    public String receiveString() throws IOException {
        int size = this.receiveInteger();
        return new String(this.receive(new byte[size])).trim();
    }

    /**
     * @return Protocol.
     */
    public synchronized Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * @return Socket. (Null if protocol is UDP)
     */
    public synchronized Socket getTCPSocket() {
        return this.tcpSocket;
    }

    /**
     * @return DatagramSocket. (Null if protocol is TCP)
     */
    public synchronized DatagramSocket getUDPSocket() {
        return this.udpSocket;
    }

    /**
     * @return InetAddress. (Null if protocol is TCP)
     */
    public synchronized InetAddress getAddress() {
        return this.address;
    }

    /**
     * @return Port. (Null if protocol is TCP)
     */
    public synchronized int getPort() {
        return this.port;
    }
    
    /**
     * @param inputStream ByteArrayInputStream containing the data source.
     */
    public synchronized void setUdpDataInputStream(ByteArrayInputStream
            inputStream) {
        this.udpDataInputStream = inputStream;
    }
    
    /**
     * @return ByteArrayOutputStream full of all data to send.
     */
    public synchronized ByteArrayOutputStream getUdpDataOutputStream() {
        return this.udpDataOutputStream;
    }
}
