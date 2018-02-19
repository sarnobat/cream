import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.csv.CSVParser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class CsvHeapCycleRemover {

	public static void main(String[] args) {
		BufferedReader br = null;
		Multimap<String, String> parentToChildrenMap = HashMultimap.create();
		Set<String> nonRootNodesSet = new HashSet<String>();
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = br.readLine()) != null) {
				//System.err.println("main(): line = " + line);
				CSVParser p = new CSVParser(new StringReader(line));
				String[] line2 = p.getLine();
				String parent = line2[0];
				String child = line2[1];
				parentToChildrenMap.put(parent, child);
				if (nonRootNodesSet.contains(child)) {
					System.err.println("Already has a parent: " + child);
					System.err.println("Removed: " + line);
				} else {
					System.out.println(line);
				}
				nonRootNodesSet.add(child);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void checkForCycles(String root,
			Multimap<String, String> parentToChildrenMap, Set<String> visited) {

		Collection<String> children = parentToChildrenMap.get(root);
		for (String child : children) {
			if (visited.contains(child)) {
				throw new RuntimeException("Child of itself: " + child);
			} else {
				visited.add(child);
			}
			checkForCycles(child, parentToChildrenMap, visited);
		}
	}

	private static Set<TreeNode> getLeafNodes(TreeNode rootNode) {
		Set<TreeNode> leafNodes1 = new HashSet<TreeNode>();
		getLeafNodes(rootNode, leafNodes1);
		return ImmutableSet.copyOf(leafNodes1);
	}

	// TODO: Bad - mutable input
	private static void getLeafNodes(TreeNode rootNode, Set<TreeNode> oLeafNodes) {
		if (rootNode.getChild().size() == 0) {
			oLeafNodes.add(rootNode);
		} else {
			for (TreeNode child : rootNode.getChild()) {
				getLeafNodes(child, oLeafNodes);
			}
		}
	}

	private static TreeNode buildTree(String root, Multimap<String, String> m) {
		Set<TreeNode> childNodes = new HashSet<TreeNode>();
		for (String childNodeData : m.get(root)) {
			System.err.println("buildTree() - " + root);
			TreeNode childNode = buildTree(childNodeData, m);
			childNodes.add(childNode);
		}
		TreeNode n = new TreeNode(root, childNodes);
		return n;
	}

	private static class TreeNode {
		private final String data;

		private final Set<TreeNode> child;
		// TODO: can we make this final?
		private TreeNode parent;

		TreeNode(String data, Set<TreeNode> child) {
			this.data = data;
			this.child = ImmutableSet.copyOf(child);
			for (TreeNode child1 : child) {
				child1.setParent(this);
			}
		}

		public void setParent(TreeNode parent1) {
			if (parent == null) {
				parent = parent1;
			} else {
				throw new RuntimeException(parent.getData());
			}
		}

		public TreeNode getParent() {
			return parent;
		}

		public String getData() {
			return data;
		}

		public Set<TreeNode> getChild() {
			return child;
		}
	}
}

