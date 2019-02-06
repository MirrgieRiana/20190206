package resguesser;

import java.net.URL;

public final class ThreadEntry
{

	public final String location;
	public final URL url;
	public final int index;
	public final String title;
	public final int responseCount;

	public ThreadEntry(String location, URL url, int index, String title, int responseCount)
	{
		this.location = location;
		this.url = url;
		this.index = index;
		this.title = title;
		this.responseCount = responseCount;
	}

	@Override
	public String toString()
	{
		return String.format("%s %4d %4d %s",
			location,
			index,
			responseCount,
			title);
	}

}
