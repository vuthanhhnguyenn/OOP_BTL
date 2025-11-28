  Build
     ```powershell
     $files = Get-ChildItem -Recurse -Filter *.java src/main/java | ForEach-Object { $_.FullName }
     $cp = (
       "lib/sqlite-jdbc-3.45.2.0.jar",
       "lib/slf4j-api-2.0.12.jar",
       "lib/slf4j-simple-2.0.12.jar"
     ) -join ';'
     javac -cp $cp -encoding UTF8 -d out $files
     ```
  
  Chạy cổng bệnh nhân:
   ```bash
   java -cp "out;lib/sqlite-jdbc-3.45.2.0.jar;lib/slf4j-api-2.0.12.jar;lib/slf4j-simple-2.0.12.jar" com.clinic.triage.patient.ui.PatientApp   
   ```
  Chạy cổng bác sĩ:
   ```bash
   java -cp "out;lib/sqlite-jdbc-3.45.2.0.jar;lib/slf4j-api-2.0.12.jar;lib/slf4j-simple-2.0.12.jar" com.clinic.triage.doctor.ui.DoctorLoginFrame
   ```
