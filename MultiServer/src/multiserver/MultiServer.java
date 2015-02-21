package multiserver;

import java.io.*;
import static java.lang.System.exit;
import java.net.*;

public class MultiServer {
    
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static final int maxClientsCount = 10;
    private static final clientThread[] threads = new clientThread[maxClientsCount];
    int portNumber = 2222;
    
    public void init (){
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server is start on port: " + portNumber);
        }    
        catch (IOException e) {
            System.out.println(e);
        }
    }
    public void start (){
         while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream output = new PrintStream(clientSocket.getOutputStream());
                    output.println("Server too busy. Try later.");
                    output.close();
                    clientSocket.close();
                }
            } 
            catch (IOException e) {
                System.out.println(e);
            }
        }   
    }
    
    public static void main(String[] args) {
       MultiServer server = new MultiServer();
       server.init();
       server.start();
    }
}

class clientThread extends Thread {
    
    private DataInputStream inputStream = null;
    private PrintStream outputStream = null;
    private Socket clientSocket = null;
    private final clientThread[] threads;
    private final int maxClientsCount;
    public final static int SOCKET_PORT = 13267;
    public static String FILE_TO_SEND = "D:";

    public clientThread(Socket clientSocket, clientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    @Override
    public void run() {
        int maxClientsCount = this.maxClientsCount;
        clientThread[] threads = this.threads;
        String line;
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new PrintStream(clientSocket.getOutputStream());
            File[] listDir = null;
            do {
                line = inputStream.readLine();
                System.out.println(line);
                if (line.contains("quit")) {
                    outputStream.println(line);
                    exit(1);
                }
                else if (line.contains("list")) {
                    listDir = ListDirectory(FILE_TO_SEND);
                    outputStream.println(line);  
                    outputStream.println(listDir.length);  
                    for (File list1 : listDir) {
                        if (list1.isFile()) {
                            outputStream.println("File " + list1.getName());
                        } else if (list1.isDirectory()) {
                            outputStream.println("Directory " + list1.getName());
                        }
                    }       
                }
                else if (line.contains("send")) {
                    outputStream.println(line);
                    SendFile();
                }
                else if (line.contains("..")) {
                    int index = FILE_TO_SEND.lastIndexOf('\\');
                    FILE_TO_SEND = FILE_TO_SEND.substring(0, index);
                    outputStream.println(line);
                    listDir = null;
                }
                else{   
                    boolean isTrue = false;
                    if (listDir != null) {
                        for (File list1 : listDir) {
                            String str = list1.toString();
                            if (str.contains(line)) {
                                isTrue = true;
                                break;
                            }
                        }  
                    }
                    if (isTrue) {
                        FILE_TO_SEND = FILE_TO_SEND + "\\" + line;
                        listDir = null;
                        outputStream.println(line);
                    }
                    else{
                        outputStream.println("Wrong command");
                    }
                }
            } while (true);
        } 
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public static File[] ListDirectory (String directory) {
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();
        return listOfFiles;
    }
    
    public static void SendFile () throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        ServerSocket servsock = null;
        Socket sock = null;
        try {
            servsock = new ServerSocket(SOCKET_PORT);
            boolean isTrue = true;
            while (isTrue) {
                System.out.println("Waiting...");
                try {
                    sock = servsock.accept();
                    System.out.println("Accepted connection : " + sock);
                    // send file
                    File myFile = new File (FILE_TO_SEND);
                    byte [] mybytearray  = new byte [(int)myFile.length()];
                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);
                    bis.read(mybytearray,0,mybytearray.length);
                    os = sock.getOutputStream();
                    System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
                    os.write(mybytearray,0,mybytearray.length);
                    os.flush();
                    System.out.println("Done.");
                    isTrue = false;
                }
                finally {
                  if (bis != null) bis.close();
                  if (os != null) os.close();
                  if (sock!=null) sock.close();
                }
            }
        }
        finally {
          if (servsock != null) servsock.close();
        }
    }
}