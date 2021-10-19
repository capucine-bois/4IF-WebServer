///A Simple Web Server (WebServer.java)

package src.http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class WebServer {
  static final String INIT_DIR= "src/doc/";

  protected void start() {
    ServerSocket s;

    String fileName = null;
    System.out.println("Webserver starting up on port 3000");
    System.out.println("(press ctrl-c to exit)");
    System.out.println(System.getProperty("user.dir"));
  ;
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

          System.out.println("REQUEST :");
          System.out.println(header);

          if (!header.isEmpty()) {
            switch(requestMethod) {
              case "GET":
                executeGETmethod(fileName, out, dataOut);
                break;
              case "POST":
                // code block
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

  public void executeGETmethod(String fileName, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
    if(fileName.equals("/")) {
      fileName = INIT_DIR + "index.html";
    }else{
      fileName= INIT_DIR + fileName;
    }
    System.out.println("le file est " + fileName);
    File file = new File(fileName);
    int fileLength = 0;
    String codeStatus = "OK 200", extension = "";
    byte[] fileData;

    if (!file.exists()) {
      codeStatus = "Error 404";
      System.out.println("error 404 94");
    } else {
      fileLength = (int) file.length();
      extension = null;
      int extensionPos = fileName.lastIndexOf('.');
      if (extensionPos > 0) {
        extension = fileName.substring(extensionPos + 1);
      } else {
        codeStatus = "Error 404";
        System.out.println("103 err 404");
      }
    }
    String content = getTypeFromExtension(extension) + "/" + extension;
    printHeader(content, codeStatus, fileLength, out);

    if(codeStatus.equals("OK 200")){
      fileData = readData(file);
      dataOut.write(fileData, 0,fileLength);
      dataOut.flush();
    }
  }


  public byte[] readData(File file) throws IOException {
    System.out.println("on entre dans read data");
    int lengthFile= (int) file.length();
    FileInputStream dataStream = null;
    byte[] dataArray = new byte[lengthFile];
    try{
      dataStream = new FileInputStream(file);
      dataStream.read(dataArray);
    } finally {
      if (dataStream != null) dataStream.close();
    }
    return dataArray;
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

  public void printHeader(String content, String status, int length, PrintWriter out) {
    if(status.equals("OK 200")) {
      out.println("HTTP/1.1 200 OK");
      out.println("Server: Java HTTP Server from Capucine and Arthur : 1.0");
      out.println("Date: " + new Date());
      out.println("Content-type: " + content);
      out.println("Content-length: " + length);
      System.out.println("on est bien passé dans le bon if du rint header");
    } else {
      out.println("HTTP/1.1 " + status);
    }
    out.println();
    out.flush();
  }

  /**
   * Start the application.
   * 
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
