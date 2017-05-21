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
import java.util.Collections;
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
public class HttpCsvGraphMoveNodeDown {

	@javax.ws.rs.Path("")
	public static class MyResource { // Must be public

		@GET
		@javax.ws.rs.Path("")
		@Produces("application/json")
		// We can only call this once between each browser refresh. Our method
		// should in theory not need to know the other siblings.
		public Response moveDown(@QueryParam("value") String iNodeToMoveDown)
				throws JSONException, IOException {
			System.err.println("[DEBUG] begin new method: " + iNodeToMoveDown);
			Collection<String> otherSiblings = getOtherSiblings(iNodeToMoveDown);
			System.out
					.println("HttpCsvGraphMoveNodeDown.MyResource.moveDown() 2");
			if (otherSiblings.size() == 0) {
				throw new RuntimeException("Impossible 2: " + iNodeToMoveDown);
			}
			String newParent = randomSelect(otherSiblings);
			System.out
					.println("HttpCsvGraphMoveNodeDown.MyResource.moveDown() 3");
			
			return moveDown(iNodeToMoveDown, newParent);
		}

		private static String randomSelect(Collection<String> otherSiblings) {
			System.out
					.println("HttpCsvGraphMoveNodeDown.MyResource.randomSelect() 1");
			List<String> toShuffle = new LinkedList<String>();
			toShuffle.addAll(otherSiblings);
			System.out
			.println("HttpCsvGraphMoveNodeDown.MyResource.randomSelect() 2");
			Collections.shuffle(toShuffle);
			System.out
			.println("HttpCsvGraphMoveNodeDown.MyResource.randomSelect() 3");
			if (toShuffle.size() > 0) {
				return toShuffle.get(0);
			} else {
				System.out
						.println("HttpCsvGraphMoveNodeDown.MyResource.randomSelect() impossible?");
				throw new RuntimeException("This shouldn't happen should it?");
			}
		}

		private static Collection<String> getOtherSiblings(String iNodeToMoveDown) throws IOException {
			System.out.println("HttpCsvGraphMoveNodeDown.MyResource.getOtherSiblings()");
			String parent = getParent(iNodeToMoveDown);
			return remove(getChildren(parent), iNodeToMoveDown);
		}

		private static Collection<String> remove(Collection<String> iNodes,
				String iExclude) {
			System.out.println("HttpCsvGraphMoveNodeDown.MyResource.remove() begin");
			ImmutableSet.Builder<String> reduced = ImmutableSet.builder();
			for (String aNode : iNodes) {
				if (!aNode.equals(iExclude)) {
					reduced.add(aNode);
				}
			}
			System.out.println("HttpCsvGraphMoveNodeDown.MyResource.remove() end");
			return reduced.build();
		}

		private static Collection<String> getChildren(String iParentNode) throws IOException {
			System.out
					.println("HttpCsvGraphMoveNodeDown.MyResource.getChildren() begin");
			List<String> children = new LinkedList<String>();
			System.out.println("HttpCsvGraphMoveNodeDown.MyResource.getChildren() 1");
			List<String> lines;
			try {
				lines = getFileLines(HttpCsvGraphMoveNodeDown.filepath);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			System.out.println("HttpCsvGraphMoveNodeDown.MyResource.getChildren() 2");
			for (String line : lines) {
				if (line.contains(iParentNode)) { // avoid expensive parsing
					String[] relationship = new CSVParser(
							new StringReader(line)).getLine();
					String aNode = relationship[0];
					System.out
							.println("HttpCsvGraphMoveNodeDown.MyResource.getChildren() aNode = " + aNode);
					if (aNode.equals(iParentNode)) {
						children.add(relationship[1]);
					}
				}
			}
			System.out
					.println("HttpCsvGraphMoveNodeDown.MyResource.getChildren() end");
			return children;
		}

		private static String getParent(String iChildNode) throws IOException {
			System.out
					.println("HttpCsvGraphMoveNodeDown.MyResource.getParent()");
			List<String> lines = getFileLines(HttpCsvGraphMoveNodeDown.filepath);
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

		/**
		 * @param iToMove
		 *            - Node to move
		 * @param allNodes
		 *            - all nodes connected to the node to move, including
		 *            itself (which ideally it shouldn't)
		 */
		@GET
		@javax.ws.rs.Path("moveDownOld")
		@Produces("application/json")
		@Deprecated
		// We can only call this once between each browser refresh. Our method
		// should in theory not need to know the other siblings.
		public Response move(@QueryParam("value") String iToMove,
				@QueryParam("otherChildren") String iSiblings)
				throws JSONException, IOException {

			System.err.println("[DEBUG] begin: " + iToMove);
			System.err.println("[DEBUG] DO NOT CALL THIS METHOD: " + iSiblings);
			
			String newParent = findNewParent(iToMove, new JSONObject(iSiblings));
			
			return moveDown(iToMove, newParent);
		}

		// TODO: Rename as "reparent"
		private Response moveDown(String iNodeToMoveDown, String newParent)
				throws IOException {
			if (newParent == null) {
				System.err.println("[DEBUG] 7: " + iNodeToMoveDown);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", "Found no new parent");
				return Response.serverError()
						.header("Access-Control-Allow-Origin", "*")
						.type("application/json")
						.entity(jsonObject.toString()).build();
			}
			System.err.println("[DEBUG] 4: " + iNodeToMoveDown);
						
			if (iNodeToMoveDown.endsWith("\"")) {
				System.err.println("[DEBUG] 5: " + iNodeToMoveDown);
				throw new RuntimeException("Developer Error 1 (I don't remember why this is invalid)");
			}
			List<String> beforeRemoval = getFileLines(HttpCsvGraphMoveNodeDown.filepath);
			List<String> afterRemoval = removeRelationshipWithParent(iNodeToMoveDown, beforeRemoval);
			System.err.println("[DEBUG] 7.5: " + iNodeToMoveDown);
			int removed = beforeRemoval.size() - afterRemoval.size();
			if (removed > 0) {
				System.err.println("[DEBUG] 8: removed " + removed);
				String s = "\""+newParent+"\",\"" +iNodeToMoveDown+ "\"";
				List<String> lines = new LinkedList<String>();
				lines.addAll(afterRemoval);
				lines.add(s);
				System.err.println("[DEBUG] 9: " + s);
				FileUtils.writeLines(Paths.get(filepath).toFile(), lines, false);
				System.err.println("[DEBUG] Successfully removed from file: " + iNodeToMoveDown);
				JSONObject jsonObject = new JSONObject();
				return Response.ok().header("Access-Control-Allow-Origin", "*")
						.type("application/json")
						.entity(jsonObject.toString()).build();
			} else {
				System.err.println("[DEBUG] 10: Nothing got removed");
				JSONObject jsonObject = new JSONObject();
				return Response.serverError()
						.header("Access-Control-Allow-Origin", "*")
						.type("application/json")
						.entity(jsonObject.toString()).build();
			}
		}

		private static List<String> getFileLines(String filepath) throws IOException {
			System.out
					.println("HttpCsvGraphMoveNodeDown.MyResource.getFileLines()");
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
					.println("HttpCsvGraphMoveNodeDown.MyResource.removeRelationshipWithParent()");
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

		@Deprecated
		private String findNewParent(String iToMove, JSONObject allNodes) {
			String newParent = null;
			// TODO: Shuffle the keys so the new parent is random rather than bunched
			for (String aNode : allNodes.keySet()) {
				if (aNode.equals(iToMove)) {
					
				} else {
					newParent = aNode;
					break;
				}
			}
			return newParent;
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
