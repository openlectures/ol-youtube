

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ManagerGUI {

	private JFrame frmOlyoutube;
	private JTextField txtFeedUrl;
	private ManagerLoginGUI managerLoginGui;

	private String username;
	private String password;
	private JButton btnSynchronizePlaylists;
	private JButton btnCheckFeedUrl;
	private JButton btnSynchronizeDescriptions;
	private JButton btnSynchronizeTitles;
	private JProgressBar progressBarPlaylist;
	private JPanel panel_2;
	private JProgressBar progressBarTitle;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ManagerGUI window = new ManagerGUI();
					window.frmOlyoutube.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 */
	public ManagerGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		managerLoginGui = new ManagerLoginGUI();
		managerLoginGui.setVisible(true);
		managerLoginGui.setModal(true);
		managerLoginGui.setLocationRelativeTo(null);

		username = managerLoginGui.getUsername();
		password = managerLoginGui.getPassword();

		frmOlyoutube = new JFrame();
		frmOlyoutube.setPreferredSize(new Dimension(800, 640));
		frmOlyoutube.setTitle("ol-youtube");
		frmOlyoutube.setBounds(100, 100, 800, 631);
		frmOlyoutube.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		frmOlyoutube.getContentPane().setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		frmOlyoutube.getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblWebApiParser = new JLabel("Web API Parser");
		GridBagConstraints gbc_lblWebApiParser = new GridBagConstraints();
		gbc_lblWebApiParser.gridwidth = 2;
		gbc_lblWebApiParser.insets = new Insets(0, 0, 5, 0);
		gbc_lblWebApiParser.gridx = 0;
		gbc_lblWebApiParser.gridy = 0;
		panel.add(lblWebApiParser, gbc_lblWebApiParser);

		JLabel lblYoloApiFeed = new JLabel("yOLo API Feed URL:");
		GridBagConstraints gbc_lblYoloApiFeed = new GridBagConstraints();
		gbc_lblYoloApiFeed.insets = new Insets(0, 0, 5, 5);
		gbc_lblYoloApiFeed.gridx = 0;
		gbc_lblYoloApiFeed.gridy = 1;
		panel.add(lblYoloApiFeed, gbc_lblYoloApiFeed);

		txtFeedUrl = new JTextField();
		GridBagConstraints gbc_txtFeedUrl = new GridBagConstraints();
		gbc_txtFeedUrl.insets = new Insets(0, 0, 5, 0);
		gbc_txtFeedUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFeedUrl.gridx = 1;
		gbc_txtFeedUrl.gridy = 1;
		panel.add(txtFeedUrl, gbc_txtFeedUrl);
		txtFeedUrl.setText("Feed URL");
		txtFeedUrl.setColumns(10);

		btnCheckFeedUrl = new JButton("Check Feed URL");
		GridBagConstraints gbc_btnCheckFeedUrl = new GridBagConstraints();
		gbc_btnCheckFeedUrl.gridwidth = 2;
		gbc_btnCheckFeedUrl.gridx = 0;
		gbc_btnCheckFeedUrl.gridy = 2;
		panel.add(btnCheckFeedUrl, gbc_btnCheckFeedUrl);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 1;
		frmOlyoutube.getContentPane().add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JLabel lblTitleSynchronization = new JLabel("Title Synchronization");
		GridBagConstraints gbc_lblTitleSynchronization = new GridBagConstraints();
		gbc_lblTitleSynchronization.insets = new Insets(0, 0, 5, 0);
		gbc_lblTitleSynchronization.gridx = 0;
		gbc_lblTitleSynchronization.gridy = 0;
		panel_1.add(lblTitleSynchronization, gbc_lblTitleSynchronization);

		progressBarTitle = new JProgressBar();
		GridBagConstraints gbc_progressBarTitle = new GridBagConstraints();
		gbc_progressBarTitle.insets = new Insets(0, 0, 5, 0);
		gbc_progressBarTitle.gridx = 0;
		gbc_progressBarTitle.gridy = 1;
		panel_1.add(progressBarTitle, gbc_progressBarTitle);

		btnSynchronizeTitles = new JButton("Synchronize Titles");
		GridBagConstraints gbc_btnSynchronizeTitles = new GridBagConstraints();
		gbc_btnSynchronizeTitles.gridx = 0;
		gbc_btnSynchronizeTitles.gridy = 2;
		panel_1.add(btnSynchronizeTitles, gbc_btnSynchronizeTitles);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.gridwidth = 3;
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 2;
		frmOlyoutube.getContentPane().add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 0, 0 };
		gbl_panel_4.rowHeights = new int[] { 0, 0 };
		gbl_panel_4.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_4.add(scrollPane, gbc_scrollPane);

		JTextArea txtrConsole = new JTextArea();
		txtrConsole.setText("Console");
		scrollPane.setViewportView(txtrConsole);

		panel_2 = new JPanel();
		panel_2.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 2;
		gbc_panel_2.gridy = 1;
		frmOlyoutube.getContentPane().add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel lblDescriptionSynchronization = new JLabel(
				"Description Synchronization");
		GridBagConstraints gbc_lblDescriptionSynchronization = new GridBagConstraints();
		gbc_lblDescriptionSynchronization.insets = new Insets(0, 0, 5, 0);
		gbc_lblDescriptionSynchronization.gridx = 0;
		gbc_lblDescriptionSynchronization.gridy = 0;
		panel_2.add(lblDescriptionSynchronization,
				gbc_lblDescriptionSynchronization);

		JProgressBar progressBarDescription = new JProgressBar();
		GridBagConstraints gbc_progressBarDescription = new GridBagConstraints();
		gbc_progressBarDescription.fill = GridBagConstraints.VERTICAL;
		gbc_progressBarDescription.insets = new Insets(0, 0, 5, 0);
		gbc_progressBarDescription.gridx = 0;
		gbc_progressBarDescription.gridy = 1;
		panel_2.add(progressBarDescription, gbc_progressBarDescription);

		btnSynchronizeDescriptions = new JButton("Synchronize Descriptions");
		GridBagConstraints gbc_btnSynchronizeDescriptions = new GridBagConstraints();
		gbc_btnSynchronizeDescriptions.gridx = 0;
		gbc_btnSynchronizeDescriptions.gridy = 2;
		panel_2.add(btnSynchronizeDescriptions, gbc_btnSynchronizeDescriptions);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 1;
		frmOlyoutube.getContentPane().add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 0, 0 };
		gbl_panel_3.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_3.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		JLabel lblPlaylistSynchronization = new JLabel(
				"Playlist Synchronization");
		GridBagConstraints gbc_lblPlaylistSynchronization = new GridBagConstraints();
		gbc_lblPlaylistSynchronization.insets = new Insets(0, 0, 5, 0);
		gbc_lblPlaylistSynchronization.gridx = 0;
		gbc_lblPlaylistSynchronization.gridy = 0;
		panel_3.add(lblPlaylistSynchronization, gbc_lblPlaylistSynchronization);

		progressBarPlaylist = new JProgressBar();
		GridBagConstraints gbc_progressBarPlaylist = new GridBagConstraints();
		gbc_progressBarPlaylist.insets = new Insets(0, 0, 5, 0);
		gbc_progressBarPlaylist.gridx = 0;
		gbc_progressBarPlaylist.gridy = 1;
		panel_3.add(progressBarPlaylist, gbc_progressBarPlaylist);

		btnSynchronizePlaylists = new JButton("Synchronize Playlists");
		GridBagConstraints gbc_btnSynchronizePlaylists = new GridBagConstraints();
		gbc_btnSynchronizePlaylists.gridx = 0;
		gbc_btnSynchronizePlaylists.gridy = 2;
		panel_3.add(btnSynchronizePlaylists, gbc_btnSynchronizePlaylists);

		frmOlyoutube.setVisible(true);
		btnCheckFeedUrl.addActionListener(new ButtonListener());
	}

	public class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource() == btnCheckFeedUrl) {
				try {
					checkFeed();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void checkFeed() throws InterruptedException,
				ExecutionException {
			SwingWorker<Integer, Void> worker = new CheckFeedWorker(
					txtFeedUrl.getText());
			worker.execute();
			System.out.println("checkFeed(): " + worker.get()
					+ " Checkpoints found.");
		}

	}

	public class CheckFeedWorker extends SwingWorker<Integer, Void> {

		private WebAPIParser webApiParser;
		private String apiUrl;
		public String TEST_ATTRIBUTE = "title";

		public CheckFeedWorker(String apiUrl) {
			this.apiUrl = apiUrl;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			webApiParser = new WebAPIParser(apiUrl);
			String json = webApiParser.getJson(TEST_ATTRIBUTE);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Type collectionType = new TypeToken<Collection<GsonCheckpoint>>() {
			}.getType();

			ArrayList<GsonCheckpoint> gsonCheckpoints = gson.fromJson(json,
					collectionType);

			return gsonCheckpoints.size();
		}

		public class GsonCheckpoint {
			private String attribute;
			private String value;
			private String key;
		}
	}
}
