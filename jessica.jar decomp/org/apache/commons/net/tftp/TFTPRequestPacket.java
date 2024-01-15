/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.tftp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import org.apache.commons.net.tftp.TFTPPacket;
import org.apache.commons.net.tftp.TFTPPacketException;

public abstract class TFTPRequestPacket
extends TFTPPacket {
    static final String[] _modeStrings = new String[]{"netascii", "octet"};
    static final byte[][] _modeBytes = new byte[][]{{110, 101, 116, 97, 115, 99, 105, 105, 0}, {111, 99, 116, 101, 116, 0}};
    int _mode;
    String _filename;

    TFTPRequestPacket(InetAddress destination, int port, int type, String filename, int mode) {
        super(type, destination, port);
        this._filename = filename;
        this._mode = mode;
    }

    TFTPRequestPacket(int type, DatagramPacket datagram) throws TFTPPacketException {
        super(type, datagram.getAddress(), datagram.getPort());
        int index;
        byte[] data = datagram.getData();
        if (this.getType() != data[1]) {
            throw new TFTPPacketException("TFTP operator code does not match type.");
        }
        StringBuffer buffer = new StringBuffer();
        int length = datagram.getLength();
        for (index = 2; index < length && data[index] != 0; ++index) {
            buffer.append((char)data[index]);
        }
        this._filename = buffer.toString();
        if (index >= length) {
            throw new TFTPPacketException("Bad filename and mode format.");
        }
        buffer.setLength(0);
        ++index;
        while (index < length && data[index] != 0) {
            buffer.append((char)data[index]);
            ++index;
        }
        String mode = buffer.toString().toLowerCase();
        length = _modeStrings.length;
        for (index = 0; index < length; ++index) {
            if (!mode.equals(_modeStrings[index])) continue;
            this._mode = index;
            break;
        }
        if (index >= length) {
            throw new TFTPPacketException("Unrecognized TFTP transfer mode: " + mode);
        }
    }

    final DatagramPacket _newDatagram(DatagramPacket datagram, byte[] data) {
        int fileLength = this._filename.length();
        int modeLength = _modeBytes[this._mode].length;
        data[0] = 0;
        data[1] = (byte)this._type;
        System.arraycopy(this._filename.getBytes(), 0, data, 2, fileLength);
        data[fileLength + 2] = 0;
        System.arraycopy(_modeBytes[this._mode], 0, data, fileLength + 3, modeLength);
        datagram.setAddress(this._address);
        datagram.setPort(this._port);
        datagram.setData(data);
        datagram.setLength(fileLength + modeLength + 3);
        return datagram;
    }

    public final DatagramPacket newDatagram() {
        int fileLength = this._filename.length();
        int modeLength = _modeBytes[this._mode].length;
        byte[] data = new byte[fileLength + modeLength + 4];
        data[0] = 0;
        data[1] = (byte)this._type;
        System.arraycopy(this._filename.getBytes(), 0, data, 2, fileLength);
        data[fileLength + 2] = 0;
        System.arraycopy(_modeBytes[this._mode], 0, data, fileLength + 3, modeLength);
        return new DatagramPacket(data, data.length, this._address, this._port);
    }

    public final int getMode() {
        return this._mode;
    }

    public final String getFilename() {
        return this._filename;
    }
}

