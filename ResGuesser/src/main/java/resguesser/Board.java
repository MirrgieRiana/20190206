package resguesser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Board
{

	private final String domain;
	private final String boardName;

	public Board(String domain, String boardName) throws MalformedURLException
	{
		this.domain = domain;
		this.boardName = boardName;
	}

	public List<ThreadEntry> getThreadList() throws MalformedURLException, IOException
	{
		URL url = new URL("https://" + domain + "/" + boardName + "/subback.html");
		return parseThreadList(Util.getHtml(() -> Cacher.open(url)));
	}

	private static Pattern patternThreadEntry = Pattern.compile("<a href=\"([0-9a-zA-Z/]*)/l50\">(\\d*): (.*?) \\((\\d*)\\)</a>");

	private List<ThreadEntry> parseThreadList(String html) throws NumberFormatException, MalformedURLException
	{
		List<ThreadEntry> threadEntries = new ArrayList<>();

		Matcher matcher = patternThreadEntry.matcher(html);
		while (matcher.find()) {
			threadEntries.add(new ThreadEntry(
				matcher.group(1),
				new URL("https://" + domain + "/test/read.cgi/" + boardName + "/" + matcher.group(1)),
				Integer.parseInt(matcher.group(2)),
				matcher.group(3),
				Integer.parseInt(matcher.group(4))));
		}

		return threadEntries;
	}

}
