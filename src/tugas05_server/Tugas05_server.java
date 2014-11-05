/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugas05_server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author user
 */
public class Tugas05_server {

    public final static int PORT = 6060;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket connection = server.accept();
                    Thread task = new ClientHandling(connection);
                    task.start();
                }
                catch (IOException ex) {}
            }
        } catch (IOException ex) {
            System. err. println("Couldn't start server" );
        }
    }
    
    private static class ClientHandling extends Thread {
        private final Socket connection;
        
        ClientHandling(Socket connection) {
            this.connection = connection;
        }
        
        @Override
        public void run() {
            try {
                File myFile = new File("s.pdf");
//                while (true) {
                    byte[] mybytearray = new byte[(int) myFile.length()];

                    // file attribut
                    Path file = Paths.get("s.pdf");
                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

                    System.out.println("creationTime: " + attr.creationTime());
                    System.out.println("lastAccessTime: " + attr.lastAccessTime());
                    System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

                    System.out.println("isDirectory: " + attr.isDirectory());
                    System.out.println("isOther: " + attr.isOther());
                    System.out.println("isRegularFile: " + attr.isRegularFile());
                    System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
                    System.out.println("size: " + attr.size());

                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                    bis.read(mybytearray, 0, mybytearray.length);
                    OutputStream os = connection.getOutputStream();
                    os.write(mybytearray, 0, mybytearray.length);
                    os.flush();
                    connection.close();
//                }
            }
            catch (IOException ex) {
                System. err. println(ex);
            }
            finally {
                try {
                    connection. close();
                }
                catch (IOException e) {
                    // ignore;
                }
            }
        }
    }
    
}