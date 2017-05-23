import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVParser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Csv2HtmlTree {

	public static void main(String[] args) throws IOException {

		//
		// Read the data
		//
		BufferedReader br = null;
		Multimap<String, String> m = HashMultimap.create();
		Map<String, String> child2ParentMap = new HashMap<String, String>();
		br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		int i = 1;
		while ((line = br.readLine()) != null) {
			System.err.println(i++);
			CSVParser p = new CSVParser(new StringReader(line));
			String[] line2 = p.getLine();
			String parent = line2[0];
			String child = line2[1];
			m.put(parent, child);
			child2ParentMap.put(child, parent);
		}

		//
		// Print the data
		//
		Set<String> roots = getRoots(child2ParentMap);
		StringBuffer html = new StringBuffer();
		for (String root : roots) {
			html.append(printSubtreeHtml(root, m, ""));
			// html.append("\n");
		}
		System.out.println(html);
	}

	private static Set<String> getRoots(Map<String, String> child2ParentMap) {
		return Sets.difference(ImmutableSet.copyOf(child2ParentMap.values()),
				child2ParentMap.keySet());
	}

	private static StringBuffer printSubtreeHtml(String root,
			Multimap<String, String> m, String prefix) {
		StringBuffer sb = new StringBuffer();
		String url1 ;
		if (root.startsWith("http")) {
			url1 = root;
		} else {
			url1 = "http://netgear.rohidekar.com:44452" + root;
		}
		sb.append(prefix + "<img src='" + url1 + "' height=100>\n");
		if (m.get(root).isEmpty()) {

		} else {
//			sb.append(prefix + "<blockquote>\n");
			sb.append(prefix + "<br>\n");
			for (String child : m.get(root)) {
				sb.append(printSubtreeHtml(child, m, prefix + "<img src='http://1x1px.me/FFFFFF-1.png' width=100>"));
				 sb.append("<br>\n");
			}
//			sb.append(prefix + "</blockquote>\n");
//			sb.append(prefix + "<br>\n");
		}
		return sb;
	}

	private static StringBuffer printSubtreePlaintext(String root,
			Multimap<String, String> m, String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix + root + "\n");
		for (String child : m.get(root)) {
			sb.append(printSubtreePlaintext(child, m, prefix + "    "));
			// sb.append("\n");
		}
		return sb;
	}
}
