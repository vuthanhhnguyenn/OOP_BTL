package com.clinic.triage.doctor.ui;

import com.clinic.triage.model.Patient;
import com.clinic.triage.util.UiUtils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class PrescriptionDialog extends JDialog {
    private final Patient patient;
    private JTextArea contentArea;
    private String prescriptionContent;

    public PrescriptionDialog(JFrame owner, Patient patient) {
        super(owner, "Kê đơn thuốc cho " + patient.getQueueNumber(), true);
        this.patient = patient;
        UiUtils.configureLookAndFeel();
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        add(new JLabel("Bệnh nhân: " + patient.getFullName()), BorderLayout.NORTH);
        contentArea = UiUtils.createTextArea();
        add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Hủy");
        cancel.addActionListener(e -> {
            prescriptionContent = null;
            dispose();
        });
        JButton save = new JButton("Lưu đơn thuốc");
        save.addActionListener(e -> {
            prescriptionContent = contentArea.getText();
            dispose();
        });
        actions.add(cancel);
        actions.add(save);
        add(actions, BorderLayout.SOUTH);

        setSize(500, 400);
        UiUtils.center(this);
    }

    public String getPrescriptionContent() {
        return prescriptionContent;
    }
}
