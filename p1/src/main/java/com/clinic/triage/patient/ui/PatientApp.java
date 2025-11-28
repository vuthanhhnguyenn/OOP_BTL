package com.clinic.triage.patient.ui;

import com.clinic.triage.model.Department;
import com.clinic.triage.model.Patient;
import com.clinic.triage.model.Prescription;
import com.clinic.triage.service.TriageService;
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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PatientApp extends JFrame {
    private final TriageService triageService = new TriageService();

    private JTextField nameField;
    private JSpinner ageSpinner;
    private JTextArea symptomArea;
    private JTextArea registrationResultArea;
    private JTextField queueLookupField;
    private JTextArea prescriptionArea;
    private DefaultListModel<Department> departmentListModel;
    private JList<Department> departmentList;

    public PatientApp() {
        super("Cổng bệnh nhân - Phân loại khám bệnh");
        UiUtils.configureLookAndFeel();
        buildUi();
    }

    private void buildUi() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Đăng ký khám", buildRegistrationPanel());
        tabs.add("Tra cứu đơn thuốc", buildPrescriptionLookupPanel());
        add(tabs, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 540);
        UiUtils.center(this);
    }

    private JPanel buildRegistrationPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(16, 0));
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Họ và tên"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField();
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Tuổi"), gbc);
        gbc.gridx = 1;
        ageSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 120, 1));
        formPanel.add(ageSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả triệu chứng"), gbc);
        gbc.gridx = 1;
        symptomArea = UiUtils.createTextArea();
        JScrollPane symptomScroll = UiUtils.wrap(symptomArea);
        symptomScroll.setPreferredSize(new Dimension(320, 140));
        formPanel.add(symptomScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Đăng ký và lấy số thứ tự");
        submitButton.addActionListener(e -> registerPatient());
        formPanel.add(submitButton, gbc);

        gbc.gridy++;
        registrationResultArea = UiUtils.createTextArea();
        registrationResultArea.setEditable(false);
        registrationResultArea.setBackground(new Color(248, 249, 251));
        registrationResultArea.setBorder(BorderFactory.createTitledBorder("Kết quả"));
        registrationResultArea.setText("Nhập thông tin ");
        formPanel.add(registrationResultArea, gbc);

        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buildDepartmentPanel(), BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildDepartmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Chọn khoa (tùy chọn, để trống để tự phân loại)"));

        departmentListModel = new DefaultListModel<>();
        departmentList = new JList<>(departmentListModel);
        departmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        departmentList.setVisibleRowCount(10);
        departmentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department department) {
                    label.setText(department.getName() + " (" + department.getCode() + ")");
                }
                return label;
            }
        });
        JScrollPane departmentScroll = new JScrollPane(departmentList);
        panel.add(departmentScroll, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Tải lại danh sách");
        refreshButton.addActionListener(e -> loadDepartments());
        panel.add(refreshButton, BorderLayout.SOUTH);

        loadDepartments();
        return panel;
    }

    private JPanel buildPrescriptionLookupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new JLabel("Nhập mã bệnh nhân / số thứ tự"), gbc);
        gbc.gridx = 1;
        queueLookupField = new JTextField();
        panel.add(queueLookupField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton searchButton = new JButton("Xem đơn thuốc");
        searchButton.addActionListener(e -> lookupPrescription());
        panel.add(searchButton, gbc);

        gbc.gridy++;
        prescriptionArea = UiUtils.createTextArea();
        prescriptionArea.setEditable(false);
        panel.add(UiUtils.wrap(prescriptionArea), gbc);
        return panel;
    }

    private void registerPatient() {
        String name = nameField.getText();
        int age = (Integer) ageSpinner.getValue();
        String symptoms = symptomArea.getText();
        Department selectedDepartment = departmentList.getSelectedValue();
        String departmentCode = selectedDepartment != null ? selectedDepartment.getCode() : null;
        try {
            TriageService.PatientRegistrationResult result = triageService.registerPatient(
                    name, age, symptoms, departmentCode);
            Patient patient = result.patient();
            String message = """
                    Chúc mừng %s!
                    Khoa tiếp nhận: %s
                    Số thứ tự: %s
                    Vui lòng di chuyển tới khu vực khoa %s và chờ được gọi.
                    """.formatted(
                    patient.getFullName(),
                    result.department().getName(),
                    patient.getQueueNumber(),
                    result.department().getName());
            registrationResultArea.setText(message);
            UiUtils.showInfo(this, "Đăng ký thành công!\nSố của bạn: " + patient.getQueueNumber());
            nameField.setText("");
            symptomArea.setText("");
        } catch (Exception ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }

    private void lookupPrescription() {
        String queueNumber = queueLookupField.getText() == null ? "" : queueLookupField.getText().trim();
        if (queueNumber.isEmpty()) {
            UiUtils.showError(this, "Vui lòng nhập mã bệnh nhân hoặc số thứ tự.");
            return;
        }
        queueNumber = queueNumber.toUpperCase(Locale.ROOT);
        Optional<Prescription> prescription = triageService.findPrescriptionByQueueNumber(queueNumber);
        if (prescription.isEmpty()) {
            prescriptionArea.setText("Chưa tìm thấy đơn thuốc cho số " + queueNumber + ".\n" +
                    "Nếu bạn mới khám, vui lòng thử lại sau vài phút.");
            return;
        }
        Optional<Patient> patientOptional = triageService.findPatientByQueueNumber(queueNumber);
        String patientName = patientOptional.map(Patient::getFullName).orElse("không xác định");
        String examNote = patientOptional.map(Patient::getExamNote)
                .filter(note -> note != null && !note.isBlank())
                .orElse("Chưa có ghi chú khám / kết quả xét nghiệm");
        Prescription data = prescription.get();
        StringBuilder builder = new StringBuilder();
        builder.append("Đơn thuốc cho số ").append(data.getQueueNumber()).append("\n");
        builder.append("Bệnh nhân: ").append(patientName).append("\n");
        builder.append("Ngày kê: ").append(formatDateTime(data.getCreatedAt())).append("\n");
        builder.append("Ghi chú bác sĩ: ").append(examNote).append("\n\n");
        builder.append(data.getContent());
        prescriptionArea.setText(builder.toString());
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Không xác định";
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(raw);
            return odt.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(raw);
            return ldt.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {
        }
        return raw;
    }

    private void loadDepartments() {
        if (departmentListModel == null) {
            return;
        }
        departmentListModel.clear();
        triageService.listDepartments().forEach(departmentListModel::addElement);
        // Do not auto-select
        departmentList.clearSelection();
    }

    public static void main(String[] args) {
        UiUtils.runOnUiThread(() -> new PatientApp().setVisible(true));
    }
}
