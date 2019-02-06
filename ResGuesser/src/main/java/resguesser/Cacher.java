package resguesser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import mirrg.boron.util.UtilsFile;
import mirrg.boron.util.string.PercentEncoding;

public class Cacher
{

	private static final File dirCache = new File("cache");

	public static InputStream open(URL url) throws IOException
	{
		File file = getCacheFile(url);
		if (!file.exists()) {
			try (InputStream in = pull(url);
				OutputStream out = UtilsFile.getOutputStreamWithMkdirs(file)) {
				byte[] buffer = new byte[4096];
				while (true) {
					int length = in.read(buffer);
					if (length == -1) break;
					out.write(buffer, 0, length);
				}
			}
		}
		return new FileInputStream(file);
	}

	private static File getCacheFile(URL url)
	{
		return new File(dirCache, PercentEncoding.encode(url.toString()) + ".html");
	}

	private static InputStream pull(URL url) throws IOException
	{
		System.err.println("Request: " + url); // TODO
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} // TODO

		return new OkHttpClient().newCall(new Request.Builder()
			.url(url)
			.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0")
			.get()
			.build()).execute().body().byteStream();
	}

}
