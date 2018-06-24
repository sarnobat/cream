import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class HeapExportFiles {

	public static void main(String[] args) {
		Multimap<Path, Path> parentToChildren = HashMultimap.create();
		Map<Path, Path> childToParent = new HashMap<Path, Path>();
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new InputStreamReader(System.in));
			while ((line = br.readLine()) != null) {
				// log message
				System.err.println("[DEBUG] current line is: " + line);

				StringReader stringReader = new StringReader(line);
				try {
					String[] r = new CSVParser(stringReader).getLine();
					if (!Paths.get(r[0]).toFile().exists()) {
						System.err.println("[WARN] Does not exist: " + r[0]);
						continue;
					}
					if (!Paths.get(r[1]).toFile().exists()) {
						System.err.println("[WARN] Does not exist: " + r[1]);
						continue;
					}
					if (Paths.get(r[1]).normalize().equals(Paths.get(r[0]).normalize())) {
						System.err.println("[WARN] Cycle, will cause infinite loop: " + line);
						continue;
					}
					parentToChildren.put(Paths.get(r[0]), Paths.get(r[1]));
					childToParent.put(Paths.get(r[1]), Paths.get(r[0]));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
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

		Set<Path> roots = Sets.difference(parentToChildren.keySet(),
				childToParent.keySet());
		Path rootOutputDir = Files.createTempDir().toPath();
		for (Path root : roots) {
			writeTreeToDisk(rootOutputDir, root, parentToChildren);
		}
		System.out.println("File heap written to: "
				+ rootOutputDir.toAbsolutePath());
	}

	private static void writeTreeToDisk(Path parentOutputDir, // /tmp/dir.123456
			Path parentInputFile, // "/e/new/other/1.jpg"
			Multimap<Path, Path> parentToChildren) {
		if (!parentOutputDir.toFile().exists()) {
			System.err
					.println("Developer error - do not call this method unless the parent directory has already been created");
			System.exit(-1);
		}
		// Copy the file to the current output dir (include timestamps if
		// possible)
		// cp "/e/new/other/1.jpg" /tmp/dir.123456
		Path outputfile;
		try {
			outputfile = Paths.get(parentOutputDir.toAbsolutePath().toString()
					+ "/" + parentInputFile.getFileName());
			Files.copy(parentInputFile.toFile(), outputfile.toFile());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		}
		// 1) mkdir "/tmp/dir.123456/1.jpg_/"
		Path parentInputFileDir;
		// Add the current root in a new top level dir
		parentInputFileDir =
		// "/tmp/dir.123456/1.jpg_/"
		Paths.get(parentOutputDir.toAbsolutePath().toString() + "/"
				+ parentInputFile.getFileName().toString() + "_");
		// Paths.get(parentInputFile .toAbsolutePath().toString() + "_"); //
		// "/e/new/other/1.jpg_"
		// Paths.get(parentOutputDir.toAbsolutePath()
		// +"/"+FilenameUtils.getBaseName(parentInputFile.toAbsolutePath().toString()));
		// //parentInputFile.toAbsolutePath().
		new File(parentInputFileDir.toAbsolutePath().toString()).mkdir();
		for (Path child : parentToChildren.get(parentInputFile)) {
			writeTreeToDisk(parentInputFileDir, // "/tmp/dir.123456/1.jpg_/"
					child // "/e/new/other/2.jpg"
					, parentToChildren);
		}

	}
}

