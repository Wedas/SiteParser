package items.parser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MySQLParser extends Parser {
	private Properties			properties;
	private Connection			connection;
	private String				tableName	= "catalogue";
	private PreparedStatement	preparedSt;
	private int					rowNum		= 0;

	public MySQLParser(Properties dbProperties) {
		properties = dbProperties;
	}

	public void processURL() {
		try {
			if (url == null)
				return;

			connection = DriverManager.getConnection(
					"jdbc:mysql://" + properties.getProperty("Host") + ":" + properties.getProperty("Port")
							+ "?autoReconnect=true&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC",
					properties.getProperty("User"), properties.getProperty("Password"));

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("create database " + properties.getProperty("Database name"));
			StringBuffer sql = new StringBuffer(
					"create table " + properties.getProperty("Database name") + "." + tableName + " (id int not null");
			for (String[] quality : qualityList) {
				sql.append(", " + quality[0] + " text");
			}
			sql.append(", primary key (id))");
			stmt.executeUpdate(sql.toString());

			processPage(url);
			log("Items processed: " + itemPages.size());
			stmt.close();
			connection.close();
		} catch (Exception e) {
			log(e.getMessage() + "\n");
		}

	}

	private void processPage(String url) throws IOException, SQLException {
		pages.add(url);
		Document docCatalogue = Jsoup.connect(url).get();
		Elements catElements = docCatalogue.getAllElements();
		for (Element catEl : catElements) {
			if (catEl.hasClass(nextPage[0].getText())) {

				Elements catEls = catEl.getAllElements();
				for (Element catEelement : catEls) {

					if (catEelement.hasAttr("href")) {

						if (!itemPages.contains(catEelement.attr("abs:" + "href"))) {
							String itemPage = catEelement.attr("abs:" + "href");
							itemPages.add(itemPage);
							log("Processing item: " + itemPage + "\n");
							Document doc = Jsoup.connect(itemPage).get();
							Elements elements = doc.getAllElements();
							rowNum++;
							Statement statement = connection.createStatement();
							statement.executeUpdate("insert into " + properties.getProperty("Database name") + "."
									+ tableName + " (id) values (" + rowNum + ")");

							for (Element el : elements) {
								for (int i = 0; i < qualityList.size(); i++) {
									String[] qualitySet = qualityList.get(i);
									if (el.hasClass(qualitySet[2])) {
										Elements els = el.getAllElements();
										for (Element element : els) {
											if (element.tagName().equals(qualitySet[1])) {
												String prepSQL = String.format("update "
														+ properties.getProperty("Database name") + "." + tableName
														+ " set %s = concat(ifnull(%s, ''), ?)" + " where id=?",
														qualitySet[0], qualitySet[0]);

												preparedSt = connection.prepareStatement(prepSQL);
												preparedSt.setString(1, element.text());
												preparedSt.setInt(2, rowNum);
												preparedSt.executeUpdate();
												break;

											}
										}
									}
								}
							}

						}
						break;
					}

				}
			}
			if (catEl.hasClass(nextPage[1].getText())) {
				Elements els = catEl.getAllElements();
				for (Element element : els) {
					if (element.hasAttr("href") && (!pages.contains(element.attr("abs:" + "href")))) {
						processPage(element.attr("abs:" + "href"));
						break;
					}

				}
			}
		}

	}
}
