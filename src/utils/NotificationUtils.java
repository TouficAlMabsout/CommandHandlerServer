package utils;

import javax.swing.*;

import commandhandler.CommandHandler;

import java.awt.*;
import java.awt.event.ActionListener;

public class NotificationUtils {

    public static String popNotification(String message) {
        if (message == null || message.isEmpty()) {
        	CommandHandler.logger.info("Message is null or empty");
            return "Message received is null or empty";
        }

        // Use SwingUtilities to ensure code runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a non-modal dialog
                JDialog dialog = new JDialog((JFrame) null, "Notification", false);
                dialog.setLayout(new BorderLayout());
                dialog.setBackground(Color.WHITE); // Set background color

                // Create a label with the message and a larger font
                JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
                messageLabel.setFont(new Font("SansSerif", Font.BOLD, 32)); // Set font size
                messageLabel.setForeground(Color.DARK_GRAY); // Set font color
                dialog.add(messageLabel, BorderLayout.CENTER);

                // Create a panel for the button
                JPanel buttonPanel = new JPanel();
                buttonPanel.setBackground(Color.WHITE); // Set panel background color
                buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

                // Create and style the button
                JButton closeButton = new JButton("OK");
                closeButton.setFont(new Font("SansSerif", Font.BOLD, 16));
                closeButton.setForeground(Color.BLACK);
                closeButton.setBackground(Color.RED); // Set button background color
                closeButton.setOpaque(true); // Make button opaque to show background color
                closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Set border
                closeButton.setPreferredSize(new Dimension(80, 40));
                closeButton.setFocusPainted(false); // Remove focus painting
                // ActionListener to dispose of the dialog
                ActionListener closeAction = e -> dialog.dispose();
                closeButton.addActionListener(closeAction);
                
                buttonPanel.add(closeButton);
                dialog.add(buttonPanel, BorderLayout.SOUTH);
                
                // Pack the dialog to fit the content
                dialog.pack();
                
                // Center the dialog on the screen
                dialog.setLocationRelativeTo(null);
                
                // Default close operation
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                
                dialog.setVisible(true);
                dialog.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
                CommandHandler.logger.info("error occured for displaying the message");
            }
        });
        
        CommandHandler.logger.info("Message displayed");
        return "Message successfully displayed";
    }
}
