import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.*;

public class FilesInPairExistsCheck {

	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = br.readLine()) != null) {

String b =      StringUtils.newStringUtf8(Base64.decodeBase64(line));
    //return Base64.encodeBase64String(StringUtils.getBytesUtf8(s));

				System.out.println(b);
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

