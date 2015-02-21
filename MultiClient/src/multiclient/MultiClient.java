package multiclient;

import java.io.*;
import static java.lang.System.exit;
import java.net.*;

public class MultiClient implements Runnable{
    
    private static Socket clientSocket = null;
    private static PrintStream outputStream = null;
    private static DataInputStream inputStream = null;
    private static BufferedReader inputLine = null;
    private static boolean closed = false;
    int portNumber = 2222;
    String host = "localhost";
    
    public final static int SOCKET_PORT = 13267;      
    public final static String SERVER = "127.0.0.1";  
    public final static String FILE_TO_RECEIVED = "C:\\Users\\Emil\\Desktop\\copy.jpg";  

    public final static int FILE_SIZE = 6022386; 
    
    public void init(){
        try {
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());
        } 
        catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } 
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + host);
        }
    }
    
    public void start(){
        if (clientSocket != null && outputStream != null && inputStream != null) {
            try {
                new Thread(new MultiClient()).start();
                while (!closed) {
                    outputStream.println(inputLine.readLine().trim());
                }
                outputStream.close();
                inputStream.close();
                clientSocket.close();
            } 
            catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }
    
    public static void main(String[] args) {
        MultiClient client = new MultiClient();
        client.init();
        client.start();
              
    }
    
    @Override
    public void run() {
        String responseLine;
        try {
            System.out.println("The client started. Type command(\"list\"/\"send\"/\"..\"/\"quit\")");
            while (true) {
                responseLine = inputStream.readLine();
                if (responseLine.contains("quit")) {
                    exit(1);
                }
                else if(responseLine.contains("list")) {
                    responseLine = inputStream.readLine();
                    int number = Integer.parseInt(responseLine);
                    for (int i = 0; i < number; i++) {
                        responseLine = inputStream.readLine();
                        System.out.println(responseLine);
                    }
                }
                else if (responseLine.contains("send")) {
                     ReceivedFile();
                }
                else{
                    System.out.println(responseLine);
                }
            }
        } 
        catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
    
    public static void ReceivedFile () throws IOException {
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Socket sock = null;
        try {
            sock = new Socket(SERVER, SOCKET_PORT);
            System.out.println("Connecting...");

            // receive file
            byte [] mybytearray  = new byte [FILE_SIZE];
            InputStream is = sock.getInputStream();
            fos = new FileOutputStream(FILE_TO_RECEIVED);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;

            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos.write(mybytearray, 0 , current);
            bos.flush();
            System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)");
        }
        finally {
            if (fos != null) fos.close();
            if (bos != null) bos.close();
            if (sock != null) sock.close();
        }
    }
}