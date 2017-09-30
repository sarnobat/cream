import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVParser;

public class FilesInPairExistsCheck {

	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = br.readLine()) != null) {

				String prefix = "";
				String[] r = new CSVParser(new StringReader(line)).getLine();
				if (!Paths.get(r[0]).toFile().exists()) {
					System.err.println("[ERROR] Does not exist: " + r[0]);
					prefix = "BROKEN: ";
				}
				if (!Paths.get(r[1]).toFile().exists()) {
					System.err.println("[ERROR] Does not exist: " + r[1]);
					prefix = "BROKEN: ";
				}

				System.out.println(prefix + line);
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
}

