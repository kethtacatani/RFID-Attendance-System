
package rfid.attendance;


import com.fazecast.jSerialComm.SerialPort;
import com.opencsv.CSVWriter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import static rfid.attendance.RFIDConnection.arduinoPort;
import java.sql.*;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import static rfid.attendance.QueryProcessor.host;
import static rfid.attendance.QueryProcessor.database;
import static rfid.attendance.QueryProcessor.username;
import static rfid.attendance.QueryProcessor.password;
import static rfid.attendance.QueryProcessor.customUser;


/**
 *
 * @author Keth Dominic
 */

//        initComponents();
//        recentRecordsTable.setShowGrid(false);
//        recentRecordsTable.getTableHeader().setVisible(false);
//        recentRecordsTable.setAutoResizeMode(recentRecordsTable.AUTO_RESIZE_ALL_COLUMNS);
public class HomePanel extends javax.swing.JFrame {
    
    PreparedStatement statement;
    
    // <editor-fold defaultstate="collapsed" desc="Main Panel">    
    DefaultTableModel model;
    Object tablerow [][];
    String tablecol []= {"ID No.","Last Name","First Name","M.I.","Course","Type","Time","Status"};
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="Manage Students Panel">   
    DefaultTableModel manageStudentModel;
    Object manageStudentTablerow [][];
    String manageStudentTablecol []= {"ID No.","RFID ID","Last Name","First Name","Middle Name.","Age","Gender","Address","<html>Contact<br>Number","Email","Year","Course","Block","Status"};
    
  
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="Statistics Panel">   
    DefaultTableModel courseCountModel;
    Object courseCount [][];
    String courseColumn []= {"Course","Count"};
    
  
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc="VIEW CSVPanel">   
    DefaultTableModel viewCSVModel;
    Object viewCSVRow [][];
    String viewCSVColumn []= {"RFID ID","ID No.","Last Name","First Name","Middle Name.","Age","Gender","Address","<html>Contact<br>Number","Email","Year","Course","Block","Status","Type"};
    String viewCSVColumnRecords []= {"ID No.","RFID ID","Event","Date","Time-in","Time-Out","Time-in Status","Time-Out Status"};

  
    // </editor-fold> 
    
     // <editor-fold defaultstate="collapsed" desc="Events Panel">   
    DefaultTableModel eventModel;
    Object eventsROw [][];
    String eventColumn []= {"ID","Event Name","Date","Students Involved","Year","Time-in","Time-out","<html>Total<br>Present"};
    
  
    // </editor-fold> 
    
    ArrayList<String> scannedRFID = new ArrayList<>();
    ArrayList<String> scanTime = new ArrayList<>();
    ArrayList<String> scanType = new ArrayList<>();
    
   
    
    
    LocalDate currentDate;
    LocalTime currentTime;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
    DateTimeFormatter longDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    public static String date;
    public static String longDate;
    public static String time;
    
    String event = "School Day";
    String rawEvent="";
    String courseInvolved = "\'BSCS\',\'BSIT-Elect\',\'BSIT-FPST\',\'BSF\',\'BEEd\',\'BSED- English\',\'BSED- Math\',\'BSM\',";
    String yearInvolved= "\'1\',\'2\',\'3 Tech\',\'4\'";
    String rawCourseInvolved="BSCS,BSIT-Elect,BSIT-FPST,BSF,BEEd,BSED- English,BSED- Math,BSM,";
    String rawYearInvolved="1,2,3,4,";
    int totalStudents = 0;
    String timeInStart = "";
    String timeInEnd = "";
    String timeOutStart = "";
    String timeOutEnd = "";
    String attendanceStatus="";
    String attendanceType="";
    int totalPresentCount = 0;
    Timer timer;
    String csvPath="";
    String inOut = "";
    String csvQuery="";
    String csvType="";
    String csvFileName="";
    String selectedRowValue="";
    
    
  
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledTask;
      
    //RFIDATTENDANCE arduino;
    Image img = new ImageIcon(this.getClass().getResource("/logoattendance.png")).getImage();
    QueryProcessor qp;
    RFIDConnection rfidCon;
    public HomePanel() {
        
        this.setIconImage(img);
        qp = new QueryProcessor();
        
        rfidCon = new RFIDConnection();
        initComponents();   
        preProcess();
        displayRecentScans();
        displayManageStudentTable();
        databaseConnection.setText("<html>Database: "+(DBConnection.databaseConnection.equals("Connected | MySQL")?"<font color='green'>":"<font color='red'>")
                +DBConnection.databaseConnection+" "+(checkIfMaindatabase()?"| Main":"| Local </font></html>"));
        
        eventLabel.setText(event);
        
        timer = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update the time value
                
                currentDate = LocalDate.now();
                currentTime = LocalTime.now();
                date = currentDate.format(dateFormatter);
                longDate = currentDate.format(longDateFormatter);
                time = currentTime.format(timeFormatter);
                
                // Update the JLabel text
                time = time.replaceAll("am", "AM").replaceAll("pm", "PM");
                dateTimeLabel.setText(longDate+" | "+time );
                //System.out.println("updating "+longDate+" | "+time );
            }
        });
        

        timer.setInitialDelay(0);
        timer.start();

        updateStudentCount();
        displayStatistics();
        
         manageStudentFrame.setLocationRelativeTo(null);
          settingsFrame.setLocationRelativeTo(null);
          addStudentsFrame.setLocationRelativeTo(null);
          addEvent.setLocationRelativeTo(null);
          manageEventFrame.setLocationRelativeTo(null);
          csvFrame.setLocationRelativeTo(null);
           checkCSV.setLocationRelativeTo(null);
           courseList.setLocationRelativeTo(null);
           editHeadingDialog.setLocationRelativeTo(null);
        
     
        

        // Start the timer
        
        
    }
    
    
    public void preProcess(){
        //System.out.println("Pre-Process");
        String[][] records = qp.getAllRecord("SELECT 1 FROM `events` WHERE `recent_event` =  'recentEvent'");
        waitingTime.setText(qp.getSpecificField("Select `c2` from `misctable` where `name`='waitingTime'"));
       if(records!=null){
            String query1 ="Select `event_name`,`date`,`students_involved`, `year`,`time_in_range`,`time_out_range` from `events` WHERE `recent_event`='recentEvent'";
            //System.out.println("query is "+query1);
            String[] recentInfo = qp.getSpecificRow(query1);
            //System.out.println("date is "+Arrays.toString(recentInfo));
            if(recentInfo[1].equals(LocalDate.now().format(dateFormatter))){
                //if recentEvent date matched the current date
                event = (recentInfo[0].length()>9 && recentInfo[0].substring(0,9).equals("School Da"))?"School Day":recentInfo[0];
                courseInvolved=recentInfo[2];
                yearInvolved = recentInfo[3];
                
                timeInStart = (recentInfo[4]!=null)?recentInfo[4].split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[0]:null;
                timeInEnd = (recentInfo[4]!=null)?recentInfo[4].split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[1]:null;
                timeOutStart = (recentInfo[5]!=null)?recentInfo[5].split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[0]:null;
                timeOutEnd = (recentInfo[5]!=null)?recentInfo[5].split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[1]:null; 
                
                //System.out.println(" time "+timeInStart+" "+timeInEnd+" "+timeOutStart+" "+timeOutEnd);
                String query = "Select `rfid_id`, TIME_FORMAT(`time_in`, '%h:%i %p') AS formatted_time_in,"
                    + "CASE WHEN time_out IS NULL THEN 'Time-in' ELSE 'Time-out' END AS time_status from `student_record` WHERE `event`='"+recentInfo[0]+"'";
                Object row[][]=qp.getAllRecord(query);
                clearScanRecords();
                if(row!=null){
                    
                    
                    for (int i = 0; i < row.length; i++) {
                        scannedRFID.add(row[i][0].toString());
                        scanTime.add(row[i][1].toString());
                        scanType.add(row[i][2].toString());
                    }
                    
                }
                
            }
            else{
                // if recentEvent date did not match the current date
                addEvent("recentEvent", event+" "+LocalDate.now().format(DateTimeFormatter.ofPattern("YY-MM-dd")), rawYearInvolved,null,null,rawCourseInvolved);
                courseInvolved= rawCourseInvolved;
                yearInvolved = rawYearInvolved;
                
            }
            
            if(recentInfo[4]!=null){
                timeInLabel.setText("Time-in: "+timeInStart+" - "+timeInEnd);
            }
            else{
                timeInLabel.setText("");
            }
            if(recentInfo[5]!=null){
                timeOutLabel.setText("Time-out: "+timeOutStart+" - "+timeOutEnd);
            }
            else{
                timeOutLabel.setText("");
            }
            System.out.println(recentInfo[5]);
            System.out.println(recentInfo[4]);
            rawEvent= recentInfo[0];
            eventLabel.setText(event);
            String getYearInvolved = this.yearInvolved.substring(0, this.yearInvolved.length()-1);
            String getCourseInvolved = this.courseInvolved.substring(0, this.courseInvolved.length()-1);
            yearInvolved = "\'" + getYearInvolved.replace(",", "\',\'") + "\'";
            courseInvolved = "\'" + getCourseInvolved.replace(",", "\',\'") + "\'";
            totalStudents = Integer.parseInt(qp.getSpecificField("SELECT COUNT(*) AS `total_students` FROM `student_info` WHERE `course` IN ("+courseInvolved+") AND `year` IN ("+yearInvolved+")"));
            
            totalPresent.setText(scannedRFID.size()+"");
            totalStudentStats.setText("/"+totalStudents);  
            displayStatistics();
            
        }
        else{
            //if no recentEvent exist or database in empty
            addEvent("recentEvent", event+" "+LocalDate.now().format(DateTimeFormatter.ofPattern("YY-MM-dd")), rawYearInvolved,null,null,rawCourseInvolved);
            //System.out.println("adding recent");
            rawEvent= event+" "+LocalDate.now().format(DateTimeFormatter.ofPattern("YY-MM-dd"));
        }
        displayLCDMessage(3);
    }
    
    public static void restartApplication() {
        String javaBin = System.getProperty("java.home") + "/bin/java";
        String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
        String className = System.getProperty("sun.java.command");

        // Create a new process to restart the application
        try {
            ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classPath, className);
            builder.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Exit the current instance of the application
        System.exit(0);
    }
    
//    public void checkArduinoConnection(int delay) {
//    if (scheduledTask != null && !scheduledTask.isDone()) {
//        // If there is a previous task running, cancel it
//        scheduledTask.cancel(true);
//    }
//    executor = Executors.newSingleThreadScheduledExecutor();
//    scheduledTask = executor.schedule(() -> {
//        // Code to be executed after the delay
//        
//        String lcdMessage = "%ok";
//        arduinoPort.writeBytes(lcdMessage.getBytes(), lcdMessage.length());
//        checkArduinoConnection(3);
//    }, delay, TimeUnit.SECONDS);
//}
 
    public void displayLCDMessage(int delay) {
       if (scheduledTask != null && !scheduledTask.isDone()) {
        // If there is a previous task running, cancel it
        scheduledTask.cancel(true);
    }
    executor = Executors.newSingleThreadScheduledExecutor();
    scheduledTask = executor.schedule(() -> {
        // Code to be executed after the delay
        
        String lcdMessage = (event + "                  ").substring(0, 20) + "Tap RFID to Scan                ";
        arduinoPort.writeBytes(lcdMessage.getBytes(), lcdMessage.length());

    }, delay, TimeUnit.SECONDS);
}

public void cancelLCDMessage() {
    if (scheduledTask != null && !scheduledTask.isDone()) {
        // If there is a task running, cancel it
        scheduledTask.cancel(true);
    }
    
    // Shutdown the executor when you no longer need it
    if (executor != null && !executor.isShutdown()) {
        executor.shutdown();
    }
}
    
    public void executeArduinoWrite(String line1, String line2, int delay) {
        int newDelay=5;
        if(delay!=0){
            newDelay=delay;
        }
        //System.out.println(delay +" new "+newDelay);
        //System.out.println("Executing arduino write");
        
        String message=(line1+"                  ").substring(0,20)+line2;
        //System.out.println(message);
        if (arduinoPort != null && arduinoPort.isOpen()) {
            //System.out.println("port open for message");
            arduinoPort.writeBytes(message.getBytes(), message.length());
            displayLCDMessage(newDelay);
        }
        
    }
    
    public void browseFiles(){
        JFileChooser fileChooser = new JFileChooser();

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File selectedFile = fileChooser.getSelectedFile();
            // Perform any necessary operations with the selected file
            csvPath = selectedFile.getAbsolutePath();
            //System.out.println("path "+csvPath);
            String cvFileShortName= (selectedFile.getAbsolutePath().length()>50)?selectedFile.getAbsolutePath().substring(0, 20)+".....\\"+selectedFile.getName():selectedFile.getAbsolutePath();
            csvName.setText(cvFileShortName);
            csvFileName= selectedFile.getName();
            
        }
    }
    
    public void createtStatment(String insertQuery){
        try {
            statement = qp.con.prepareStatement(insertQuery);
        } catch (SQLException ex) {
            Logger.getLogger(HomePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//    public void loadingDialog(int maxRow, int row){
//        
//        int percent = maxRow!=0?(row/maxRow)*100:0;
//        
//         while(percent<100){
//                progressBar.setValue(percent);
//                progressLbl.setText("Inserting "+row+""+(maxRow!=0?"/"+maxRow:"")+" rows");
//                }
//    
//    }
    
    public static void exportToCSV(ResultSet resultSet, String fileName, String insertTable) throws SQLException, IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            
            String filePath = fileChooser.getSelectedFile().getPath()+".csv";
            try (FileWriter fileWriter = new FileWriter(filePath);
                 CSVWriter csvWriter = new CSVWriter(fileWriter)) {
                
                String[] additionalRow = {insertTable};
                csvWriter.writeNext(additionalRow);
                // Write column headers
                csvWriter.writeAll(resultSet, true);

                // Write data rows
                while (resultSet.next()) {
                    String[] rowData = new String[resultSet.getMetaData().getColumnCount()];
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        rowData[i - 1] = resultSet.getString(i);
                        //System.out.println(rowData);
                    }
                    csvWriter.writeNext(rowData);
                }

                JOptionPane.showMessageDialog(null, "Exported Successfully");
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(null, "Export Failed \n"+e);
            }
        }
    }
    
    public boolean importCSV(PreparedStatement statement){
        
        
        try{
            BufferedReader reader = new BufferedReader(new FileReader(csvPath));
             //System.out.println("Import path "+csvPath);
            int row=1;
            String line;
            while ((line = reader.readLine()) != null) {
                ////System.out.println("line is "+line.substring(0, 4).replaceAll("\"", "").replaceAll(";", ""));
                String[] columns = line.split(",");
               
                if(row==1 && uploadCSV.getName().equals("event")){
                    ////System.out.println("Table created "+line.substring(1, line.length()-8));
                    if(!checkEventExist(columns[7].replace("'", ""))){
                        qp.executeUpdate(line.replace("\"", ""));
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "Event "+columns[7].replace("'", "")+" already exist!\nOverwrite this event instead");
                        csvFrame.setVisible(false);
                        return false;
                    }
                    
                    
                }
                if (row >= 3) { 
                   // //System.out.println("array is "+line);
                    String[] clone =  line.split(",");
                    //System.out.println("cloe "+Arrays.toString(clone));
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
              
                   // System.out.println("array is "+Arrays.toString(data));
                   // System.out.println(data[7]);
//                    for (int i = 0; i < data.length-(!checkIfMaindatabase()&& uploadCSV.getName().equals("student")?1:0); i++) {
                    for (int i = 0; i < data.length; i++) {
                        if(data[i]!=null && data[i].matches("\\d+") && i!=8){
                            statement.setInt(i+1, Integer.parseInt(data[i].replaceAll("\"", "").replaceAll(";", "")));
                        }else{
                            //System.out.println("dat "+i+""+data[i]);
                             if(data[i].contains("/")){
                                 statement.setString(i + 1, data[i].replaceAll("\"", "").replaceAll(";", "").isEmpty()?null:data[i].replaceAll("\"", "").replaceAll(";", "").replace("/","-"));
                             }
                             statement.setString(i + 1, data[i].replaceAll("\"", "").replaceAll(";", "").isEmpty()?null:data[i].replaceAll("\"", "").replaceAll(";", ""));
                        }
                       
                    }

                    // Execute the insert statement
                   // System.out.println("astaement "+statement.toString());
                    statement.addBatch();
                    if (row % 100 == 0) {
                        statement.executeBatch(); // Execute the batch operation
                    }
                }
                row++;
            }
            if (row > 3) {
                statement.executeBatch();
            }
            if(!getRowData(manageEventTable, eventModel, 2).equals(qp.getAllRecord("SELECT * FROM `temp_student_record`")[2][3])){
                JOptionPane.showMessageDialog(null, "Cannot overwrite event with different dates","Error",JOptionPane.ERROR_MESSAGE);
                qp.executeUpdate("DELETE FROM `temp_student_info`;");
                return false;
            }
            JOptionPane.showMessageDialog(null,"File Import Success");
    
            return true;
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null, e,"File Import Error", JOptionPane.ERROR_MESSAGE);
        }
       
   
        return false;
    }
    
    public boolean overwritetEventCSV(String[][] allRecords){
        try{
            
            String eventName=getRowData(manageEventTable, eventModel, 1);
            
            for (int j = 0; j < allRecords.length; j++) {
                String[] data = allRecords[j];
                   // System.out.println("array is "+Arrays.toString(data));
                    //System.out.println(data[7]);
                    
                        if( qp.getSpecificField("Select `student_id` from `student_record` WHERE `student_id`="+data[0]+" AND `event` = '"+data[2]+"'")!=null){
                            //System.out.println("edit");
                            createtStatment("UPDATE `student_record` SET `student_id`=?,`rfid_id`=?,`event`=?,`date`=?,`time_in`=?,`time_out`=?,`status`=?,`status_timeout`=? WHERE `student_id`="+data[0]+" AND `event` = '"+data[2]+"'");
                        }
                        else{
                            createtStatment("INSERT  INTO `student_record`(`student_id`, `rfid_id`, `event`, `date`, `time_in`, `time_out`, `status`, `status_timeout`) VALUES (?,?,?,?,?,?,?,?);");
                        }
                        
                       // System.out.println("legnth is"+data.length);
                            for (int i = 0; i < data.length; i++) {
                              //  System.out.println("data");
                                
                                if(i==2){
                                    statement.setString(i+1,eventName);
                                }
                                else if(data[i]!=null && data[i].matches("\\d+")){
                                    statement.setInt(i+1, Integer.parseInt(data[i].replaceAll("\"", "").replaceAll(";", "")));
                                }
                                else{
                                   // System.out.println("dat "+i+""+data[i]);
                                     statement.setString(i + 1,data[i]==null || data[i].replaceAll("\"", "").replaceAll(";", "").isEmpty()?null:data[i].replaceAll("\"", "").replaceAll(";", ""));
                                  //   System.out.println("diri?");
                                } 
                            }
                        // Execute the insert statement
//                        System.out.println("astaement "+statement.toString());
                        statement.executeUpdate();


            }

            JOptionPane.showMessageDialog(null,"File Import Success");
            return true;
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null, e,"File Import Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    public boolean importStudentCSV(String[][] allRecords){
    
        try{
     
            for (int j = 0; j < allRecords.length; j++) {
                String[] data = allRecords[j];
                //System.out.println("length "+allRecords.length);
                   // System.out.println("array is "+Arrays.toString(data));
                    //System.out.println(data[7]);
                    if(data[14].equals("delete")){
                        createtStatment("DELETE FROM `student_info` WHERE `student_id` = ?;");
                        statement.setInt(1,Integer.parseInt(data[1]));
                    }else{
                         if(data[14].equals("add")){
                            createtStatment("INSERT  IGNORE into `student_info` values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        }
                        else if(data[14].equals("edit") && !checkStudentIDRecord(data[1], "student_info").equals("")){
                         //   System.out.println("edit");
                            createtStatment("UPDATE `student_info` SET `rfid_id`= ? ,`student_id`= ? ,`first_name`= ? ,"
                                + "`middle_name`= ? ,`last_name`= ? ,`age`= ? ,`gender`= ? ,`address`= ? ,`contact_number`= ? ,"
                                + "`email`= ? ,`year`= ? ,`course`= ? ,`block`= ? ,`status`= ?  WHERE `student_id`='"+data[1]+"'");
                        }
                        else if (data[14].equals("edit") && checkStudentIDRecord(data[1], "student_info").equals("")){
                            createtStatment("INSERT IGNORE into `student_info` values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        }
                        //System.out.println(data.length-1);
                            for (int i = 0; i < data.length-1; i++) {
                               // System.out.println("data");

                                if(data[i]!=null && data[i].matches("\\d+") && i!=8){
                                    statement.setInt(i+1, Integer.parseInt(data[i].replaceAll("\"", "").replaceAll(";", "")));
                                }else{
                                  //  System.out.println("dat "+i+""+data[i]);
                                     statement.setString(i + 1,data[i]==null || data[i].replaceAll("\"", "").replaceAll(";", "").isEmpty()?null:data[i].replaceAll("\"", "").replaceAll(";", ""));
                                  //   System.out.println("diri?");
                                } 
                            }
                            
                    }
                        // Execute the insert statement
                       statement.executeUpdate();
//                        if ((j + 1) % 100 == 0 || j == allRecords.length - 1) {
//                            System.out.println("astaement  "+j+ "  "+allRecords.length+statement.toString()); 
//                            statement.executeBatch(); // Execute the batch operation
//                        }
            }
            JOptionPane.showMessageDialog(null,"File Import Success");
            return true;
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null, e,"File Import Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
   

    /**
     *
     */
    public void clearScanRecords(){
        scannedRFID.clear();
        scanTime.clear();
        scanType.clear();
    }
    
    public void resetMainFrame(){
        displayRecentScans();
        displayStatistics();
        updateStudentCount();
        System.out.println("eh");
    }
    
    public boolean updateRecentEvent(){
        
        System.out.println("Updating recent event");
        String[][] records = qp.getAllRecord("SELECT 1 FROM `events` WHERE `recent_event` =  'recentEvent'");
        
        if(qp.executeUpdate("UPDATE `events` SET `recent_event`= null WHERE `recent_event` = 'recentEvent'") || records == null){
            System.out.println("Update Successful event "+LocalDate.now().format(dateFormatter) );
            return true;
        }
        return false;
    }
    public void updateStudentCount(){
        System.out.println("Updating student count");
        System.out.println(scanType.toString());
        totalPresent.setText(scannedRFID.size()+"");
        if(qp.executeUpdate("UPDATE `events` SET `total_present` = "+scannedRFID.size()+" WHERE `events`.`event_name` = '"+rawEvent+"'"))
        timedIn.setText("Timed-in: "+scanType.stream().filter(element -> element.equals("Time-in")).count()+"");
        timedOut.setText("Timed-out: "+scanType.stream().filter(element ->  element.equals("Time-out")).count()+"");
    }
    
    public  void displayStatistics(){
        addOddRowColorRenderer(studentPerCourseTable);
        System.out.println("Displaying User Statistcs");
        
        
        courseCount = qp.getAllRecord("SELECT `student_info`.`course`, COUNT(`student_record`.`student_id`) AS count FROM `student_info` "
                + "LEFT JOIN `student_record` ON `student_info`.`student_id` = `student_record`.`student_id` "
                + "WHERE `student_record`.`event` = '"+rawEvent+"'  GROUP BY `student_info`.`course`;");
        
        courseCountModel = new DefaultTableModel(courseCount,courseColumn);
        studentPerCourseTable.setModel(courseCountModel);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(timedOut.RIGHT);
        studentPerCourseTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        studentPerCourseTable.setShowGrid(false);
        studentPerCourseTable.getTableHeader().setVisible(false);
        
    }
    
    public void displayRecentScans(){
        
        addOddRowColorRenderer(recentRecordsTable);
        System.out.println("Displaying Recent Scans");
       // "ID No.","Last Name","First Name","M.I.","Type","Time","Status"};
       
       
        
        String query = "SELECT `student_info`.`student_id`, `student_info`.`last_name`, `student_info`.`first_name`, IFNULL(LEFT(`middle_name`, 1), '') AS `first_letter1`,`student_info`.`course`,"
                + "CASE WHEN time_out IS NULL THEN 'Time-in' ELSE 'Time-out' END AS time_status, TIME_FORMAT(`student_record`.`time_in`, '%h:%i %p') AS formatted_time_in,"
                + "CASE WHEN time_out IS NULL THEN `student_record`.`status` ELSE `student_record`.`status_timeout` END AS student_status FROM `student_record`, `student_info` WHERE `student_record`.`student_id` = `student_info`.`student_id` "
                + "AND `student_record`.`event` = '"+rawEvent+"' AND `student_record`.`date` = '"+LocalDate.now().format(dateFormatter)+"' "
                + "AND `course` LIKE '%"+((!courseSortCB.getSelectedItem().toString().equals("All Courses"))?courseSortCB.getSelectedItem().toString():"")+"%' "
                + "AND `year` LIKE '%"+((!yearSortCB.getSelectedItem().toString().equals("Year"))?yearSortCB.getSelectedItem().toString():"")+"%' "
                + " "+inOut+" AND CONCAT_WS(`student_info`.`student_id`,  `student_info`.`last_name`, `student_info`.`first_name`, `student_info`.`course`,"
                + "`student_info`.`age`,`student_info`.`gender`,`student_info`.`address`,`student_info`.`contact_number`,`student_info`.`status`) LIKE '%"+searchTF.getText()+"%' ORDER BY time_in DESC, time_out DESC;";
        System.out.println(query);
        csvQuery = query;
        tablerow = qp.getAllRecord(query);
        model = new DefaultTableModel(tablerow,tablecol);
        recentRecordsTable.setModel(model);
        recentScanCount.setText("Count: " + (tablerow != null ? tablerow.length : 0));
        
        recentRecordsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        recentRecordsTable.getColumnModel().getColumn(3).setPreferredWidth(5);
        recentRecordsTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        recentRecordsTable.getColumnModel().getColumn(5).setPreferredWidth(20);
        recentRecordsTable.getColumnModel().getColumn(7).setPreferredWidth(15);
        
        
    }
    
    private void addOddRowColorRenderer(JTable table) {
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private Color alternateColor = new Color(220, 220, 220);
            private Color whiteColor = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (isSelected) {
                // Row is selected, set the background to the selection color
               // System.out.println(row +"column "+column);
                comp.setBackground(table.getSelectionBackground());
            } else {
                
                Color bg = (row % 2 == 0 ? alternateColor : whiteColor);
                if(table.getName()!= null && table.getName().equals("recentScanTable")){
                    
                    if((table.getValueAt(row, 5)!=null?table.getValueAt(row, 5).toString():"null").equals("Time-in")){
                    bg=whiteColor;
                    }
                    else if((table.getValueAt(row, 5)!=null?table.getValueAt(row, 5).toString():"null").equals("Time-out")){
                        bg=alternateColor;
                    }
                }
                
                // Row is not selected, set the background color based on odd/even row
                
                comp.setBackground(bg);
            }
            
//            Object column5Value = table.getValueAt(row, 5); // Assuming column5 index is 5
//            //System.out.println("Value of column5: " + (column5Value!=null?column5Value:"null"));
        
            
            return comp;
        }
    });
}
    
    public void displayManageStudentTable(){
        addOddRowColorRenderer(manageStudentTable);
        //System.out.println("Displaying Manage Student Table");
        String header="", headerCol="";
        header += (headerID.isSelected())?"`student_id`,":"";
        headerCol += (headerID.isSelected()) ? "\"ID\"," : "";
        header += (headerRFID.isSelected())?"`rfid_id`,":"";
        headerCol += (headerRFID.isSelected()) ? "\"RFID\"," : "";
        header += (headerFN.isSelected())?"`first_name`,":"";
        headerCol += (headerFN.isSelected()) ? "\"First Name\"," : "";
        header += (headerMN.isSelected())?"`middle_name`,":"";
        headerCol += (headerMN.isSelected()) ? "\"Middle Name\"," : "";
        header += (headerLN.isSelected())?"`last_name`,":"";
        headerCol += (headerLN.isSelected()) ? "\"Last Name\"," : "";
        header += (headerAge.isSelected())?"`age`,":"";
        headerCol += (headerAge.isSelected()) ? "\"Age\"," : "";
        header += (headerGender.isSelected())?"`gender`,":"";
        headerCol += (headerGender.isSelected()) ? "\"Gender\"," : "";
        header += (headerAddress.isSelected())?"`address`,":"";
        headerCol += (headerAddress.isSelected()) ? "\"Address\"," : "";
        header += (headerContact.isSelected())?"`contact_number`,":"";
        headerCol += (headerContact.isSelected()) ? "\"Contact\"," : "";
        header += (headerEmail.isSelected())?"`email`,":"";
        headerCol += (headerEmail.isSelected()) ? "\"Email\"," : "";
        header += (headerYear.isSelected())?"`year`,":"";
        headerCol += (headerYear.isSelected()) ? "\"Year\"," : "";
        header += (headerCourse.isSelected())?"`course`,":"";
        headerCol += (headerCourse.isSelected()) ? "\"Course\"," : "";
        header += (headerBlock.isSelected())?"`block`,":"";
        headerCol += (headerBlock.isSelected()) ? "\"Block\"," : "";
        header += (headerStatus.isSelected())?"`status`,":"";
        headerCol += (headerStatus.isSelected()) ? "\"Status\"," : "";
        
        if(header.equals("")){
            header="*";
        }
        else{
            header = header.substring(0,header.length()-1);
            manageStudentTablecol= headerCol.substring(0,headerCol.length()-1).replaceAll("\"", "").split(",");
        }
        String searchQuery = "SELECT "+header+" FROM `student_info`"
                + "WHERE `course` LIKE '%"+((!courseSortStudentCB.getSelectedItem().toString().equals("All Courses"))?courseSortStudentCB.getSelectedItem().toString():"")+"%' "
                + "AND `year` LIKE '%"+((!yearSortStudentCB.getSelectedItem().toString().equals("Year"))?yearSortStudentCB.getSelectedItem().toString():"")+"%' "
                + "AND CONCAT_WS(`rfid_id`, `student_id`, `first_name`, `middle_name`, `last_name`, `age`, `gender`, `address`, `contact_number`, `email`, `year`, `course`, `block`, `status`) LIKE '%"+searchStudent.getText()+"%'";
        
        manageStudentTablerow = qp.getAllRecord(searchQuery);
        rowCount.setText("Count: " + (manageStudentTablerow != null ? manageStudentTablerow.length : 0));
        manageStudentModel = new DefaultTableModel(manageStudentTablerow,manageStudentTablecol);
        manageStudentTable.setModel(manageStudentModel);
        manageStudentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        manageStudentTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        manageStudentTable.getColumnModel().getColumn(5).setPreferredWidth(25);
        manageStudentTable.getColumnModel().getColumn(6).setPreferredWidth(35);
        manageStudentTable.getColumnModel().getColumn(10).setPreferredWidth(25);
        manageStudentTable.getColumnModel().getColumn(12).setPreferredWidth(15);
        manageStudentTable.getColumnModel().getColumn(13).setPreferredWidth(40);
        JTableHeader tableHeader = manageStudentTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 32));
        //manageStudentTable.getColumnModel().getColumn(12).setPreferredWidth(50);
        
    }
    
    public void displayEvents(){
        addOddRowColorRenderer(manageEventTable);
        String query = "SELECT `events`.`event_id`, `events`.`event_name`, `events`.`date`, CASE WHEN `events`.`students_involved`= 'BSCS,BSIT-Elect,BSIT-FPST,BSF,BEEd,BSED- English,BSED- Math,BSM,' "
                + "THEN 'All Courses' ELSE `events`.`students_involved` END AS students_involved, `events`.`year`, `events`.`time_in_range`, `events`.`time_out_range`, "
                + "COUNT(`student_record`.`event`) AS `count` FROM `events` LEFT JOIN `student_record` ON `events`.`event_name` = `student_record`.`event` "
                + "WHERE `students_involved` LIKE '%"+((!courseSortEventCB.getSelectedItem().toString().equals("All Courses"))?courseSortEventCB.getSelectedItem().toString():"")+"%' "
                + "AND `year` LIKE '%"+((!yearSortEventCB.getSelectedItem().toString().equals("Year"))?yearSortEventCB.getSelectedItem().toString():"")+"%'"
                + "AND CONCAT_WS(`events`.`event_id`, `events`.`date`, `events`.`event_name`, `events`.`students_involved`, `events`.`year`, `events`.`time_in_range`, `events`.`time_out_range`, "
                + "`events`.`total_present`, `events`.`recent_event`) LIKE '%"+searchEvent.getText()+"%' "
                + "GROUP BY `events`.`event_id`, `events`.`event_name` ORDER BY CASE WHEN `events`.`recent_event` = 'recentEvent' "
                + "THEN 0 ELSE 1 END, `events`.`event_id` DESC;";
        //System.out.println("event "+query);
        eventsROw=qp.getAllRecord(query);
        eventModel = new DefaultTableModel(eventsROw,eventColumn);
        manageEventTable.setModel(eventModel);
        manageEventTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        manageEventTable.getColumnModel().getColumn(2).setPreferredWidth(20);
        manageEventTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        manageEventTable.getColumnModel().getColumn(4).setPreferredWidth(20);
        manageEventTable.getColumnModel().getColumn(7).setPreferredWidth(15);
        JTableHeader tableHeader = manageEventTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 32));
        
    }
    
    public void displayCSVTable(String csv){
        addOddRowColorRenderer(viewCSVTable);
        
        if(csvType.equals("event")){
            String query="Select * from `temp_student_record`";
            viewCSVRow = qp.getAllRecord(query);
            viewCSVModel = new DefaultTableModel(viewCSVRow, viewCSVColumnRecords);
            viewCSVTable.setModel(viewCSVModel);
            viewCSVTable.getColumnModel().getColumn(0).setPreferredWidth(10);
            viewCSVTable.getColumnModel().getColumn(2).setPreferredWidth(20);
            viewCSVTable.getColumnModel().getColumn(3).setPreferredWidth(200);
            viewCSVTable.getColumnModel().getColumn(4).setPreferredWidth(20);
            viewCSVTable.getColumnModel().getColumn(7).setPreferredWidth(15);
            JTableHeader tableHeader = viewCSVTable.getTableHeader();
            tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 32));
        }
        else if (csvType.equals("view")){
            String query = "SELECT `student_info`.`student_id`, `student_info`.`last_name`, `student_info`.`first_name`, IFNULL(LEFT(`middle_name`, 1), '') AS `first_letter1`,`student_info`.`course`,"
                + "CASE WHEN time_out IS NULL THEN 'Time-in' ELSE 'Time-out' END AS time_status, TIME_FORMAT(`student_record`.`time_in`, '%h:%i %p') AS formatted_time_in,"
                + "CASE WHEN time_out IS NULL THEN `student_record`.`status` ELSE `student_record`.`status_timeout` END AS student_status FROM `student_record`, `student_info` WHERE `student_record`.`student_id` = `student_info`.`student_id` "
                + "AND `student_record`.`event` = '"+getRowData(manageEventTable, eventModel, 1)+"' AND `student_record`.`date` = '"+getRowData(manageEventTable, eventModel, 2)+"' "
                + "AND `course` LIKE '%"+((!courseSortCB1.getSelectedItem().toString().equals("All Courses"))?courseSortCB1.getSelectedItem().toString():"")+"%' "
                + "AND `year` LIKE '%"+((!yearSortCB1.getSelectedItem().toString().equals("Year"))?yearSortCB1.getSelectedItem().toString():"")+"%' "
                + " "+inOut+" AND CONCAT_WS(`student_info`.`student_id`,  `student_info`.`last_name`, `student_info`.`first_name`, `student_info`.`course`,"
                + "`student_info`.`age`,`student_info`.`gender`,`student_info`.`address`,`student_info`.`contact_number`,`student_info`.`status`) LIKE '%"+search1.getText()+"%' ORDER BY time_in DESC, time_out DESC;";
            viewCSVRow = qp.getAllRecord(query);
            viewCSVModel = new DefaultTableModel(viewCSVRow, tablecol);
            viewCSVTable.setModel(viewCSVModel);
            System.out.println("view "+query);
        }
        else{
            String query="Select * from `temp_student_info`";
            viewCSVRow = qp.getAllRecord(query);
            viewCSVModel = new DefaultTableModel(viewCSVRow, viewCSVColumn);
            viewCSVTable.setModel(viewCSVModel);
                    manageStudentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            viewCSVTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            viewCSVTable.getColumnModel().getColumn(5).setPreferredWidth(25);
            viewCSVTable.getColumnModel().getColumn(6).setPreferredWidth(35);
            viewCSVTable.getColumnModel().getColumn(10).setPreferredWidth(25);
            viewCSVTable.getColumnModel().getColumn(12).setPreferredWidth(15);
            viewCSVTable.getColumnModel().getColumn(13).setPreferredWidth(40);
            JTableHeader tableHeader = viewCSVTable.getTableHeader();
            tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 32));
            viewCSVTable.getColumnModel().getColumn(12).setPreferredWidth(50);
        }
    }
    
    public void displayTimeSpinner(String time, JSpinner spinner){
        
        
        int hour = (time.contains("pm"))? Integer.parseInt(time.split(":")[0])+12:Integer.parseInt(time.split(":")[0]);        
//        int hour = Integer.parseInt(time.substring(0, 2).replace("0", "").replace(":", ""));
        int minute = Integer.parseInt(time.substring(2,5).replace(":", "").replaceAll(" ", ""));
        
        //System.out.println("Time Spinner"+hour+" "+minute);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Date startTime = calendar.getTime();

        SpinnerDateModel spinnerDateModel = new SpinnerDateModel(startTime, null, null, Calendar.HOUR_OF_DAY);
        spinner.setModel(spinnerDateModel);  // Update the model of the provided spinner

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinner, format.toPattern());
        spinner.setEditor(dateEditor);

        //System.out.println("Time Spinner");
    }
    
    public String getRowData(JTable table, DefaultTableModel tableModel, int row){
        Object cell = tableModel.getValueAt(table.getSelectedRow(),row);
        return (tableModel.getValueAt(table.getSelectedRow(),row)!=null?cell.toString():null);
}
    
    public void insertStudentAttendance(String rfidId){
        //System.out.println("Inserting Student Attendance");
        
        String studentId, lastName, firstName, middleInit,timeIn=time,timeOut= null, status, course, year, block;
        String query = "Select * FROM student_info WHERE `rfid_id`= '"+rfidId+"' AND `course` IN ("+courseInvolved+") AND `year` IN ("+yearInvolved+")";
        String[] result=null;
        try{
            result = qp.getSpecificRow(query);
        }
        catch(Exception e){
            
        }
        
        if(result!=null){
            //System.out.println("String is "+Arrays.toString(result));
            studentId = result[1];
            lastName = result[4];
            firstName= result[2];
            middleInit = (result[3].length()>0)?result[3].substring(0, 1):"";
            status= "";
            course = result[11];
            year= result[10];
            block= result[12];
            String query1="";
            //System.out.println(scannedRFID+" "+scanTime+" "+scanType+" "+firstName);
            String name="";
            if(firstName.trim().split("\\s+").length>1){
                  name = lastName+" "+firstName.substring(0, 1)+""+firstName.substring(firstName.indexOf(" ")+1,firstName.indexOf(" ")+2 )+".";
            }
            else{
                  name = lastName+" "+firstName.substring(0, 1)+".";
            }
            String courseYear = course+" "+year+""+block;
           // String nameCourse = name.substring(0,16)+courseYear;
            //System.out.println(scannedRFID.toString());
            
            if(timeInStart!=null && timeOutStart!=null && timedifference(timeOutEnd, time)<0 && !scannedRFID.contains(rfidId)){
                executeArduinoWrite("ledrAttendance","Closed",2);
            }
            else if (!scannedRFID.contains(rfidId)){
                //System.out.println(time+" "+timeInStart+" "+rfidId+" "+firstName);
                if( timeInStart!=null && timedifference(time, timeInStart) < 0){
                    
                    executeArduinoWrite("ledrTime-in","Too Early",2);
                }
                else{
                    if(null!=timeInStart){
                        if(timedifference(timeInEnd, time) < 0){
                            status="Late";
                        }
                        else if(timedifference(time, timeInStart) >= 0){
                            status="On-time";
                        }
                }
                    scannedRFID.add(rfidId);
                    scanTime.add(time);
                    scanType.add("Time-in");
                    query1 = "INSERT IGNORE into `student_record` (`student_id`,`rfid_id`,`event`,`date`,`time_in`,`time_out`,`status`) VALUES "
                           + "('"+studentId+"','"+rfidId+"','"+rawEvent+"','"+date+"',STR_TO_DATE('"+time+"', '%h:%i %p'),NULL,'"+status+"')"; 
                    System.out.println(query1);
                }
      
            }
            else{
                if( scanType.get(scannedRFID.indexOf(rfidId)).equals("Time-in")){
                   //System.out.println(time+" "+timeOutStart+" "+rfidId+" "+firstName);
                    //if time in is not null and too early
                    if( timeOutStart!=null && timedifference(time, timeOutStart) < 0){
                        System.out.println("diff "+timedifference(time, scanTime.get(scannedRFID.indexOf(rfidId))));
                        if(timeInEnd!=null && timeInEnd!=null && timedifference(timeInEnd, time) > 0){
                            executeArduinoWrite("ledrAlready "+scanType.get(scannedRFID.indexOf(rfidId)).substring(5),name,2);
                        }
                        
                        else{
                            executeArduinoWrite("ledrTime-out","Too Early",2);
                        }
                        
                    }
                    
                    else{
                        //if time-out is not null
                        if(null!=timeOutStart && timedifference(time, scanTime.get(scannedRFID.indexOf(rfidId))) > Integer.parseInt(waitingTime.getText())){
                            if(timedifference(time, timeOutStart) >= 0){
                                status="On-Time";
                            }
                            else if(timedifference(timeOutEnd, time) < 0){
                                status="Late";
                            }
                            
                            scanType.set(scannedRFID.indexOf(rfidId),"Time-out");
                            query1 = "UPDATE `student_record` SET `time_out` = STR_TO_DATE('"+time+"', '%h:%i %p'), `status_timeout`='"+status+"' WHERE rfid_id ='"+rfidId+"' AND `event`= '"+rawEvent+"'";
                         }
                        //if time out is null
                        else if(timedifference(time, scanTime.get(scannedRFID.indexOf(rfidId))) > Integer.parseInt(waitingTime.getText())){
                            
                            scanType.set(scannedRFID.indexOf(rfidId),"Time-out");
                            query1 = "UPDATE `student_record` SET `time_out` = STR_TO_DATE('"+time+"', '%h:%i %p') WHERE rfid_id ='"+rfidId+"' AND `event`= '"+rawEvent+"'";
                        }
                        else{
                        executeArduinoWrite("ledrAlready "+scanType.get(scannedRFID.indexOf(rfidId)).substring(5),name,2);
                    }
                        
                        
                        
                    
                    }   
                     
                }
                else{
                        executeArduinoWrite("ledrAlready "+scanType.get(scannedRFID.indexOf(rfidId)).substring(5),name,2);
                    }
                
                
                }
                //System.out.println("Query is " +query1);
                 if(!query1.isEmpty()){
                     qp.executeUpdate(query1);
                     executeArduinoWrite("ledb"+name,courseYear,2);
                     displayEvents();
                     resetMainFrame();
                     
                }
//                 else if(query1.isEmpty() && timeOutStart==null){
//                      executeArduinoWrite("ledrAlready "+scanType.get(scannedRFID.indexOf(rfidId)).substring(5),name,2);
//                 }
//                 
            }
        else{
            //System.out.println("No record found");
            executeArduinoWrite("ledrNo Record Found!","ID: "+rfidId,2);
        }
        
    }
    
    public long timedifference(String time1, String time2){
        //System.out.println("Getting time diffrence");
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
        
        Date date1;
        Date date2;
        long differenceInMinutes = 0;
        try {
            date1 = format.parse(time1);
            date2 = format.parse(time2);
            long differenceInMillis = date1.getTime() - date2.getTime();
            differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
        //System.out.println("diff "+differenceInMinutes);
        return differenceInMinutes;
    }
    
    public String checkStudentIDRecord(String id, String table){
        if(qp.getSpecificRow("Select `student_id` FROM `"+table+"` WHERE `student_id`="+id+"")!=null){
            return "ID exist";
        }
        return "";
    }
    public String checkStudentRFIDRecord(String rfid, String table){
        if (qp.getSpecificRow("Select `rfid_id` FROM `"+table+"` WHERE `rfid_id`='"+rfid+"'")!=null){
        return "Student ID exist";
        }
        return "";
    }
    
    public boolean checkIfMaindatabase(){
            return qp.getSpecificRow("SELECT * FROM `misctable` where `name`='testEvent'")!=null;
    }
    
    public boolean checkIfStudentExistOnNewRecord(String id){
        //System.out.println("id "+id);
        return qp.getSpecificRow("SELECT * FROM `temp_student_info` where `student_id`="+id+"")!=null;
    }
    
    public boolean addStudent(String table, String addType, String id, String rfid){
        ////System.out.println("adds "+getRowData(manageStudentTable, manageStudentModel, 0)+" "+rfid+" nor");
        //System.out.println("Adding student");
        String errorMessage="";
        String query="";
       
       if(addType==""){
           id= addIDNoTF.getText();
           rfid=addRFIDTF.getText();
                query="Insert into `"+table+"` values('"+addRFIDTF.getText()+"','"+addIDNoTF.getText()+"','"+addFNameTF.getText()
                +"','"+addMNameTF.getText()+"','"+addLNameTF.getText()+"','"+addAgeTF.getText()+"','"+genderCB.getSelectedItem().toString()+"','"+addAddressTF.getText()
                +"','"+contactNumTF.getText()+"','"+addEmailTF.getText()+"@bisu.edu.ph"+"',"+yearCB.getSelectedItem().toString().substring(0,1)+",'"+courseCB.getSelectedItem().toString()+"'"
                        + ",'"+blockCB.getSelectedItem().toString()+ "','"+statusCB.getSelectedItem().toString()+"'"+addType+") ";
          
            }
       else{
            query="Insert into `"+table+"` values('"+getRowData(manageStudentTable, manageStudentModel, 1)+"','"+getRowData(manageStudentTable, manageStudentModel, 0)
                   +"','"+getRowData(manageStudentTable, manageStudentModel, 2)+"','"+getRowData(manageStudentTable, manageStudentModel, 3)
                   +"','"+getRowData(manageStudentTable, manageStudentModel, 4)+"','"+getRowData(manageStudentTable, manageStudentModel, 5)
                   +"','"+getRowData(manageStudentTable, manageStudentModel, 6)+"','"+getRowData(manageStudentTable, manageStudentModel, 7)
                +"','"+getRowData(manageStudentTable, manageStudentModel, 8)+"','"+getRowData(manageStudentTable, manageStudentModel, 9)
                   +"',"+getRowData(manageStudentTable, manageStudentModel, 10)+",'"+getRowData(manageStudentTable, manageStudentModel, 11)+"'"
                        + ",'"+getRowData(manageStudentTable, manageStudentModel, 12)+ "','"+getRowData(manageStudentTable, manageStudentModel, 13)+"'"+addType+") ";
       }
        //System.out.println("add "+id+" "+addType+" nor");
            errorMessage+=checkStudentIDRecord(id, table);
            errorMessage+=checkStudentRFIDRecord(rfid,table);
         
            if(errorMessage.equals("") && qp.executeUpdate(query)){

                return true;
            }
        JOptionPane.showMessageDialog(null, errorMessage,"Error",JOptionPane.ERROR_MESSAGE);
        return false;

    }
    
    public boolean updateStudent(String id, String rfid, String table, String type){
        String errorMessage="";
        if(!addIDNoTF.getText().trim().equals(id)){
                errorMessage+=checkStudentIDRecord(addIDNoTF.getText(),table);
        }
        if(!addRFIDTF.getText().trim().equals(rfid)){
                errorMessage+=checkStudentRFIDRecord(addRFIDTF.getText(),table);
        }
            if(errorMessage.equals("") && qp.executeUpdate("UPDATE `"+table+"` SET `rfid_id`='"+addRFIDTF.getText()+"',`student_id`='"+addIDNoTF.getText()+"',`first_name`='"
                    +addFNameTF.getText()+"',`middle_name`='"+addMNameTF.getText()+"',`last_name`='"+addLNameTF.getText()+"',`age`='"+addAgeTF.getText()
                    +"',`gender`='"+genderCB.getSelectedItem().toString()+"',`address`='"+addAddressTF.getText()+"',`contact_number`='"
                    +contactNumTF.getText()+"',`email`='"+addEmailTF.getText().substring(0,addEmailTF.getText().length()-12)+"@bisu.edu.ph"+"',`year`='"+yearCB.getSelectedItem().toString().substring(0,1)+"',"
                    + "`course`='"+courseCB.getSelectedItem().toString()+"',`block`='"+blockCB.getSelectedItem().toString()+ "',`status`='"+statusCB.getSelectedItem().toString()
                    +"' "+type+" WHERE `student_id` = '"+id+"'")){
                return true;
            }
       JOptionPane.showMessageDialog(null, errorMessage,"Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public boolean deleteStudent(String table,String id){
        if(qp.executeUpdate("DELETE FROM `"+table+"` WHERE `student_id` ="+id)){
            return true;
        }
        return false;
    }
    
    public boolean addEvent(String recentEvent, String event, String year, String timeIn,String timeOut, String course){
        if(updateRecentEvent()&&qp.executeUpdate("Insert into `events`( `date`, `event_name`, `students_involved`, `year`, `time_in_range`, `time_out_range`,`recent_event`) values ('"+LocalDate.now().format(dateFormatter)+"','"+event+"','"+course
            +"','"+year+"',"+timeIn+","+timeOut+",'"+recentEvent+"')" )){
            return true;
        }
        return false;
    }
    
    public boolean updateEvent( String event, String year, String timeIn,String timeOut, String course, String id){
        if(qp.executeUpdate("UPDATE `events` SET `event_name`='"+event+"',`students_involved`='"+course+"',`year`='"+year+"',`time_in_range`="+timeIn+",`time_out_range`="+timeOut+" WHERE `event_id`="+Integer.parseInt(id)+"")){
            return true;
        }
        return false;
    }
    
    public boolean clearEvent(String event){
        if(qp.executeUpdate("DELETE FROM `student_record` WHERE `event` = '"+event+"'")){
            return true;
        }
        return false;
    }
    
    public boolean deleteEvent(String id){
        if(qp.executeUpdate("DELETE FROM `events` WHERE `event_id` = "+id+"")){
            return true;
        }
        return false;
    }
    
    public boolean resumeEvent(String event){
        updateRecentEvent();
            if(qp.executeUpdate("UPDATE `events` SET `recent_event`= 'recentEvent' WHERE `event_name` = '"+event+"'")){
                preProcess();
                displayRecentScans();
                displayManageStudentTable();
                displayEvents();
                displayStatistics();
                displayLCDMessage(3);
              return true;
            }
            return false;
    }
    
    public boolean checkEventExist(String event){
      if(qp.getSpecificRow("SELECT *  FROM `events` WHERE `event_name` = '"+event+"'")!=null){
        return true;
    }
      return false;
    }
    
    public void checkStudentList(){
        
        if(bscsCB.isSelected()&& bsitElectCB.isSelected()&&bsitFoodTechCB.isSelected()&&bsfCB.isSelected()&&beedCB.isSelected()&&bsedEnglish.isSelected()&&bsedMathCB.isSelected()&&bsm.isSelected()){
            allCoursesCB.setSelected(true);
        }
        else{
            allCoursesCB.setSelected(false);
        }
    }
    
    public void manageEventDialogConditions(String type){
        DateFormatSymbols symbols = new DateFormatSymbols();
        symbols.setAmPmStrings(new String[]{"am", "pm"});
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", symbols);
        String timeInStartFormatted = dateFormat.format(timeInStartSpinner.getValue());
        String timeInEndFormatted = dateFormat.format(timeInEndSpinner.getValue());
        String timeOutStartFormatted = dateFormat.format(timeOutStartSpinner.getValue());
        String timeOutEndFormatted = dateFormat.format(timeOutEndSpinner.getValue());
        String errorMsg="";

            if(eventNameTF.getText().isEmpty()){
                errorMsg += "- Event name required!\n";
            }
            if(checkEventExist(eventNameTF.getText()) && addEventRecordBtn.getText().equals("Add")){
                errorMsg += "- Event name already exist!\n";
            }
            if(!bscsCB.isSelected() && !bsfCB.isSelected() && !bsm.isSelected() && !beedCB.isSelected() && !bsitElectCB.isSelected() && !bsitFoodTechCB.isSelected() && !bsedEnglish.isSelected() && !bsedMathCB.isSelected()){
                errorMsg += "- No Course Selected!\n";
            }
            if(!firstYearCb.isSelected() && !seconYearCB.isSelected() && !thirdYearCB.isSelected() && !fourthYearCB.isSelected()){
                errorMsg += "- No Students Selected!\n";
            }
            if((timedifference(timeInEndFormatted, timeInStartFormatted)<0 || timeInStartFormatted.equals(timeInEndFormatted))&& !noTimeIn.isSelected()){
                errorMsg += "- Time in range is invalid or the same!\n";
            }
            if((timedifference(timeOutEndFormatted, timeOutStartFormatted)<0 || timeOutStartFormatted.equals(timeOutEndFormatted)) && !noTimeOut.isSelected()){
                errorMsg += "- Time out range is invalid or the same!\n";
            }
            if((timedifference( timeOutStartFormatted,timeInEndFormatted))<0 && !noTimeOut.isSelected() && !noTimeIn.isSelected()){
                errorMsg += "- Time out is earlier than time in!\n";
            }
        if(!errorMsg.equals("")){
            JOptionPane.showMessageDialog(null, errorMsg, type.substring(0, 1).toUpperCase() + type.substring(1)+" Event Error", JOptionPane.ERROR_MESSAGE);
        }
        else{
            

            String course="", year="";
            course += bscsCB.isSelected() ? bscsCB.getText() + "," : "";
            course += bsitElectCB.isSelected() ? bsitElectCB.getText() + "," : "";
            course += bsitFoodTechCB.isSelected() ? bsitFoodTechCB.getText() + "," : "";
            course += bsfCB.isSelected() ? bsfCB.getText() + "," : "";
            course += beedCB.isSelected() ? beedCB.getText() + "," : "";
            course += bsedEnglish.isSelected() ? bsedEnglish.getText() + "," : "";
            course += bsedMathCB.isSelected() ? bsedMathCB.getText() + "," : "";
            course += bsm.isSelected() ? bsm.getText() + "," : "";

            year += firstYearCb.isSelected() ? "1," : "";
            year += seconYearCB.isSelected() ? "2," : "";
            year += thirdYearCB.isSelected() ? "3," : "";
            year += fourthYearCB.isSelected() ? "4," : "";
            //System.out.println("course is "+course);
            
            String timeIn = (noTimeIn.isSelected()?null:"'"+timeInStartFormatted+timeInEndFormatted+"'");
            String timeOut = (noTimeOut.isSelected()?null:"'"+timeOutStartFormatted+timeOutEndFormatted+"'");
//            if (timeInStart == null && timeInEnd == null && timeOutStart==null && timeOutEnd==null){
//                timeIn=null;
//                timeOut=null;
//            }
            
            int confirm = JOptionPane.showConfirmDialog(null, "Confirm "+type+"ing this event?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            if(type.equals("add")){
                if(addEvent("recentEvent", eventNameTF.getText(),year,timeIn, timeOut,course)){ 
                    JOptionPane.showMessageDialog(null, "Event Added Successfully");
                    preProcess();
                    displayRecentScans();
                    displayManageStudentTable();
                    addEvent.setVisible(false);
                    timeInStart = noTimeIn.isSelected()?null:timeInStartFormatted;
                    timeInEnd = noTimeIn.isSelected()?null:timeInEndFormatted; 
                    timeOutStart = noTimeOut.isSelected()?null:timeOutStartFormatted;
                    timeOutEnd = noTimeOut.isSelected()?null:timeOutEndFormatted;
                }
            }
            else{
               if( updateEvent( eventNameTF.getText(), year, timeIn, timeOut, course, getRowData(manageEventTable, eventModel,0))){
                   JOptionPane.showMessageDialog(null, "Event Saved Successfully");
                   preProcess();
                   displayRecentScans();
                   displayManageStudentTable();
                   displayEvents();
                   addEvent.setVisible(false);
               }
            }
            
            
        }
            //System.out.println(" ");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editHeadingDialog = new javax.swing.JDialog();
        headerRFID = new javax.swing.JCheckBox();
        headerID = new javax.swing.JCheckBox();
        headerFN = new javax.swing.JCheckBox();
        headerLN = new javax.swing.JCheckBox();
        headerMN = new javax.swing.JCheckBox();
        headerAge = new javax.swing.JCheckBox();
        headerAddress = new javax.swing.JCheckBox();
        headerEmail = new javax.swing.JCheckBox();
        headerYear = new javax.swing.JCheckBox();
        headerCourse = new javax.swing.JCheckBox();
        headerBlock = new javax.swing.JCheckBox();
        headerStatus = new javax.swing.JCheckBox();
        headerGender = new javax.swing.JCheckBox();
        headerContact = new javax.swing.JCheckBox();
        saveHeader = new javax.swing.JButton();
        courseList = new javax.swing.JDialog();
        bscsCB = new javax.swing.JCheckBox();
        bsitFoodTechCB = new javax.swing.JCheckBox();
        bsitElectCB = new javax.swing.JCheckBox();
        bsfCB = new javax.swing.JCheckBox();
        beedCB = new javax.swing.JCheckBox();
        bsedMathCB = new javax.swing.JCheckBox();
        bsedEnglish = new javax.swing.JCheckBox();
        bsm = new javax.swing.JCheckBox();
        csvFrame = new javax.swing.JFrame();
        csvName = new javax.swing.JTextField();
        browseCSV = new javax.swing.JButton();
        uploadCSV = new javax.swing.JButton();
        cancelCSV = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        checkCSV = new javax.swing.JFrame();
        jScrollPane5 = new javax.swing.JScrollPane();
        viewCSVTable = new javax.swing.JTable();
        approve = new javax.swing.JButton();
        approveAllRow = new javax.swing.JButton();
        declineAllRow = new javax.swing.JButton();
        declineRow = new javax.swing.JButton();
        courseSortCB1 = new javax.swing.JComboBox<>();
        yearSortCB1 = new javax.swing.JComboBox<>();
        search1 = new javax.swing.JTextField();
        inOutCB1 = new javax.swing.JComboBox<>();
        settingsFrame = new javax.swing.JFrame();
        tabPanels = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        waitingTime = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        saveGeneralBtn = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        hostCB = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        hostConnection = new javax.swing.JLabel();
        hostCustom = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        saveConnectionBtn = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        hostUsername = new javax.swing.JTextField();
        hostPassword = new javax.swing.JPasswordField();
        loadingDialogFrame = new javax.swing.JFrame();
        progressBars = new javax.swing.JProgressBar();
        progressLbl = new javax.swing.JLabel();
        manageStudentFrame = new javax.swing.JFrame();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        addStudentBtn = new javax.swing.JButton();
        importStudentCSV = new javax.swing.JButton();
        updateStudentBtn = new javax.swing.JButton();
        deleteStudentBtn = new javax.swing.JButton();
        exportStudentCSVBtn = new javax.swing.JButton();
        pendingStudentCSV = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        searchStudent = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        manageStudentTable = new javax.swing.JTable();
        courseSortStudentCB = new javax.swing.JComboBox<>();
        yearSortStudentCB = new javax.swing.JComboBox<>();
        rowCount = new javax.swing.JLabel();
        manageEventFrame = new javax.swing.JFrame();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        addEventBtn1 = new javax.swing.JButton();
        importEventCSVBtn = new javax.swing.JButton();
        updateEventBtn = new javax.swing.JButton();
        deleteEventBtn = new javax.swing.JButton();
        resumeEventBtn = new javax.swing.JButton();
        clearEvent = new javax.swing.JButton();
        exportEventCSV = new javax.swing.JButton();
        overWriteEvent = new javax.swing.JButton();
        viewEvent = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        searchEvent = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        manageEventTable = new javax.swing.JTable();
        courseSortEventCB = new javax.swing.JComboBox<>();
        yearSortEventCB = new javax.swing.JComboBox<>();
        addEvent = new javax.swing.JFrame();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        eventNameTF = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        addEventRecordBtn = new javax.swing.JButton();
        firstYearCb = new javax.swing.JCheckBox();
        seconYearCB = new javax.swing.JCheckBox();
        thirdYearCB = new javax.swing.JCheckBox();
        fourthYearCB = new javax.swing.JCheckBox();
        allCoursesCB = new javax.swing.JCheckBox();
        customStudents = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 30);
        Date startTime = calendar.getTime();

        SpinnerDateModel sm = new SpinnerDateModel(startTime, null, null, Calendar.HOUR_OF_DAY);
        timeInStartSpinner = new javax.swing.JSpinner(sm);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 8);
        calendar2.set(Calendar.MINUTE, 00);
        Date startTime2 = calendar2.getTime();

        SpinnerDateModel sm2 = new SpinnerDateModel(startTime2, null, null, Calendar.HOUR_OF_DAY);
        timeInEndSpinner = new javax.swing.JSpinner(sm2);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.set(Calendar.HOUR_OF_DAY, 17);
        calendar4.set(Calendar.MINUTE, 0);
        Date startTime4 = calendar4.getTime();

        SpinnerDateModel sm4 = new SpinnerDateModel(startTime4, null, null, Calendar.HOUR_OF_DAY);
        timeOutEndSpinner = new javax.swing.JSpinner(sm4);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.set(Calendar.HOUR_OF_DAY, 16);
        calendar3.set(Calendar.MINUTE, 30);
        Date startTime3 = calendar3.getTime();

        SpinnerDateModel sm3 = new SpinnerDateModel(startTime3, null, null, Calendar.HOUR_OF_DAY);
        timeOutStartSpinner = new javax.swing.JSpinner(sm3);
        jLabel23 = new javax.swing.JLabel();
        noTimeOut = new javax.swing.JCheckBox();
        noTimeIn = new javax.swing.JCheckBox();
        addStudentsFrame = new javax.swing.JFrame();
        addRFIDTF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        addIDNoTF = new javax.swing.JTextField();
        addRFIDStatus = new javax.swing.JToggleButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        addFNameTF = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        addMNameTF = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        addLNameTF = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        addAgeTF = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        addAddressTF = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        addEmailTF = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        addStudentRecordBtn = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        yearCB = new javax.swing.JComboBox<>();
        courseCB = new javax.swing.JComboBox<>();
        blockCB = new javax.swing.JComboBox<>();
        statusCB = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        contactNumTF = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        genderCB = new javax.swing.JComboBox<>();
        jFrame4 = new javax.swing.JFrame();
        mainPanel = new javax.swing.JPanel();
        ribbonPanel = new javax.swing.JPanel();
        addEventBtn = new javax.swing.JButton();
        recentEventsBtn = new javax.swing.JButton();
        manageStudentsBtn = new javax.swing.JButton();
        settingsBtn = new javax.swing.JButton();
        recordsPanel = new javax.swing.JPanel();
        eventLabel = new javax.swing.JLabel();
        recordsTablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        recentRecordsTable = new javax.swing.JTable();
        searchTF = new javax.swing.JTextField();
        courseSortCB = new javax.swing.JComboBox<>();
        yearSortCB = new javax.swing.JComboBox<>();
        inOutCB = new javax.swing.JComboBox<>();
        exportCSV = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        enableAttendanceBtn = new javax.swing.JToggleButton();
        arduinoStatus = new javax.swing.JLabel();
        dateTimeLabel = new javax.swing.JLabel();
        databaseConnection = new javax.swing.JLabel();
        recentScanCount = new javax.swing.JLabel();
        statisticsPanel = new javax.swing.JPanel();
        totalStudentStats = new javax.swing.JLabel();
        line = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        totalPresent = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        studentPerCourseTable = new javax.swing.JTable();
        timedOut = new javax.swing.JLabel();
        timedIn = new javax.swing.JLabel();
        timeInLabel = new javax.swing.JLabel();
        timeOutLabel = new javax.swing.JLabel();

        editHeadingDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        editHeadingDialog.setTitle("Edit Headers");
        editHeadingDialog.setBackground(new java.awt.Color(255, 255, 255));
        editHeadingDialog.setIconImage(null);
        editHeadingDialog.setMinimumSize(new java.awt.Dimension(264, 275));

        headerRFID.setSelected(true);
        headerRFID.setText("RFID ID");

        headerID.setSelected(true);
        headerID.setText("ID No.");

        headerFN.setSelected(true);
        headerFN.setText("First Name");

        headerLN.setSelected(true);
        headerLN.setText("Last Name");

        headerMN.setSelected(true);
        headerMN.setText("Middle Name");

        headerAge.setSelected(true);
        headerAge.setText("Age");

        headerAddress.setSelected(true);
        headerAddress.setText("Address");

        headerEmail.setSelected(true);
        headerEmail.setText("Email");

        headerYear.setSelected(true);
        headerYear.setText("Year");

        headerCourse.setSelected(true);
        headerCourse.setText("Course");

        headerBlock.setSelected(true);
        headerBlock.setText("Block");

        headerStatus.setSelected(true);
        headerStatus.setText("Status");

        headerGender.setSelected(true);
        headerGender.setText("Gender");

        headerContact.setSelected(true);
        headerContact.setText("Contact No.");

        saveHeader.setText("Save");
        saveHeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveHeaderActionPerformed(evt);
            }
        });

        editHeadingDialog.setIconImage(img);

        javax.swing.GroupLayout editHeadingDialogLayout = new javax.swing.GroupLayout(editHeadingDialog.getContentPane());
        editHeadingDialog.getContentPane().setLayout(editHeadingDialogLayout);
        editHeadingDialogLayout.setHorizontalGroup(
            editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editHeadingDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(headerRFID, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(headerAge, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(headerMN)
                        .addComponent(headerFN, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                        .addComponent(headerLN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(headerID, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(headerGender, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerYear, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerBlock, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerContact, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editHeadingDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveHeader)
                .addGap(95, 95, 95))
        );
        editHeadingDialogLayout.setVerticalGroup(
            editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editHeadingDialogLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editHeadingDialogLayout.createSequentialGroup()
                        .addComponent(headerRFID)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(headerID)
                            .addComponent(headerContact)))
                    .addComponent(headerAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editHeadingDialogLayout.createSequentialGroup()
                        .addComponent(headerLN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerFN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerMN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerAge)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerGender))
                    .addGroup(editHeadingDialogLayout.createSequentialGroup()
                        .addComponent(headerEmail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerYear)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerCourse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerBlock)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerStatus)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveHeader)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        courseList.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        courseList.setTitle("Choose Course");
        courseList.setBackground(new java.awt.Color(255, 255, 255));
        courseList.setIconImage(null);
        courseList.setMinimumSize(new java.awt.Dimension(264, 156));
        courseList.setSize(new java.awt.Dimension(264, 156));

        bscsCB.setSelected(true);
        bscsCB.setText("BSCS");
        bscsCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bscsCBMouseClicked(evt);
            }
        });

        bsitFoodTechCB.setSelected(true);
        bsitFoodTechCB.setText("BSIT-FPST");
        bsitFoodTechCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bsitFoodTechCBMouseClicked(evt);
            }
        });

        bsitElectCB.setSelected(true);
        bsitElectCB.setText("BSIT-Elect");
        bsitElectCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bsitElectCBMouseClicked(evt);
            }
        });

        bsfCB.setSelected(true);
        bsfCB.setText("BSF");
        bsfCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bsfCBMouseClicked(evt);
            }
        });

        beedCB.setSelected(true);
        beedCB.setText("BEEd");
        beedCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                beedCBMouseClicked(evt);
            }
        });

        bsedMathCB.setSelected(true);
        bsedMathCB.setText("BSED- Math");
        bsedMathCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bsedMathCBMouseClicked(evt);
            }
        });
        bsedMathCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsedMathCBActionPerformed(evt);
            }
        });

        bsedEnglish.setSelected(true);
        bsedEnglish.setText("BSED- English");
        bsedEnglish.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bsedEnglishMouseClicked(evt);
            }
        });

        bsm.setSelected(true);
        bsm.setText("BSM");
        bsm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bsmMouseClicked(evt);
            }
        });

        courseList.setIconImage(img);

        javax.swing.GroupLayout courseListLayout = new javax.swing.GroupLayout(courseList.getContentPane());
        courseList.getContentPane().setLayout(courseListLayout);
        courseListLayout.setHorizontalGroup(
            courseListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(courseListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(courseListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bsfCB)
                    .addComponent(bsitFoodTechCB, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addComponent(bsitElectCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bscsCB, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(courseListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(courseListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(bsedEnglish, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                        .addComponent(beedCB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(bsedMathCB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(bsm))
                .addGap(14, 14, 14))
        );
        courseListLayout.setVerticalGroup(
            courseListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(courseListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(courseListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(courseListLayout.createSequentialGroup()
                        .addComponent(bscsCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsitElectCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsitFoodTechCB))
                    .addGroup(courseListLayout.createSequentialGroup()
                        .addComponent(beedCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsedMathCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsedEnglish)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(courseListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bsfCB)
                    .addComponent(bsm))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        csvFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        csvFrame.setTitle("Choose CSV File");
        csvFrame.setMinimumSize(new java.awt.Dimension(420, 130));

        browseCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_browse_folder_25px.png"))); // NOI18N
        browseCSV.setText("Browse");
        browseCSV.setFocusPainted(false);
        browseCSV.setIconTextGap(10);
        browseCSV.setRolloverEnabled(false);
        browseCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCSVActionPerformed(evt);
            }
        });

        uploadCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_download_25px_2.png"))); // NOI18N
        uploadCSV.setText("Import");
        uploadCSV.setFocusPainted(false);
        uploadCSV.setIconTextGap(10);
        uploadCSV.setName("event"); // NOI18N
        uploadCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadCSVActionPerformed(evt);
            }
        });

        cancelCSV.setText("Cancel");
        cancelCSV.setFocusPainted(false);
        cancelCSV.setIconTextGap(10);

        csvFrame.setIconImage(img);

        javax.swing.GroupLayout csvFrameLayout = new javax.swing.GroupLayout(csvFrame.getContentPane());
        csvFrame.getContentPane().setLayout(csvFrameLayout);
        csvFrameLayout.setHorizontalGroup(
            csvFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(csvFrameLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(csvFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(csvName, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                    .addGroup(csvFrameLayout.createSequentialGroup()
                        .addComponent(browseCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(uploadCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        csvFrameLayout.setVerticalGroup(
            csvFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(csvFrameLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(csvName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(csvFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browseCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(uploadCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        checkCSV.setTitle("View Table");
        checkCSV.setMinimumSize(new java.awt.Dimension(975, 380));

        viewCSVTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        viewCSVTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewCSVTableMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(viewCSVTable);

        approve.setText("Approve");
        approve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                approveActionPerformed(evt);
            }
        });

        approveAllRow.setText("Approve All");
        approveAllRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                approveAllRowActionPerformed(evt);
            }
        });

        declineAllRow.setText("Decline All");
        declineAllRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                declineAllRowActionPerformed(evt);
            }
        });

        declineRow.setText("Decline");
        declineRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                declineRowActionPerformed(evt);
            }
        });

        courseSortCB1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Courses", "BSCS", "BSIT-Elect", "BSIT-FPST", "BSF", "BEEd", "BSED- English", "BSED- Math", "BSM" }));
        courseSortCB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                courseSortCB1ActionPerformed(evt);
            }
        });

        yearSortCB1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Year", "1", "2", "3", "4" }));
        yearSortCB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearSortCB1ActionPerformed(evt);
            }
        });

        search1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                search1KeyReleased(evt);
            }
        });

        inOutCB1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "In/Out", "Time-in", "Time-out" }));
        inOutCB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inOutCB1ActionPerformed(evt);
            }
        });

        checkCSV.setIconImage(img);

        javax.swing.GroupLayout checkCSVLayout = new javax.swing.GroupLayout(checkCSV.getContentPane());
        checkCSV.getContentPane().setLayout(checkCSVLayout);
        checkCSVLayout.setHorizontalGroup(
            checkCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkCSVLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(checkCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(checkCSVLayout.createSequentialGroup()
                        .addComponent(inOutCB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(courseSortCB1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearSortCB1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(search1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(checkCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 930, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(checkCSVLayout.createSequentialGroup()
                            .addComponent(declineAllRow, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(declineRow, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(approveAllRow, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(approve, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        checkCSVLayout.setVerticalGroup(
            checkCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkCSVLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(checkCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(checkCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(search1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(yearSortCB1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(courseSortCB1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(checkCSVLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(inOutCB1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(checkCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(approve)
                    .addComponent(approveAllRow)
                    .addComponent(declineAllRow)
                    .addComponent(declineRow))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        settingsFrame.setTitle("Settings");
        settingsFrame.setMinimumSize(new java.awt.Dimension(560, 381));

        jLabel31.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel31.setText("Timeout Waiting Time:");

        waitingTime.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        waitingTime.setText("1");
        waitingTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waitingTimeActionPerformed(evt);
            }
        });
        waitingTime.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                waitingTimeKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                waitingTimeKeyTyped(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel32.setText("minute/s");

        saveGeneralBtn.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        saveGeneralBtn.setText("Save");
        saveGeneralBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveGeneralBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(waitingTime, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(208, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveGeneralBtn)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(waitingTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 250, Short.MAX_VALUE)
                .addComponent(saveGeneralBtn)
                .addGap(17, 17, 17))
        );

        tabPanels.addTab("General", jPanel7);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Database Connection:");

        hostCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "localhost" }));

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel27.setText("Host:");

        hostConnection.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hostConnection.setText("Main");

        hostCustom.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                hostCustomMouseClicked(evt);
            }
        });

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_help_15px_1.png"))); // NOI18N
        jLabel29.setToolTipText("Check IpV4 in cmd, type ipconfig and input Ipv4 address here");

        saveConnectionBtn.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        saveConnectionBtn.setText("Save Connection");
        saveConnectionBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConnectionBtnActionPerformed(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel28.setText("Password:");

        jLabel30.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel30.setText("Username:");

        hostUsername.setText("root");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveConnectionBtn))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(hostConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(hostCB, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(hostCustom, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(hostPassword)
                                            .addComponent(hostUsername))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 23, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(hostConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(hostCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostCustom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(hostUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(hostPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 160, Short.MAX_VALUE)
                .addComponent(saveConnectionBtn)
                .addContainerGap())
        );

        tabPanels.addTab("Connection", jPanel8);

        settingsFrame.setIconImage(img);

        javax.swing.GroupLayout settingsFrameLayout = new javax.swing.GroupLayout(settingsFrame.getContentPane());
        settingsFrame.getContentPane().setLayout(settingsFrameLayout);
        settingsFrameLayout.setHorizontalGroup(
            settingsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsFrameLayout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(tabPanels, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        settingsFrameLayout.setVerticalGroup(
            settingsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPanels, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPanels.getAccessibleContext().setAccessibleName("TabPanels");

        loadingDialogFrame.setAlwaysOnTop(true);
        loadingDialogFrame.setLocationByPlatform(true);
        loadingDialogFrame.setMinimumSize(new java.awt.Dimension(291, 68));
        loadingDialogFrame.setUndecorated(true);
        loadingDialogFrame.setLocationRelativeTo(null);

        progressLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout loadingDialogFrameLayout = new javax.swing.GroupLayout(loadingDialogFrame.getContentPane());
        loadingDialogFrame.getContentPane().setLayout(loadingDialogFrameLayout);
        loadingDialogFrameLayout.setHorizontalGroup(
            loadingDialogFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loadingDialogFrameLayout.createSequentialGroup()
                .addGroup(loadingDialogFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(loadingDialogFrameLayout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(progressLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(loadingDialogFrameLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(progressBars, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        loadingDialogFrameLayout.setVerticalGroup(
            loadingDialogFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loadingDialogFrameLayout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(progressLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBars, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        manageStudentFrame.setTitle("Manage Students");
        manageStudentFrame.setMinimumSize(new java.awt.Dimension(981, 570));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        addStudentBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        addStudentBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_add_user_male_40px.png"))); // NOI18N
        addStudentBtn.setText("Add");
        addStudentBtn.setToolTipText("Add Student Manually");
        addStudentBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addStudentBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addStudentBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addStudentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudentBtnActionPerformed(evt);
            }
        });

        importStudentCSV.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        importStudentCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_export_csv_40px.png"))); // NOI18N
        importStudentCSV.setText("Import CSV");
        importStudentCSV.setToolTipText("Import CSV for multiple students");
        importStudentCSV.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        importStudentCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        importStudentCSV.setMargin(new java.awt.Insets(2, 2, 2, 2));
        importStudentCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        importStudentCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importStudentCSVActionPerformed(evt);
            }
        });

        updateStudentBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        updateStudentBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_registration_40px.png"))); // NOI18N
        updateStudentBtn.setText("Edit Student");
        updateStudentBtn.setToolTipText("Update Student");
        updateStudentBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        updateStudentBtn.setEnabled(false);
        updateStudentBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateStudentBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        updateStudentBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateStudentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateStudentBtnActionPerformed(evt);
            }
        });

        deleteStudentBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        deleteStudentBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_disposal_40px.png"))); // NOI18N
        deleteStudentBtn.setText("Delete Student");
        deleteStudentBtn.setToolTipText("Delete Student");
        deleteStudentBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        deleteStudentBtn.setEnabled(false);
        deleteStudentBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteStudentBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteStudentBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteStudentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteStudentBtnActionPerformed(evt);
            }
        });

        exportStudentCSVBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        exportStudentCSVBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_export_csv_40px.png"))); // NOI18N
        exportStudentCSVBtn.setText("Export CSV");
        exportStudentCSVBtn.setToolTipText("Export CSV for multiple students displayed on the table");
        exportStudentCSVBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        exportStudentCSVBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportStudentCSVBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        exportStudentCSVBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportStudentCSVBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportStudentCSVBtnActionPerformed(evt);
            }
        });

        pendingStudentCSV.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        pendingStudentCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_data_pending_40px.png"))); // NOI18N
        pendingStudentCSV.setText("Pending");
        pendingStudentCSV.setToolTipText("Import CSV for multiple students");
        pendingStudentCSV.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        pendingStudentCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pendingStudentCSV.setMargin(new java.awt.Insets(2, 2, 2, 2));
        pendingStudentCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pendingStudentCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pendingStudentCSVActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addStudentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importStudentCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pendingStudentCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportStudentCSVBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateStudentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteStudentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pendingStudentCSV)
                    .addComponent(exportStudentCSVBtn)
                    .addComponent(deleteStudentBtn)
                    .addComponent(updateStudentBtn)
                    .addComponent(importStudentCSV)
                    .addComponent(addStudentBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        searchStudent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchStudentKeyReleased(evt);
            }
        });

        manageStudentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        manageStudentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                manageStudentTableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(manageStudentTable);

        courseSortStudentCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Courses", "BSCS", "BSIT-Elect", "BSIT-FPST", "BSF", "BEEd", "BSED- English", "BSED- Math", "BSM" }));
        courseSortStudentCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                courseSortStudentCBActionPerformed(evt);
            }
        });

        yearSortStudentCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Year", "1", "2", "3", "4" }));
        yearSortStudentCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearSortStudentCBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 931, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(rowCount, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(415, 415, 415)
                        .addComponent(courseSortStudentCB, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearSortStudentCB, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(searchStudent)))
                .addGap(16, 16, 16))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(courseSortStudentCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(yearSortStudentCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(searchStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(rowCount, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        manageStudentFrame.setIconImage(img);

        javax.swing.GroupLayout manageStudentFrameLayout = new javax.swing.GroupLayout(manageStudentFrame.getContentPane());
        manageStudentFrame.getContentPane().setLayout(manageStudentFrameLayout);
        manageStudentFrameLayout.setHorizontalGroup(
            manageStudentFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageStudentFrameLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        manageStudentFrameLayout.setVerticalGroup(
            manageStudentFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        manageEventFrame.setTitle("Manage Events");
        manageEventFrame.setMinimumSize(new java.awt.Dimension(997, 579));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        addEventBtn1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        addEventBtn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_calendar_plus_40px.png"))); // NOI18N
        addEventBtn1.setText("Add Event");
        addEventBtn1.setToolTipText("Add Event");
        addEventBtn1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addEventBtn1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addEventBtn1.setMargin(new java.awt.Insets(2, 3, 2, 3));
        addEventBtn1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addEventBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEventBtn1ActionPerformed(evt);
            }
        });

        importEventCSVBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        importEventCSVBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_export_csv_40px.png"))); // NOI18N
        importEventCSVBtn.setText("Import CSV");
        importEventCSVBtn.setToolTipText("Add CSV for an event");
        importEventCSVBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        importEventCSVBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        importEventCSVBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        importEventCSVBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        importEventCSVBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importEventCSVBtnActionPerformed(evt);
            }
        });

        updateEventBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        updateEventBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_edit_property_40px.png"))); // NOI18N
        updateEventBtn.setText("Edit Event");
        updateEventBtn.setToolTipText("Update Event");
        updateEventBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        updateEventBtn.setEnabled(false);
        updateEventBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateEventBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        updateEventBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateEventBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEventBtnActionPerformed(evt);
            }
        });

        deleteEventBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        deleteEventBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_remove_property_40px.png"))); // NOI18N
        deleteEventBtn.setText("Delete Event");
        deleteEventBtn.setToolTipText("Delete Event");
        deleteEventBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        deleteEventBtn.setEnabled(false);
        deleteEventBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteEventBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteEventBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteEventBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEventBtnActionPerformed(evt);
            }
        });

        resumeEventBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        resumeEventBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_play_property_40px.png"))); // NOI18N
        resumeEventBtn.setText("Resume Event");
        resumeEventBtn.setToolTipText("Resume Event");
        resumeEventBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        resumeEventBtn.setEnabled(false);
        resumeEventBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resumeEventBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resumeEventBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resumeEventBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeEventBtnActionPerformed(evt);
            }
        });

        clearEvent.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        clearEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_delete_document_40px.png"))); // NOI18N
        clearEvent.setText("Clear Event");
        clearEvent.setToolTipText("Clear Records Event");
        clearEvent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        clearEvent.setEnabled(false);
        clearEvent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearEvent.setMargin(new java.awt.Insets(2, 2, 2, 2));
        clearEvent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearEventActionPerformed(evt);
            }
        });

        exportEventCSV.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        exportEventCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_import_csv_40px_1.png"))); // NOI18N
        exportEventCSV.setText("Export CSV");
        exportEventCSV.setToolTipText("Export selected event");
        exportEventCSV.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        exportEventCSV.setEnabled(false);
        exportEventCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportEventCSV.setMargin(new java.awt.Insets(2, 2, 2, 2));
        exportEventCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportEventCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportEventCSVActionPerformed(evt);
            }
        });

        overWriteEvent.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        overWriteEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_compose_40px.png"))); // NOI18N
        overWriteEvent.setText("Overwrite");
        overWriteEvent.setToolTipText("Overwrite existing event");
        overWriteEvent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        overWriteEvent.setEnabled(false);
        overWriteEvent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        overWriteEvent.setMargin(new java.awt.Insets(2, 3, 2, 3));
        overWriteEvent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        overWriteEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overWriteEventActionPerformed(evt);
            }
        });

        viewEvent.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        viewEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_view_40px.png"))); // NOI18N
        viewEvent.setText("View Event");
        viewEvent.setToolTipText("Resume Event");
        viewEvent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        viewEvent.setEnabled(false);
        viewEvent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewEvent.setMargin(new java.awt.Insets(2, 2, 2, 2));
        viewEvent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        viewEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewEventActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addEventBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(overWriteEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importEventCSVBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportEventCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateEventBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteEventBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(resumeEventBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(viewEvent)
                    .addComponent(overWriteEvent)
                    .addComponent(exportEventCSV)
                    .addComponent(clearEvent)
                    .addComponent(resumeEventBtn)
                    .addComponent(deleteEventBtn)
                    .addComponent(updateEventBtn)
                    .addComponent(importEventCSVBtn)
                    .addComponent(addEventBtn1))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        searchEvent.setForeground(new java.awt.Color(51, 51, 51));
        searchEvent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchEventKeyReleased(evt);
            }
        });

        manageEventTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        manageEventTable.setEditingColumn(1);
        manageEventTable.setEditingRow(1);
        manageEventTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                manageEventTableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(manageEventTable);

        courseSortEventCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Courses", "BSCS", "BSIT-Elect", "BSIT-FPST", "BSF", "BEEd", "BSED- English", "BSED- Math", "BSM" }));
        courseSortEventCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                courseSortEventCBActionPerformed(evt);
            }
        });

        yearSortEventCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Year", "1", "2", "3", "4" }));
        yearSortEventCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearSortEventCBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 931, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(543, 543, 543)
                        .addComponent(courseSortEventCB, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearSortEventCB, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(searchEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(searchEvent))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(courseSortEventCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(yearSortEventCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        manageEventFrame.setIconImage(img);

        javax.swing.GroupLayout manageEventFrameLayout = new javax.swing.GroupLayout(manageEventFrame.getContentPane());
        manageEventFrame.getContentPane().setLayout(manageEventFrameLayout);
        manageEventFrameLayout.setHorizontalGroup(
            manageEventFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageEventFrameLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        manageEventFrameLayout.setVerticalGroup(
            manageEventFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        addEvent.setTitle("Add Event");
        addEvent.setMinimumSize(new java.awt.Dimension(482, 255));

        jLabel17.setText("Course:");

        jLabel18.setText("Event Name:");

        eventNameTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventNameTFActionPerformed(evt);
            }
        });

        jLabel20.setText("Students:");

        jLabel21.setText("Time-in:");

        jLabel22.setText("Time-out");

        addEventRecordBtn.setText("Add");
        addEventRecordBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEventRecordBtnActionPerformed(evt);
            }
        });

        firstYearCb.setSelected(true);
        firstYearCb.setText("1st Year");
        firstYearCb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstYearCbActionPerformed(evt);
            }
        });

        seconYearCB.setSelected(true);
        seconYearCB.setText("2nd Year");
        seconYearCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seconYearCBActionPerformed(evt);
            }
        });

        thirdYearCB.setSelected(true);
        thirdYearCB.setText("3rd Year");
        thirdYearCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thirdYearCBActionPerformed(evt);
            }
        });

        fourthYearCB.setSelected(true);
        fourthYearCB.setText("4th Year");
        fourthYearCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fourthYearCBActionPerformed(evt);
            }
        });

        allCoursesCB.setSelected(true);
        allCoursesCB.setText("All Courses");
        allCoursesCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                allCoursesCBMouseClicked(evt);
            }
        });

        customStudents.setText("Custom");
        customStudents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customStudentsActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("to");

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        JSpinner.DateEditor de = new JSpinner.DateEditor(timeInStartSpinner, format.toPattern());
        timeInStartSpinner.setEditor(de);
        timeInStartSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                timeInStartSpinnerStateChanged(evt);
            }
        });
        timeInStartSpinner.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                timeInStartSpinnerMouseClicked(evt);
            }
        });

        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm a");
        JSpinner.DateEditor de2 = new JSpinner.DateEditor(timeInEndSpinner, format.toPattern());
        timeInEndSpinner.setEditor(de2);

        SimpleDateFormat format4 = new SimpleDateFormat("hh:mm a");
        JSpinner.DateEditor de4 = new JSpinner.DateEditor(timeOutEndSpinner, format4.toPattern());
        timeOutEndSpinner.setEditor(de4);

        SimpleDateFormat format3 = new SimpleDateFormat("hh:mm a");
        JSpinner.DateEditor de3 = new JSpinner.DateEditor(timeOutStartSpinner, format3.toPattern());
        timeOutStartSpinner.setEditor(de3);

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel23.setText("to");

        noTimeOut.setText("No Time-out");
        noTimeOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noTimeOutActionPerformed(evt);
            }
        });

        noTimeIn.setText("No Time-in");
        noTimeIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noTimeInActionPerformed(evt);
            }
        });

        addEvent.setIconImage(img);

        javax.swing.GroupLayout addEventLayout = new javax.swing.GroupLayout(addEvent.getContentPane());
        addEvent.getContentPane().setLayout(addEventLayout);
        addEventLayout.setHorizontalGroup(
            addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addEventLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addEventLayout.createSequentialGroup()
                        .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addEventLayout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addGap(45, 45, 45))
                            .addGroup(addEventLayout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addGap(39, 39, 39)))
                        .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(addEventLayout.createSequentialGroup()
                                .addComponent(timeInStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeInEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(addEventLayout.createSequentialGroup()
                                .addComponent(timeOutStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeOutEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(noTimeOut, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(noTimeIn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(addEventRecordBtn)
                        .addGroup(addEventLayout.createSequentialGroup()
                            .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(addEventLayout.createSequentialGroup()
                                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(22, 22, 22))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addEventLayout.createSequentialGroup()
                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                            .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(addEventLayout.createSequentialGroup()
                                    .addComponent(allCoursesCB)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(customStudents))
                                .addGroup(addEventLayout.createSequentialGroup()
                                    .addComponent(firstYearCb)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(seconYearCB)
                                    .addGap(12, 12, 12)
                                    .addComponent(thirdYearCB)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                    .addComponent(fourthYearCB))
                                .addComponent(eventNameTF)))))
                .addGap(22, 22, 22))
        );
        addEventLayout.setVerticalGroup(
            addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addEventLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eventNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel17)
                        .addComponent(customStudents))
                    .addComponent(allCoursesCB, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(seconYearCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(thirdYearCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fourthYearCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(firstYearCb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeInEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel19)
                        .addComponent(noTimeIn))
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel21)
                        .addComponent(timeInStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeOutEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel23)
                        .addComponent(noTimeOut))
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeOutStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel22)))
                .addGap(15, 15, 15)
                .addComponent(addEventRecordBtn)
                .addGap(28, 28, 28))
        );

        addStudentsFrame.setTitle("Add Student");
        addStudentsFrame.setMinimumSize(new java.awt.Dimension(425, 470));

        addRFIDTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRFIDTFActionPerformed(evt);
            }
        });

        jLabel3.setText("RFID ID:");

        jLabel4.setText("ID No. :");

        addIDNoTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                addIDNoTFKeyTyped(evt);
            }
        });

        addRFIDStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_off_20px.png"))); // NOI18N
        addRFIDStatus.setSelected(true);
        addRFIDStatus.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_off_20px.png"))); // NOI18N
        addRFIDStatus.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_on_20px.png"))); // NOI18N
        addRFIDStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRFIDStatusActionPerformed(evt);
            }
        });

        jLabel5.setText("RFID");

        jLabel6.setText("First Name:");

        jLabel7.setText("Middle Name:");

        addMNameTF.setToolTipText("optional");

        jLabel8.setText("Last Name:");

        jLabel9.setText("Age:");

        addAgeTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                addAgeTFKeyTyped(evt);
            }
        });

        jLabel10.setText("Address:");

        jLabel11.setText("Email:");

        jLabel12.setText("Course:");

        jLabel13.setText("Year:");

        jLabel15.setText("Status");

        addStudentRecordBtn.setText("Add");
        addStudentRecordBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudentRecordBtnActionPerformed(evt);
            }
        });

        jLabel24.setText("@bisu.edu.ph");

        yearCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1st Year", "2nd Year", "3rd Year", "4th Year" }));

        courseCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BSCS", "BSIT-Elect", "BSIT-FPST", "BSF", "BEEd", "BSED- English", "BSED- Math", "BSM" }));

        blockCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "A", "B", "C", "D", "E", "F" }));

        statusCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Regular", "Irregular" }));

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("Contact #:");

        contactNumTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contactNumTFActionPerformed(evt);
            }
        });
        contactNumTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                contactNumTFKeyTyped(evt);
            }
        });

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Block:");

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Gender:");

        genderCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));

        addStudentsFrame.setIconImage(img);

        javax.swing.GroupLayout addStudentsFrameLayout = new javax.swing.GroupLayout(addStudentsFrame.getContentPane());
        addStudentsFrame.getContentPane().setLayout(addStudentsFrameLayout);
        addStudentsFrameLayout.setHorizontalGroup(
            addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentsFrameLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(statusCB, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addIDNoTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(addStudentsFrameLayout.createSequentialGroup()
                                .addComponent(addAgeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(genderCB, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(addAddressTF, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addStudentsFrameLayout.createSequentialGroup()
                                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(addEmailTF, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                                        .addComponent(yearCB, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(addStudentRecordBtn)
                                        .addComponent(blockCB, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(courseCB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(contactNumTF, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                                        .addComponent(addRFIDTF, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(7, 7, 7)
                                        .addComponent(addRFIDStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(addFNameTF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(addMNameTF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(addLNameTF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        addStudentsFrameLayout.setVerticalGroup(
            addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentsFrameLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel4))
                    .addComponent(addIDNoTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addRFIDTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addRFIDStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))))
                .addGap(6, 6, 6)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel6))
                    .addComponent(addFNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel7))
                    .addComponent(addMNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel8))
                    .addComponent(addLNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(genderCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(addAgeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14))))
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel9)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(contactNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(addAddressTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel24)
                        .addComponent(addEmailTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel12))
                    .addGroup(addStudentsFrameLayout.createSequentialGroup()
                        .addComponent(courseCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(yearCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(blockCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))))
                .addGap(8, 8, 8)
                .addGroup(addStudentsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addComponent(addStudentRecordBtn)
                .addContainerGap(68, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jFrame4Layout = new javax.swing.GroupLayout(jFrame4.getContentPane());
        jFrame4.getContentPane().setLayout(jFrame4Layout);
        jFrame4Layout.setHorizontalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame4Layout.setVerticalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BISU Calape Attendance Monitoring System");
        setMinimumSize(new java.awt.Dimension(1033, 663));
        setSize(new java.awt.Dimension(1033, 663));

        mainPanel.setBackground(new java.awt.Color(204, 204, 204));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainPanel.setForeground(new java.awt.Color(255, 255, 255));

        ribbonPanel.setBackground(new java.awt.Color(255, 255, 255));
        ribbonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        addEventBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        addEventBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons88_calendar_plus_50px.png"))); // NOI18N
        addEventBtn.setText("Add Event");
        addEventBtn.setToolTipText("Add Event");
        addEventBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addEventBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addEventBtn.setMargin(new java.awt.Insets(2, 3, 2, 3));
        addEventBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addEventBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEventBtnActionPerformed(evt);
            }
        });

        recentEventsBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        recentEventsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_schedule_50px.png"))); // NOI18N
        recentEventsBtn.setText("Manage Events");
        recentEventsBtn.setToolTipText("Manage Recent Events");
        recentEventsBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        recentEventsBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        recentEventsBtn.setMargin(new java.awt.Insets(2, 4, 2, 4));
        recentEventsBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        recentEventsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentEventsBtnActionPerformed(evt);
            }
        });

        manageStudentsBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        manageStudentsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_select_user_50px.png"))); // NOI18N
        manageStudentsBtn.setText("Manage Students");
        manageStudentsBtn.setToolTipText("Manage Student Records");
        manageStudentsBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        manageStudentsBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        manageStudentsBtn.setMargin(new java.awt.Insets(2, 0, 2, 0));
        manageStudentsBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        manageStudentsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageStudentsBtnActionPerformed(evt);
            }
        });

        settingsBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        settingsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_settings_50px.png"))); // NOI18N
        settingsBtn.setText("Settings");
        settingsBtn.setToolTipText("Settings");
        settingsBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        settingsBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsBtn.setMargin(new java.awt.Insets(2, 0, 2, 0));
        settingsBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        settingsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ribbonPanelLayout = new javax.swing.GroupLayout(ribbonPanel);
        ribbonPanel.setLayout(ribbonPanelLayout);
        ribbonPanelLayout.setHorizontalGroup(
            ribbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ribbonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addEventBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recentEventsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manageStudentsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ribbonPanelLayout.setVerticalGroup(
            ribbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ribbonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ribbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recentEventsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addEventBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(manageStudentsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        recordsPanel.setBackground(new java.awt.Color(255, 255, 255));
        recordsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        eventLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        eventLabel.setText("Event: ");

        recordsTablePanel.setBackground(new java.awt.Color(255, 255, 255));
        recordsTablePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        recentRecordsTable.setAutoCreateRowSorter(true);
        recentRecordsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        recentRecordsTable.setName("recentScanTable"); // NOI18N
        jScrollPane1.setViewportView(recentRecordsTable);

        searchTF.setToolTipText("Search");
        searchTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchTFKeyReleased(evt);
            }
        });

        courseSortCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Courses", "BSCS", "BSIT-Elect", "BSIT-FPST", "BSF", "BEEd", "BSED- English", "BSED- Math", "BSM" }));
        courseSortCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                courseSortCBActionPerformed(evt);
            }
        });

        yearSortCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Year", "1", "2", "3", "4" }));
        yearSortCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearSortCBActionPerformed(evt);
            }
        });

        inOutCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "In/Out", "Time-in", "Time-out" }));
        inOutCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inOutCBActionPerformed(evt);
            }
        });

        exportCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_import_csv_15px_2.png"))); // NOI18N
        exportCSV.setText("Export CSV");
        exportCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportCSVActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_available_updates_20px.png"))); // NOI18N
        jButton2.setToolTipText("Refresh");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout recordsTablePanelLayout = new javax.swing.GroupLayout(recordsTablePanel);
        recordsTablePanel.setLayout(recordsTablePanelLayout);
        recordsTablePanelLayout.setHorizontalGroup(
            recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsTablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
                    .addGroup(recordsTablePanelLayout.createSequentialGroup()
                        .addComponent(exportCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inOutCB, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(courseSortCB, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearSortCB, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchTF, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        recordsTablePanelLayout.setVerticalGroup(
            recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsTablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(inOutCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(searchTF, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(courseSortCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yearSortCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(exportCSV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        jLabel16.setText("Enable Attendance:");

        enableAttendanceBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_off_20px.png"))); // NOI18N
        enableAttendanceBtn.setSelected(true);
        enableAttendanceBtn.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_off_20px.png"))); // NOI18N
        enableAttendanceBtn.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_on_20px.png"))); // NOI18N
        enableAttendanceBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                enableAttendanceBtnMouseClicked(evt);
            }
        });

        arduinoStatus.setText("Scan Status: ");

        dateTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        dateTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dateTimeLabel.setText("March 20, 2023 | 9:23 AM");

        databaseConnection.setText("Database:");

        recentScanCount.setText("Count:");

        javax.swing.GroupLayout recordsPanelLayout = new javax.swing.GroupLayout(recordsPanel);
        recordsPanel.setLayout(recordsPanelLayout);
        recordsPanelLayout.setHorizontalGroup(
            recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recordsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(recordsPanelLayout.createSequentialGroup()
                        .addComponent(eventLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dateTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(recordsPanelLayout.createSequentialGroup()
                        .addComponent(recentScanCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(37, 37, 37)
                        .addComponent(arduinoStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(databaseConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(enableAttendanceBtn)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );
        recordsPanelLayout.setVerticalGroup(
            recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eventLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recordsTablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableAttendanceBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel16)
                    .addComponent(arduinoStatus)
                    .addComponent(databaseConnection)
                    .addComponent(recentScanCount))
                .addContainerGap())
        );

        statisticsPanel.setBackground(new java.awt.Color(255, 255, 255));
        statisticsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        totalStudentStats.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        totalStudentStats.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        totalStudentStats.setText("/1290");
        totalStudentStats.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        totalStudentStats.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        line.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        javax.swing.GroupLayout lineLayout = new javax.swing.GroupLayout(line);
        line.setLayout(lineLayout);
        lineLayout.setHorizontalGroup(
            lineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );
        lineLayout.setVerticalGroup(
            lineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("Consolas", 1, 18)); // NOI18N
        jLabel2.setText("Students Present");

        totalPresent.setFont(new java.awt.Font("Century Gothic", 1, 48)); // NOI18N
        totalPresent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalPresent.setText("0");
        totalPresent.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        totalPresent.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        studentPerCourseTable.setAutoCreateRowSorter(true);
        studentPerCourseTable.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        studentPerCourseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ));
        studentPerCourseTable.setDoubleBuffered(true);
        studentPerCourseTable.setFocusable(false);
        studentPerCourseTable.setRowHeight(25);
        studentPerCourseTable.setRowMargin(2);
        jScrollPane2.setViewportView(studentPerCourseTable);

        timedOut.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        timedOut.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        timedOut.setText("Timed-out: ");
        timedOut.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        timedIn.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        timedIn.setText("Timed-in: ");

        timeInLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeInLabel.setText("   ");

        timeOutLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeOutLabel.setText("     ");

        javax.swing.GroupLayout statisticsPanelLayout = new javax.swing.GroupLayout(statisticsPanel);
        statisticsPanel.setLayout(statisticsPanelLayout);
        statisticsPanelLayout.setHorizontalGroup(
            statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statisticsPanelLayout.createSequentialGroup()
                .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statisticsPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(statisticsPanelLayout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(statisticsPanelLayout.createSequentialGroup()
                                    .addComponent(timedIn, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(timedOut, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(line, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(timeInLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(timeOutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(statisticsPanelLayout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(totalPresent, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalStudentStats, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        statisticsPanelLayout.setVerticalGroup(
            statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statisticsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPresent, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalStudentStats))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(line, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timedOut)
                    .addComponent(timedIn))
                .addGap(5, 5, 5)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(timeInLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeOutLabel)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(recordsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statisticsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(ribbonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(ribbonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statisticsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(recordsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void manageStudentsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageStudentsBtnActionPerformed
        manageStudentFrame.setVisible(true);
       
        displayManageStudentTable();
    }//GEN-LAST:event_manageStudentsBtnActionPerformed

    private void settingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsBtnActionPerformed
        // TODO add your handling code here:
          hostConnection.setText(checkIfMaindatabase()?"Main":"Local");
          hostUsername.setText(qp.username);
          hostPassword.setText(qp.password);
          hostCB.removeAllItems();
          hostCB.addItem("localhost");
          for (String host: qp.ipv4List){
              hostCB.addItem(host);
            
          }
          hostCB.setSelectedItem(qp.host);
         
          settingsFrame.setVisible(true);
    }//GEN-LAST:event_settingsBtnActionPerformed

    private void addStudentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudentBtnActionPerformed
        addStudentRecordBtn.setText("Add");
        addStudentsFrame.setTitle("Add Student");
        String rfidId = RFIDConnection.rfidId;
        
        addStudentsFrame.setVisible(true);
        
        addRFIDTF.setText(rfidId);
        
        
    }//GEN-LAST:event_addStudentBtnActionPerformed

    private void addEventBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventBtnActionPerformed
        // TODO add your handling code here:
        addEventRecordBtn.setText("Add");
        
        addEvent.setVisible(true);
        
    }//GEN-LAST:event_addEventBtnActionPerformed

    private void addStudentRecordBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudentRecordBtnActionPerformed
        updateStudentBtn.setEnabled(false);
        deleteStudentBtn.setEnabled(false);
        if(!addIDNoTF.equals("") && !addRFIDTF.equals("") && !addFNameTF.equals("") && !addMNameTF.equals("") && !addLNameTF.equals("")
               && !addAgeTF.equals("") && !addAddressTF.equals("") && !contactNumTF.equals("") && !addEmailTF.equals("")){
            if(addStudentRecordBtn.getText().equals("Add")){
                if(addStudent("student_info","",null,null)){
                    JOptionPane.showMessageDialog(null, "Student Added Successfully");
                    addStudentsFrame.setVisible(false);
                }
                if(!checkIfMaindatabase()){
                    addStudent("temp_student_info",",'add'",null,null);
                }
            }
            else{
                if(updateStudent(getRowData(manageStudentTable, manageStudentModel, 0),getRowData(manageStudentTable, manageStudentModel, 1),"student_info","" )){
                    
                    if(!checkIfMaindatabase() && checkIfStudentExistOnNewRecord(addIDNoTF.getText())){
                        updateStudent(getRowData(manageStudentTable, manageStudentModel, 0),getRowData(manageStudentTable, manageStudentModel, 1),"temp_student_info",",`type`='edit'");
                    }
                    else if(!checkIfMaindatabase() && !checkIfStudentExistOnNewRecord(addIDNoTF.getText())){
                        addStudent("temp_student_info",",'edit'",getRowData(manageStudentTable, manageStudentModel, 0),getRowData(manageStudentTable, manageStudentModel, 1));
                    }
                    
                    JOptionPane.showMessageDialog(null, "Student Saved Successfully");
                    addStudentsFrame.setVisible(false);
                }
            }
            preProcess();
            resetMainFrame();
            displayManageStudentTable();
        }
        else{
            JOptionPane.showMessageDialog(null, "Incomplete Information!","Error",JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_addStudentRecordBtnActionPerformed

    private void enableAttendanceBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_enableAttendanceBtnMouseClicked
        // TODO add your handling code here:
        if(!enableAttendanceBtn.isSelected()){
            executeArduinoWrite("Attendance","Paused",86400);
           
            //System.out.println("selected");
        }
        else{
            executeArduinoWrite("Attendance","Resumed",0);
        }
    }//GEN-LAST:event_enableAttendanceBtnMouseClicked

    private void bsedMathCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsedMathCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bsedMathCBActionPerformed

    private void bscsCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bscsCBMouseClicked
        // TODO add your handling code here:
        checkStudentList();
    }//GEN-LAST:event_bscsCBMouseClicked

    private void bsitElectCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bsitElectCBMouseClicked
        // TODO add your handling code here:
        checkStudentList();

    }//GEN-LAST:event_bsitElectCBMouseClicked

    private void bsitFoodTechCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bsitFoodTechCBMouseClicked
        // TODO add your handling code here:
        checkStudentList();
    }//GEN-LAST:event_bsitFoodTechCBMouseClicked

    private void bsfCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bsfCBMouseClicked
        // TODO add your handling code here:
        checkStudentList();
    }//GEN-LAST:event_bsfCBMouseClicked

    private void beedCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_beedCBMouseClicked
        // TODO add your handling code here:
        checkStudentList();
    }//GEN-LAST:event_beedCBMouseClicked

    private void bsedMathCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bsedMathCBMouseClicked
        // TODO add your handling code here:
        checkStudentList();
    }//GEN-LAST:event_bsedMathCBMouseClicked

    private void bsedEnglishMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bsedEnglishMouseClicked
        // TODO add your handling code here:
        checkStudentList();
    }//GEN-LAST:event_bsedEnglishMouseClicked

    private void bsmMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bsmMouseClicked
        // TODO add your handling code here:
        checkStudentList();
    }//GEN-LAST:event_bsmMouseClicked

    private void addEventBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventBtn1ActionPerformed
        // TODO add your handling code here:
        addEventBtnActionPerformed(evt);
    }//GEN-LAST:event_addEventBtn1ActionPerformed

    private void recentEventsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentEventsBtnActionPerformed
        // TODO add your handling code here:
        
        manageEventFrame.setVisible(true);
        
        displayEvents();
    }//GEN-LAST:event_recentEventsBtnActionPerformed

    private void manageEventTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manageEventTableMouseClicked
        // TODO add your handling code here:
        exportEventCSV.setEnabled(true);
        viewEvent.setEnabled(true);
        updateEventBtn.setEnabled(true);
        clearEvent.setEnabled(true);
        deleteEventBtn.setEnabled(true);
        overWriteEvent.setEnabled(true);
        if(getRowData(manageEventTable, eventModel,2).equals(date) && !event.equals(getRowData(manageEventTable, eventModel,1))){
            resumeEventBtn.setEnabled(true);
        }
        else{
            resumeEventBtn.setEnabled(false);
        }
        if(!getRowData(manageEventTable, eventModel, 1).equals("School Day "+LocalDate.now().format(DateTimeFormatter.ofPattern("YY-MM-dd")))){
            deleteEventBtn.setEnabled(true);
        }
        else{
            deleteEventBtn.setEnabled(false);
        }
    }//GEN-LAST:event_manageEventTableMouseClicked

    private void resumeEventBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeEventBtnActionPerformed
        // TODO add your handling code here:
         int confirm = JOptionPane.showConfirmDialog(null, "Confirm resume event?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            if(resumeEvent(getRowData(manageEventTable, eventModel, 1))){
                 JOptionPane.showMessageDialog(null, "Event Resumed Successfully");
            }
        }
    }//GEN-LAST:event_resumeEventBtnActionPerformed

    private void updateEventBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateEventBtnActionPerformed
        // TODO add your handling code here:
        addEvent.setTitle("Edit Event");
        addEventRecordBtn.setText("Save");
        
        String courseIncluded = getRowData(manageEventTable, eventModel, 3);
        String yearIncluded = getRowData(manageEventTable, eventModel, 4);
        
        
        eventNameTF.setText(getRowData(manageEventTable, eventModel, 1));
        allCoursesCB.setSelected((courseIncluded.contains("All Courses")));
        bscsCB.setSelected((courseIncluded.contains("BSCS") || courseIncluded.contains("All Courses")));
        bsitElectCB.setSelected((courseIncluded.contains("BSIT-Elect") || courseIncluded.contains("All Courses")));
        bsitFoodTechCB.setSelected((courseIncluded.contains("BSIT-FPST") || courseIncluded.contains("All Courses")));
        bsfCB.setSelected((courseIncluded.contains("BSF") || courseIncluded.contains("All Courses")));
        beedCB.setSelected((courseIncluded.contains("BEEd") || courseIncluded.contains("All Courses")));
        bsedEnglish.setSelected((courseIncluded.contains("BSED- English") || courseIncluded.contains("All Courses")));
        bsedMathCB.setSelected((courseIncluded.contains("SED- Math") || courseIncluded.contains("All Courses")));
        bsm.setSelected((courseIncluded.contains("BSM") || courseIncluded.contains("All Courses")));
        firstYearCb.setSelected((yearIncluded.contains("1")));
        seconYearCB.setSelected((yearIncluded.contains("2")));
        thirdYearCB.setSelected((yearIncluded.contains("3")));
        fourthYearCB.setSelected((yearIncluded.contains("4")));
        addEvent.setVisible(true);
        
        String timeIn= getRowData(manageEventTable, eventModel, 5);
        String timeOut= getRowData(manageEventTable, eventModel, 6);
        
        if(getRowData(manageEventTable, eventModel, 5)!=null){
            displayTimeSpinner(timeIn.split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[0], timeInStartSpinner);
            displayTimeSpinner(timeIn.split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[1], timeInEndSpinner);
            noTimeIn.setSelected(false);
            noTimeInActionPerformed(evt);
        }
        else{
            noTimeIn.setSelected(true);
            noTimeInActionPerformed(evt);
        }
        if(getRowData(manageEventTable, eventModel, 6)!=null){
            
            displayTimeSpinner(timeOut.split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[0], timeOutStartSpinner);
            displayTimeSpinner(timeOut.split("(?<!\\d)(?=(\\d{1,2}:\\d{2}\\s*[ap]m))")[1], timeOutEndSpinner);
            noTimeOut.setSelected(false);
            noTimeOutActionPerformed(evt);
        }
        else{
            noTimeOut.setSelected(true);
            noTimeOutActionPerformed(evt);
        }
    }//GEN-LAST:event_updateEventBtnActionPerformed

    private void deleteEventBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEventBtnActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm delete event?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            if(getRowData(manageEventTable, eventModel, 1).equals(rawEvent)){
                String id=getRowData(manageEventTable, eventModel, 0);
                if(resumeEvent("School Day "+LocalDate.now().format(DateTimeFormatter.ofPattern("YY-MM-dd"))) && deleteEvent(id)){
                     JOptionPane.showMessageDialog(null, "Event Deleted Successfully");
                }
                
                
            }
            else{
                if(deleteEvent(getRowData(manageEventTable, eventModel, 0))){
                    displayEvents();
                     JOptionPane.showMessageDialog(null, "Event Deleted Successfully");
                }
            }
        }
    }//GEN-LAST:event_deleteEventBtnActionPerformed

    private void clearEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearEventActionPerformed
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm clear event records?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            if(clearEvent(getRowData(manageEventTable, eventModel, 1))){
                JOptionPane.showMessageDialog(null, "Event Records Deleted Successfully");
            }
            if(getRowData(manageEventTable, eventModel, 1).equals(rawEvent)){
                resetMainFrame(); 
                preProcess();
                //System.out.println("schol");
            }
            
            displayEvents();
            
        }
            
    }//GEN-LAST:event_clearEventActionPerformed

    private void addRFIDTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRFIDTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addRFIDTFActionPerformed

    private void addRFIDStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRFIDStatusActionPerformed
        // TODO add your handling code here:
       
    }//GEN-LAST:event_addRFIDStatusActionPerformed

    private void addAgeTFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addAgeTFKeyTyped
        // TODO add your handling code here:
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_addAgeTFKeyTyped

    private void addIDNoTFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addIDNoTFKeyTyped
        // TODO add your handling code here:
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_addIDNoTFKeyTyped

    private void contactNumTFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contactNumTFKeyTyped
        // TODO add your handling code here:
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_contactNumTFKeyTyped

    private void updateStudentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateStudentBtnActionPerformed
        // TODO add your handling code here:
        addStudentRecordBtn.setText("Save");
        addStudentsFrame.setTitle("Edit Student");
        addIDNoTF.setText(getRowData(manageStudentTable, manageStudentModel, 0));
        addRFIDTF.setText(getRowData(manageStudentTable, manageStudentModel, 1));
        addFNameTF.setText(getRowData(manageStudentTable, manageStudentModel, 2));
        addMNameTF.setText(getRowData(manageStudentTable, manageStudentModel, 3));
        addLNameTF.setText(getRowData(manageStudentTable, manageStudentModel, 4));
        addAgeTF.setText(getRowData(manageStudentTable, manageStudentModel, 5));
        genderCB.setSelectedItem(getRowData(manageStudentTable, manageStudentModel, 6));
        addAddressTF.setText(getRowData(manageStudentTable, manageStudentModel, 7));
        contactNumTF.setText(getRowData(manageStudentTable, manageStudentModel, 8));
        addEmailTF.setText(getRowData(manageStudentTable, manageStudentModel, 9));
        yearCB.setSelectedIndex(Integer.parseInt(getRowData(manageStudentTable, manageStudentModel, 10))-1);
        courseCB.setSelectedItem(getRowData(manageStudentTable, manageStudentModel, 11));
        blockCB.setSelectedItem(getRowData(manageStudentTable, manageStudentModel, 12));
        statusCB.setSelectedItem(getRowData(manageStudentTable, manageStudentModel, 13));
        
        addStudentsFrame.setVisible(true);
        
        
        
       
    }//GEN-LAST:event_updateStudentBtnActionPerformed

    private void manageStudentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manageStudentTableMouseClicked
            // TODO add your handling code here:
       // selectedRowValue=getRowData(manageStudentTable, manageStudentModel, ERROR)
        exportStudentCSVBtn.setEnabled(true);
        updateStudentBtn.setEnabled(true);
        deleteStudentBtn.setEnabled(true);
    }//GEN-LAST:event_manageStudentTableMouseClicked

    private void contactNumTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contactNumTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_contactNumTFActionPerformed

    private void deleteStudentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteStudentBtnActionPerformed
        // TODO add your handling code here:
        ////System.out.println("row "+getRowData(manageStudentTable, manageStudentModel, 0));
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm deleting this student?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            String id=getRowData(manageStudentTable, manageStudentModel, 0);
            String rfid=getRowData(manageStudentTable, manageStudentModel, 1);
            if(!checkIfMaindatabase() && checkIfStudentExistOnNewRecord(id)){
                        updateStudent(id,rfid,"temp_student_info",",`type`='delete'");
            }
            else if(!checkIfMaindatabase() && !checkIfStudentExistOnNewRecord(id)){
                //System.out.println("row "+id);        
                addStudent("temp_student_info",",'delete'",id,rfid);
            }
            
            deleteStudent("student_info",getRowData(manageStudentTable, manageStudentModel, 0));
            JOptionPane.showMessageDialog(null, "Student Record Deleted Successfully");
            preProcess();
            resetMainFrame();
            displayManageStudentTable();
            
        }
        
    }//GEN-LAST:event_deleteStudentBtnActionPerformed

    private void searchTFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchTFKeyReleased
        // TODO add your handling code here:
        displayRecentScans();
        if(csvType.equals("view")){
            displayCSVTable("view");
        }
    }//GEN-LAST:event_searchTFKeyReleased

    private void courseSortCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_courseSortCBActionPerformed
        // TODO add your handling code here:
        displayRecentScans();
        if(csvType.equals("view")){
            displayCSVTable("view");
        }
    }//GEN-LAST:event_courseSortCBActionPerformed

    private void yearSortCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearSortCBActionPerformed
        // TODO add your handling code here:
        displayRecentScans();
        if(csvType.equals("view")){
            displayCSVTable("view");
        }
    }//GEN-LAST:event_yearSortCBActionPerformed

    private void courseSortStudentCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_courseSortStudentCBActionPerformed
        // TODO add your handling code here:
        displayManageStudentTable();
    }//GEN-LAST:event_courseSortStudentCBActionPerformed

    private void yearSortStudentCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearSortStudentCBActionPerformed
        // TODO add your handling code here:
        displayManageStudentTable();
    }//GEN-LAST:event_yearSortStudentCBActionPerformed

    private void searchStudentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchStudentKeyReleased
        // TODO add your handling code here:
        displayManageStudentTable();
    }//GEN-LAST:event_searchStudentKeyReleased

    private void saveHeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveHeaderActionPerformed
        // TODO add your handling code here:
        displayManageStudentTable();
        editHeadingDialog.setVisible(false);
    }//GEN-LAST:event_saveHeaderActionPerformed

    private void courseSortEventCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_courseSortEventCBActionPerformed
        // TODO add your handling code here:
        displayEvents();
    }//GEN-LAST:event_courseSortEventCBActionPerformed

    private void yearSortEventCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearSortEventCBActionPerformed
        // TODO add your handling code here:
        displayEvents();
    }//GEN-LAST:event_yearSortEventCBActionPerformed

    private void searchEventKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchEventKeyReleased
        // TODO add your handling code here:
        displayEvents();
    }//GEN-LAST:event_searchEventKeyReleased

    private void importEventCSVBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importEventCSVBtnActionPerformed
        // TODO add your handling code here:    
        
        csvFrame.setVisible(true);
        
        uploadCSV.setName("event");
        String insertQuery="INSERT INTO `student_record`(`student_id`, `rfid_id`, `event`, `date`, `time_in`, `time_out`, `status`,`status_timeout`) VALUES (?,?,?,?,?,?,?,?)";
        createtStatment(insertQuery);
    }//GEN-LAST:event_importEventCSVBtnActionPerformed

    private void browseCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseCSVActionPerformed
        // TODO add your handling code here:
        browseFiles();
    }//GEN-LAST:event_browseCSVActionPerformed

    private void uploadCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadCSVActionPerformed
        // TODO add your handling code here:
        
        if(uploadCSV.getName().equals("event")){
            if(importCSV(statement)){
                csvFrame.setVisible(false);
                
                displayEvents(); 
            }
            
        }else if(uploadCSV.getName().equals("student")){
            qp.executeUpdate("DELETE FROM `temp_student_info`;");
            csvType="student";
            if(importCSV(statement)){
                csvFrame.setVisible(false);
                displayManageStudentTable();
                if(checkIfMaindatabase()){
                   
                    checkCSV.setVisible(true);
                    displayCSVTable(csvType);
                    
                }
                resetMainFrame();
            }
        }
        else if(uploadCSV.getName().equals("overwrite")){
            qp.executeUpdate("DELETE FROM `temp_student_record`;");
            int confirm = JOptionPane.showConfirmDialog(null, "Confirm overwriting this event?\nCSV File: "+csvFileName+"\nto Overwrite to Event: "+getRowData(manageEventTable, eventModel, 2), "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            if(confirm == JOptionPane.YES_OPTION){
                csvType="event";
                if(importCSV(statement)){
                    csvFrame.setVisible(false);
                    
                    checkCSV.setVisible(true);
                    displayCSVTable(csvType);
                    

                }
            }
            
        }
        
        
    }//GEN-LAST:event_uploadCSVActionPerformed

    private void inOutCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inOutCBActionPerformed
        // TODO add your handling code here:
        if(inOutCB.getSelectedIndex()==0){
            inOut="";
        }
        else if (inOutCB.getSelectedIndex()==1){
            inOut="AND `time_out` IS NULL";
        }
        else if (inOutCB.getSelectedIndex()==2){
            inOut="AND `time_out` IS NOT NULL";
        }
             displayRecentScans();

    }//GEN-LAST:event_inOutCBActionPerformed

    private void exportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCSVActionPerformed
        // TODO add your handling code here:
        String query = csvQuery;
        try {
            ResultSet resultSet = qp.stmt.executeQuery(query);
            exportToCSV(resultSet, rawEvent,null);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        
    }//GEN-LAST:event_exportCSVActionPerformed

    private void importStudentCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importStudentCSVActionPerformed
        // TODO add your handling code here:
        csvFrame.setVisible(true);
        
        String insertQuery="";
        uploadCSV.setName("student");
        if(!checkIfMaindatabase()){
             insertQuery="Insert  IGNORE  into `student_info` values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        }else{
             insertQuery="Insert  IGNORE  into `temp_student_info` values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";   
        }
        createtStatment(insertQuery);
    }//GEN-LAST:event_importStudentCSVActionPerformed

    private void exportEventCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportEventCSVActionPerformed
        // TODO add your handling code here:
        String query = "SELECT `student_id`, `rfid_id`, `event`, DATE_FORMAT(`date`, '%Y-%m-%d') AS date_string, `time_in`, `time_out`, `status`, `status_timeout` FROM `student_record` WHERE `event`='"+getRowData(manageEventTable, eventModel, 1)+"'";
        String insertQuery="INSERT INTO `events`( `date`, `event_name`, `students_involved`, `year`, `time_in_range`, `time_out_range`, `total_present`) VALUES ("
                + "'"+getRowData(manageEventTable, eventModel, 2)+"','"+getRowData(manageEventTable, eventModel, 1)+"','"+(getRowData(manageEventTable, eventModel, 3).equals("All Courses")?"BSCS,BSIT-Elect,BSIT-FPST,BSF,BEEd,BSED- English,BSED- Math,BSM,":getRowData(manageEventTable, eventModel, 3))
                +"','"+getRowData(manageEventTable, eventModel, 4)+"',"+(getRowData(manageEventTable, eventModel, 5)!=null?"'"+getRowData(manageEventTable, eventModel, 5)+"'":null)+","+(getRowData(manageEventTable, eventModel, 6)!=null?"'"+getRowData(manageEventTable, eventModel, 6)+"'":null)
                +","+(getRowData(manageEventTable, eventModel, 7)!=null?"'"+getRowData(manageEventTable, eventModel, 7)+"'":null)+")";
        //System.out.println("export query is "+insertQuery);
        try {
            ResultSet resultSet = qp.stmt.executeQuery(query);

            exportToCSV(resultSet,getRowData(manageEventTable, eventModel, 1),insertQuery);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_exportEventCSVActionPerformed

    private void exportStudentCSVBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportStudentCSVBtnActionPerformed
        // TODO add your handling code here:s
        String query = "";
         if(checkIfMaindatabase()){
             query="SELECT * FROM `student_info` ";
        }else{
             query="SELECT * FROM `temp_student_info` ";
        }
        try {
            ResultSet resultSet = qp.stmt.executeQuery(query);
            exportToCSV(resultSet, "student_infos"+date,"Comment");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
       
    }//GEN-LAST:event_exportStudentCSVBtnActionPerformed

    private void viewCSVTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewCSVTableMouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_viewCSVTableMouseClicked

    private void approveAllRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_approveAllRowActionPerformed
        // TODO add your handling code here:
        String[][] allRecords = {{}};
        
            if(uploadCSV.getName().equals("student")){
                allRecords = qp.getAllRecord("SELECT * FROM `temp_student_info`");
                if(importStudentCSV(allRecords)){
                    qp.executeUpdate("DELETE FROM `temp_student_info`");
                    displayManageStudentTable();
                    resetMainFrame();
                }
            }else if(uploadCSV.getName().equals("event") || uploadCSV.getName().equals("overwrite")){
                System.out.println(csvFileName);
                int confirm = JOptionPane.showConfirmDialog(null, "Confirm approving all rows?\nCSV File: "+csvFileName+"\nto Overwrite to Event: "+getRowData(manageEventTable, eventModel, 2), "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if(confirm == JOptionPane.YES_OPTION){
                    allRecords = qp.getAllRecord("SELECT * FROM `temp_student_record`");
                     if(overwritetEventCSV(allRecords)){
                         qp.executeUpdate("DELETE FROM `temp_student_record`");
                         displayEvents();
                         resetMainFrame();
                     }
                }
            }
        
        displayCSVTable(csvType);
        
        
        
        
    }//GEN-LAST:event_approveAllRowActionPerformed

    private void pendingStudentCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pendingStudentCSVActionPerformed
        // TODO add your handling code here:
        checkCSV.setVisible(true);
        displayCSVTable("student");
        uploadCSV.setName("student");
        csvType="student";
        
    }//GEN-LAST:event_pendingStudentCSVActionPerformed

    private void approveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_approveActionPerformed
        // TODO add your handling code here:
        String[][] allRecords ={{}};
        String column="";
        if(uploadCSV.getName().equals("student")){
            allRecords = qp.getAllRecord("SELECT * FROM `temp_student_info` WHERE `student_id`="+getRowData(viewCSVTable, viewCSVModel, 1)+"");
            column=getRowData(viewCSVTable, viewCSVModel, 1);
            if(importStudentCSV(allRecords)){
                qp.executeUpdate("DELETE FROM `temp_student_info` where `student_id`="+column+"");
                displayManageStudentTable();
                displayCSVTable(csvType);
                resetMainFrame();
            }
        }else if(uploadCSV.getName().equals("event") || uploadCSV.getName().equals("overwrite")){
            allRecords = qp.getAllRecord("SELECT * FROM `temp_student_record` WHERE `student_id`="+getRowData(viewCSVTable, viewCSVModel, 0)+" AND `event`='"+getRowData(viewCSVTable, viewCSVModel, 2)+"'");            
            
            column=getRowData(viewCSVTable, viewCSVModel, 0);
            if(overwritetEventCSV(allRecords)){
                qp.executeUpdate("DELETE FROM `temp_student_record` where `student_id`="+column+"");
                displayEvents();
                displayCSVTable(csvType);
            }
        }
        
        
    }//GEN-LAST:event_approveActionPerformed

    private void declineRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declineRowActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm declining "+getRowData(viewCSVTable, viewCSVModel, 1)+"?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            if(csvType.equals("student")){
                deleteStudent("temp_student_info",getRowData(viewCSVTable, viewCSVModel, 1));
            }
            if(csvType.equals("event")){
                deleteStudent("temp_student_record",getRowData(viewCSVTable, viewCSVModel, 1));
            }
        }
        //System.out.println("type "+csvType);
        
        displayCSVTable(csvType);
    }//GEN-LAST:event_declineRowActionPerformed

    private void declineAllRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declineAllRowActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm declining all rows?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            if(csvType.equals("student")){
                qp.executeUpdate("DELETE FROM `temp_student_info`;");
            }
            if(csvType.equals("event")){
                qp.executeUpdate("DELETE FROM `temp_student_record`;");
            }
            
            displayCSVTable(csvType);
        }
    }//GEN-LAST:event_declineAllRowActionPerformed

    private void overWriteEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overWriteEventActionPerformed
        // TODO add your handling code here:
        csvFrame.setVisible(true);
        
        uploadCSV.setName("overwrite");
       // uploadCSV.setText("Overwrite");
        //uploadCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_compose_40px.png")));
        String  insertQuery ="Insert  IGNORE  into `temp_student_record` values (?,?,?,?,?,?,?,?)";  
        
        createtStatment(insertQuery);
    }//GEN-LAST:event_overWriteEventActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
            // TODO add your handling code here:
        restartApplication();
        displayLCDMessage(3);
        preProcess();
        resetMainFrame();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void saveConnectionBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveConnectionBtnActionPerformed
        // TODO add your handling code here:
        
        
        host=hostCustom.getText().isEmpty()?hostCB.getSelectedItem().toString():hostCustom.getText().toString();
        System.out.println("custom "+host);
        username=hostUsername.getText();
        password=hostPassword.getText();
        database="rfid_attendance";
        
        customUser=true;
        try {
            qp.connectToDatabase();
//        qp = new QueryProcessor();
        } catch (SQLException ex) {
            Logger.getLogger(HomePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        settingsFrame.setVisible(false);
        preProcess();
        resetMainFrame();
        
    }//GEN-LAST:event_saveConnectionBtnActionPerformed

    private void hostCustomMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hostCustomMouseClicked
        // TODO add your handling code here:
       
    }//GEN-LAST:event_hostCustomMouseClicked

    private void courseSortCB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_courseSortCB1ActionPerformed
        // TODO add your handling code here:
        courseSortCBActionPerformed(evt);
    }//GEN-LAST:event_courseSortCB1ActionPerformed

    private void yearSortCB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearSortCB1ActionPerformed
        // TODO add your handling code here:
        yearSortCBActionPerformed(evt);
    }//GEN-LAST:event_yearSortCB1ActionPerformed

    private void search1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search1KeyReleased
        // TODO add your handling code here:
        searchTFKeyReleased(evt);
    }//GEN-LAST:event_search1KeyReleased

    private void viewEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewEventActionPerformed
        // TODO add your handling code here:
        csvType="view";
        displayCSVTable(csvType);
        approve.setVisible(false);
        approveAllRow.setVisible(false);
        declineRow.setVisible(false);
        declineAllRow.setVisible(customUser);
        checkCSV.setVisible(true);
    }//GEN-LAST:event_viewEventActionPerformed

    private void inOutCB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inOutCB1ActionPerformed
        // TODO add your handling code here:
        if(inOutCB1.getSelectedIndex()==0){
            inOut="";
        }
        else if (inOutCB1.getSelectedIndex()==1){
            inOut="AND `time_out` IS NULL";
        }
        else if (inOutCB1.getSelectedIndex()==2){
            inOut="AND `time_out` IS NOT NULL";
        }
             displayCSVTable("view");
    }//GEN-LAST:event_inOutCB1ActionPerformed

    private void noTimeInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noTimeInActionPerformed
        // TODO add your handling code here:
        if(noTimeIn.isSelected()){
            timeInStartSpinner.setEnabled(false);
            timeInEndSpinner.setEnabled(false);
        }
        else{
            timeInStartSpinner.setEnabled(true);
            timeInEndSpinner.setEnabled(true);
        }
    }//GEN-LAST:event_noTimeInActionPerformed

    private void noTimeOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noTimeOutActionPerformed
        // TODO add your handling code here:
        if(noTimeOut.isSelected()){
            timeOutStartSpinner.setEnabled(false);
            timeOutEndSpinner.setEnabled(false);
        }
        else{
            timeOutStartSpinner.setEnabled(true);
            timeOutEndSpinner.setEnabled(true);
        }
    }//GEN-LAST:event_noTimeOutActionPerformed

    private void timeInStartSpinnerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_timeInStartSpinnerMouseClicked
        // TODO add your handling code here:
        ////System.out.println("time in"+ timeInStart.toString());
    }//GEN-LAST:event_timeInStartSpinnerMouseClicked

    private void timeInStartSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_timeInStartSpinnerStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_timeInStartSpinnerStateChanged

    private void customStudentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customStudentsActionPerformed
        // TODO add your handling code here:
        courseList.setVisible(rootPaneCheckingEnabled);
        
    }//GEN-LAST:event_customStudentsActionPerformed

    private void allCoursesCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_allCoursesCBMouseClicked
        // TODO add your handling code here:
        if(allCoursesCB.isSelected()){
            bscsCB.setSelected(true);
            bsitElectCB.setSelected(true);
            bsitFoodTechCB.setSelected(true);
            bsfCB.setSelected(true);
            beedCB.setSelected(true);
            bsedEnglish.setSelected(true);
            bsedMathCB.setSelected(true);
            bsm.setSelected(true);
        }
    }//GEN-LAST:event_allCoursesCBMouseClicked

    private void fourthYearCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fourthYearCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fourthYearCBActionPerformed

    private void thirdYearCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thirdYearCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_thirdYearCBActionPerformed

    private void seconYearCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seconYearCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_seconYearCBActionPerformed

    private void firstYearCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstYearCbActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_firstYearCbActionPerformed

    private void addEventRecordBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventRecordBtnActionPerformed
        // TODO add your handling code here:
        updateEventBtn.setEnabled(false);
        deleteEventBtn.setEnabled(false);
        clearEvent.setEnabled(false);
        if(addEventRecordBtn.getText().equals("Add")){
            manageEventDialogConditions("add");
        }
        else{
            manageEventDialogConditions("edit");
        }

    }//GEN-LAST:event_addEventRecordBtnActionPerformed

    private void eventNameTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventNameTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_eventNameTFActionPerformed

    private void waitingTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waitingTimeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_waitingTimeActionPerformed

    private void waitingTimeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_waitingTimeKeyTyped
        // TODO add your handling code here:
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_waitingTimeKeyTyped

    private void waitingTimeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_waitingTimeKeyReleased
        // TODO add your handling code here:
        
    }//GEN-LAST:event_waitingTimeKeyReleased

    private void saveGeneralBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveGeneralBtnActionPerformed
            // TODO add your handling code here:
     if(qp.executeUpdate("UPDATE `misctable` SET `c2`='"+waitingTime.getText()+"' WHERE `name`='waitingTime'")){
         JOptionPane.showMessageDialog(null, "Successfully Saved");
     }
    }//GEN-LAST:event_saveGeneralBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HomePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HomePanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addAddressTF;
    private javax.swing.JTextField addAgeTF;
    private javax.swing.JTextField addEmailTF;
    private javax.swing.JFrame addEvent;
    private javax.swing.JButton addEventBtn;
    private javax.swing.JButton addEventBtn1;
    private javax.swing.JButton addEventRecordBtn;
    private javax.swing.JTextField addFNameTF;
    private javax.swing.JTextField addIDNoTF;
    private javax.swing.JTextField addLNameTF;
    private javax.swing.JTextField addMNameTF;
    public javax.swing.JToggleButton addRFIDStatus;
    public javax.swing.JTextField addRFIDTF;
    private javax.swing.JButton addStudentBtn;
    private javax.swing.JButton addStudentRecordBtn;
    private javax.swing.JFrame addStudentsFrame;
    private javax.swing.JCheckBox allCoursesCB;
    private javax.swing.JButton approve;
    private javax.swing.JButton approveAllRow;
    public javax.swing.JLabel arduinoStatus;
    private javax.swing.JCheckBox beedCB;
    private javax.swing.JComboBox<String> blockCB;
    private javax.swing.JButton browseCSV;
    private javax.swing.JCheckBox bscsCB;
    private javax.swing.JCheckBox bsedEnglish;
    private javax.swing.JCheckBox bsedMathCB;
    private javax.swing.JCheckBox bsfCB;
    private javax.swing.JCheckBox bsitElectCB;
    private javax.swing.JCheckBox bsitFoodTechCB;
    private javax.swing.JCheckBox bsm;
    private javax.swing.JButton cancelCSV;
    private javax.swing.JFrame checkCSV;
    private javax.swing.JButton clearEvent;
    private javax.swing.JTextField contactNumTF;
    private javax.swing.JComboBox<String> courseCB;
    private javax.swing.JDialog courseList;
    private javax.swing.JComboBox<String> courseSortCB;
    private javax.swing.JComboBox<String> courseSortCB1;
    private javax.swing.JComboBox<String> courseSortEventCB;
    private javax.swing.JComboBox<String> courseSortStudentCB;
    private javax.swing.JFrame csvFrame;
    private javax.swing.JTextField csvName;
    private javax.swing.JButton customStudents;
    private javax.swing.JLabel databaseConnection;
    private javax.swing.JLabel dateTimeLabel;
    private javax.swing.JButton declineAllRow;
    private javax.swing.JButton declineRow;
    private javax.swing.JButton deleteEventBtn;
    private javax.swing.JButton deleteStudentBtn;
    private javax.swing.JDialog editHeadingDialog;
    public javax.swing.JToggleButton enableAttendanceBtn;
    private javax.swing.JLabel eventLabel;
    private javax.swing.JTextField eventNameTF;
    private javax.swing.JButton exportCSV;
    private javax.swing.JButton exportEventCSV;
    private javax.swing.JButton exportStudentCSVBtn;
    private javax.swing.JCheckBox firstYearCb;
    private javax.swing.JCheckBox fourthYearCB;
    private javax.swing.JComboBox<String> genderCB;
    private javax.swing.JCheckBox headerAddress;
    private javax.swing.JCheckBox headerAge;
    private javax.swing.JCheckBox headerBlock;
    private javax.swing.JCheckBox headerContact;
    private javax.swing.JCheckBox headerCourse;
    private javax.swing.JCheckBox headerEmail;
    private javax.swing.JCheckBox headerFN;
    private javax.swing.JCheckBox headerGender;
    private javax.swing.JCheckBox headerID;
    private javax.swing.JCheckBox headerLN;
    private javax.swing.JCheckBox headerMN;
    private javax.swing.JCheckBox headerRFID;
    private javax.swing.JCheckBox headerStatus;
    private javax.swing.JCheckBox headerYear;
    private javax.swing.JComboBox<String> hostCB;
    private javax.swing.JLabel hostConnection;
    private javax.swing.JTextField hostCustom;
    private javax.swing.JPasswordField hostPassword;
    private javax.swing.JTextField hostUsername;
    private javax.swing.JButton importEventCSVBtn;
    private javax.swing.JButton importStudentCSV;
    private javax.swing.JComboBox<String> inOutCB;
    private javax.swing.JComboBox<String> inOutCB1;
    private javax.swing.JButton jButton2;
    private javax.swing.JFrame jFrame4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPanel line;
    private javax.swing.JFrame loadingDialogFrame;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JFrame manageEventFrame;
    private javax.swing.JTable manageEventTable;
    private javax.swing.JFrame manageStudentFrame;
    private javax.swing.JTable manageStudentTable;
    private javax.swing.JButton manageStudentsBtn;
    private javax.swing.JCheckBox noTimeIn;
    private javax.swing.JCheckBox noTimeOut;
    private javax.swing.JButton overWriteEvent;
    private javax.swing.JButton pendingStudentCSV;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JProgressBar progressBars;
    private javax.swing.JLabel progressLbl;
    private javax.swing.JButton recentEventsBtn;
    private javax.swing.JTable recentRecordsTable;
    private javax.swing.JLabel recentScanCount;
    private javax.swing.JPanel recordsPanel;
    private javax.swing.JPanel recordsTablePanel;
    private javax.swing.JButton resumeEventBtn;
    private javax.swing.JPanel ribbonPanel;
    private javax.swing.JLabel rowCount;
    private javax.swing.JButton saveConnectionBtn;
    private javax.swing.JButton saveGeneralBtn;
    private javax.swing.JButton saveHeader;
    private javax.swing.JTextField search1;
    private javax.swing.JTextField searchEvent;
    private javax.swing.JTextField searchStudent;
    private javax.swing.JTextField searchTF;
    private javax.swing.JCheckBox seconYearCB;
    private javax.swing.JButton settingsBtn;
    private javax.swing.JFrame settingsFrame;
    private javax.swing.JPanel statisticsPanel;
    private javax.swing.JComboBox<String> statusCB;
    private javax.swing.JTable studentPerCourseTable;
    private javax.swing.JTabbedPane tabPanels;
    private javax.swing.JCheckBox thirdYearCB;
    private javax.swing.JSpinner timeInEndSpinner;
    private javax.swing.JLabel timeInLabel;
    private javax.swing.JSpinner timeInStartSpinner;
    private javax.swing.JSpinner timeOutEndSpinner;
    private javax.swing.JLabel timeOutLabel;
    private javax.swing.JSpinner timeOutStartSpinner;
    private javax.swing.JLabel timedIn;
    private javax.swing.JLabel timedOut;
    private javax.swing.JLabel totalPresent;
    private javax.swing.JLabel totalStudentStats;
    private javax.swing.JButton updateEventBtn;
    private javax.swing.JButton updateStudentBtn;
    private javax.swing.JButton uploadCSV;
    private javax.swing.JTable viewCSVTable;
    private javax.swing.JButton viewEvent;
    private javax.swing.JTextField waitingTime;
    private javax.swing.JComboBox<String> yearCB;
    private javax.swing.JComboBox<String> yearSortCB;
    private javax.swing.JComboBox<String> yearSortCB1;
    private javax.swing.JComboBox<String> yearSortEventCB;
    private javax.swing.JComboBox<String> yearSortStudentCB;
    // End of variables declaration//GEN-END:variables
}
