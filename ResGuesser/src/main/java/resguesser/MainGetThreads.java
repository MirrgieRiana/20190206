package resguesser;

import java.util.List;

public class MainGetThreads
{

	public static void main(String[] args) throws Exception
	{
		List<ThreadEntry> threadEntries = new Board("medaka.5ch.net", "otoge").getThreadList();

		for (ThreadEntry threadEntry : threadEntries) {

			System.out.println("Start:  " + threadEntry.url);
			Util.getHtml(() -> Cacher.open(threadEntry.url));
			System.out.println("Finish: " + threadEntry.url);

		}

	}

}
