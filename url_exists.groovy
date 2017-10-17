import javax.naming.*;
import java.net.*;


public class DNSLookup
{
    public static void main(String[] args)
    {
		BufferedReader br = null;
		try {
		  br = new BufferedReader(new InputStreamReader(System.in));
		  String line;
		  while ((line = br.readLine()) != null) {
		  
		  	try {
			final URL url = new URL(line);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			int responseCode = huc.getResponseCode();

			if (responseCode > 299) {
				System.err.println(responseCode);
			} else {
				if (huc.getContentLength() < 50) {
	                                System.err.println("[ERROR] Too small response: " + line);
				} else {
					System.out.println(line);
					continue;
				}
			}
			} catch (Exception e) {
			}
			System.err.println(line);			
			System.out.println("[Not found] " + line);
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
