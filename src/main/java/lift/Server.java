package lift;

import main.java.lift.events.MessageListener;
import main.java.lift.net.MessageReceiver;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.Set;
import lift.events.SocketListener;
import lift.net.ConnectionReceiver;
import java.util.concurrent.*;

/**
 * Class server provides two-way communication between clients (elevator users) and elevator
 */

public class Server implements SocketListener, MessageListener, Closeable, PropertyChangeListener {

    private ConnectionReceiver receiver;

    private MessageReceiver messageReceiver;

    private Set<Socket> clients;

    public Lift lift;

    private Server() throws IOException {
        receiver = new ConnectionReceiver(8080);
        receiver.addListener(this);
        clients = new CopyOnWriteArraySet<>();
        messageReceiver = new MessageReceiver(clients);
        messageReceiver.addListener(this);
        lift = new Lift();
        lift.addListener(this);
    }

    private void send(Socket socket, String message) {
        byte[] data = message.getBytes();
        try {
            OutputStream stream = socket.getOutputStream();
            stream.write(data);
            stream.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void onMessangeReceived(Socket sender, String message) {
        Integer floor = Integer.parseInt(message);
        if (floor >= lift.getNumberOfFirstFoor() && floor <= lift.getNumberOfFoors())
        {
            System.out.println("To " + floor + " floor");

            lift.call(floor);

            for (Socket client : clients) {
                if (client.equals(sender)) {
                    send(client, "Arrive!");
                }
            }
            System.out.println("Arrive!");
        }
        else {
            System.err.println("Error in message delivery");
        }
    }

    @Override
    public void onSocketConnected(Socket socket) {
        clients.add(socket);
    }

    public void start() {
        Thread receiverThread = new Thread(receiver);
        receiverThread.setDaemon(true);
        receiverThread.start();

        receiverThread = new Thread(messageReceiver);
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    @Override
    public void close() throws IOException {
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }

        if (messageReceiver != null) {
            messageReceiver.close();
            messageReceiver = null;
        }

        if (clients != null) {
            for (Socket client : clients) {
                client.close();
            }
            clients.clear();
            clients = null;
        }
    }

    public static void main(String[] args) {
        try (Reader consoleReader = new InputStreamReader(System.in);
             BufferedReader in = new BufferedReader(consoleReader);
             Server server = new Server()) {

            server.start();

            System.out.println("Server start on port 8080! to exit press Enter >> ");
            in.readLine();

        } catch (IOException ex) {
            System.err.println("Server close unexpected!");
            ex.printStackTrace(System.err);

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println(evt.getNewValue());
        for (Socket client : clients) {
                send(client, evt.getNewValue().toString() + " ");
        }

    }
}