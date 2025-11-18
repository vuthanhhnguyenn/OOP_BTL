# Ứng dụng Java OOP phân loại bệnh nhân

Hệ thống gồm hai ứng dụng desktop độc lập (cổng bệnh nhân và cổng bác sĩ). Bệnh nhân nhập thông tin, được phân khoa dựa trên triệu chứng và nhận số thứ tự theo dạng `PREFIX-xx`. Bác sĩ đăng nhập để xem hàng chờ của khoa mình, đánh dấu đã khám và kê đơn thuốc. Dữ liệu lưu qua JDBC/SQLite.

## Công nghệ đã dùng
- Java 17+, kiến trúc thuần OOP.
- Swing/AWT cho giao diện tiếng Việt có dấu.
- JDBC + SQLite (có thể thay bằng MySQL/PostgreSQL cho triển khai LAN chính thức).
- Thư viện JDBC đính kèm `lib/`: `sqlite-jdbc-3.45.2.0.jar`, `slf4j-api-2.0.12.jar`, `slf4j-simple-2.0.12.jar`.
- Mã hóa mật khẩu bằng SHA-256 (`PasswordHasher`).

## Cấu trúc thư mục
```
src/main/java/com/clinic/triage
 ├─ config, db: cấu hình & khởi tạo database
 ├─ model, dao, service: lớp nghiệp vụ thuần OOP
 ├─ patient/ui: ứng dụng bệnh nhân (`PatientApp`)
 └─ doctor/ui: ứng dụng bác sĩ (`DoctorLoginFrame`, `DoctorDashboardFrame`)
database/
 └─ clinic-triage.db (seed sẵn)
lib/
 ├─ sqlite-jdbc-3.45.2.0.jar
 ├─ slf4j-api-2.0.12.jar
 └─ slf4j-simple-2.0.12.jar
```
Chi tiết từng lớp mô tả tại `docs/code_explanation.md`.

## Khởi tạo & chạy thử
1. Cài JDK 17 trở lên.
2. Kiểm tra thư mục `lib/` đã có đủ 3 JAR nói trên (đã được cung cấp).
3. (Tuỳ chọn) Cấu hình đường dẫn database trong `config/app.properties` hoặc đặt biến môi trường `CLINIC_DB_PATH`. Mặc định hệ thống dùng file `database/clinic-triage.db`.
4. Biên dịch toàn bộ nguồn:
   - PowerShell (Windows):
     ```powershell
     $files = Get-ChildItem -Recurse -Filter *.java src/main/java | ForEach-Object { $_.FullName }
     $cp = (
       "lib/sqlite-jdbc-3.45.2.0.jar",
       "lib/slf4j-api-2.0.12.jar",
       "lib/slf4j-simple-2.0.12.jar"
     ) -join ';'
     javac -cp $cp -encoding UTF8 -d out $files
     ```
   - Bash (macOS/Linux):
     ```bash
     CP="lib/sqlite-jdbc-3.45.2.0.jar:lib/slf4j-api-2.0.12.jar:lib/slf4j-simple-2.0.12.jar"
     javac -cp "$CP" -encoding UTF-8 -d out $(find src/main/java -name "*.java")
     ```
5. Chạy cổng bệnh nhân:
   ```bash
   java -cp "out;lib/sqlite-jdbc-3.45.2.0.jar;lib/slf4j-api-2.0.12.jar;lib/slf4j-simple-2.0.12.jar" com.clinic.triage.patient.ui.PatientApp   # Windows
   java -cp "out:lib/sqlite-jdbc-3.45.2.0.jar:lib/slf4j-api-2.0.12.jar:lib/slf4j-simple-2.0.12.jar" com.clinic.triage.patient.ui.PatientApp    # macOS/Linux
   ```
6. Chạy cổng bác sĩ:
   ```bash
   java -cp "out;lib/sqlite-jdbc-3.45.2.0.jar;lib/slf4j-api-2.0.12.jar;lib/slf4j-simple-2.0.12.jar" com.clinic.triage.doctor.ui.DoctorLoginFrame
   java -cp "out:lib/sqlite-jdbc-3.45.2.0.jar:lib/slf4j-api-2.0.12.jar:lib/slf4j-simple-2.0.12.jar" com.clinic.triage.doctor.ui.DoctorLoginFrame
   ```

### Tài khoản bác sĩ mẫu
Đã seed trong database: `bs_tmh`, `bs_mat`, `bs_rhm`, `bs_da`, `bs_cxk`, `bs_tm`, `bs_tk`, `bs_hh`, `bs_thgm`, `bs_ntd`, `bs_ttn`, `bs_ung`, `bs_ctch` (mật khẩu mặc định `123456`).

## Thiết kế database & gợi ý triển khai LAN
- File SQLite mặc định: `database/clinic-triage.db`. Có thể đổi bằng chỉnh sửa `config/app.properties` (trường `database.path`) hoặc đặt biến môi trường `CLINIC_DB_PATH` trỏ tới vị trí mong muốn. Biến môi trường sẽ ưu tiên cao nhất.
- Mô hình 2 máy (máy bác sĩ làm server): đặt file DB trên máy bác sĩ và chia sẻ qua mạng nội bộ (UNC). Trên cả hai máy, cấu hình `database.path` hoặc `CLINIC_DB_PATH` trỏ tới UNC đó (ví dụ `\\\\BACSI-PC\\ClinicDB\\clinic-triage.db`). Máy bệnh nhân chỉ cần chạy `PatientApp` với cùng classpath là đọc/ghi chung.
- Muốn hỗ trợ nhiều người dùng đồng thời → chuyển sang MySQL/PostgreSQL, sửa `AppConfig.getDatabaseUrl()` và thêm driver tương ứng vào `lib/`.
- Hai ứng dụng desktop chỉ giao tiếp qua database chung, nên có thể đặt DB trên máy chủ nội bộ và chia sẻ qua LAN.
- Khuyến nghị sản xuất: máy chủ DB + backup định kỳ, máy trạm bác sĩ/bệnh nhân kết nối bằng JDBC string, có thể xây REST service trung gian nếu cần phân quyền phức tạp.

## Triệu chứng & phân khoa
Quy tắc phân khoa lưu ở bảng `symptom_rules`, chi tiết trong `docs/symptom_rules_analysis.md`. Nên nhập dữ liệu tiếng Việt thường (không viết hoa đầu câu) để khớp chính xác.

## Hướng phát triển tiếp
- Thêm lịch sử khám, tái khám, đính kèm xét nghiệm.
- Đồng bộ thời gian thực bằng WebSocket/server trung gian thay vì polling.
- Tùy chọn in đơn thuốc từ giao diện bác sĩ.
- Nâng cấp bảo mật (salt riêng từng mật khẩu, phân quyền đa vai trò, ghi log truy cập).
