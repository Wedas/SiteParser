package items.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.JTextField;

public abstract class Parser {
	protected List<String[]>	qualityList	= new ArrayList<>();
	protected List<String>		pages		= new ArrayList<>();
	protected Set<String>		itemPages	= new HashSet<>();
	protected JTextArea			infoArea;
	public String				url;
	protected JTextField[]		nextPage;
	protected int				qualityNumber;

	public void setQualities(List<JTextField[]> qualityFieldList) {
		for (JTextField[] textArray : qualityFieldList) {
			String[] qualityArray = new String[textArray.length];
			for (int i = 0; i < textArray.length; i++) {
				qualityArray[i] = textArray[i].getText();
			}
			qualityList.add(qualityArray);
		}
		qualityNumber = qualityList.size();

	}

	public void setLog(JTextArea infoArea) {
		this.infoArea = infoArea;
	}

	public void setURL(String url) {
		this.url = url;

	}

	public void log(String message) {
		infoArea.append(message + "\n");
		infoArea.setCaretPosition(infoArea.getDocument().getLength());
	}

	public abstract void processURL();

	public void setNextPage(JTextField[] nextPage) {
		this.nextPage = nextPage;

	}

}
