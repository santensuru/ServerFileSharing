/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugas05_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 *
 * @author user
 */
public class Tugas05_server {

    public final static int PORT = 6060;
    public final static ArrayList<Pair<Socket, String>> allConnection = new ArrayList<Pair<Socket, String>>();
    private static byte[] mybytearray = new byte[1024];
    private static int bytesRead = 0;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try {
                    int i = 0;
                    Socket connection = server.accept();
                    allConnection.add(new Pair<Socket, String>(connection, ""));
                    Thread task = new ClientHandling(connection);
                    task.start();
                }
                catch (IOException ex) {
                    System.err.println("Error: " + ex.toString());
                }
            }
        }
        catch (IOException ex) {
            System.err.println("Couldn't start server. Error: " + ex.toString());
        }
    }
    
    private static class ClientHandling extends Thread {
        private final Socket connection;
        private final InputStream is;
        private final OutputStream os;
        private BufferedInputStream bis;
        private final BufferedOutputStream bos;
        private String kirim = "";
        private String terima = "";
        private int buf;
        private String command = "";
        private ArrayList<Pair<Socket, String>> destination = new ArrayList<Pair<Socket, String>>();
//        private String[] activeDest;
        
        ClientHandling(Socket connection) throws IOException {
            this.connection = connection;
            is = connection.getInputStream();
            os = connection.getOutputStream();
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(os);
        }
        
        @Override
        public void run() {
            try {
//                for(Socket s: allConnection) {
//                    System.out.println(s.getRemoteSocketAddress());
//                }
                kirim = "2 welcome\r\n";
                bos.write(kirim.getBytes());
                bos.flush();
                while (true) {
                    kirim = "";
                    terima = "";
                    command = "";
                    do {
                        buf = is.read();
                        terima = terima.concat(String.valueOf((char) buf));
                    } while(!terima.contains("\r\n") || buf == 1024);
                    if (terima.contains("who")) {
                        kirim = "2 oke,\r\n";
                        for(Pair a: allConnection) {
                            Socket s = (Socket) a.getLeft();
                            kirim = kirim.concat("\t" + s.getRemoteSocketAddress().toString() + "\r\n");
                        }
                    }
                    else if (terima.contains("send")) {
                        kirim = "1 accepted, to?\r\n";
                    }
                    else if (terima.contains("/")) {
//                        System.out.println(terima);
                        
//                        activeDest = terima.split(";");
//                        for(String a: activeDest) {
//                            System.out.println(a);
                            for(Pair a: allConnection) {
                                Socket s = (Socket) a.getLeft();
//                                System.out.println(a.contains(s.getRemoteSocketAddress().toString()));
                                if (terima.contains(s.getRemoteSocketAddress().toString())) {
                                    Pair<Socket, String> pair = new Pair<Socket, String>(s, "false");
                                    destination.add(pair);
//                                    System.out.println(s.getRemoteSocketAddress());
                                }
                            }
//                        }
                        
                        kirim = "";
                        for(Pair p: destination) {
                            Socket s = (Socket) p.getLeft();
                            String ckirim = "";
//                            String cterima = "";
//                            InputStream cis = s.getInputStream();
                            OutputStream cos = s.getOutputStream();
                            BufferedOutputStream cbos = new BufferedOutputStream(cos);
                            ckirim = "request send from /ip:port" + this.connection.getRemoteSocketAddress().toString() + ", accept?\r\n";
                            cbos.write(ckirim.getBytes());
                            cbos.flush();
                            int  l = allConnection.indexOf(new Pair<Socket, String>(s, ""));
                            while (true) {
                                System.out.println("-_-");
//                                l = allConnection.indexOf(new Pair<Socket, String>(s, ""));
//                                System.out.println(l);
                                System.out.println(allConnection.get(l).getRight());
                                String str_l = (String) allConnection.get(l).getRight();
                                if (str_l.contains("true")) {
                                    p.setRight("true");
                                    kirim = kirim.concat("2 oke, " + s.getRemoteSocketAddress() + " ready\r\n");
                                    bos.write(kirim.getBytes());
                                    bos.flush();
//                                    terima = "";
//                                    do {
//                                        buf = is.read();
//                                        terima = terima.concat(String.valueOf((char) buf));
////                                        System.out.println(terima);
//                                    } while(!terima.contains("\r\n") || buf == 1024);
//                                    if (terima.contains("take")) {
//                                        command = terima.replace("take ", "");
////                                        System.out.println(command);
//                                        String temp = "";
//                                        do {
//                                            buf = is.read();
////                                            bytesRead = bis.read(mybytearray, 0, mybytearray.length);
////                                            cbos.write(mybytearray, 0, bytesRead);
////                                            System.out.println((char) bytesRead);
//                                            temp = temp.concat(String.valueOf((char) buf));
//                                        } while(!temp.contains("\r\n") || bytesRead == 1024);
//                                        cbos.write(temp.getBytes());
//                                        //cbos.write("\r\n".getBytes());
//                                        cbos.flush();
//                                        kirim = "2 file sent\r\n";
//                                        bos.write(kirim.getBytes());
//                                        bos.flush();
//                                    }
//                                    else {
//                                        break;
//                                    }
////                                    <transfer mode>
//                                    status = "";
                                    break;
                                }
                                else if (str_l.contains("false")){
                                    kirim = kirim.concat("3 !, " + s.getRemoteSocketAddress() + " reject\r\n");
                                    allConnection.get(l).setRight("");
//                                    status = "";
                                    bos.write(kirim.getBytes());
                                    bos.flush();
                                    break;
                                }
                            }
                        }
                        
                        while (true) {
                        terima = "";
                            do {
                                buf = is.read();
                                terima = terima.concat(String.valueOf((char) buf));
                                System.out.println(terima);
                            } while(!terima.contains("\r\n") || buf == 1024);

                            if (terima.contains("take")) {
                                command = terima.replace("take ", "");
//                                command = command.concat("\r\n");
                                break;
                            }
                        }
                        
                        for(Pair p: destination) {
                            String str = (String) p.getRight();
                            Socket s = (Socket) p.getLeft();
                            OutputStream cos = s.getOutputStream();
                            BufferedOutputStream cbos = new BufferedOutputStream(cos);

                            if (str == "true") {
                                cbos.write(command.getBytes());
                                cbos.flush();
                            }
                        }
                        
                        String temp = "";
                        do {
                            buf = is.read();
                            temp = temp.concat(String.valueOf((char) buf));
                            for(Pair p: destination) {
                                String str = (String) p.getRight();
                                Socket s = (Socket) p.getLeft();
                                OutputStream cos = s.getOutputStream();
                                BufferedOutputStream cbos = new BufferedOutputStream(cos);

                                if (str == "true") {
                                    cbos.write((char) buf);
                                    cbos.flush();
                                }
                                
                            }
                        } while(!temp.contains("\r\n") || buf == 1024);
                        
                        kirim = "2 file sent\r\n";
                        bos.write(kirim.getBytes());
                        bos.flush();
                        
                        destination.clear();
                        kirim = "2 success :)\r\n";
                        for(Pair p: allConnection) {
                            p.setRight("");
                        }
                    }
                    else if (terima.contains("accept")) {
                        int l = allConnection.indexOf(new Pair<Socket, String>(connection, ""));
                        allConnection.get(l).setRight("true");
                        kirim = "2 oke, listen mode\r\n";
                        bos.write(kirim.getBytes());
                        bos.flush();
//                        <listen mode>
                        String str_l = (String) allConnection.get(l).getRight();
                        while (str_l.contains("true")) {
                            str_l = (String) allConnection.get(l).getRight();  
//                            do {
//                                bos.write(temp.getBytes());
//                            } while(bytesRead == 1024);
//                            bos.flush();
                            System.out.println("_-_");
                        }
                        kirim = "2 success :)\r\n";
                    }
                    else if (terima.contains("reject")) {
                        int l = allConnection.indexOf(new Pair<Socket, String>(connection, ""));
                        allConnection.get(l).setRight("false");
                        kirim = "2 Oke\r\n";
                    }
                    bos.write(kirim.getBytes());
                    bos.flush();
                }
//                File myFile = new File("s.pdf");
//                while (true) {
//                    byte[] mybytearray = new byte[(int) myFile.length()];
//
//                    // file attribut
//                    Path file = Paths.get("s.pdf");
//                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
//
//                    System.out.println("creationTime: " + attr.creationTime());
//                    System.out.println("lastAccessTime: " + attr.lastAccessTime());
//                    System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
//
//                    System.out.println("isDirectory: " + attr.isDirectory());
//                    System.out.println("isOther: " + attr.isOther());
//                    System.out.println("isRegularFile: " + attr.isRegularFile());
//                    System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
//                    System.out.println("size: " + attr.size());
//
//                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
//                    bis.read(mybytearray, 0, mybytearray.length);
//                    OutputStream os = connection.getOutputStream();
//                    os.write(mybytearray, 0, mybytearray.length);
//                    os.flush();
//                    connection.close();
//                }
            }
            catch (IOException ex) {
                System.err.println("Error: " + ex.toString());
            }
            finally {
                try {
                    connection. close();
                }
                catch (IOException e) {
                    System.err.println("Error: " + e.toString());
                    // ignore;
                }
            }
        }
    }
    
}