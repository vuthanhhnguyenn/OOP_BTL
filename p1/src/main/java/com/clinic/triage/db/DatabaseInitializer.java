package com.clinic.triage.db;

import com.clinic.triage.config.AppConfig;
import com.clinic.triage.util.PasswordHasher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void runMigrations() {
        try (Connection connection = DriverManager.getConnection(AppConfig.getDatabaseUrl())) {
            connection.setAutoCommit(false);
            createTables(connection);
            ensurePatientExamNoteColumn(connection);
            seedDepartments(connection);
            seedDoctors(connection);
            seedSymptomRules(connection);
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể khởi tạo database", e);
        }
    }

    private static void ensurePatientExamNoteColumn(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE patients ADD COLUMN exam_note TEXT");
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message == null || !message.toLowerCase().contains("duplicate column")) {
                throw e;
            }
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS departments (" +
                    "code TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "queue_prefix TEXT NOT NULL)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS doctors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "display_name TEXT NOT NULL, " +
                    "department_code TEXT NOT NULL, " +
                    "FOREIGN KEY(department_code) REFERENCES departments(code))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "full_name TEXT NOT NULL, " +
                    "age INTEGER NOT NULL, " +
                    "symptoms TEXT NOT NULL, " +
                    "department_code TEXT NOT NULL, " +
                    "queue_number TEXT NOT NULL, " +
                    "exam_note TEXT, " +
                    "status TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL, " +
                    "updated_at TEXT NOT NULL, " +
                    "FOREIGN KEY(department_code) REFERENCES departments(code))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS prescriptions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER NOT NULL, " +
                    "doctor_id INTEGER NOT NULL, " +
                    "department_code TEXT NOT NULL, " +
                    "queue_number TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL, " +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id), " +
                    "FOREIGN KEY(doctor_id) REFERENCES doctors(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS symptom_rules (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "keyword TEXT UNIQUE NOT NULL, " +
                    "department_code TEXT NOT NULL, " +
                    "explanation TEXT, " +
                    "FOREIGN KEY(department_code) REFERENCES departments(code))");
        }
    }

    private static void seedDepartments(Connection connection) throws SQLException {
        String[][] departments = {
                {"TMH", "Tai - Mũi - Họng", "TMH"},
                {"MAT", "Mắt", "MAT"},
                {"RHM", "Răng - Hàm - Mặt", "RHM"},
                {"DA", "Da liễu", "DAL"},
                {"CXK", "Cơ - Xương - Khớp", "CXK"},
                {"TM", "Tim mạch", "TM"},
                {"TK", "Thần kinh", "TK"},
                {"HH", "Hô hấp", "HH"},
                {"THGM", "Tiêu hóa - Gan mật", "THG"},
                {"NTD", "Nội tiết - Đái tháo đường", "NTD"},
                {"TTN", "Thận - Tiết niệu / Nam khoa", "TTN"},
                {"UNG", "Ung bướu (Ung thư)", "UNG"},
                {"CTCH", "Chấn thương chỉnh hình", "CTCH"}
        };
        String sql = "INSERT INTO departments(code, name, queue_prefix) VALUES(?, ?, ?) " +
                "ON CONFLICT(code) DO UPDATE SET name = excluded.name, queue_prefix = excluded.queue_prefix";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String[] department : departments) {
                statement.setString(1, department[0]);
                statement.setString(2, department[1]);
                statement.setString(3, department[2]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void seedDoctors(Connection connection) throws SQLException {
        Object[][] doctors = {
                {"bs_tmh", "Bác sĩ Hạnh", "TMH"},
                {"bs_mat", "Bác sĩ Lan", "MAT"},
                {"bs_rhm", "Bác sĩ Phong", "RHM"},
                {"bs_da", "Bác sĩ Dũng", "DA"},
                {"bs_cxk", "Bác sĩ Tài", "CXK"},
                {"bs_tm", "Bác sĩ Minh", "TM"},
                {"bs_tk", "Bác sĩ Thảo", "TK"},
                {"bs_hh", "Bác sĩ Lâm", "HH"},
                {"bs_thgm", "Bác sĩ Huy", "THGM"},
                {"bs_ntd", "Bác sĩ Khoa", "NTD"},
                {"bs_ttn", "Bác sĩ Sơn", "TTN"},
                {"bs_ung", "Bác sĩ Trang", "UNG"},
                {"bs_ctch", "Bác sĩ Việt", "CTCH"}
        };
        String sql = "INSERT INTO doctors(username, password_hash, display_name, department_code) VALUES(?, ?, ?, ?) " +
                "ON CONFLICT(username) DO UPDATE SET password_hash = excluded.password_hash, " +
                "display_name = excluded.display_name, department_code = excluded.department_code";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] doctor : doctors) {
                statement.setString(1, (String) doctor[0]);
                statement.setString(2, PasswordHasher.hash("123456"));
                statement.setString(3, (String) doctor[1]);
                statement.setString(4, (String) doctor[2]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void seedSymptomRules(Connection connection) throws SQLException {
        Object[][] rules = {
                {"viêm họng", "TMH", "Đau họng kéo dài cần bác sĩ tai - mũi - họng theo phác đồ BYT"},
                {"viêm amidan", "TMH", "Viêm amidan tái phát thuộc nhóm bệnh lý TMH"},
                {"viêm tai giữa", "TMH", "Chảy mủ tai/viêm tai giữa phải được soi tai chuyên khoa"},
                {"viêm xoang", "TMH", "Triệu chứng nghẹt mũi, nhức đầu do viêm xoang cần khám TMH"},
                {"ù tai", "TMH", "Ù tai kéo dài có thể liên quan bệnh lý ốc tai - tiền đình, chuyển TMH"},
                {"ngáy", "TMH", "Ngáy và ngủ ngáy kèm khó thở mũi thuộc phạm vi TMH"},
                {"khó thở mũi", "TMH", "Tắc nghẽn đường thở trên cần nội soi TMH để đánh giá"},

                {"cận", "MAT", "Khúc xạ mắt bất thường cần đo tại khoa Mắt"},
                {"viễn", "MAT", "Viễn thị/loạn thị được điều chỉnh bởi bác sĩ Mắt"},
                {"loạn thị", "MAT", "Loạn thị cần khám khúc xạ chuyên khoa"},
                {"đau mắt đỏ", "MAT", "Đau mắt đỏ/viêm kết mạc phải được khám tại khoa Mắt"},
                {"viêm kết mạc", "MAT", "Viêm kết mạc là bệnh lý thường gặp của khoa Mắt"},
                {"đục thủy tinh thể", "MAT", "Đục thủy tinh thể thuộc chuyên khoa phẫu thuật mắt"},
                {"tăng nhãn áp", "MAT", "Tăng nhãn áp cần đo áp lực mắt định kỳ tại khoa Mắt"},

                {"sâu răng", "RHM", "Sâu răng cần điều trị tại khoa Răng - Hàm - Mặt"},
                {"viêm tủy", "RHM", "Viêm tủy răng thuộc chuyên khoa Răng - Hàm - Mặt"},
                {"nhổ răng khôn", "RHM", "Nhổ răng khôn được thực hiện tại Răng - Hàm - Mặt"},
                {"niềng răng", "RHM", "Niềng răng chỉnh nha là dịch vụ của khoa Răng - Hàm - Mặt"},
                {"gãy xương hàm", "RHM", "Gãy hoặc tổn thương vùng hàm mặt cần phẫu thuật RHM"},
                {"tổn thương vùng mặt", "RHM", "Va chạm vùng mặt nên đánh giá bởi bác sĩ RHM"},

                {"mụn", "DA", "Mụn trứng cá nên điều trị bởi bác sĩ da liễu"},
                {"viêm da", "DA", "Các thể viêm da mãn cần khoa Da liễu theo dõi"},
                {"dị ứng da", "DA", "Dị ứng nổi mẩn đỏ cần khám khoa Da liễu"},
                {"mề đay", "DA", "Mề đay kéo dài thuộc phạm vi Da liễu"},
                {"vảy nến", "DA", "Vảy nến nằm trong nhóm bệnh mãn tính của Da liễu"},
                {"nấm da", "DA", "Nhiễm nấm da, lang ben cần kê toa tại Da liễu"},
                {"zona", "DA", "Bệnh zona thần kinh được điều trị bởi bác sĩ Da liễu"},
                {"bệnh da niêm mạc", "DA", "Các bệnh lây qua đường tình dục gây tổn thương da cần Da liễu"},

                {"thoái hóa khớp", "CXK", "Thoái hóa khớp thuộc quản lý khoa Cơ - Xương - Khớp"},
                {"đau lưng", "CXK", "Đau lưng mạn nghi ngờ thoát vị cần khám CXK"},
                {"thoát vị đĩa đệm", "CXK", "Thoát vị đĩa đệm cần bác sĩ chỉnh hình - CXK"},
                {"gout", "CXK", "Đau khớp gout cần điều trị tại khoa Cơ - Xương - Khớp"},
                {"viêm khớp dạng thấp", "CXK", "Viêm khớp dạng thấp là bệnh chuyên khoa CXK"},

                {"tăng huyết áp", "TM", "Tăng huyết áp phải được quản lý tại khoa Tim mạch"},
                {"mạch vành", "TM", "Đau ngực nghi mạch vành cần khám Tim mạch"},
                {"suy tim", "TM", "Suy tim cần theo dõi sát bởi bác sĩ Tim mạch"},
                {"rối loạn nhịp tim", "TM", "Đánh trống ngực/rối loạn nhịp nên vào khoa Tim mạch"},

                {"đau đầu", "TK", "Đau đầu tái diễn cần đánh giá tại khoa Thần kinh"},
                {"động kinh", "TK", "Co giật/động kinh là bệnh đặc thù của Thần kinh"},
                {"tai biến mạch máu não", "TK", "Tai biến/đột quỵ cần xử trí Thần kinh"},
                {"đột quỵ", "TK", "Nghi ngờ đột quỵ chuyển ngay khoa Thần kinh"},
                {"rối loạn vận động", "TK", "Rối loạn vận động cần chuyên gia Thần kinh đánh giá"},

                {"viêm phế quản", "HH", "Ho đàm, viêm phế quản thuộc khoa Hô hấp"},
                {"hen phế quản", "HH", "Hen phế quản cần điều trị kiểm soát tại Hô hấp"},
                {"copd", "HH", "COPD nằm trong nhóm bệnh mãn tính của khoa Hô hấp"},
                {"viêm phổi", "HH", "Triệu chứng sốt, khó thở do viêm phổi nên khám Hô hấp"},

                {"đau dạ dày", "THGM", "Đau dạ dày, viêm loét thuộc khoa Tiêu hóa - Gan mật"},
                {"trào ngược", "THGM", "Trào ngược dạ dày thực quản nên khám TH-GM"},
                {"viêm gan", "THGM", "Viêm gan virus cần được quản lý tại Tiêu hóa - Gan mật"},
                {"gan nhiễm mỡ", "THGM", "Gan nhiễm mỡ thuộc nhóm bệnh nội tiêu hóa"},
                {"bệnh túi mật", "THGM", "Sỏi hoặc viêm túi mật điều trị tại khoa TH-GM"},

                {"tiểu đường", "NTD", "Tiểu đường/đái tháo đường thuộc khoa Nội tiết"},
                {"đái tháo đường", "NTD", "Kiểm soát đường huyết cần bác sĩ Nội tiết"},
                {"rối loạn tuyến giáp", "NTD", "Cường/nhược giáp do Nội tiết quản lý"},
                {"tuyến thượng thận", "NTD", "Rối loạn nội tiết thượng thận cần khám NTD"},
                {"rối loạn mỡ máu", "NTD", "Mỡ máu cao liên quan hội chứng chuyển hóa do Nội tiết quản lý"},

                {"sỏi thận", "TTN", "Sỏi thận, đau hông lưng thuộc khoa Thận - Tiết niệu"},
                {"viêm bàng quang", "TTN", "Tiểu buốt, viêm bàng quang cần bác sĩ Tiết niệu"},
                {"rối loạn tiểu tiện", "TTN", "Rối loạn tiểu tiện phải đánh giá tại khoa TTN"},
                {"rối loạn cương", "TTN", "Nam khoa, rối loạn cương cần khám TTN"},
                {"viêm tuyến tiền liệt", "TTN", "Viêm tuyến tiền liệt là bệnh của khoa TTN"},
                {"vô sinh nam", "TTN", "Vô sinh nam nên được tư vấn bởi bác sĩ nam khoa"},

                {"ung thư vú", "UNG", "Tầm soát/điều trị ung thư vú tại khoa Ung bướu"},
                {"ung thư phổi", "UNG", "Ung thư phổi cần điều trị hóa xạ trị tại Ung bướu"},
                {"u lành", "UNG", "Các loại u lành tính được theo dõi tại khoa Ung bướu"},
                {"u ác", "UNG", "Phát hiện u ác tính chuyển ngay Ung bướu"},
                {"tầm soát ung thư", "UNG", "Khám tầm soát định kỳ thuộc khoa Ung bướu"},

                {"gãy xương", "CTCH", "Gãy xương cần cố định tại khoa Chấn thương chỉnh hình"},
                {"trật khớp", "CTCH", "Trật khớp cấp cứu tại Chấn thương chỉnh hình"},
                {"biến dạng xương khớp", "CTCH", "Biến dạng bẩm sinh hoặc sau chấn thương thuộc CTCH"},
                {"chấn thương thể thao", "CTCH", "Chấn thương thể thao cần bác sĩ CTCH đánh giá"}
        };
        String sql = "INSERT INTO symptom_rules(keyword, department_code, explanation) VALUES(?, ?, ?) " +
                "ON CONFLICT(keyword) DO UPDATE SET department_code = excluded.department_code, " +
                "explanation = excluded.explanation";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] rule : rules) {
                statement.setString(1, (String) rule[0]);
                statement.setString(2, (String) rule[1]);
                statement.setString(3, (String) rule[2]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public static String now() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
