import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

class FunctionIntegral {
    public double f(double x) {
        double F = 1 / x;
        return F;
    }
}

class MyThread extends Thread {
    double limUp;
    double limDown;
    double limStep;
    int num;
    DatagramSocket socket;
    InetAddress address;

    MyThread(String name, double _limUp, double _limDown, double _limStep, int _n, DatagramSocket _socket, InetAddress _address) {
        super(name);
        limUp = _limUp;
        limDown = _limDown;
        limStep = _limStep;
        num = _n;
        socket = _socket;
        address = _address;
    }

    public void run() {
        double sum = 0;
        while (limDown + limStep < limUp) {
            sum += ((Math.exp(-limDown) + Math.exp(-(limDown + limStep))) / 2) * limStep;
            limDown += limStep;
        }
        sum += ((Math.exp(-limDown) + Math.exp(-limUp)) / 2) * limStep;

        String message = sum + " " + num;
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 26);
        try {
            socket.send(packet);
            System.out.print("Ok!\n");
        } catch (IOException ex) {
            Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

public class Client {
    public static void main(String[] args) throws SocketException, IOException {
        DatagramSocket socket = new DatagramSocket(17);
        DatagramSocket socketSend = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        System.out.print("waiting\n");
        while (true) {
            byte[] buffer = new byte[256];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);
            if (request.getLength() != 0) {
                String Message = new String(request.getData(), 0, request.getLength());
                String strTop = "",
                        strLower = "",
                        strStep = "",
                        strNum = "";

                int size = Message.length();

                int j = 0;
                while (Message.charAt(j) != ' ') {
                    strTop += Message.charAt(j);
                    j++;
                }
                j++;

                while (Message.charAt(j) != ' ') {
                    strLower += Message.charAt(j);
                    j++;
                }
                j++;

                while (Message.charAt(j) != ' ') {
                    strStep += Message.charAt(j);
                    j++;
                }
                j++;

                while (j != size) {
                    strNum += Message.charAt(j);
                    j++;
                }

                MyThread thread = new MyThread("thread", Double.parseDouble(strTop), Double.parseDouble(strLower), Double.parseDouble(strStep), Integer.parseInt(strNum), socketSend, address);
                thread.start();
            }
        }
    }
}