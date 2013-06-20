
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPasswordField;

public class ManagerLoginGUI extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtYoutubeUsername;
	private LoginCheck loginCheck;
	private JButton loginButton;
	private JLabel lblErrorMessages;
	private JPasswordField pwdPassword;
	private String username;
	private String password;

	/**
	 * Create the dialog.
	 */
	public ManagerLoginGUI() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		loginCheck = new LoginCheck();

		setBounds(100, 100, 387, 198);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, 1.0, 1.0,
				Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblYoutubeUsername = new JLabel("YouTube Username:");
			GridBagConstraints gbc_lblYoutubeUsername = new GridBagConstraints();
			gbc_lblYoutubeUsername.insets = new Insets(0, 0, 5, 5);
			gbc_lblYoutubeUsername.anchor = GridBagConstraints.EAST;
			gbc_lblYoutubeUsername.gridx = 0;
			gbc_lblYoutubeUsername.gridy = 0;
			contentPanel.add(lblYoutubeUsername, gbc_lblYoutubeUsername);
		}
		{
			txtYoutubeUsername = new JTextField();
			txtYoutubeUsername.setText("YouTube Username");
			GridBagConstraints gbc_txtYoutubeUsername = new GridBagConstraints();
			gbc_txtYoutubeUsername.insets = new Insets(0, 0, 5, 0);
			gbc_txtYoutubeUsername.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtYoutubeUsername.gridx = 1;
			gbc_txtYoutubeUsername.gridy = 0;
			contentPanel.add(txtYoutubeUsername, gbc_txtYoutubeUsername);
			txtYoutubeUsername.setColumns(10);
		}
		{
			JLabel lblYoutubePassword = new JLabel("YouTube Password:");
			GridBagConstraints gbc_lblYoutubePassword = new GridBagConstraints();
			gbc_lblYoutubePassword.anchor = GridBagConstraints.EAST;
			gbc_lblYoutubePassword.insets = new Insets(0, 0, 5, 5);
			gbc_lblYoutubePassword.gridx = 0;
			gbc_lblYoutubePassword.gridy = 1;
			contentPanel.add(lblYoutubePassword, gbc_lblYoutubePassword);
		}
		{
			lblErrorMessages = new JLabel("Error Messages");
			lblErrorMessages.setVisible(false);
			{
				pwdPassword = new JPasswordField();
				pwdPassword.setText("Password");
				GridBagConstraints gbc_pwdPassword = new GridBagConstraints();
				gbc_pwdPassword.insets = new Insets(0, 0, 5, 0);
				gbc_pwdPassword.fill = GridBagConstraints.HORIZONTAL;
				gbc_pwdPassword.gridx = 1;
				gbc_pwdPassword.gridy = 1;
				contentPanel.add(pwdPassword, gbc_pwdPassword);
			}
			GridBagConstraints gbc_lblErrorMessages = new GridBagConstraints();
			gbc_lblErrorMessages.gridwidth = 2;
			gbc_lblErrorMessages.gridx = 0;
			gbc_lblErrorMessages.gridy = 2;
			contentPanel.add(lblErrorMessages, gbc_lblErrorMessages);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				loginButton = new JButton("Login");
				loginButton.setActionCommand("OK");
				loginButton.addActionListener(new ButtonListener());
				buttonPane.add(loginButton);
				getRootPane().setDefaultButton(loginButton);
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource() == loginButton) {
				lblErrorMessages.setText("Checking Login.");
				lblErrorMessages.repaint();

				boolean success = loginCheck.checkLogin(
						txtYoutubeUsername.getText(),
						String.valueOf(pwdPassword.getPassword()));
				if (!success) {
					lblErrorMessages.setText("Invalid Login.");
					lblErrorMessages.repaint();
					lblErrorMessages.setVisible(true);
				} else {
					lblErrorMessages.setText("Login Success.");
					lblErrorMessages.repaint();
					username = txtYoutubeUsername.getText();
					password = String.valueOf(pwdPassword.getPassword());
					ManagerLoginGUI.this.setVisible(false);
					ManagerLoginGUI.this.setModal(false);
				}
			}
		}
	}
}
