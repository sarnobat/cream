import com.google.common.collect.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**  */
public class HttpCsvGraphMoveNode {

	@javax.ws.rs.Path("")
	public static class MyResource { // Must be public

		/**
		 * @param iToMove
		 *            - Node to move
		 * @param allNodes
		 *            - all nodes connected to the node to move, including
		 *            itself (which ideally it shouldn't)
		 */
		@GET
		@javax.ws.rs.Path("")
		@Produces("application/json")
		public Response move(@QueryParam("value") String iToMove,
				@QueryParam("otherChildren") String iSiblings)
				throws JSONException, IOException {

			System.err.println("[DEBUG] begin: " + iToMove);

			List<String> lines = FileUtils.readLines(new File(filepath),
					Charset.defaultCharset());

			System.err.println("[DEBUG] 2: " + iSiblings);
			JSONObject allNodes = new JSONObject(iSiblings);
			System.err.println("[DEBUG] 3: " + iSiblings);
			String newParent = null;
			for (String aNode : allNodes.keySet()) {
				if (aNode.equals(iToMove)) {
					
				} else {
					newParent = aNode;
					break;
				}
			}
			if (newParent == null) {
				System.err.println("[DEBUG] 7: " + iToMove);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", "Found no new parent");
				return Response.serverError()
						.header("Access-Control-Allow-Origin", "*")
						.type("application/json")
						.entity(jsonObject.toString()).build();
			}
			System.err.println("[DEBUG] 4: " + iToMove);
						
			if (iToMove.endsWith("\"")) {
				System.err.println("[DEBUG] 5: " + iToMove);
				throw new RuntimeException("Developer Error 1");
			}
			try {
				for (String line : ImmutableList.copyOf(lines)) {
					//System.err.println("[DEBUG] 5.5: " + line);
					if (line.endsWith("\"" + iToMove + "\"")) {
						System.err.println("[DEBUG] 6: " + iToMove);
						lines.remove(line);
						System.err.println("[DEBUG] 6.5: " + iToMove);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
//			System.err.println("[DEBUG] 6.75: " + iToMove);		
			
			System.err.println("[DEBUG] 7.5: " + iToMove);
			if (lines.remove(iToMove)) {
				System.err.println("[DEBUG] 8: " + iToMove);
				lines.add(newParent);
				System.err.println("[DEBUG] 9: " + iToMove);
				FileUtils.writeLines(Paths.get(filepath).toFile(), lines, true);
				System.err.println("[DEBUG] Successfully removed from file: " + iToMove);
				JSONObject jsonObject = new JSONObject();
				return Response.ok().header("Access-Control-Allow-Origin", "*")
						.type("application/json")
						.entity(jsonObject.toString()).build();
			} else {
				System.err.println("[DEBUG] 10: " + iToMove);
				JSONObject jsonObject = new JSONObject();
				return Response.serverError()
						.header("Access-Control-Allow-Origin", "*")
						.type("application/json")
						.entity(jsonObject.toString()).build();
			}
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
