///A Simple Web Server (WebServer.java)

package src.http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class WebServer {
  static final File INIT_DIR = new File("./doc");

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
        PrintWriter out = new PrintWriter(remote.getOutputStream());
        // get binary output stream to client (for requested data)
        BufferedOutputStream dataOut = new BufferedOutputStream(remote.getOutputStream());
        String str = ".";

        while (str != null && !str.equals("")) {
          //requete tapée par l'utilisateur
          str = in.readLine();
          // we parse the request with a string tokenizer
          StringTokenizer lineClient = new StringTokenizer(str);
          String requestMethod = lineClient.nextToken().toUpperCase(); // we get the HTTP method of the client
          // we get file requested
          fileName = lineClient.nextToken().toLowerCase();

          System.out.println("REQUEST :");
          System.out.println(str);

          if (!str.isEmpty()) {

            switch(requestMethod) {
              case "GET":
                executeGETmethod(fileName, out);
                break;
              case "POST":
                // code block
                break;
              default:
                //erreur
                break;
            }


          }

          // Send the response
          // Send the headers
          out.println("HTTP/1.0 200 OK");
          //out.println("Content-Type: text/html");
          //out.println("Server: Bot");
          // this blank line signals the end of the headers
          out.println("");
          // Send the HTML page
          out.println("<H1>Welcome to the Ultra Mini-WebServer</H2>");
        }

        out.flush();
        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
  }

  public void executeGETmethod(String fileName, BufferedOutputStream out){
    File file = new File(fileName);
    int fileLength = (int) file.length();
    String extension = null;
    int extensionPos = fileName.lastIndexOf('.');
    if(extensionPos > 0){
      extension.substring(extensionPos+1);
    }

    String content = getTypeFromExtension(extension);




  }

  public String getTypeFromExtension(String extension){
    String type ="";

    return  type;
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
