package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.net.ModbusTCPListener;
import com.ghgande.j2mod.modbus.net.TCPConnectionHandler;
import com.ghgande.j2mod.modbus.net.TCPSlaveConnection;
import com.ghgande.j2mod.modbus.procimg.DigitalIn;
import com.ghgande.j2mod.modbus.procimg.DigitalOut;
import com.ghgande.j2mod.modbus.procimg.FIFO;
import com.ghgande.j2mod.modbus.procimg.File;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;

public class TestClient {

  static final String[] serialIds = {"000001", "000002", "000003", "000004", "000005", "000006", "000007"};
  static final String serverIp = "localhost";
  static final int serverPort = 30651;

  static final Random random = new Random();

  public static void main(String[] args) throws Exception {
    Thread t = null;
    for (String serialId : serialIds) {
      t = new Thread(getClientRunnable(serialId));
      t.start();
    }
    t.join();
  }

  public static Runnable getClientRunnable(final String serialId) throws Exception {
    return () -> {
      try {
        Socket clientSocket = new Socket(serverIp, serverPort);
        clientSocket.setKeepAlive(true);
        clientSocket.setSoTimeout(0);

        // Send serial id
        OutputStream out = clientSocket.getOutputStream();
        for (char c : serialId.toCharArray()) {
          out.write(c);
        }

        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(clientSocket.getInetAddress(), 30000 + random.nextInt(1000), 1, true);
        slave.addProcessImage(1, new DumbProcessImage());
        slave.open();
        slave.injectSocket(clientSocket);

        while (true) {
          Thread.sleep(10000);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }


  static class DumbProcessImage implements ProcessImage {

    @Override
    public DigitalOut[] getDigitalOutRange(int offset, int count) throws IllegalAddressException {
      return new DigitalOut[0];
    }

    @Override
    public DigitalOut getDigitalOut(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public int getDigitalOutCount() {
      return 0;
    }

    @Override
    public DigitalIn[] getDigitalInRange(int offset, int count) throws IllegalAddressException {
      return new DigitalIn[0];
    }

    @Override
    public DigitalIn getDigitalIn(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public int getDigitalInCount() {
      return 0;
    }

    @Override
    public InputRegister[] getInputRegisterRange(int offset, int count)
        throws IllegalAddressException {
      return new InputRegister[0];
    }

    @Override
    public InputRegister getInputRegister(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public int getInputRegisterCount() {
      return 0;
    }

    @Override
    public Register[] getRegisterRange(int offset, int count) throws IllegalAddressException {
      Register[] regs = new Register[count];
      for (int i = 0; i < count; i++) {
        regs[i] = new SimpleRegister(offset + i);
      }
      return regs;
    }

    @Override
    public Register getRegister(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public int getRegisterCount() {
      return 0;
    }

    @Override
    public File getFile(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public File getFileByNumber(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public int getFileCount() {
      return 0;
    }

    @Override
    public FIFO getFIFO(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public FIFO getFIFOByAddress(int ref) throws IllegalAddressException {
      return null;
    }

    @Override
    public int getFIFOCount() {
      return 0;
    }
  }

}











































