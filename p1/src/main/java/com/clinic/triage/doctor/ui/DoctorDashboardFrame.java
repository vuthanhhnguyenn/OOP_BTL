package com.clinic.triage.doctor.ui;

import com.clinic.triage.model.Doctor;
import com.clinic.triage.model.Patient;
import com.clinic.triage.model.Prescription;
import com.clinic.triage.service.DoctorService;
import com.clinic.triage.util.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class DoctorDashboardFrame extends JFrame {
    private final Doctor doctor;
    private final DoctorService doctorService = new DoctorService();
    private final DefaultListModel<Patient> waitingModel = new DefaultListModel<>();
    private Timer autoRefreshTimer;
    private JList<Patient> waitingList;
    private JTextArea detailArea;
    private JLabel queueSummaryLabel;

    public DoctorDashboardFrame(Doctor doctor) {
        super("Bảng điều khiển bác sĩ - " + doctor.getDisplayName());
        this.doctor = doctor;
        UiUtils.configureLookAndFeel();
        buildUi();
        refreshQueue();
        autoRefreshTimer = new Timer(10000, e -> refreshQueue());
        autoRefreshTimer.start();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        JLabel header = new JLabel("Khoa: " + doctor.getDepartmentCode() + " | " + doctor.getDisplayName(), SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        add(header, BorderLayout.NORTH);

        waitingList = new JList<>(waitingModel);
        waitingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Patient patient) {
                    label.setText(patient.getQueueNumber() + " - " + patient.getFullName() + " (" + patient.getAge() + "t)");
                }
                return label;
            }
        });
        waitingList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showPatientDetail(waitingList.getSelectedValue());
            }
        });
        JScrollPane listPane = new JScrollPane(waitingList);
        listPane.setBorder(BorderFactory.createTitledBorder("Hàng chờ"));

        detailArea = UiUtils.createTextArea();
        detailArea.setEditable(false);
        detailArea.setText("Chọn một bệnh nhân để xem triệu chứng chi tiết.");
        JScrollPane detailPane = new JScrollPane(detailArea);
        detailPane.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPane, detailPane);
        splitPane.setResizeWeight(0.4);
        add(splitPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        queueSummaryLabel = new JLabel("Hàng chờ hiện tại: 0 bệnh nhân");
        queueSummaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        footer.add(queueSummaryLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Làm mới");
        refreshButton.addActionListener(e -> refreshQueue());
        JButton doneButton = new JButton("Đã khám");
        doneButton.addActionListener(e -> markExamined());
        JButton prescribeButton = new JButton("Kê đơn thuốc...");
        prescribeButton.addActionListener(e -> prescribe());
        actions.add(refreshButton);
        actions.add(doneButton);
        actions.add(prescribeButton);
        footer.add(actions, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);

        setSize(950, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (autoRefreshTimer != null) {
                    autoRefreshTimer.stop();
                }
            }
        });
        UiUtils.center(this);
    }

    private void refreshQueue() {
        List<Patient> patients = doctorService.findWaitingPatients(doctor.getDepartmentCode());
        waitingModel.clear();
        patients.forEach(waitingModel::addElement);
        queueSummaryLabel.setText("Hàng chờ hiện tại: " + waitingModel.size() + " bệnh nhân");
        if (waitingModel.isEmpty()) {
            detailArea.setText("Chưa có bệnh nhân nào đang chờ.");
        } else {
            waitingList.setSelectedIndex(0);
        }
    }

    private void showPatientDetail(Patient patient) {
        if (patient == null) {
            detailArea.setText("Chọn bệnh nhân để xem chi tiết.");
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Số: ").append(patient.getQueueNumber()).append("\n")
                .append("Tên: ").append(patient.getFullName()).append("\n")
                .append("Tuổi: ").append(patient.getAge()).append("\n")
                .append("Trạng thái: ").append(patient.getStatus()).append("\n\n")
                .append("Triệu chứng:\n").append(patient.getSymptoms());
        if (patient.getExamNote() != null && !patient.getExamNote().isBlank()) {
            builder.append("\n\nGhi chú khám gần nhất:\n").append(patient.getExamNote());
        }
        detailArea.setText(builder.toString());
    }

    private void markExamined() {
        Patient patient = waitingList.getSelectedValue();
        if (patient == null) {
            UiUtils.showError(this, "Vui lòng chọn bệnh nhân");
            return;
        }
        ExamResultDialog dialog = new ExamResultDialog(this, patient);
        dialog.setVisible(true);
        ExamResultDialog.Action action = dialog.getSelectedAction();
        if (action == null) {
            return;
        }
        String note = dialog.getExamNote();
        if (note.isBlank()) {
            UiUtils.showError(this, "Vui lòng nhập kết quả khám/ghi chú.");
            return;
        }
        if (action == ExamResultDialog.Action.COMPLETED) {
            doctorService.markAsExamined(patient, note);
            UiUtils.showInfo(this, "Đã đánh dấu hoàn tất khám cho " + patient.getFullName());
            refreshQueue();
        } else {
            doctorService.saveExamNote(patient, note);
            UiUtils.showInfo(this, "Đã lưu yêu cầu xét nghiệm cho " + patient.getFullName() + ". Bệnh nhân vẫn nằm trong hàng chờ.");
            refreshQueue();
        }
    }

    private void prescribe() {
        Patient patient = waitingList.getSelectedValue();
        if (patient == null) {
            UiUtils.showError(this, "Vui lòng chọn bệnh nhân");
            return;
        }
        PrescriptionDialog dialog = new PrescriptionDialog(this, patient);
        dialog.setVisible(true);
        String content = dialog.getPrescriptionContent();
        if (content == null) {
            return;
        }
        try {
            Prescription prescription = doctorService.createPrescription(doctor, patient, content);
            UiUtils.showInfo(this, "Đã lưu đơn thuốc cho \"" + patient.getFullName() + "\"\n" +
                    "Số: " + prescription.getQueueNumber());
            refreshQueue();
        } catch (Exception ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }
}
