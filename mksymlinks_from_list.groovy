import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MkSymlinksFromList {

	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			int i = 1;
			while ((line = br.readLine()) != null) {
				// log message
				Path p = Paths.get(line);

				String s = "ln -s \"" + line + "\" \"" + "./" + String.format("%03d", i) + "__" + p.getFileName().toString() + "\"";

				// program output
				System.out.println(s);
				i++;
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