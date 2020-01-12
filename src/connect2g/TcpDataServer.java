package connect2g;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.Socket;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.RandomAccessFile;
import java.io.File;
import javax.swing.SwingWorker;

public class TcpDataServer  extends Thread{

    public static void main(String[] args) {
        try {

            ServerSocket serverSocket = new ServerSocket(6555);
            System.out.println("waiting...");
            new Thread(new Worker(serverSocket.accept())).start();

        } catch (IOException ex) {

        }
    }

}
class ddoProgress extends SwingWorker<Void, Void> {

    @Override
    protected Void doInBackground() throws Exception {
   Client.progressBar.setValue(Worker.percentage);
        if (Worker.percentage==100) {
            Client.openFolder.setVisible(true);
            
        }
    return null;
    }
    
}
class Worker implements Runnable {

    static int percentage = 0;
    private Socket target_socket;
    private DataInputStream din;
    private DataOutputStream dout;

    //constructor
    public Worker(Socket s) {
        try {
            target_socket = s;
            din = new DataInputStream(target_socket.getInputStream());
            dout = new DataOutputStream(target_socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Overriden run method
    @Override
    public void run() {
        RandomAccessFile rw = null;
        long current_file_pointer = 0;
        boolean loop_break = false;
        Client.progressBar.setValue(0);
        Client.progressBar.setVisible(true);
        while (true) {
            try {
                byte[] initialize = new byte[1];
                din.read(initialize, 0, initialize.length);
                if (initialize[0] == 2) {
                    byte[] cmd_buffer = new byte[3];
                    din.read(cmd_buffer, 0, cmd_buffer.length);
                    byte[] recived_file = ReadStream();
                    switch (Integer.parseInt(new String(cmd_buffer))) {
                        case 124:
                            File destination = new File("/home/" + System.getProperty("user.name") + "/Downloads/zedShare");
                            destination.mkdir();
                            rw = new RandomAccessFile("/home/" + System.getProperty("user.name") + "/Downloads/zedShare/" + new String(recived_file), "rw");
                            dout.write(CreateDataPacket("125".getBytes("UTF8"), String.valueOf(current_file_pointer).getBytes("UTF8")));
                            dout.flush();

                            break;
                        case 126:
                            rw.seek(current_file_pointer);
                            rw.write(recived_file);
                            current_file_pointer = rw.getFilePointer();
                            System.out.println("Download percentage: " + (int) (((float) current_file_pointer / rw.length()) * 100) + "%");
                            percentage = (int) (((float) current_file_pointer / rw.length()) * 100);
                            ddoProgress object = new ddoProgress();
                            object.execute();
                            dout.write(CreateDataPacket("125".getBytes("UTF8"), String.valueOf(current_file_pointer).getBytes("UTF8")));
                            dout.flush();
                            break;
                        case 127:
                            if ("Closed".equals(new String(recived_file))) {
                                loop_break = true;
                            }
                            break;
                    }
                }
                if (loop_break == true) {
                    target_socket.close();
                    Client.ready.setText("Recieved");
                }
            } catch (IOException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private byte[] ReadStream() {
        byte[] databuff = null;
        try {

            //tryblock
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
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return databuff;
    }

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
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packet;

    }
}
