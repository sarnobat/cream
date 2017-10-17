import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.*;
import org.json.JSONException;

/**  */
public class HttpGrep {

	@javax.ws.rs.Path("")
	public static class MyResource { // Must be public

		@GET
		@javax.ws.rs.Path("")
		@Produces("application/json")
		public Response grep(@QueryParam("value") String iValue, @QueryParam("key") String iKey)
				throws JSONException, IOException {

			System.err.println("[DEBUG] begin: " + iValue);

			List<String> lines = FileUtils.readLines(new File("/home/sarnobat/www/" + iKey),
					Charset.defaultCharset());

			String s = "";
			for (String line : lines) {
				if (line.contains(iValue)) {
					s += line + "\n";
				}
			}

			System.err.println("[DEBUG] Success: " + iValue);

			return Response.ok().header("Access-Control-Allow-Origin", "*")
					.type("text/plain")
					.entity(s).build();

			// return Response.serverError()
			// .header("Access-Control-Allow-Origin", "*")
			// .type("application/json")
			// .entity(new JSONObject().toString()).build();
		}


    @GET
    @javax.ws.rs.Path("/health")
    @Produces("application/json")
    public Response health() {
      Response r = Response.ok().header("Access-Control-Allow-Origin", "*").type("application/json").entity(new JSONObject().toString()).build();
      return r;
    }

	}

	private static String filepath;

	@SuppressWarnings("unused")
	public static void main(String[] args) throws URISyntaxException,
			IOException, KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException,
			InterruptedException {

		String port = null;
		_parseOptions: {

			Options options = new Options().addOption("h", "help", false,
					"show help.").addOption("f", "file", true,
					"use FILE to write incoming data to");

			// This doesn't work with java 7
			// "hasarg" is needed when the option takes a value
			options.addOption(Option.builder("p").longOpt("port").hasArg()
					.required().build());

			try {
				CommandLine cmd = new DefaultParser().parse(options, args);
				port = cmd.getOptionValue("p", "4444");
				filepath = cmd.getOptionValue("f");

				if (cmd.hasOption("h")) {

					// This prints out some help
					HelpFormatter formater = new HelpFormatter();

					formater.printHelp("httpgrep.groovy", options);
					System.exit(0);
				}
			} catch (ParseException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		try {
			String url = "http://localhost:" + port + "/";
			JdkHttpServerFactory.createHttpServer(new URI(url),
					new ResourceConfig(MyResource.class));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Port already listened on.");
			System.exit(-1);
		}
	}
}
