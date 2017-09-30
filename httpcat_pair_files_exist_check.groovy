import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONException;
import org.apache.commons.csv.CSVParser;

/** Write to disk, but only after checking that both elements of the CSV pair exist on disk */
public class HttpCatFilesExist {

  @javax.ws.rs.Path("")
  public static class MyResource { // Must be public

    @GET
    @javax.ws.rs.Path("")
    @Produces("application/json")
    public Response list(@QueryParam("value") String iValue, @QueryParam("key") String iKey,
        @QueryParam("categoryId") String iCategoryId) throws JSONException, IOException {
      System.err.println("list()");
      String line = iValue;
      System.err.println("[DEBUG] Writing to stdout: " + line);

	  // This too I wish I could do in a separate process in a pipeline
	  // but for some reason things aren't getting passed down the
	  // pipeline reliably.
      checkFilesExist : {
          String[] r = new CSVParser(new StringReader(line)).getLine();
        	  if (!Paths.get(r[0]).toFile().exists()) {
        		  System.err.println("[ERROR] Does not exist: " + r[0]);
        		  throw new RuntimeException("Does not exist: " + r[0]);
        	  }
        	  if (!Paths.get(r[1]).toFile().exists()) {
        		  System.err.println("[ERROR] Does not exist: " + r[1]);
        		  throw new RuntimeException("Does not exist: " + r[1]);
        	  }
      }
      
      // I wish I didn't have to do this in Java but I found that even though
      // the browser was returning success, nothing was getting written to the
      // file.
      System.err.println("[DEBUG] about to write to file: " + filepath);
      FileUtils.write(Paths.get(filepath).toFile(), line + "\n", "UTF-8", true);
      System.err.println("[DEBUG] wrote to file");
      System.out.println(line);
      Response r = Response.ok().header("Access-Control-Allow-Origin", "*").type("application/json").entity(new JSONObject().toString()).build();
      System.err.println("[DEBUG] response constructed");
      return r;
    }
  }

  private static String filepath;// = System.getProperty("user.home") +
                                 // "/sarnobat.git/yurl_queue_httpcat.txt";

  public static void main(String[] args)
      throws URISyntaxException, IOException, KeyManagementException, UnrecoverableKeyException,
      NoSuchAlgorithmException, KeyStoreException, CertificateException, InterruptedException {
    
    String port;
    _parseOptions: {

      Options options = new Options()
          .addOption("h", "help", false, "show help.");

      Option option = Option.builder("f").longOpt("file").desc("use FILE to write incoming data to").hasArg()
          .argName("FILE").build();
      options.addOption(option);

      // This doesn't work with java 7
      // "hasarg" is needed when the option takes a value
      options.addOption(Option.builder("p").longOpt("port").hasArg().required().build());

      try {
        CommandLine cmd = new DefaultParser().parse(options, args);
        port = cmd.getOptionValue("p", "4444");
        filepath = cmd.getOptionValue("f");

        if (cmd.hasOption("h")) {
          new HelpFormatter().printHelp("httpcat_with_write.groovy", options);
          System.out.println("HttpCatFilesExist.main() 1");
          System.exit(0);
          return;
        }
      } catch (ParseException e) {
    	  System.out.println("HttpCatFilesExist.main() 2");
        e.printStackTrace();
	System.exit(-1);
	return;
      }
    }
    try {
      JdkHttpServerFactory.createHttpServer(new URI("http://localhost:" + port + "/"), new ResourceConfig(MyResource.class));
    } catch (Exception e) {
      System.err.println("Port already listened on.");
      System.exit(-1);
    }
  }
}
