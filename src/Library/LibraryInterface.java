
package Library;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import Agents.LibraryAdmin;
import javax.swing.JTextField;

public class LibraryInterface extends JFrame {

   
    private LibraryAdmin myAgent;
    private JTextField Appointmentype, token_Numb, token_option, token_issue_date;

    public LibraryInterface(LibraryAdmin a) {
        super(a.getLocalName());
        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(4, 4));
        p.add(new JLabel("Book name : "));
        Appointmentype = new JTextField(15);
        p.add(Appointmentype);

        p.add(new JLabel("Book version : "));
        token_Numb = new JTextField(15);
        p.add(token_Numb);

        p.add(new JLabel("Token Created Date: "));
        token_issue_date = new JTextField(15);
        p.add(token_issue_date);

        p.add(new JLabel("Immediate possible (need book author's name): "));
        token_option = new JTextField(15);
        p.add(token_option);

        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Booking");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String appointment_Type = Appointmentype.getText().trim();
                    String token_Number = token_Numb.getText().trim();
                    String token_optin_value = token_option.getText().trim();
                    String date = token_issue_date.getText().trim();

                    myAgent.updateRecBook(appointment_Type, Integer.parseInt(token_Number), token_optin_value, date);
                    Appointmentype.setText("");
                    token_Numb.setText("");
                    token_option.setText("");
                    token_issue_date.setText("");
                    JOptionPane.showMessageDialog(LibraryInterface.this, "[*] Add Appointment Type for booking!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LibraryInterface.this, "Invalid value " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

}
