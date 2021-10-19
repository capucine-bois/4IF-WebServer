///A Simple Web Server (WebServer.java)

package src.http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class WebServer {
  static final String INIT_DIR= "src/doc";

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
    for (;;) {
      try {
        // wait for a connection
        Socket remote = s.accept();
        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
        BufferedInputStream fluxIn = new BufferedInputStream(remote.getInputStream());
        PrintWriter out = new PrintWriter(remote.getOutputStream());
        // get binary output stream to client (for requested data)
        BufferedOutputStream dataOut = new BufferedOutputStream(remote.getOutputStream());
        String str = ".";

        int i =0;
        String requestMethod = null;
        StringTokenizer lineClient;
        String header ="";
        while (str != null && !str.equals("")) {
          str = in.readLine();
          if(i==0) {
            // we parse the request with a string tokenizer
            lineClient = new StringTokenizer(str);
            requestMethod = lineClient.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            fileName = lineClient.nextToken().toLowerCase();
          }
          //requete tapée par l'utilisateur
          i++;
          header += str + "\n\r";
        }

          System.out.println("REQUEST CLIENT:");
          System.out.println(header);

          if (!header.isEmpty()) {
            switch(requestMethod) {
              case "GET":
                executeGETmethod(fileName, out, dataOut);
                break;
              case "HEAD":
                executeHEADmethod(fileName, out, dataOut);
                break;
              case "POST":
                executePOSTmethode(fileName, out, dataOut, fluxIn);
                break;
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


  public String getTypeFromExtension(String extension){
    String type ="";
  switch (extension) {
    case "jpg" :
    case "jpeg" :
    case "gif" :
    case "png" :
    case "bmp" :
      type = "image";
      break;
    case "mp4" :
      type = "video";
      break;
    case"txt" :
    case "docx" :
    case "doc" :
    case "pdf" :
    case "md" :
    case "html":
      type = "text";
      break;
    default :
      type = "unknown";
      break;
    }
    System.out.println("le type est " + type);
    return  type;
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


 public void buildHeader(String fileName, PrintWriter out, BufferedOutputStream dataOut, File file, int fileLength,boolean get) throws IOException {
   if(fileName.equals("/")) {
     fileName = INIT_DIR + "/index.html";
   }else{
     fileName= INIT_DIR + fileName;
   }
   file = new File(fileName);
   String codeStatus = "OK 200", extension = "";
   if (!file.exists()) {
     codeStatus = "404 not found";
     fileName = INIT_DIR + "/error404.html";
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


   if(get) {
     byte[] fileData = readData(file);
     dataOut.write(fileData, 0,fileLength);
     dataOut.flush();
   }



 }


  public byte[] readData(File file) throws IOException {
    int lengthFile= (int) file.length();
    FileInputStream dataStream = null;
    byte[] dataArray = new byte[lengthFile];
    try{
      dataStream = new FileInputStream(file);
      dataStream.read(dataArray);
    } finally {
      if (dataStream != null) dataStream.close();
    }
    System.out.println(dataArray);
    return dataArray;
  }



  public void printHeader(String content, String status, int length, PrintWriter out, String fileName) {
    out.println("HTTP/1.1 " +status);
    out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
    out.println("Date: " + new Date());
    out.println("Content-type: " + content);
    out.println("Content-length: " + length);
    out.println("Content-name: " + fileName);
    out.println();
    out.flush();
  }


  public void executePOSTmethode(String fileName, PrintWriter out, BufferedOutputStream dataOut, BufferedInputStream in) throws IOException {
    /*int fileLength = 0;
    File file = new File("");
    buildHeader(fileName, out, dataOut, file, fileLength, false);*/
    /*File file = new File("");
    int fileLength = 0;
    if(fileName.equals("/")) {
      fileName = INIT_DIR + "/index.html";
    }else{
      fileName= INIT_DIR + fileName;
    }
    file = new File(fileName);
    String codeStatus = "OK 200", extension = "";
    if (!file.exists()) {
      codeStatus = "404 not found";
      fileName = INIT_DIR + "/error404.html";
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
    printHeader(content, codeStatus, fileLength, out, fileName);*/

    File file = new File(fileName);

    boolean existed = file.exists();

    // Ouverture d'un flux d'�criture binaire vers le fichier, en mode insertion � la fin
    BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file, existed));

    //byte[] fileData = readData(file);
    byte[] fileData = new byte[256];
    while(in.available() > 0) {
      int nbRead = in.read(fileData);
      fileOut.write(fileData, 0, nbRead);
    }
    //fileOut.write(fileData, 0, (int)file.length());

    //fileOut.write(fileData, 0, fileLength);
    fileOut.flush();

    //Fermeture du flux d'�criture vers le fichier
    fileOut.close();


  }


  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
