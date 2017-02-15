/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/
//A website that I used that was really contributing to this project
//  was the bufferreader website.
//  Website : 
//  http://www.avajava.com/tutorials/lessons/how-do-i-read-a-string-from-a-file-line-by-line.html?page=1
import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.nio.file.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Arrays;

public class WebWorker implements Runnable
{

private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}
public static class Variables{
    public static String filepath;
    public static String cType;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      readHTTPRequest(is);
      if(Variables.cType.equals("html")){
        writeHTTPHeader(os,"text/html");
        }
      else{
        String ty = "image/";
        ty = ty.concat(Variables.cType);
        writeHTTPHeader(os,ty);
      }
      writeContent(os,Variables.cType);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private void readHTTPRequest(InputStream is)
{
   String line;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
        System.err.println("Request line: ("+line+")");
		String[] a = line.split(" ");
        if (a[0].equals("GET")){
            Variables.filepath = a[1];
            String ty = Variables.filepath;
            String[] type = ty.split("\\.");
            Variables.cType= type[1];
	    }
	 if (line.length()==0) break;
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }
   return;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   os.write("HTTP/1.1 200 OK\n".getBytes());
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String s) throws Exception
{
	Date d = new Date();
   	DateFormat df = DateFormat.getDateTimeInstance();
    String dotS = ".";
    dotS = dotS.concat(Variables.filepath);
	File file = new File(dotS);
// Error procedure
    if(!file.exists()){
        os.write("<html><head></head><body>\n".getBytes());
        os.write("<h1>404 Not Found</h1>\n".getBytes());
        os.write("<h3> The file you tried to look up is not available.</h3>\n".getBytes());
        os.write("</body></html>\n".getBytes());
        }
	try {
//  For text part of the file
        if(s.equals("html")){
          FileReader fr = new FileReader(file);
		  BufferedReader br = new BufferedReader(fr);
		  StringBuffer sr = new StringBuffer();
		  String msg;
		  while ((msg = br.readLine()) != null) {			
			sr.append(msg);
			sr.append("\n");
		  }
		  fr.close();
		  String repString = sr.toString();
		  repString = repString.replaceAll("<cs371date>",df.format(d));
          String finalString = repString.replaceAll("<cs371server>", "Server: Infinite Memes");
		  os.write(finalString.getBytes());
        }
//  For image part of file
        else{
            FileInputStream iS = new FileInputStream(dotS);
            int nread;
            while((nread = iS.read())!= -1){
                os.write(nread);
            }
            iS.close();
        }
		}
    catch (IOException e) {
			e.printStackTrace();
    }
}

} // end class
