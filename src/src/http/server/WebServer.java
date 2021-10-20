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
                // attente de connexion client
                Socket socketClient = s.accept();
                System.out.println("Connection, sending data.");
                // flux d'entrée du serveur
                BufferedInputStream in = new BufferedInputStream(socketClient.getInputStream());
                // Permet d'écrire dans le header
                PrintWriter out = new PrintWriter(socketClient.getOutputStream());
                // le buffer dans lequel on écrit le body qu'on affiche au client
                BufferedOutputStream dataOut = new BufferedOutputStream(socketClient.getOutputStream());

                List<String> header = getHeaderFromClient(in);

                // Affichage du header de la requête du client
                System.out.println("Request Header:");
                System.out.println(header.get(0));

                // Récupération de la méthode et de la ressource
                StringTokenizer parse = new StringTokenizer(header.get(0));
                String methodRequested = parse.nextToken().toUpperCase();
                String resourceRequested = parse.nextToken().toLowerCase().substring(1);


                if (!header.isEmpty()) {
                    System.out.println("on rentre dans le switch");
                    switch (methodRequested) {
                        case "GET":
                            executeGETmethod(resourceRequested, dataOut, out);
                            break;
                        case "HEAD":
                            executeHEADmethod(resourceRequested, dataOut, out);
                            break;
                        case "POST":
                            executePOSTmethod(in, dataOut, resourceRequested, out);
                            break;
                        case "PUT":
                            executePUTmethod(in, dataOut, resourceRequested, out);
                            break;
                        case "DELETE":
                            executeDELETEmethod(in, dataOut, resourceRequested, out);
                        default:
                            //erreur
                            break;
                    }
                }
                dataOut.flush();
                socketClient.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }


    // donne le type du fichier en fonction de son extension
    public String getTypeFromExtension(String extension) {
        String type = "";
        switch (extension) {
            case "jpg":
            case "jpeg":
                type = "image/jpeg";
                break;
            case "gif":
                type = "image/gif";
                break;
            case "png":
                type = "image/png";
                break;
            case "mp4":
                type = "video/mp4";
                break;
            case "docx":
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                break;
            case "pdf":
                type = "application/pdf";
                break;
            case "md":
                type = "text/markdown";
                break;
            case "html":
                type = "text/html";
                break;
            default:
                type = "text/plain";
                break;
        }
        return type;
    }

    // execute la requete selon la methode get
    public void executeGETmethod(String fileName, BufferedOutputStream dataOut, PrintWriter out) throws IOException {
        // on récupère le fichier à afficher, que ce soit l'index, un html d'erreur ou le fichier spéicifié
        // on construit également le header
        File file = buildHeaderGetandPost(fileName, out);
        byte[] fileData = readData(file);
        dataOut.write(fileData, 0, (int) file.length());
        dataOut.flush();

    }

    // execute la requete selon la methode head
    public void executeHEADmethod(String fileName, BufferedOutputStream dataOut, PrintWriter out) throws IOException {

        buildHeaderGetandPost(fileName, out);
    }

    // Construction du header pour les méthodes get et head. Renvoie le fichier à afficher dans le cas de la méthode get
    public File buildHeaderGetandPost(String fileName, PrintWriter out) throws IOException {
        // si pas de fichier spécifié, on affiche index.html
        if (fileName.equals("/") || fileName.equals("")) {
            fileName = INIT_DIR + "index.html";
        } else {
            fileName = INIT_DIR + fileName;
        }
        File file = new File(fileName);
        String codeStatus = "OK 200", extension = "";
        // on vérifie que le fichier existe, sinon message d'erreur
        if (!file.exists()) {
            codeStatus = "404 not found";
            fileName = INIT_DIR + "error404.html";
            file = new File(fileName);
        }
        int fileLength = (int) file.length();
        extension = null;
        int extensionPos = fileName.lastIndexOf('.');
        if (extensionPos > 0) {
            extension = fileName.substring(extensionPos + 1);
        } else { // pas sûr de vraiment pouvoir passer là
            codeStatus = "404 not found";
        }

        String content = getTypeFromExtension(extension);
        // on affiche le headerr avec les infos récolotées
        printHeader(content, codeStatus, fileLength, out, fileName);

        return file;
    }

    // on lit le fichier en paramètre et on le retourne sous forme de tableau de byte
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
        return dataArray;
    }

    // affiche le header dans le print writer out
    public void printHeader(String content, String status, int length, PrintWriter out, String fileName) throws IOException {
        String header = "HTTP/1.1 " + status + "\r\n";
        header += "Server: Java HTTP Server from Capucine and Arthur : 1.0" + "\r\n";
        header += "Date: " + new Date() + "\r\n";
        if (!content.equals(""))
            header += "Content-Type: " + content + "\r\n";
        if (length != -1)
            header += "Content-length: " + length + "\r\n";
        if (!fileName.equals(""))
            header += "Content-name: " + fileName + "\r\n";
        out.println(header);
        out.flush();
    }

    // Execution de la méthode post
    public void executePOSTmethod(BufferedInputStream in, BufferedOutputStream dataOut, String fileName, PrintWriter out) throws IOException {
        // création du header et renvoie du fichier modifié/créé pour l'afficher. Le booléen indique si
        // la méthode appelante est put ou post, put nécessitant l'écrasement du fichier
        File file = buildHeaderPutAndPost(in, fileName, out, false);
        byte[] fileDataToPrint = readData(file);
        dataOut.write(fileDataToPrint, 0, (int) file.length());
        dataOut.flush();
    }

    // Execution de la methode put
    public void executePUTmethod(BufferedInputStream in, BufferedOutputStream dataOut, String fileName, PrintWriter out) {
        // création du header et écrasement du fichier
        buildHeaderPutAndPost(in, fileName, out, true);
    }

    // execution de la méthode delete
    public void executeDELETEmethod(BufferedInputStream in, BufferedOutputStream dataOut, String fileName, PrintWriter out) {
        try {
            String codeStatus = "";
            File file = new File(fileName);
            boolean alreadyHere = file.exists();
            // succes de la suppresion fu fichier
            boolean success = false;
            // affiche une page d'erreur si le fichier n'a pas été trouvé ou si le fichier est protégé
            String print = "";
            // la ressource était présente et la ressource est un fichier
            if (alreadyHere && file.isFile())
                success = file.delete();
            // si le fichier n'était pas présent 
            if (!alreadyHere) {
                codeStatus = "404 Not found";
                print = "404";
            } else if (success) {
                codeStatus = "204 No Content";
            } else {
                // si le fichier existe mais ne peut pas être supprimé
                codeStatus = "403 Forbidden";
                print = "403";
            }
            // creation du header
            String fileNameError = "";
            printHeader("", codeStatus, -1, out, "");
            // s'il y a une erreur on affiche une page d'erreur selon l'erreur détectée 
            switch (print) {
                case "404":
                    fileNameError = INIT_DIR + "errorNonTrouve.html";
                    break;
                case "403":
                    fileNameError = INIT_DIR + "errorAccesInterdit.html";
                    break;
                default:
                    break;
            }
            // si une erreur est détectée on affiche la page d'erreur correspondante
            if (!fileNameError.equals("")) {
                File fileError = new File(fileNameError);
                byte[] fileData = readData(fileError);
                dataOut.write(fileData, 0, (int) fileError.length());
                dataOut.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                String codeStatus = "500 Internal Server Error";
                printHeader("", codeStatus, -1, out, "");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }
    
    // on écrit les données récupérées au format byte pour les insérer dans le fichier en paramètre
    private void writeData(File file, byte[] fileData, boolean exists) throws IOException {
        FileOutputStream fileToModif = null;
        try {
            fileToModif = new FileOutputStream(file, exists);
            fileToModif.write(fileData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileToModif != null) fileToModif.close();
        }
    }
    
    public List<String> getHeaderFromClient(BufferedInputStream in) throws IOException {
        int charRead = in.read();
        List<String> listeHeader = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        while (charRead != -1) {
            header.append((char) charRead);
            if (header.toString().endsWith("\r\n")) {
                if (header.toString().equals("\r\n")) {
                    break;
                } else {
                    listeHeader.add(header.substring(0, header.toString().lastIndexOf("\r\n")));
                    header = new StringBuilder();
                }
            }
            charRead = in.read();
        }
        System.out.println(listeHeader);
        return listeHeader;
    }


    public File buildHeaderPutAndPost(BufferedInputStream in, String fileName, PrintWriter out, boolean put) {
        File file = null;
        try {
            String codeStatus = "";
            file = new File(fileName);
            boolean alreadyHere = file.exists();
            // si la méthode est appelé pour construire le header d'une méthode put on écrase le fichier

            if (put) {
                FileWriter ecrasement = new FileWriter(file);
                ecrasement.close();
            }

            List<Byte> fileData = new ArrayList<>();
            while (in.available() > 0) {
                fileData.add((byte) in.read());
            }

            byte[] fileDataArray = new byte[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                fileDataArray[i] = fileData.get(i);
            }
            writeData(file, fileDataArray, alreadyHere);

            if (!put && alreadyHere) {
                codeStatus = "200 OK";
            } else if (put && alreadyHere) {
                codeStatus = "204 No content";
            } else {
                codeStatus = "201 Created";
            }

            // creation du header
            printHeader("", codeStatus, -1, out, "");
            // on affiche le fichier dans lequel on a ecrit dans le body


        } catch (Exception e) {
            e.printStackTrace();
            try {
                String codeStatus = "500 Internal Server Error";
                printHeader("", codeStatus, -1, out, "");

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return file;
    }


    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
    }
}
