package com.clinic.triage.doctor.ui;

import com.clinic.triage.model.Doctor;
import com.clinic.triage.service.AuthService;
import com.clinic.triage.util.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;

public class DoctorLoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public DoctorLoginFrame() {
        super("Cổng bác sĩ - Đăng nhập");
        UiUtils.configureLookAndFeel();
        buildUi();
    }

    private void buildUi() {
    setLayout(new BorderLayout());
    JPanel content = new JPanel(new BorderLayout());
    content.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

    JLabel title = new JLabel("Đăng nhập cổng bác sĩ", SwingConstants.CENTER);
    title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
    content.add(title, BorderLayout.NORTH);

    JPanel formWrapper = new JPanel(new BorderLayout());
    formWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)));

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);       
    gbc.anchor = GridBagConstraints.WEST;


    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;                              
    gbc.fill = GridBagConstraints.NONE;
    form.add(new JLabel("Tài khoản"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;                             
    gbc.fill = GridBagConstraints.HORIZONTAL;
    usernameField = new JTextField(22);
    usernameField.setPreferredSize(new java.awt.Dimension(260, 32));
    form.add(usernameField, gbc);

   
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    form.add(new JLabel("Mật khẩu"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    passwordField = new JPasswordField(22);
    passwordField.setPreferredSize(new java.awt.Dimension(260, 32));
    passwordField.addActionListener(e -> doLogin());
    form.add(passwordField, gbc);

    
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;                            
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;

    loginButton = new JButton("Đăng nhập");
    loginButton.setPreferredSize(new java.awt.Dimension(160, 36));
    loginButton.addActionListener(e -> doLogin());
    form.add(loginButton, gbc);

    formWrapper.add(form, BorderLayout.CENTER);
    content.add(formWrapper, BorderLayout.CENTER);

    JLabel helper = new JLabel(
            "Nhập tài khoản được phòng CNTT cấp để vào bảng điều khiển.",
            SwingConstants.CENTER
    );
    helper.setForeground(new Color(90, 90, 90));
    helper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
    content.add(helper, BorderLayout.SOUTH);

    add(content, BorderLayout.CENTER);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    pack();
    setMinimumSize(new java.awt.Dimension(480, 280));
    UiUtils.center(this);
    usernameField.requestFocusInWindow();
}


    private void doLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        setControlsEnabled(false);
        try {
            Optional<Doctor> doctor = authService.login(username, password);
            if (doctor.isEmpty()) {
                UiUtils.showError(this, "Sai tài khoản hoặc mật khẩu");
                return;
            }
            DoctorDashboardFrame dashboardFrame = new DoctorDashboardFrame(doctor.get());
            dashboardFrame.setVisible(true);
            dispose();
        } finally {
            setControlsEnabled(true);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        if (loginButton != null) {
            loginButton.setEnabled(enabled);
        }
    }

    public static void main(String[] args) {
        UiUtils.runOnUiThread(() -> new DoctorLoginFrame().setVisible(true));
    }
}
