import com.google.common.collect.*;

import java.util.regex.*;
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
public class HttpCsvGraphReparent {

	@javax.ws.rs.Path("")
	public static class MyResource { // Must be public

		@GET
		@javax.ws.rs.Path("")
		@Produces("application/json")
		// We can only call this once between each browser refresh. Our method
		// should in theory not need to know the other siblings.
		public Response moveUp(@QueryParam("child") String iChild, 
					@QueryParam("newParent") String newParent,
					@QueryParam("heapFile") String heapFile)
				throws JSONException, IOException {
			
			reparent(iChild, newParent, "/home/sarnobat/www/" + heapFile);
			
			System.out
					.println("HttpCsvGraphReparent.MyResource.moveUp() 2");
			JSONObject jsonObject = new JSONObject();
			return Response.ok().header("Access-Control-Allow-Origin", "*")
					.type("application/json")
					.entity(jsonObject.toString()).build();
			
		}


    @GET
    @javax.ws.rs.Path("/health")
    @Produces("application/json")
    public Response health() {
      Response r = Response.ok().header("Access-Control-Allow-Origin", "*").type("application/json").entity(new JSONObject().toString()).build();
      return r;
    }

		private static Collection<String> getOtherSiblings(String iNodeToMoveUp, String filepath) throws IOException {
			System.out.println("HttpCsvGraphReparent.MyResource.getOtherSiblings()");
			String parent = getParent(iNodeToMoveUp);
			return remove(getChildren(parent, filepath), iNodeToMoveUp);
		}

		private static Collection<String> remove(Collection<String> iNodes,
				String iExclude) {
			System.out.println("HttpCsvGraphReparent.MyResource.remove() begin");
			ImmutableSet.Builder<String> reduced = ImmutableSet.builder();
			for (String aNode : iNodes) {
				if (!aNode.equals(iExclude)) {
					reduced.add(aNode);
				}
			}
			System.out.println("HttpCsvGraphReparent.MyResource.remove() end 1");
			Collection<String> c = reduced.build();
			
			System.out.println("HttpCsvGraphReparent.MyResource.remove() end 2");
			return c;
		}

		private static Collection<String> getChildren(String iParentNode, String filepath) throws IOException {
			System.out
					.println("HttpCsvGraphReparent.MyResource.getChildren() begin");
			List<String> children = new LinkedList<String>();
			System.out.println("HttpCsvGraphReparent.MyResource.getChildren() 1");
			List<String> lines;
			try {
				lines = getFileLines(filepath);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			System.out.println("HttpCsvGraphReparent.MyResource.getChildren() 2");
			for (String line : lines) {
				if (line.contains(iParentNode)) { // avoid expensive parsing
					String[] relationship = new CSVParser(
							new StringReader(line)).getLine();
					String aNode = relationship[0];
					System.out
							.println("HttpCsvGraphReparent.MyResource.getChildren() aNode = " + aNode);
					if (aNode.equals(iParentNode)) {
						children.add(relationship[1]);
					}
				}
			}
			System.out
					.println("HttpCsvGraphReparent.MyResource.getChildren() end");
			return children;
		}

		private static String getParent(String iChildNode, String filepath) throws IOException {
			System.out
					.println("HttpCsvGraphReparent.MyResource.getParent()");
			List<String> lines = getFileLines(filepath);
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

		private void reparent(String iNodeToReparent, String newParent, String heapFile)
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
			List<String> beforeRemoval = getFileLines(heapFile);
			List<String> afterRemoval = removeRelationshipWithParent(iNodeToReparent, beforeRemoval);
// 			System.err.println("[DEBUG] 7.5: " + iNodeToReparent);
			int removed = beforeRemoval.size() - afterRemoval.size();
			if (removed > 0) {
// 				System.err.println("[DEBUG] 8: will remove " + removed);
				String s = "\""+newParent+"\",\"" +iNodeToReparent+ "\"";
				List<String> lines = new LinkedList<String>();
				lines.addAll(afterRemoval);
				lines.add(s);
// 				System.err.println("[DEBUG] 8.5: checking for cycles ");
				_checkForCycles : {
					Map<String, String> childToParent = parseLinesAndReverse(lines);
// 					System.err.println("[DEBUG] 8.6");
					// if (childToParent.size() < lines.size()) {
// 						System.err.println("[DEBUG] 8.7");
// 						// children must be unique, so if the same child appears twice,
// 						// the keyset only stores the last added one
// 						System.err.println("[ERROR] Cycle found");
// 						throw new RuntimeException("Cycle found");
// 					} else {
// 						System.err.println("[DEBUG] No cycles found");
// 					}
				}
				
// 				System.err.println("[DEBUG] 9: " + s);
				FileUtils.writeLines(Paths.get(filepath).toFile(), lines, false);
				System.err.println("[DEBUG] Successfully removed from file: " + iNodeToReparent);
			} else {
				System.err.println("[DEBUG] 10: Couldn't find parent row for " + iNodeToReparent);
				throw new RuntimeException("Nothing got removed");
			}
		}
		
		private static Map<String, String> parseLinesAndReverse(List<String> lines) {
// 			System.err.println("[DEBUG] parseLinesAndReverse() - begin ");
			Map<String, String> childToParent = new HashMap<String, String>();
// 			System.err.println("[DEBUG] parseLinesAndReverse() - 1");
			Pattern p = Pattern.compile("\"(.*)\",\"(.*)\"");
// 			System.err.println("[DEBUG] parseLinesAndReverse() - 2");
			for (String line : lines) {
// 				System.err.println("[DEBUG] parseLinesAndReverse() - 3");
				Matcher m = p.matcher(line);
// 				System.err.println("[DEBUG] parseLinesAndReverse() - 4");
				if (!m.find()) {
					System.err.println("[DEBUG] parseLinesAndReverse() - 4.01 no match:  " + line);
					continue;
				}
				String parent  = "";
				try {
					parent = m.group(1);
				} catch (Exception e) {
				
					System.err.println("[DEBUG] parseLinesAndReverse() - 4.1 " + line);
					e.printStackTrace();
					throw e;
				}
				
// 								System.err.println("[DEBUG] parseLinesAndReverse() - 5");
				String child = m.group(2);
// 								System.err.println("[DEBUG] parseLinesAndReverse() - 6");
// 				System.err.println("[DEBUG] parent = " + parent);
// 				System.err.println("[DEBUG] child = " + child);
				if (childToParent.containsKey(child)) {
					if (!childToParent.get(child).equals(parent)) {
						System.err.println("[WARNING] duplicate line (ideally it shouldn't have):  " + line);
						System.err.println("[DEBUG] Child already has a parent:  " + child);
						System.err.println("[DEBUG] Parent is:  " + childToParent.get(child));
						System.err.println("[DEBUG] Attempted to change parent to:  " + parent);
						throw new RuntimeException("Child already has a parent " + child);
					}
				}
				childToParent.put(child, parent);
			}
// 			System.err.println("[DEBUG] parseLinesAndReverse() - end ");
			return childToParent;
		}

		private static List<String> getFileLines(String filepath) throws IOException {
			System.out
					.println("HttpCsvGraphReparent.MyResource.getFileLines()");
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
					.println("HttpCsvGraphReparent.MyResource.removeRelationshipWithParent() - " + iChildNodeToRemove);
			List<String> lines = new LinkedList<String>();
			lines.addAll(beforeRemoval);
			try {
				for (String line : beforeRemoval) {
					//System.err.println("[DEBUG] 5.5: " + line);
					if (line.endsWith("\"" + iChildNodeToRemove + "\"")) {
						System.err.println("[DEBUG] 6: " + iChildNodeToRemove);
						lines.remove(line);
// 						System.err.println("[DEBUG] 6.5: " + iChildNodeToRemove);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<String> afterRemoval = ImmutableList.copyOf(lines);
			return afterRemoval;
		}
	}


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
