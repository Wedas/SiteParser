package items.parser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class CatalogueParser extends JFrame {

	private Box					qualityBox;
	private List<JTextField[]>	qualityList	= new ArrayList<>();
	private JTextField[]		nextPage	= new JTextField[2];
	private File				fileXLS;
	private JTextArea			infoArea;
	private Properties			dbProperties;

	public CatalogueParser() {

		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel rightPanel = new JPanel(new BorderLayout());

		qualityBox = new Box(BoxLayout.Y_AXIS);
		addFirstQuality();

		JScrollPane rightPane = new JScrollPane(qualityBox);
		rightPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rightPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		rightPane.setPreferredSize(new Dimension(400, 200));
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());

		final JLabel url = new JLabel("URL:");
		final JTextField urlField = new JTextField(20);
		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(url);
		box.add(urlField);
		leftPanel.add(box, BorderLayout.NORTH);
		leftPanel.setPreferredSize(new Dimension(300, 250));
		// leftPanel.add(url);
		// leftPanel.add(urlField);
		add(leftPanel, BorderLayout.WEST);

		rightPanel.add(rightPane, BorderLayout.NORTH);
		rightPanel.setPreferredSize(new Dimension(400, 250));
		add(rightPanel, BorderLayout.EAST);

		JButton startButton = new JButton("Start");

		infoArea = new JTextArea();
		infoArea.setLineWrap(true);
		bottomPanel.setPreferredSize(new Dimension(700, 250));
		JScrollPane textScroll = new JScrollPane(infoArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		infoArea.setEditable(false);
		bottomPanel.add(textScroll);
		add(bottomPanel, BorderLayout.SOUTH);

		JLabel output = new JLabel("Output to:");
		final JRadioButton excelButton = new JRadioButton("Excel", false);
		final JRadioButton mysqlButton = new JRadioButton("MySQL", false);
		final ButtonGroup outputButtons = new ButtonGroup();
		outputButtons.add(excelButton);
		outputButtons.add(mysqlButton);
		excelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser fileChooser = new JFileChooser();
				int result = fileChooser.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					String fileName = fileChooser.getSelectedFile().getAbsolutePath();
					if (!fileName.endsWith(".xls"))
						fileName += ".xls";
					fileXLS = new File(fileName);
					infoArea.append("Output to: " + fileXLS.getAbsolutePath() + "\n");

				} else {
					outputButtons.clearSelection();
				}

			}
		});

		mysqlButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String host = "Host";
				String port = "Port";
				String userName = "User";
				String password = "Password";
				String dbName = "Database name";

				JTextField hostField = new JTextField();
				JTextField portField = new JTextField();
				JTextField userField = new JTextField();
				JPasswordField passField = new JPasswordField();
				JTextField dbField = new JTextField();

				Object[] dialog = { host + ":", hostField, port + ":", portField, userName + ":", userField,
						password + ":", passField, dbName+":", dbField };
				int result = JOptionPane.showConfirmDialog(null, dialog, "Database parameters",
						JOptionPane.OK_CANCEL_OPTION);

				if (result == JOptionPane.OK_OPTION) {
					dbProperties = new Properties();
					dbProperties.setProperty(host, hostField.getText());
					dbProperties.setProperty(port, portField.getText());
					dbProperties.setProperty(userName, userField.getText());
					if (passField.getPassword() == null || passField.getPassword().length == 0)
						dbProperties.setProperty(password, "");
					else
						dbProperties.setProperty(password, String.valueOf(passField.getPassword()));
					dbProperties.setProperty(dbName, dbField.getText());
				} else
					outputButtons.clearSelection();
			}
		});

		box.add(output);
		box.add(excelButton);
		box.add(mysqlButton);

		JButton addQualityButton = new JButton("Add column");
		addQualityButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CatalogueParser.this.addQuality();

			}

		});
		box.add(addQualityButton);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(startButton);
		leftPanel.add(buttonPanel, BorderLayout.SOUTH);

		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						if (!(excelButton.isSelected() || mysqlButton.isSelected()) || urlField.getText().equals("")) {
							infoArea.append("Please specify the URL and Output\n");
							return;
						}

						Parser parser = null;
						if (excelButton.isSelected()) {
							parser = new ExcelParser(fileXLS);
						} else
							parser = new MySQLParser(dbProperties);

						parser.setQualities(qualityList);
						parser.setNextPage(nextPage);
						parser.setLog(infoArea);
						parser.setURL(urlField.getText());
						parser.processURL();

					}
				});
				thread.start();
			}
		});

		setVisible(true);
		setTitle("CatalogueParser");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));
		pack();

	}

	private void addFirstQuality() {

		JTextField itemClass = new JTextField("item");
		JTextField nextPageClass = new JTextField("navigation-top");

		nextPage[0] = itemClass;
		nextPage[1] = nextPageClass;
		JPanel nextPanel = new JPanel();
		nextPanel.add(new JLabel("Item class"));
		nextPanel.add(itemClass);
		nextPanel.add(new JLabel("Next page class"));
		nextPanel.add(nextPageClass);
		qualityBox.add(nextPanel);

	}

	private void addQuality() {

		String columnName = "Column Name";
		String tag = "Tag";
		String className = "Class";

		JTextField qualityField = new JTextField();
		JTextField qualityTagField = new JTextField();
		JTextField qualityClassField = new JTextField();

		Object[] dialog = { columnName + ":", qualityField, tag + ":", qualityTagField, className + ":",
				qualityClassField };
		int result = JOptionPane.showConfirmDialog(null, dialog, "Add column", JOptionPane.OK_CANCEL_OPTION);

		if (result == JOptionPane.OK_OPTION) {

			final JTextField[] columnData = { qualityField, qualityTagField, qualityClassField };
			final JPanel panel = new JPanel();
			panel.add(new JLabel(columnName));
			panel.add(qualityField);
			panel.add(new JLabel(tag));
			panel.add(qualityTagField);
			panel.add(new JLabel(className));
			panel.add(qualityClassField);

			JButton removeButton = new JButton("x");
			removeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					qualityBox.remove(panel);
					qualityList.remove(columnData);
					qualityBox.revalidate();
					qualityBox.repaint();

				}
			});
			panel.add(removeButton);

			qualityBox.add(panel);
			pack();
			qualityList.add(columnData);
		}

	}	
}
