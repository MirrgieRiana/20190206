package resguesser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import mirrg.boron.util.UtilsFile;
import mirrg.boron.util.string.PercentEncoding;
import mirrg.boron.util.struct.Tuple3;
import mirrg.boron.util.suppliterator.ISuppliterator;

public class MainParse
{

	private static Pattern pattern = Pattern.compile("\\A>>(\\d+)");

	public static void main(String[] args) throws Exception
	{
		List<ThreadEntry> threadEntries = new Board("medaka.5ch.net", "otoge").getThreadList();

		Map<String, Integer> map = new HashMap<>();
		Consumer<String> a = key -> map.put(key, map.getOrDefault(key, 0) + 1);

		for (ThreadEntry threadEntry : threadEntries) {
			List<Tuple3<Integer, Integer, byte[]>> thread = new ArrayList<>();

			String html = Util.getHtml(() -> Cacher.open(threadEntry.url));
			Document document = Jsoup.parse(html);

			//System.out.println(document.select("div.thread").html());

			for (Element element : document.select("div.thread").select("div.post")) {
				int number = Integer.parseInt(element.select("span.number").get(0).text());
				String name = element.select("span.name").get(0).text();
				String date = element.select("span.date").get(0).text();
				String uid = element.select("span.uid").get(0).text();

				//System.out.println(element.select("div.message").get(0).html());

				StringBuilder message = new StringBuilder();

				for (Node node : element.select("div.message > span.escaped").get(0).childNodes()) {

					if (node.nodeName().equals("br")) {
						// 改行
						//System.out.println("[BR]");
						a.accept("br");
						message.append("\n");
					} else if (node.nodeName().equals("hr")) {
						// 水平線
						//System.out.println("[BR]");
						a.accept("hr");
						message.append("\n");
					} else if (node.nodeName().equals("img")) {
						// 画像
						//System.out.println("[BR]");
						a.accept("image");
						message.append("\n");
					} else if (node.nodeName().equals("mark")
						&& node.attr("style").startsWith("display: block; width: ")) {
						// マーク
						//System.out.println("[BR]");
						a.accept("mark");
						message.append("\n");
					} else if (node.nodeName().equals("small")
						&& node.attr("style").equals("color: #999;")
						&& node.outerHtml().contains("<br>Rock54: Caution(BBR-MD5:")) {
						// 外部リンク
						a.accept("warning");
						//System.out.println("[" + ((TextNode) node).text() + "]");
						message.append("\n");
					} else if (node instanceof TextNode) {
						// 通常文
						//System.out.println("[" + ((TextNode) node).text() + "]");
						a.accept("text");
						message.append(node.toString().trim());
					} else if (node.nodeName().equals("a")
						&& node.attr("target").equals("_blank")
						&& node.attr("href").startsWith("../test/read.cgi/")) {
						// レスアンカー
						//System.out.println("[" + ((TextNode) node).text() + "]");
						a.accept("res");
						message.append(((TextNode) node.childNode(0)).text());
					} else if (node.nodeName().equals("a")
						&& node.attr("href").startsWith("http://jump.5ch.net/?")
						&& node.attr("target").equals("_blank")) {
						// 外部リンク
						//System.out.println("[" + ((TextNode) node).text() + "]");
						a.accept("exlink");
						message.append("\n");
						message.append(((TextNode) node.childNode(0)).text());
					} else if (node.nodeName().equals("a")
						&& node.childNodeSize() == 1
						&& node.childNode(0) instanceof TextNode
						&& node.attr("href").equals(((TextNode) node.childNode(0)).text().replaceAll("2ch\\.net", "5ch.net"))) {
						// リンク
						//System.out.println("[" + ((TextNode) node).text() + "]");
						message.append(((TextNode) node.childNode(0)).text());
					} else if (node.nodeName().equals("span") && node.attr("class").equals("AA")) {
						// AA
						//System.out.println("[" + ((TextNode) node).text() + "]");
						a.accept("aa");
						message.append(((TextNode) node.childNode(0)).text());
					} else {
						System.out.println(threadEntry.url + "#" + number + " " + node);
						a.accept("other");
					}

				}

				String message2 = message.toString().replaceAll("\n+", "\n");
				int anchor = -1;
				{
					Matcher matcher = pattern.matcher(message2);
					if (matcher.find()) {
						anchor = Integer.parseInt(matcher.group(1));
					}
				}

				//System.out.println(number + " " + threadEntry.url + " " + threadEntry.title);
				//System.out.println(number + " " + anchor + " " + message2);
				//System.out.println(message2);

				thread.add(new Tuple3<>(number, anchor, message2.getBytes("utf8")));
			}

			writeData(threadEntry.url, thread);
		}

		map.entrySet().stream()
			.forEach(e -> System.out.println(e.getKey() + "\t" + e.getValue()));

	}

	private static void writeData(URL url, List<Tuple3<Integer, Integer, byte[]>> thread) throws IOException
	{

		JsonArray jsonThread = new JsonArray();

		for (Tuple3<Integer, Integer, byte[]> response : thread) {
			JsonArray jsonResponse = new JsonArray();

			jsonResponse.add(response.x);
			jsonResponse.add(response.y);
			{
				JsonArray jsonText = new JsonArray();

				if (response.z.length > 0) {
					ISuppliterator.ofByteArray(response.z)
						.forEach(b -> jsonText.add(Byte.toUnsignedInt(b)));
				}

				jsonResponse.add(jsonText);
			}

			jsonThread.add(jsonResponse);
		}

		try (PrintStream out = new PrintStream(UtilsFile.getOutputStreamWithMkdirs(new File("json", PercentEncoding.encode(url.toString()) + ".json")))) {
			out.println(new GsonBuilder()
				.setPrettyPrinting()
				.create().toJson(jsonThread));
		}

	}

}
