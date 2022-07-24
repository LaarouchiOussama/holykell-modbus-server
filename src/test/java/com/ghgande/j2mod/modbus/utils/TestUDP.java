/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghgande.j2mod.modbus.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

/**
 * A sanity check of how UDP comms should work between client and server
 * Taken from the online Oracle Java examples
 */
public class TestUDP {

    public static void main(String[] args) throws IOException {
        new QuoteServerThread().start();
        for (int i = 0; i < 100; i++) {
            System.out.println(readQuote());
        }
    }

    public static String readQuote() throws IOException {

        // get a datagram socket
        DatagramSocket socket = new DatagramSocket();

        // send request
        byte[] buf = new byte[256];
        InetAddress address = InetAddress.getByName(TestUtils.getFirstIp4Address());
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);

        // get response
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        socket.close();
        return received;
    }

    static class QuoteServerThread extends Thread {
        protected DatagramSocket socket = null;
        protected boolean moreQuotes = true;

        public QuoteServerThread() throws IOException {
            this("QuoteServer");
        }

        public QuoteServerThread(String name) throws IOException {
            super(name);
            socket = new DatagramSocket(4445, InetAddress.getByAddress(new byte[]{0, 0, 0, 0}));
        }

        public void run() {

            while (moreQuotes) {
                try {
                    byte[] buf = new byte[256];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    // figure out response
                    String dString;
                    dString = new Date().toString();
                    buf = dString.getBytes();

                    // send the response to the client at "address" and "port"
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    moreQuotes = false;
                }
            }
            socket.close();
        }
    }
}
