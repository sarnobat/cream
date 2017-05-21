import com.google.common.collect.*;

import org.apache.commons.csv.CSVParser;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.LinkedList;
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
import org.json.JSONException;
import org.json.JSONObject;

/**  */
public class HttpCsvGraphMoveNodeUp {

	@javax.ws.rs.Path("")
	public static class MyResource { // Must be public

		@GET
		@javax.ws.rs.Path("")
		@Produces("application/json")
		// We can only call this once between each browser refresh. Our method
		// should in theory not need to know the other siblings.
		public Response moveUp(@QueryParam("value") String iNodeToMoveUp)
				throws JSONException, IOException {
			
			System.err.println("[DEBUG] begin new method: " + iNodeToMoveUp);
			Collection<String> otherSiblings = getOtherSiblings(iNodeToMoveUp);
			if (otherSiblings.size() == 0) {
				throw new RuntimeException("Impossible 2: " + iNodeToMoveUp);
			}
			
			for (String sibling : otherSiblings) {
				reparent(sibling, iNodeToMoveUp);
			}
			System.out
					.println("HttpCsvGraphMoveNodeUp.MyResource.moveUp() 2");
			JSONObject jsonObject = new JSONObject();
			return Response.ok().header("Access-Control-Allow-Origin", "*")
					.type("application/json")
					.entity(jsonObject.toString()).build();
			
		}

		private static Collection<String> getOtherSiblings(String iNodeToMoveUp) throws IOException {
			System.out.println("HttpCsvGraphMoveNodeUp.MyResource.getOtherSiblings()");
			String parent = getParent(iNodeToMoveUp);
			return remove(getChildren(parent), iNodeToMoveUp);
		}

		private static Collection<String> remove(Collection<String> iNodes,
				String iExclude) {
			System.out.println("HttpCsvGraphMoveNodeUp.MyResource.remove() begin");
			ImmutableSet.Builder<String> reduced = ImmutableSet.builder();
			for (String aNode : iNodes) {
				if (!aNode.equals(iExclude)) {
					reduced.add(aNode);
				}
			}
			System.out.println("HttpCsvGraphMoveNodeUp.MyResource.remove() end");
			return reduced.build();
		}

		private static Collection<String> getChildren(String iParentNode) throws IOException {
			System.out
					.println("HttpCsvGraphMoveNodeUp.MyResource.getChildren() begin");
			List<String> children = new LinkedList<String>();
			System.out.println("HttpCsvGraphMoveNodeUp.MyResource.getChildren() 1");
			List<String> lines;
			try {
				lines = getFileLines(HttpCsvGraphMoveNodeUp.filepath);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			System.out.println("HttpCsvGraphMoveNodeUp.MyResource.getChildren() 2");
			for (String line : lines) {
				if (line.contains(iParentNode)) { // avoid expensive parsing
					String[] relationship = new CSVParser(
							new StringReader(line)).getLine();
					String aNode = relationship[0];
					System.out
							.println("HttpCsvGraphMoveNodeUp.MyResource.getChildren() aNode = " + aNode);
					if (aNode.equals(iParentNode)) {
						children.add(relationship[1]);
					}
				}
			}
			System.out
					.println("HttpCsvGraphMoveNodeUp.MyResource.getChildren() end");
			return children;
		}

		private static String getParent(String iChildNode) throws IOException {
			System.out
					.println("HttpCsvGraphMoveNodeUp.MyResource.getParent()");
			List<String> lines = getFileLines(HttpCsvGraphMoveNodeUp.filepath);
			String parent = null;
			for (String line : lines) {
				if (line.contains(iChildNode)) { // avoid expensive parsing
					String[] relationship = new CSVParser(
							new StringReader(line)).getLine();
					String aNode = relationship[1];
					if (aNode.equals(iChildNode)) {
						parent = relationship[0];
						break;
					}
				}
			}
			if (parent == null) {
				throw new RuntimeException("Developer Error : couldn't find original parent of node to be moved (so we can't determine the nodes' siblings)");
			}
			return parent;
		}

		private void reparent(String iNodeToReparent, String newParent)
				throws IOException {
			if (newParent == null) {
				System.err.println("[DEBUG] 7: " + iNodeToReparent);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", "Found no new parent");
				throw new RuntimeException("No new parent");
			}
			System.err.println("[DEBUG] 4: " + iNodeToReparent);
						
			if (iNodeToReparent.endsWith("\"")) {
				System.err.println("[DEBUG] 5: " + iNodeToReparent);
				throw new RuntimeException("Developer Error 1 (I don't remember why this is invalid)");
			}
			List<String> beforeRemoval = getFileLines(HttpCsvGraphMoveNodeUp.filepath);
			List<String> afterRemoval = removeRelationshipWithParent(iNodeToReparent, beforeRemoval);
			System.err.println("[DEBUG] 7.5: " + iNodeToReparent);
			int removed = beforeRemoval.size() - afterRemoval.size();
			if (removed > 0) {
				System.err.println("[DEBUG] 8: removed " + removed);
				String s = "\""+newParent+"\",\"" +iNodeToReparent+ "\"";
				List<String> lines = new LinkedList<String>();
				lines.addAll(afterRemoval);
				lines.add(s);
				System.err.println("[DEBUG] 9: " + s);
				FileUtils.writeLines(Paths.get(filepath).toFile(), lines, false);
				System.err.println("[DEBUG] Successfully removed from file: " + iNodeToReparent);
			} else {
				System.err.println("[DEBUG] 10: Nothing got removed");
				throw new RuntimeException("Nothing got removed");
			}
		}

		private static List<String> getFileLines(String filepath) throws IOException {
			System.out
					.println("HttpCsvGraphMoveNodeUp.MyResource.getFileLines()");
			try {
				return ImmutableList.copyOf(FileUtils.readLines(new File(
						filepath), Charset.defaultCharset()));
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		private List<String> removeRelationshipWithParent(String iChildNodeToRemove,
				List<String> beforeRemoval) {
			System.out
					.println("HttpCsvGraphMoveNodeUp.MyResource.removeRelationshipWithParent()");
			List<String> lines = new LinkedList<String>();
			lines.addAll(beforeRemoval);
			try {
				for (String line : beforeRemoval) {
					//System.err.println("[DEBUG] 5.5: " + line);
					if (line.endsWith("\"" + iChildNodeToRemove + "\"")) {
						System.err.println("[DEBUG] 6: " + iChildNodeToRemove);
						lines.remove(line);
						System.err.println("[DEBUG] 6.5: " + iChildNodeToRemove);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<String> afterRemoval = ImmutableList.copyOf(lines);
			return afterRemoval;
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
