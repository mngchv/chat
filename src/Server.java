import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Server
{

    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket serverSocket;


    public Server() {
        try {
            serverSocket = new ServerSocket(Const.Port);

            while (true) {
                Socket socket = serverSocket.accept();

                Connection connection = new Connection(socket);
                connections.add(connection);

                connection.start();

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeAll();
        }
    }


    private void closeAll()
    {
        try
        {
            serverSocket.close();

            synchronized(connections)
            {
                for (Connection connection : connections) {
                    connection.close();
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Threads are not closed! ");
        }
    }


    private class Connection extends Thread
    {
        private BufferedReader bufferedReader;
        private PrintWriter printWriter;
        private final Socket socket;

        public Connection(Socket socket) {
            this.socket = socket;

            try {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                printWriter = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }


        @Override
        public void run() {
            try {
                String name = bufferedReader.readLine();
                synchronized(connections) {
                    for (Connection connection : connections) {
                        connection.printWriter.println(name + " " + (char) 27 + "[35mcames now " + (char) 27 + "[0m");
                    }
                }

                String str;
                while (true) {
                    str = bufferedReader.readLine();
                    if(str.equals("exit")) break;

                    synchronized(connections) {
                        for (Connection connection : connections) {
                            connection.printWriter.println(name + ": " + str);
                        }
                    }
                }

                synchronized(connections) {
                    for (Connection connection : connections) {
                        connection.printWriter.println(name + " " + (char) 27 + "[35mhas left " + (char) 27 + "[0m");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }


        public void close() {
            try {
                bufferedReader.close();
                printWriter.close();
                socket.close();


                connections.remove(this);
                if (connections.size() == 0) {
                    Server.this.closeAll();
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println("Threads are not closed! ");
            }
        }
    }
}