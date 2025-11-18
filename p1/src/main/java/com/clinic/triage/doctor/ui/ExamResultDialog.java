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

public class ExamResultDialog extends JDialog {
    public enum Action {
        COMPLETED,
        REQUEST_TEST
    }

    private final JTextArea noteArea;
    private Action selectedAction;

    public ExamResultDialog(JFrame owner, Patient patient) {
        super(owner, "Cập nhật kết quả khám cho " + patient.getQueueNumber(), true);
        UiUtils.configureLookAndFeel();
        setLayout(new BorderLayout(10, 10));
        add(new JLabel("Nhập kết quả khám / ghi chú cho bệnh nhân: " + patient.getFullName()), BorderLayout.NORTH);

        noteArea = UiUtils.createTextArea();
        add(new JScrollPane(noteArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Hủy");
        cancel.addActionListener(e -> {
            selectedAction = null;
            dispose();
        });
        JButton requestTest = new JButton("Yêu cầu xét nghiệm");
        requestTest.addActionListener(e -> {
            selectedAction = Action.REQUEST_TEST;
            dispose();
        });
        JButton complete = new JButton("Đã khám xong");
        complete.addActionListener(e -> {
            selectedAction = Action.COMPLETED;
            dispose();
        });
        actions.add(cancel);
        actions.add(requestTest);
        actions.add(complete);
        add(actions, BorderLayout.SOUTH);

        setSize(520, 360);
        UiUtils.center(this);
    }

    public Action getSelectedAction() {
        return selectedAction;
    }

    public String getExamNote() {
        String text = noteArea.getText();
        return text == null ? "" : text.trim();
    }
}
