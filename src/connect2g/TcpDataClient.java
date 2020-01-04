package connect2g;
//<editor-fold defaultstate="collapse" desc="imports">

import java.io.IOException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFileChooser;
import java.io.File;
import javax.swing.*;

import java.io.RandomAccessFile;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
//</editor-fold>
//<editor-fold defaultstate="collapse" desc="TcpDataClient">

public class TcpDataClient extends Thread {

    static Timer timer;
    static int value;
    static File target_file;
    static String name;


//<editor-fold defaultstate="collapsed" desc="doProgress">
    static class doProgress extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            Client.progressBar.setValue(value);

            return null;
        }

    }
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="main">

    public static void main(String[] args) {
        worker object = new worker();
        new Thread(object).start();
    }
    //</editor-fold>
//<editor-fold defaultstate="collapsed" desc="worker">

    static class worker implements Runnable {

        public worker() {

        }

        @Override
        public void run() {
            doTheWork();

        }

    }

//</editor-fold>
   //<editor-fold defaultstate="collapsed" desc="doTheWork">

    private static void doTheWork() {
        try {
            TcpDataClient obj = new TcpDataClient();
            Socket obj_client = new Socket(InetAddress.getByName(Client.recieverAddress), 6555);
            DataInputStream din = new DataInputStream(obj_client.getInputStream());
            DataOutputStream dout = new DataOutputStream(obj_client.getOutputStream());
            JFileChooser jfc = new JFileChooser();
            int dialog_value = jfc.showOpenDialog(null);
            if (dialog_value == JFileChooser.APPROVE_OPTION) {
                 target_file = jfc.getSelectedFile();
                name =  jfc.getName(target_file);
                
                dout.write(obj.CreateDataPacket("124".getBytes("UTF8"), target_file.getName().getBytes("UTF8")));
                dout.flush();
                RandomAccessFile rw = new RandomAccessFile(target_file, "r");
                long current_file_pointer = 0;
                boolean loop_break = false;
                Client.progressBar.setVisible(true);
                Client.progressBar.setValue(0);
                //starting ticking...the clock.......

                while (true) {

                    if (din.read() == 2) {
                        byte[] cmd_buff = new byte[3];
                        din.read(cmd_buff, 0, cmd_buff.length);
                        byte[] recieved_buff = obj.ReadStream(din);
                        switch (Integer.parseInt(new String(cmd_buff))) {
                            case 125:
                                current_file_pointer = Long.valueOf(new String(recieved_buff));
                                int buff_length = (int) (rw.length() - current_file_pointer < 2000 ? rw.length() - current_file_pointer : 2000);
                                byte[] temp_buff = new byte[buff_length];
                                if (current_file_pointer != rw.length()) {
                                    rw.seek(current_file_pointer);
                                    rw.read(temp_buff, 0, temp_buff.length);
                                    value = (int) (1 + ((float) current_file_pointer / rw.length()) * 100);

                                    new doProgress().execute();

                                    dout.write(obj.CreateDataPacket("126".getBytes("UTF8"), temp_buff));
                                    dout.flush();
                                    System.out.println("Upload percentage: " + value + "%");
                                } else {
                                    loop_break = true;
                                }

                                break;
                        }
                
                    }
                    if (loop_break == true) {
                        System.out.println("stop server informed    ");
                        Client.ready.setText("File Sent");
                        dout.write(obj.CreateDataPacket("127".getBytes("UTF8"), "Close".getBytes("UTF8")));
                        dout.flush();
                        obj_client.close();
                        System.out.println("client socket closed");
                        break;
                    }

                }
            }

        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(null, "<html><font color=\"red\"><strong> Reciever Adress UnKnown</strong></font></html>", "Error", JOptionPane.ERROR_MESSAGE, null);
        } catch (IOException ex) {
            //   JOptionPane.showMessageDialog(null, "Can't connect to "+Client.recieverAddress);
            JOptionPane.showMessageDialog(null, "<html><font color=\"blue\"><strong>Can't connect to " + Client.recieverAddress + "<br> may be reciever is not ready</strong></font></html>", "Error", JOptionPane.ERROR_MESSAGE, null);

        }

    }
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="CreateDataPacket">

    private byte[] CreateDataPacket(byte[] cmd, byte[] data) {
        byte[] packet = null;
        try {

            byte[] initialize = new byte[1];
            initialize[0] = 2;
            byte[] separator = new byte[1];
            separator[0] = 4;
            byte[] data_length = String.valueOf(data.length).getBytes("UTF8");
            packet = new byte[initialize.length + cmd.length + separator.length + data_length.length + data.length];

            System.arraycopy(initialize, 0, packet, 0, initialize.length);
            System.arraycopy(cmd, 0, packet, initialize.length, cmd.length);
            System.arraycopy(data_length, 0, packet, initialize.length + cmd.length, data_length.length);
            System.arraycopy(separator, 0, packet, initialize.length + cmd.length + data_length.length, separator.length);
            System.arraycopy(data, 0, packet, initialize.length + cmd.length + data_length.length + separator.length, data.length);

        } catch (UnsupportedEncodingException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
        return packet;

    }
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="ReadStream">

    private byte[] ReadStream(DataInputStream din) {
        byte[] databuff = null;
        try {
            int b = 0; 
            String buff_length = "";
            while ((b = din.read()) != 4) {
                buff_length += (char) b;
            }
            int data_length = Integer.parseInt(buff_length);
            databuff = new byte[Integer.parseInt(buff_length)];
            int byte_read = 0;
            int byte_offset = 0;

            while (byte_offset < data_length) {
                byte_read = din.read(databuff, byte_offset, data_length - byte_offset);
                byte_offset += byte_read;
            }
        } catch (IOException ex) {
JOptionPane.showMessageDialog(null, ex);
        }
        return databuff;
    }
    //</editor-fold>
}
//</editor-fold>
