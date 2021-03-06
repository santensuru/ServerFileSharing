/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugas05_server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author
 * 1. Djuned Fernando Djusdek   5112100071
 * 2. M. Arief Ridwan           5112100097
 * 3. I Gede Arya Putra Perdana 5112100151
 * 
 * https://github.com/santensuru/ServerFileSharing
 * email: djuned.ong@gmail.com
 * 
 * version 0.0.2c beta
 */
public class Tugas05_server {

    private final static int PORT = 6060;
    private final static ArrayList<Pair<Socket, String>> allConnection = new ArrayList<>();
    private final static byte[] mybytearray = new byte[16384]; //4096
    
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
                    allConnection.add(new Pair<>(connection, ""));
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
        private final BufferedOutputStream bos;
        private String kirim = "";
        private String terima = "";
        private int buf;
        private String command = "";
        private final ArrayList<Pair<Socket, String>> destination = new ArrayList<>();
        private boolean who = false;
        private boolean send = false;
//        private String[] activeDest;
        
        ClientHandling(Socket connection) throws IOException {
            this.connection = connection;
            is = connection.getInputStream();
            os = connection.getOutputStream();
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
                    } while(!terima.contains("\r\n"));
                    if (terima.contains("who")) {
                        if (allConnection.size() > 1) {
                            kirim = "2 oke,\r\n";
                            allConnection.stream().map((a) -> (Socket) a.getLeft()).filter((s) -> (!connection.equals(s))).forEach((s) -> {
                                kirim = kirim.concat("\t" + s.getRemoteSocketAddress().toString() + "\r\n");
                            });
                            who = true;
                        }
                        else {
                            kirim = "3 only you :p\r\n";
                        }
                    }
                    else if (terima.contains("send")) {
                        if (who == true) {
                            kirim = "1 accepted, to?\r\n";
                            send = true;
                        }
                        else {
                            kirim = "3 no one online\r\n";
                        }
                    }
                    else if (terima.contains("/")) {
                        if (who == true && send == true) {
                            allConnection.stream().forEach((a) -> {
                                Socket s = (Socket) a.getLeft();
                                String str = (String) a.getRight();
                                if (terima.contains(s.getRemoteSocketAddress().toString()) && str.equals("") && !s.equals(connection)) {
                                    Pair<Socket, String> pair = new Pair<>(s, "false");
                                    destination.add(pair);
                                }
                            });
                            
                            if (destination.isEmpty()) {
                                kirim = "3 destination not found or used. (look ip:port)\r\n";
                                bos.write(kirim.getBytes());
                                bos.flush();
                                continue;
                            }
                            
                            int counter = 0;

//                            kirim = "";
                            for(Pair p: destination) {
                                Socket s = (Socket) p.getLeft();
                                String ckirim;
                                OutputStream cos = s.getOutputStream();
                                BufferedOutputStream cbos = new BufferedOutputStream(cos);
                                ckirim = "request send from " + this.connection.getRemoteSocketAddress().toString() + ", accept?\r\n";
                                cbos.write(ckirim.getBytes());
                                cbos.flush();
                                int  l = allConnection.indexOf(new Pair<>(s, ""));
                                while (true) {
                                    System.out.println("-_-");
//                                    System.out.println(allConnection.get(l).getRight());
                                    String str_l = (String) allConnection.get(l).getRight();
                                    if (str_l.contains("true")) {
                                        p.setRight("true");
                                        kirim = "2 oke, " + s.getRemoteSocketAddress() + " ready\r\n";
                                        bos.write(kirim.getBytes());
                                        bos.flush();
                                        break;
                                    }
                                    else if (str_l.contains("false")){
                                        kirim = "3 !, " + s.getRemoteSocketAddress() + " reject\r\n";
                                        allConnection.get(l).setRight("");
                                        bos.write(kirim.getBytes());
                                        bos.flush();
                                        counter++;
                                        break;
                                    }
                                }
                            }
                            
                            if (destination.size() == counter) {
                                destination.clear();
                                who = false;
                                send = false;
                                kirim = "3 all destination reject.\r\n";
                                bos.write(kirim.getBytes());
                                bos.flush();
                                continue;
                            }

                            while (true) {
                                terima = "";
                                do {
                                    buf = is.read();
                                    terima = terima.concat(String.valueOf((char) buf));
//                                    System.out.println(terima);
                                } while(!terima.contains("\r\n"));

                                if (terima.contains("take")) {
                                    command = terima.replace("take ", "file ");
                                    break;
                                }
                                else {
                                    kirim = "3 command not allow. (take)\r\n";
                                    bos.write(kirim.getBytes());
                                    bos.flush();
                                }
                            }
                            
                            for(Pair p: destination) {
                                String str = (String) p.getRight();
                                Socket s = (Socket) p.getLeft();
                                OutputStream cos = s.getOutputStream();
                                BufferedOutputStream cbos = new BufferedOutputStream(cos);

                                if (str.matches("true") == true) {
                                    cbos.write(command.getBytes());
                                    cbos.flush();
                                }
                            }
                            
//                            while (true) {
                                terima = "";
                                do {
                                    buf = is.read();
                                    terima = terima.concat(String.valueOf((char) buf));
                                } while(!terima.contains("\r\n"));
//                                break;
//                            }

                            for(Pair p: destination) {
                                String str = (String) p.getRight();
                                Socket s = (Socket) p.getLeft();
                                OutputStream cos = s.getOutputStream();
                                BufferedOutputStream cbos = new BufferedOutputStream(cos);

                                if (str.matches("true") == true) {
                                    cbos.write(terima.getBytes());
                                    cbos.flush();
                                }
                            }
                            terima = terima.replace("\r\n", "");

//                            String temp = "";
//                            //<listen mode>
//                            do {
//                                buf = is.read();
//                                temp = temp.concat(String.valueOf((char) buf));
//                                for(Pair p: destination) {
//                                    String str = (String) p.getRight();
//                                    Socket s = (Socket) p.getLeft();
//                                    OutputStream cos = s.getOutputStream();
//                                    BufferedOutputStream cbos = new BufferedOutputStream(cos);
//
//                                    if (str.matches("true") == true) {
//                                        cbos.write((char) buf);
//                                        cbos.flush();
//                                    }
//                                }
//                            } while(!temp.contains("\r\n"));
                            
                            //<file>
                            int bytesRead;
                            int flag = 0;
                            do {
                                bytesRead = is.read(mybytearray, 0, 16384);
                                flag += bytesRead;
//                                System.out.println(bytesRead + " " + flag + "/" + terima);
                                for(Pair p: destination) {
                                    String str = (String) p.getRight();
                                    if (str.matches("true") == true) {
                                        Socket s = (Socket) p.getLeft();
                                        OutputStream cos = s.getOutputStream();
                                        BufferedOutputStream cbos = new BufferedOutputStream(cos);
                                        cbos.write(mybytearray, 0, bytesRead);
                                        cbos.flush();
                                    }
                                }
                            } while(!String.valueOf(flag).equals(terima));

//                            kirim = "2 file sent\r\n";
//                            bos.write(kirim.getBytes());
//                            bos.flush();

                            destination.clear();
                            kirim = "2 success :)\r\n";
                            allConnection.stream().forEach((p) -> {
                                p.setRight("");
                            });
                        }
                        who = false;
                        send = false;
                    }
                    else if (terima.contains("accept")) {
                        int l = allConnection.indexOf(new Pair<>(connection, ""));
                        allConnection.get(l).setRight("true");
                        kirim = "2 oke, listen mode\r\n";
                        bos.write(kirim.getBytes());
                        bos.flush();
                        //<listen mode>
                        String str_l = (String) allConnection.get(l).getRight();
                        while (str_l.contains("true")) {
                            str_l = (String) allConnection.get(l).getRight();  
                            System.out.println("_-_");
                        }
                        kirim = "2 success :)\r\n";
                    }
                    else if (terima.contains("reject")) {
                        int l = allConnection.indexOf(new Pair<>(connection, ""));
                        allConnection.get(l).setRight("false");
                        kirim = "2 oke\r\n";
                    }
                    else if (terima.contains("quit")) {
                        kirim = "2 bye ;)\r\n";
                        bos.write(kirim.getBytes());
                        bos.flush();
                        int l;
                        if ((l = allConnection.indexOf(new Pair<>(connection, ""))) < 0) {
                            if ((l = allConnection.indexOf(new Pair<>(connection, "false"))) < 0) {
                                l = allConnection.indexOf(new Pair<>(connection, "true"));
                            }
                        }
                        allConnection.remove(l);
                        break;
                    }
                    else {
                        kirim = "3 command not allow. (who, send, /<ip>:<port>)\r\n";
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
                int l;
                if ((l = allConnection.indexOf(new Pair<>(connection, ""))) < 0) {
                    if ((l = allConnection.indexOf(new Pair<>(connection, "false"))) < 0) {
                        l = allConnection.indexOf(new Pair<>(connection, "true"));
                    }
                }
                allConnection.remove(l);
                System.err.println("Error: " + ex.toString());
            }
            finally {
                try {
                    connection.close();
                }
                catch (IOException e) {
                    System.err.println("Error: " + e.toString());
                    // ignore;
                }
            }
        }
    }
    
}