///A Simple Web Server (WebServer.java)

package src.http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class WebServer {
    static final String INIT_DIR = "src/doc/";

    protected void start() {
        ServerSocket s;

        String fileName = null;
        System.out.println("Webserver starting up on port 3000");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(3000);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (; ; ) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                BufferedInputStream in = new BufferedInputStream(remote.getInputStream());
                BufferedInputStream fluxIn = new BufferedInputStream(remote.getInputStream());
                PrintWriter out = new PrintWriter(remote.getOutputStream());
                // get binary output stream to client (for requested data)
                BufferedOutputStream dataOut = new BufferedOutputStream(remote.getOutputStream());
                /*String str = ".";

                int i = 0;
                String requestMethod = null;
                StringTokenizer lineClient;
                String header = "";
                if (str != null && !str.equals("")) {

                }*/
                /*
                int countEmpty = 0;
                int compteur = 0;
                boolean pass = false;
                while (str != null && !str.equals("")) {
                    if(pass)
                        pass = false;
                    str = in.readLine();
                    if(str.equals("") && countEmpty==0) {
                        if(requestMethod.equals("POST")) {
                            str = ".";
                            pass = true;
                        }
                        countEmpty++;
                    }

                    if (i == 0) {
                        // we parse the request with a string tokenizer
                        lineClient = new StringTokenizer(str);
                        requestMethod = lineClient.nextToken().toUpperCase(); // we get the HTTP method of the client
                        // we get file requested
                        fileName = lineClient.nextToken().toLowerCase();
                    }
                        //requete tapée par l'utilisateur
                    i++;
                    if(!pass)
                        header += str + "\n\r";
                    //}


                }*/


                List<String> headers = parseHTTPHeaders(in);

                    System.out.println("Request Header:");
                    System.out.println(headers.get(0));

                    StringTokenizer parse = new StringTokenizer(headers.get(0));
                    String methodRequested = parse.nextToken().toUpperCase();
                    String resourceRequested = parse.nextToken().toLowerCase().substring(1);


                if (!headers.isEmpty()) {
                    System.out.println("on rentre dans le switch");
                    switch (methodRequested) {
                        case "GET":
                            executeGETmethod(resourceRequested, out, dataOut);
                            break;
                        case "HEAD":
                            executeHEADmethod(resourceRequested, out, dataOut);
                            break;
                        case "POST":
                            /*String[] fileNameCopy = header.split("filename=\"");
                            String suiteContent = fileNameCopy[1];
                            String[] followingFile = suiteContent.split("\"");*/
                            //executePOSTmethode(fileName, out, dataOut, fluxIn);
                            executePOSTmethod(in, out, dataOut, resourceRequested);
                            break;
                        case "PUT" :
                            executePUTmethod(in, out, dataOut, resourceRequested);
                            break;
                        case "DELETE" :
                            executeDELETEmethod(in, out, dataOut, resourceRequested);
                        default:
                            //erreur
                            break;
                    }
                }
                out.flush();
                remote.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }


    public String getTypeFromExtension(String extension) {
        String type = "";
        switch (extension) {
            case "jpg":
            case "jpeg":
            case "gif":
            case "png":
            case "bmp":
                type = "image";
                break;
            case "mp4":
                type = "video";
                break;
            case "txt":
            case "docx":
            case "doc":
            case "pdf":
            case "md":
            case "html":
                type = "text";
                break;
            default:
                type = "unknown";
                break;
        }
        return type;
    }


    public void executeGETmethod(String fileName, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        int fileLength = 0;
        File file = new File("");
        buildHeader(fileName, out, dataOut, file, fileLength, true);

    }


    public void executeHEADmethod(String fileName, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        int fileLength = 0;
        File file = new File("");
        buildHeader(fileName, out, dataOut, file, fileLength, false);

    }


    public void buildHeader(String fileName, PrintWriter out, BufferedOutputStream dataOut, File file, int fileLength, boolean get) throws IOException {
        if (fileName.equals("/") || fileName.equals("")) {
            fileName = INIT_DIR + "/index.html";
        } else {
            fileName = INIT_DIR + fileName;
        }
        file = new File(fileName);
        String codeStatus = "OK 200", extension = "";
        if (!file.exists()) {
            codeStatus = "404 not found";
            fileName = INIT_DIR + "error404.html";
            file = new File(fileName);
        }
        fileLength = (int) file.length();
        extension = null;
        int extensionPos = fileName.lastIndexOf('.');
        if (extensionPos > 0) {
            extension = fileName.substring(extensionPos + 1);
        } else { // pas sûr de vraiment pouvoir passer là
            codeStatus = "404 not found";
        }

        String content = getTypeFromExtension(extension) + "/" + extension;
        printHeader(content, codeStatus, fileLength, out, fileName);


        if (get) {
            byte[] fileData = readData(file);
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
        }


    }

    public byte[] readData(File file) throws IOException {
        int lengthFile = (int) file.length();
        FileInputStream dataStream = null;
        byte[] dataArray = new byte[lengthFile];
        try {
            dataStream = new FileInputStream(file);
            dataStream.read(dataArray);
        } finally {
            if (dataStream != null) dataStream.close();
        }
        System.out.println(dataArray);
        return dataArray;
    }


    public void printHeader(String content, String status, int length, PrintWriter out, String fileName) {
        out.println("HTTP/1.1 " + status);
        out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + length);
        out.println("Content-name: " + fileName);
        out.println();
        out.flush();
    }

    /*public void executePOSTmethod(String fileNameToWrite, PrintWriter out, BufferedOutputStream dataOut, BufferedReader in, String fileToCopy) throws IOException {
        try {
            File file = new File("src/doc/" + fileNameToWrite.substring(1));
            String codeStatus = "";
            boolean alreadyHere = file.exists();
            if(!alreadyHere) {
                codeStatus = "201 Created";
            } else {
                codeStatus = "200 OK";
            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, alreadyHere));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("src/doc/" + fileToCopy));
            int j;
            // read byte by byte until end of stream
            while ((j = bis.read()) > 0) {
                bos.write(j);
            } bis.close();
            bos.close();

            int fileLength = (int) file.length();
            String extension = null;
            int extensionPos = fileNameToWrite.lastIndexOf('.');
            if (extensionPos > 0) {
                extension = fileNameToWrite.substring(extensionPos + 1);
            }
            String content = getTypeFromExtension(extension) + "/" + extension;
            printHeader(content, codeStatus, fileLength, out, INIT_DIR + fileNameToWrite);

            byte[] fileData = readData(file);
            dataOut.write(fileData, 0, (int)file.length());
            dataOut.flush();

            dataOut.close();
            dataOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/

    public void executePOSTmethod(BufferedInputStream in, PrintWriter out, BufferedOutputStream dataOut, String fileName) {
        System.out.println("POST " + fileName);
        try {
            String codeStatus = "";
            File file = new File(fileName);
            boolean alreadyHere = file.exists();

            List<Byte> fileData = new ArrayList<>();
            while (in.available() > 0) {
                fileData.add((byte) in.read());
            }

            byte[] fileDataArray = new byte[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                fileDataArray[i] = fileData.get(i);
            }
            writeFileData(file, fileDataArray, alreadyHere);

            if (alreadyHere) {
                codeStatus = "200 OK";
            } else {
                codeStatus = "201 Created";
            }
            // creation du header
            out.println("HTTP/1.1 " + codeStatus);
            out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
            out.println("Date: " + new Date());
            out.println();
            out.flush();
            // on affiche le fichier dans lequel on a ecrit dans le body
            byte[] fileDataToPrint = readData(file);
            dataOut.write(fileDataToPrint, 0, (int)file.length());
            dataOut.flush();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                String codeStatus = "500 Internal Server Error";
                out.println("HTTP/1.1 " + codeStatus);
                out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
                out.println("Date: " + new Date());
                out.println();
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }




    public void executePUTmethod(BufferedInputStream in, PrintWriter out, BufferedOutputStream dataOut, String fileName) {
        try {
            String codeStatus = "";
            File file = new File(fileName);
            boolean alreadyHere = file.exists();

            FileWriter ecrasement = new FileWriter(file);
            ecrasement.close();


            List<Byte> fileData = new ArrayList<>();
            while (in.available() > 0) {
                fileData.add((byte) in.read());
            }

            byte[] fileDataArray = new byte[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                fileDataArray[i] = fileData.get(i);
            }
            writeFileData(file, fileDataArray, alreadyHere);

            if (alreadyHere) {
                codeStatus = "204 No content";
            } else {
                codeStatus = "201 Created";
            }
            // creation du header
            out.println("HTTP/1.1 " + codeStatus);
            out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
            out.println("Date: " + new Date());

            out.println();
            out.flush();
            // on affiche le fichier dans lequel on a ecrit dans le body
            byte[] fileDataToPrint = readData(file);
            dataOut.write(fileDataToPrint, 0, (int)file.length());
            dataOut.flush();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                String codeStatus = "500 Internal Server Error";
                out.println("HTTP/1.1 " + codeStatus);
                out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
                out.println("Date: " + new Date());
                out.println();
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }


    public void executeDELETEmethod(BufferedInputStream in, PrintWriter out, BufferedOutputStream dataOut, String fileName) {
        try {
            String codeStatus = "";
            File file = new File(fileName);
            boolean alreadyHere = file.exists();
            boolean success = false;
            if(alreadyHere && file.isFile())
                success = file.delete();




            if (!alreadyHere) {
                codeStatus = "404 Not found";
            } else if(success){
                codeStatus = "204 No Content";
            } else {
                codeStatus = "403 Forbidden";
            }

            // creation du header
            out.println("HTTP/1.1 " + codeStatus);
            out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
            out.println("Date: " + new Date());

            out.println();
            out.flush();


        } catch (Exception e) {
            e.printStackTrace();
            try {
                String codeStatus = "500 Internal Server Error";
                out.println("HTTP/1.1 " + codeStatus);
                out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
                out.println("Date: " + new Date());
                out.println();
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }






    private void writeFileData(File file, byte[] fileData, boolean exists) throws IOException {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file, exists);
            fileOut.write(fileData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOut != null) fileOut.close();
        }
    }


    public List<String> parseHTTPHeaders(BufferedInputStream in) throws IOException {
        int charRead = in.read();
        List<String> headers = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        while (charRead != -1) {
            stringBuilder.append((char) charRead);
            if (stringBuilder.toString().endsWith("\r\n")) {
                if (stringBuilder.toString().equals("\r\n")) {
                    break;
                } else {
                    headers.add(stringBuilder.substring(0, stringBuilder.toString().lastIndexOf("\r\n")));
                    stringBuilder = new StringBuilder();
                }
            }
            charRead = in.read();
        }
        System.out.println(headers);
        return headers;
    }


    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
    }
}
