# Giải thích từng thành phần mã nguồn

Tài liệu này giúp người mới học Java OOP hiểu vai trò của từng lớp, luồng xử lý chính và vì sao mã được tách như hiện tại.

## 1. config
- **AppConfig**: Đọc biến môi trường CLINIC_DB_PATH (nếu có) và tính toán đường dẫn database mặc định (database/clinic-triage.db). Có phương thức getDatabaseUrl() trả về chuỗi JDBC dạng jdbc:sqlite:... để các lớp khác tái sử dụng mà không cần biết chi tiết.

## 2. db
- **DatabaseManager**: Chịu trách nhiệm tải driver SQLite, tạo thư mục lưu DB và cung cấp Connection cho toàn hệ thống. Đảm bảo quá trình khởi tạo (load driver + migrate) chạy đúng 1 lần.
- **DatabaseInitializer**: Chứa các câu lệnh tạo bảng và seed dữ liệu mẫu (khoa, bác sĩ, quy tắc triệu chứng). Hàm 
ow() chuẩn hóa định dạng thời gian ISO để lưu xuống DB.

## 3. model (các lớp thuần dữ liệu)
- **Department, Doctor, Patient, Prescription, SymptomRule**: Định nghĩa thuộc tính bất biến và getter tương ứng.
- **PatientStatus** (enum): Hai trạng thái WAITING và DONE giúp code dễ đọc, tránh nhập sai chuỗi.

## 4. dao (Data Access Object)
- **DepartmentDao**: Lấy danh sách khoa/ tìm theo mã.
- **DoctorDao**: Truy vấn thông tin bác sĩ, trả về DoctorRecord chứa thực thể bác sĩ + hash mật khẩu để AuthService so sánh.
- **PatientDao**: Thêm bệnh nhân mới, tìm theo số thứ tự, lấy hàng chờ của khoa, cập nhật trạng thái.
- **PrescriptionDao**: Lưu/ truy vấn đơn thuốc theo số thứ tự.
- **SymptomRuleDao**: Đọc toàn bộ bảng symptom_rules để phục vụ bộ phân loại.

Các DAO đều mở kết nối qua DatabaseManager, dùng PreparedStatement để tránh SQL injection và gom logic truy cập dữ liệu tại 1 nơi duy nhất.

## 5. service (luồng nghiệp vụ)
- **AuthService**: Xác thực bác sĩ. Lấy DoctorRecord từ DAO, so sánh mật khẩu bằng PasswordHasher.matches rồi trả về Doctor nếu hợp lệ.
- **SymptomClassifier**: Tải danh sách quy tắc từ DB, chuyển chuỗi triệu chứng về chữ thường (Locale vi-VN) rồi dò xem có chứa keyword nào hay không. Nếu không match, trả về khoa đầu tiên (mặc định nội tổng quát).
- **QueueNumberService**: Sinh số thứ tự dạng PREFIX-xx dựa trên prefix của khoa và số cuối cùng trong bảng patients.
- **TriageService**: Ghép các bước lại cho ứng dụng bệnh nhân: kiểm tra dữ liệu đầu vào → phân khoa → lấy số → lưu xuống patients. Cũng có hàm indPrescriptionByQueueNumber để bệnh nhân tra cứu đơn thuốc.
- **DoctorService**: Dành cho ứng dụng bác sĩ. Cung cấp hàng chờ, đánh dấu đã khám và lưu đơn thuốc (kèm cập nhật trạng thái bệnh nhân).

## 6. util
- **PasswordHasher**: Hash/match mật khẩu bằng SHA-256 và salt tĩnh để ví dụ đơn giản vẫn bảo vệ ở mức cơ bản.
- **UiUtils**: Gom các tiện ích giao diện như set Look & Feel, tạo JTextArea có cấu hình line-wrap, hiển thị dialog thông báo/lỗi và căn giữa cửa sổ.

## 7. patient.ui
- **PatientApp**: Một JFrame với JTabbedPane gồm 2 tab:
  1. *Đăng ký khám*: nhập họ tên, tuổi, mô tả triệu chứng → gọi TriageService.registerPatient. Thông báo kết quả bằng tiếng Việt, hiển thị khoa + số.
  2. *Tra cứu đơn thuốc*: nhập số thứ tự → gọi TriageService.findPrescriptionByQueueNumber + indPatientByQueueNumber để hiển thị nội dung đơn thuốc.
- Bố cục dùng GridBagLayout để dễ canh dòng + nhãn tiếng Việt.

## 8. doctor.ui
- **DoctorLoginFrame**: Form đăng nhập đơn giản. Khi AuthService trả về bác sĩ hợp lệ thì mở DoctorDashboardFrame và đóng form login.
- **DoctorDashboardFrame**: Màn hình chính của bác sĩ.
  - Trái: JList hiển thị hàng chờ (model từ DoctorService).
  - Phải: JTextArea mô tả chi tiết bệnh nhân.
  - Dưới: các nút “Làm mới”, “Đã khám”, “Kê đơn thuốc”. Nút kê đơn mở PrescriptionDialog, lưu qua DoctorService.createPrescription rồi làm mới danh sách.
  - Có Timer refresh 10 giây/lần để cập nhật hàng chờ tự động khi nhiều máy cùng sử dụng.
- **PrescriptionDialog**: JDialog modal chứa JTextArea để nhập nội dung đơn thuốc. Khi đóng với nút lưu thì trả về chuỗi prescription cho DoctorDashboardFrame.

## 9. Luồng dữ liệu tổng quan
1. Bệnh nhân đăng ký → TriageService phân khoa, tạo số, lưu vào patients.
2. Bác sĩ đăng nhập → DoctorService.findWaitingPatients lấy đúng khoa.
3. Khi bác sĩ kê đơn → lưu record trong prescriptions, cập nhật trạng thái bệnh nhân sang DONE.
4. Bệnh nhân nhập số → đọc đơn thuốc mới nhất trong prescriptions kèm thông tin bệnh nhân.

Nhờ chia tầng (config → db → dao → service → ui) nên việc thay đổi driver DB hoặc update UI đều không ảnh hưởng các tầng khác. Bạn có thể mở file Java tương ứng để xem rõ hơn cách gọi từng phương thức đã mô tả ở trên.
