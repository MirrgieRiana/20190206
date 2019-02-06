package resguesser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.mozilla.universalchardet.UniversalDetector;

import mirrg.boron.util.suppliterator.ISuppliterator;

public class Util
{

	public static String getCharset(IInputStreamSupplier sIn) throws IOException
	{
		UniversalDetector detector = new UniversalDetector(null);

		try (InputStream in = sIn.get()) {
			byte[] buffer = new byte[4096];
			while (true) {
				int length = in.read(buffer);
				if (length == -1) break;
				detector.handleData(buffer, 0, length);
			}
		}
		detector.dataEnd();

		String charset = detector.getDetectedCharset();
		return charset == null ? "utf8" : charset;
	}

	public static String getHtml(IInputStreamSupplier sIn) throws IOException
	{
		return getHtml(sIn, getCharset(sIn));
	}

	public static String getHtml(IInputStreamSupplier sIn, String charset) throws IOException
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(sIn.get(), charset))) {
			return ISuppliterator.ofStream(in.lines()).join("\n");
		}
	}

	public static interface IInputStreamSupplier
	{

		public InputStream get() throws IOException;

	}

}
