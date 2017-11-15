package items.parser;

import java.io.File;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExcelParser extends Parser {
	
	private Row				excelRow;
	private Cell			excelCell;
	private HSSFWorkbook	excelFile;
	private HSSFSheet		excelSheet;
	private int				rowNum		= 0;
	private File			file;

	public ExcelParser(File file) {
		this.file = file;
	}

	@Override
	public void processURL() {
		if (url == null)
			return;

		excelFile = new HSSFWorkbook();
		excelSheet = excelFile.createSheet("Catalogue");
		excelRow = excelSheet.createRow(rowNum);

		for (int i = 0; i < qualityNumber; i++) {

			excelCell = excelRow.createCell(i);
			excelCell.setCellValue(qualityList.get(i)[0]);

		}

		try {
			processPage(url);
			excelFile.write(file);
			log("Items processed: "+itemPages.size());
		} catch (Exception e) {
			log(e.getMessage() + "\n");
		}

	}

	private void processPage(String url) throws IOException {
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
							excelRow = excelSheet.createRow(rowNum);

							for (Element el : elements) {
								for (int i = 0; i < qualityList.size(); i++) {
									String[] qualitySet = qualityList.get(i);
									if (el.hasClass(qualitySet[2])) {
										Elements els = el.getAllElements();
										for (Element element : els) {
											if (element.tagName().equals(qualitySet[1])) {
												if (excelRow.getCell(i) == null)
													excelCell = excelRow.createCell(i);
												excelCell.setCellValue(excelCell.getStringCellValue() + element.text());												
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
