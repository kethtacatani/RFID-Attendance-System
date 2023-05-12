
package rfid.attendance;


import com.fazecast.jSerialComm.SerialPort;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import static rfid.attendance.RFIDATTENDANCE.arduinoPort;
import java.sql.*;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Keth Dominic
 */

//        initComponents();
//        recentRecordsTable.setShowGrid(false);
//        recentRecordsTable.getTableHeader().setVisible(false);
//        recentRecordsTable.setAutoResizeMode(recentRecordsTable.AUTO_RESIZE_ALL_COLUMNS);
public class HomePanel extends javax.swing.JFrame {
    
    // <editor-fold defaultstate="collapsed" desc="Main Panel">    
    DefaultTableModel model;
    Object tablerow [][];
    String tablecol []= {"ID No.","Last Name","First Name","M.I.","Type","Time","Status"};
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="Manage Students Panel">   
    DefaultTableModel manageStudentModel;
    Object manageStudentTablerow [][];
    String manageStudentTablecol []= {"ID No.","RFID ID","Last Name","First Name","Middle Name.","Age","Gender","Address","Email","Year","Course","Block","Status"};
    
  
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="Statistics Panel">   
    DefaultTableModel courseCountModel;
    Object courseCount [][];
    String courseColumn []= {"Course","Count"};
    
  
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc="Events Panel">   
    DefaultTableModel eventModel;
    Object eventsROw [][];
    String eventColumn []= {"ID","Event Name","Date","Students Involved","Year","Time-in","Time-out","Total Present"};
    
  
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
    String studentsInvolved = "All Students";
    String timeInStart = "";
    String timeInEnd = "";
    String timeOutStart = "";
    String timeOutEnd = "";
    String attendanceStatus="";
    String attendanceType="";
    int totalPresentCount = 0;
    Timer timer;
    
   
    //RFIDATTENDANCE arduino;
    QueryProcessor qp;
    public HomePanel() {
        qp = new QueryProcessor();
        initComponents();   
        preProcess();
        displayRecentScans();
        displayManageStudentTable();
        
        
        
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
                System.out.println("updating "+longDate+" | "+time );
                
            }
        });
        timer.setInitialDelay(0);
        timer.start();
        updateStudentCount();
        displayStatistics(LocalDate.now().format(dateFormatter));
        
        

        // Start the timer
        
        
    }
    
    
    public void preProcess(){
        System.out.println("Pre-Process");
        String[][] records = qp.getAllRecord("SELECT 1 FROM `events` WHERE `recent_event` =  'recentEvent'");
        if(records!=null){
            String[] recentInfo = qp.getSpecificRow("Select `event_name`,`date` from `events` WHERE `recent_event`='recentEvent'");
            System.out.println("date is "+recentInfo);
            if(recentInfo[1].equals(LocalDate.now().format(dateFormatter))){
                //if recentEvent date matched the current date
                String query = "Select `rfid_id`, TIME_FORMAT(`time_in`, '%h:%i %p') AS formatted_time_in,"
                    + "CASE WHEN time_out IS NULL THEN 'Time-in' ELSE 'Time-out' END AS time_status from `student_record` WHERE `event`='"+recentInfo[0]+"'";
                Object row[][]=qp.getAllRecord(query);
                if(row!=null){
                    for (int i = 0; i < row.length; i++) {
                        scannedRFID.add(row[i][0].toString());
                        scanTime.add(row[i][1].toString());
                        scanType.add(row[i][2].toString());
                    }
                }
                event = recentInfo[0];
                eventLabel.setText(event);
                }
            else{
                // if recentEvent date did not match the current date
                addEvent(null, null, null, null,"recentEvent", event+" "+LocalDate.now().format(DateTimeFormatter.ofPattern("YY-MM-dd")));
                
            }
        }
        else{
            //if no recentEvent exist or database in empty
            addEvent(null, null, null, null,"recentEvent", event+" "+LocalDate.now().format(DateTimeFormatter.ofPattern("YY-MM-dd")));
            System.out.println("adding recent");
        }
        

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
        totalPresent1.setText(scannedRFID.size()+"");
        timedIn.setText("Timed-in: "+scanType.stream().filter(element -> element.equals("Time-in")).count()+"");
        timedOut.setText("Timed-out: "+scanType.stream().filter(element ->  element.equals("Time-out")).count()+"");
    }
    
    public  void displayStatistics(String date){
        System.out.println("Displaying User Statistcs");
        int totalStudents = qp.getAllRecord("Select `student_id` from `student_info`").length;
        if(!event.equals("School Day")){
            
        }    
        
        totalPresent.setText("/"+totalStudents);
        
        courseCount = qp.getAllRecord("SELECT `student_info`.`course`, COUNT(`student_record`.`student_id`) AS count FROM `student_info` "
                + "LEFT JOIN `student_record` ON `student_info`.`student_id` = `student_record`.`student_id` "
                + "WHERE `student_record`.`date` = '"+date+"'  GROUP BY `student_info`.`course`;");
        courseCountModel = new DefaultTableModel(courseCount,courseColumn);
        studentPerCourseTable.setModel(courseCountModel);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(timedOut.RIGHT);
        studentPerCourseTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        studentPerCourseTable.setShowGrid(false);
        studentPerCourseTable.getTableHeader().setVisible(false);
        
    }
    
    
    
    public void executeArduinoWrite(String messagee) {
        System.out.println("Executing arduino write");
        if (arduinoPort != null && arduinoPort.isOpen()) {
            System.out.println("port open for message");
            arduinoPort.writeBytes(messagee.getBytes(), messagee.length());
            
        }
        
    }
    
    public void displayRecentScans(){
        System.out.println("Displaying Recent Scans");
       // "ID No.","Last Name","First Name","M.I.","Type","Time","Status"};
        
        String query = "SELECT `student_info`.`student_id`, `student_info`.`last_name`, `student_info`.`first_name`, LEFT(`middle_name`, 1) AS `first_letter1`,"
                + "CASE WHEN time_out IS NULL THEN 'Time-in' ELSE 'Time-out' END AS time_status, TIME_FORMAT(`student_record`.`time_in`, '%h:%i %p') AS formatted_time_in,"
                + "CASE WHEN `student_record`.`status` IS NULL THEN '' ELSE '"+attendanceStatus+"' END AS student_status FROM `student_record`, `student_info` WHERE `student_record`.`student_id` = `student_info`.`student_id` "
                + "AND `student_record`.`event` = '"+event+"' AND `student_record`.`date` = '"+LocalDate.now().format(dateFormatter)+"'";
        System.out.println(query);
        tablerow = qp.getAllRecord(query);
        model = new DefaultTableModel(tablerow,tablecol);
        recentRecordsTable.setModel(model);
        
        recentRecordsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        recentRecordsTable.getColumnModel().getColumn(3).setPreferredWidth(5);
        recentRecordsTable.getColumnModel().getColumn(5).setPreferredWidth(15);
        recentRecordsTable.getColumnModel().getColumn(6).setPreferredWidth(15);
        
        
    }
    public void displayManageStudentTable(){
        System.out.println("Displaying Manage Student Table");
        String searchQuery = "SELECT `student_id`,`rfid_id`,`last_name`, `first_name`, `middle_name`, `age`, `gender`, `address`, `email`, `year`, `course`, `block`, `status` FROM `student_info`"; 
        
        manageStudentTablerow = qp.getAllRecord(searchQuery);
        manageStudentModel = new DefaultTableModel(manageStudentTablerow,manageStudentTablecol);
        manageStudentTable.setModel(manageStudentModel);
        manageStudentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        manageStudentTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        manageStudentTable.getColumnModel().getColumn(5).setPreferredWidth(25);
        manageStudentTable.getColumnModel().getColumn(6).setPreferredWidth(35);
        manageStudentTable.getColumnModel().getColumn(9).setPreferredWidth(25);
        manageStudentTable.getColumnModel().getColumn(11).setPreferredWidth(25);
        //manageStudentTable.getColumnModel().getColumn(12).setPreferredWidth(50);
        
    }
    
    public void displayEvents(){
        String query = "SELECT `event_id`,  `event_name`,`date`, CASE WHEN "
                + "`students_involved`= 'BSCS,BSIT-Elect,BSIT-Food Tech,BSF,BEEd,BSED- English,BSED- Math,BSM,' THEN 'All Courses' ELSE `students_involved` END AS students_involved,"
                + " `year`, `time_in_range`, `time_out_range`, `total_present` FROM `events` ORDER BY "
                + "CASE WHEN `recent_event` = 'recenEvent' THEN 0 ELSE 1 END, `event_id` DESC;";
        eventsROw=qp.getAllRecord(query);
        eventModel = new DefaultTableModel(eventsROw,eventColumn);
        manageEventTable.setModel(eventModel);
        manageEventTable.getColumnModel().getColumn(3).setPreferredWidth(200);

        
    }
    
    public String getRowData(JTable table, DefaultTableModel tableModel, int row){
    return tableModel.getValueAt(table.getSelectedRow(),row).toString();
}
    
    public void insertStudentAttendance(String rfidId){
        System.out.println("Inserting Student Attendance");
        String studentId, lastName, firstName, middleInit,timeIn=time,timeOut= null, status, course, year, block;
        String query = "Select * FROM student_info WHERE `rfid_id`= '"+rfidId+"'";
        String[] result=null;
        try{
            result = qp.getSpecificRow(query);
        }
        catch(Exception e){
            
        }
        
        if(result!=null){
            System.out.println("String is "+Arrays.toString(result));
            studentId = result[1];
            lastName = result[4];
            firstName= result[2];
            middleInit = result[3].substring(0, 1);
            status= null;
            course = result[10];
            year= result[9];
            block= result[11];
            String query1="";
            System.out.println(scannedRFID.toString());
            if (!scannedRFID.contains(rfidId)){
                scannedRFID.add(rfidId);
                scanTime.add(time);
                scanType.add("Time-in");
                query1 = "INSERT into `student_record` (`student_id`,`rfid_id`,`event`,`date`,`time_in`,`time_out`,`status`) VALUES "
                    + "('"+studentId+"','"+rfidId+"','"+event+"','"+date+"',STR_TO_DATE('"+time+"', '%h:%i %p'),NULL,'"+status+"')";
            }
            else{
                   
                if(timedifference(time, scanTime.get(scannedRFID.indexOf(rfidId))) > 1 && scanType.get(scannedRFID.indexOf(rfidId)).equals("Time-in")){
                    scanType.set(scannedRFID.indexOf(rfidId),"Time-out");
                    query1 = "UPDATE `student_record` SET `time_out` = STR_TO_DATE('"+time+"', '%h:%i %p') WHERE rfid_id ='"+rfidId+"'";    
                }
                else{
                    System.out.println(lastName+" already timed-"+scanType.get(scannedRFID.indexOf(rfidId)).substring(5));
                    query1 = "";
                }
                }
                System.out.println("Query is " +query1);
                 if(!query1.isEmpty()){
                     String line="";
                     if(firstName.contains(" ")){
                        line = lastName+" "+firstName.substring(0, 1)+""+firstName.substring(firstName.indexOf(" ")+1,firstName.indexOf(" ")+2 )+".                ";
                     }
                     else{
                         line = lastName+" "+firstName.substring(0, 1)+".                      ";
                     }
                     String line2 = course+" "+year+""+block;
                     String lineWrite = line.substring(0,16)+line2;
                     qp.executeUpdate(query1);
                     executeArduinoWrite(lineWrite);
                     displayRecentScans();
                     updateStudentCount();
                     
                }
            }
        else{
            System.out.println("No record found");
            executeArduinoWrite("No Record Found! ID: "+rfidId+"                         ");
        }
        
    }
    
    public long timedifference(String time1, String time2){
        System.out.println("Getting time diffrence");
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
        System.out.println("diff "+differenceInMinutes);
        return differenceInMinutes;
    }
    
    public void addStudent(){
        System.out.println("Adding student");
        if(qp.executeUpdate("Insert into `student_info` values('"+addRFIDTF.getText()+"','"+addIDNoTF.getText()+"','"+addFNameTF.getText()
            +"','"+addMNameTF.getText()+"','"+addLNameTF.getText()+"','"+addAgeTF.getText()+"','"+addAddressTF.getText()
            +"','"+addEmailTF.getText()+"','"+addYearTF.getText()+"','"+addCourseTF.getText()+"'"
                    + ",'"+addBlockTF.getText()+ "','"+addStatusTF.getText()+"') ")){
            JOptionPane.showMessageDialog(null, "Student Added Successfully");
        }

    }
    
    public boolean addEvent(String timeInStart,String timeInEnd,String timeOutStart,String timeOutEnd, String recentEvent, String event){
        System.out.println("Adding event");
        String timeIn = "'"+timeInStart+timeInEnd+"'";
        String timeOut = "'"+timeOutStart+timeOutEnd+"'";
        if (timeInStart == null && timeInEnd == null && timeOutStart==null && timeOutEnd==null){
            timeIn=null;
            timeOut=null;
        }
        
        String course="", year="";
        if(bscsCB.isSelected()){
            course += bscsCB.getText()+",";
        }if(bsitElectCB.isSelected()){
            course += bsitElectCB.getText()+",";
        }
        if(bsitFoodTechCB.isSelected()){
            course += bsitFoodTechCB.getText()+",";
        }
        if(bsfCB.isSelected()){
            course += bsfCB.getText()+",";
        }
        if(beedCB.isSelected()){
            course += beedCB.getText()+",";
        }
        if(bsedEnglish.isSelected()){
            course += bsedEnglish.getText()+",";
        }
        if(bsedMathCB.isSelected()){
            course += bsedMathCB.getText()+",";
        }
        if(bsm.isSelected()){
            course += bsm.getText()+",";
        }
        
        if(firstYearCb.isSelected()){
            year += "1,";
        }
        if(seconYearCB.isSelected()){
            year += "2,";
        }
        if(thirdYearCB.isSelected()){
            year += "3,";
        }
        if(fourthYearCB.isSelected()){
            year += "4,";
        }
        System.out.println("course is "+course);
        if(updateRecentEvent()&&qp.executeUpdate("Insert into `events`( `date`, `event_name`, `students_involved`, `year`, `time_in_range`, `time_out_range`,`recent_event`) values ('"+LocalDate.now().format(dateFormatter)+"','"+event+"','"+course
            +"','"+year+"',"+timeIn+","+timeOut+",'"+recentEvent+"')" )){
            return true;
        }
        return false;
    }
    
    public boolean checkEventExist(String event){
      if(qp.getSpecificRow("SELECT *  FROM `events` WHERE `event_name` = '"+event+"'").length>0){
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
    
    public void addStudentDialogConditions(){
        DateFormatSymbols symbols = new DateFormatSymbols();
        symbols.setAmPmStrings(new String[]{"am", "pm"});
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", symbols);
        String timeInStartFormatted = dateFormat.format(timeInStartSpinner.getValue());
        String timeInEndFormatted = dateFormat.format(timeInEndSpinner.getValue());
        String timeOutStartFormatted = dateFormat.format(timeOutStartSpinner.getValue());
        String timeOutEndFormatted = dateFormat.format(timeOutEndSpinner.getValue());
        String errorMsg="";
        
        if(eventNameTF.equals("")|| timedifference(timeInEndFormatted, timeInStartFormatted)<0 
                || timedifference(timeOutEndFormatted, timeOutStartFormatted)<0 
                || (timedifference( timeOutStartFormatted,timeInEndFormatted))<0
                || (!bscsCB.isSelected() && !bsfCB.isSelected() && !bsm.isSelected() && !beedCB.isSelected() && !bsitElectCB.isSelected() && !bsitFoodTechCB.isSelected() && !bsedEnglish.isSelected() && !bsedMathCB.isSelected())
                || (!firstYearCb.isSelected() && !seconYearCB.isSelected() && !thirdYearCB.isSelected() && !fourthYearCB.isSelected())
                || checkEventExist(eventNameTF.getText())){
            
            
            if(eventNameTF.getText().isEmpty()){
                errorMsg += "- Event name required!\n";
            }
            if(checkEventExist(eventNameTF.getText())){
                errorMsg += "- Event name already exist!\n";
            }
            if(!bscsCB.isSelected() && !bsfCB.isSelected() && !bsm.isSelected() && !beedCB.isSelected() && !bsitElectCB.isSelected() && !bsitFoodTechCB.isSelected() && !bsedEnglish.isSelected() && !bsedMathCB.isSelected()){
                errorMsg += "- No Course Selected!\n";
            }
            if(!firstYearCb.isSelected() && !seconYearCB.isSelected() && !thirdYearCB.isSelected() && !fourthYearCB.isSelected()){
                errorMsg += "- No Students Selected!\n";
            }
            if(timedifference(timeInEndFormatted, timeInStartFormatted)<0){
                errorMsg += "- Time in range is invalid!\n";
            }
            if(timedifference(timeOutEndFormatted, timeOutStartFormatted)<0){
                errorMsg += "- Time out range is invalid!\n";
            }
            if((timedifference( timeOutStartFormatted,timeInEndFormatted))<0){
                errorMsg += "- Time out is earlier than time in!\n";
            }
            JOptionPane.showMessageDialog(null, errorMsg, "Add Event Error", JOptionPane.ERROR_MESSAGE);
        }
        else{
            int confirm = JOptionPane.showConfirmDialog(null, "Confirm adding this event?", "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(confirm == JOptionPane.YES_OPTION){
            if(addEvent(timeInStartFormatted,timeInEndFormatted,timeOutStartFormatted,timeOutEndFormatted,"recentEvent", eventNameTF.getText())){
            JOptionPane.showMessageDialog(null, "Event Added Successfully");
            preProcess();
            displayRecentScans();
            displayManageStudentTable();
            addEvent.setVisible(false);
            }
            timeInStart = timeInStartFormatted;
            timeInEnd = timeInEndFormatted;
            timeOutStart = timeOutStartFormatted;
            timeOutEnd = timeOutEndFormatted;
            
        }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        manageStudentDialog = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        addStudentBtn = new javax.swing.JButton();
        importCSVBtn = new javax.swing.JButton();
        updateStudentBtn = new javax.swing.JButton();
        deleteStudentBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        manageStudentTable = new javax.swing.JTable();
        editColumnsBtn = new javax.swing.JButton();
        editHeadingDialog = new javax.swing.JDialog();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox12 = new javax.swing.JCheckBox();
        addStudentsDialog = new javax.swing.JDialog();
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
        addYearTF = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        addCourseTF = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        addBlockTF = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        addStatusTF = new javax.swing.JTextField();
        addStudentRecordBtn = new javax.swing.JButton();
        addEvent = new javax.swing.JDialog();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        eventNameTF = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        addStudentRecordBtn1 = new javax.swing.JButton();
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
        calendar2.set(Calendar.MINUTE, 0);
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
        studentsList = new javax.swing.JDialog();
        bscsCB = new javax.swing.JCheckBox();
        bsitFoodTechCB = new javax.swing.JCheckBox();
        bsitElectCB = new javax.swing.JCheckBox();
        bsfCB = new javax.swing.JCheckBox();
        beedCB = new javax.swing.JCheckBox();
        bsedMathCB = new javax.swing.JCheckBox();
        bsedEnglish = new javax.swing.JCheckBox();
        bsm = new javax.swing.JCheckBox();
        manageEventDialog = new javax.swing.JDialog();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        addEventBtn1 = new javax.swing.JButton();
        importEventCSVBtn = new javax.swing.JButton();
        updateEventBtn = new javax.swing.JButton();
        deleteEventBtn = new javax.swing.JButton();
        resumeEventBtn = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        manageEventTable = new javax.swing.JTable();
        mainPanel = new javax.swing.JPanel();
        ribbonPanel = new javax.swing.JPanel();
        addEventBtn = new javax.swing.JButton();
        recentEventsBtn = new javax.swing.JButton();
        manageStudentsBtn = new javax.swing.JButton();
        classScheduleBtn = new javax.swing.JButton();
        settingsBtn = new javax.swing.JButton();
        recordsPanel = new javax.swing.JPanel();
        eventLabel = new javax.swing.JLabel();
        dateTimeLabel = new javax.swing.JLabel();
        recordsTablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        recentRecordsTable = new javax.swing.JTable();
        searchTF = new javax.swing.JTextField();
        sortCB = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        enableAttendanceBtn = new javax.swing.JToggleButton();
        arduinoStatus = new javax.swing.JLabel();
        statisticsPanel = new javax.swing.JPanel();
        totalPresent = new javax.swing.JLabel();
        line = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        totalPresent1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        studentPerCourseTable = new javax.swing.JTable();
        timedOut = new javax.swing.JLabel();
        timedIn = new javax.swing.JLabel();

        manageStudentDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        manageStudentDialog.setMinimumSize(new java.awt.Dimension(981, 570));
        manageStudentDialog.setModalExclusionType(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);

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

        importCSVBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        importCSVBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_add_user_group_man_man_40px.png"))); // NOI18N
        importCSVBtn.setText("Import CSV");
        importCSVBtn.setToolTipText("Import CSV for multiple students");
        importCSVBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        importCSVBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        importCSVBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        importCSVBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        updateStudentBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        updateStudentBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_registration_40px.png"))); // NOI18N
        updateStudentBtn.setText("Update Student");
        updateStudentBtn.setToolTipText("Update Student");
        updateStudentBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        updateStudentBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateStudentBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        updateStudentBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        deleteStudentBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        deleteStudentBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_disposal_40px.png"))); // NOI18N
        deleteStudentBtn.setText("Delete Student");
        deleteStudentBtn.setToolTipText("Delete Student");
        deleteStudentBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        deleteStudentBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteStudentBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteStudentBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addStudentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importCSVBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(deleteStudentBtn)
                    .addComponent(updateStudentBtn)
                    .addComponent(importCSVBtn)
                    .addComponent(addStudentBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jTextField1.setForeground(new java.awt.Color(51, 51, 51));
        jTextField1.setText("Search");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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
        jScrollPane3.setViewportView(manageStudentTable);

        editColumnsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_administrative_tools_15px.png"))); // NOI18N
        editColumnsBtn.setText("Header Settings");
        editColumnsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editColumnsBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 931, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(editColumnsBtn)
                        .addGap(510, 510, 510)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editColumnsBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
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

        javax.swing.GroupLayout manageStudentDialogLayout = new javax.swing.GroupLayout(manageStudentDialog.getContentPane());
        manageStudentDialog.getContentPane().setLayout(manageStudentDialogLayout);
        manageStudentDialogLayout.setHorizontalGroup(
            manageStudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageStudentDialogLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        manageStudentDialogLayout.setVerticalGroup(
            manageStudentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        manageStudentDialog.getAccessibleContext().setAccessibleParent(null);

        editHeadingDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        editHeadingDialog.setBackground(new java.awt.Color(255, 255, 255));
        editHeadingDialog.setIconImage(null);
        editHeadingDialog.setMinimumSize(new java.awt.Dimension(264, 193));

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("RFID ID");

        jCheckBox2.setSelected(true);
        jCheckBox2.setText("ID No.");

        jCheckBox3.setSelected(true);
        jCheckBox3.setText("First Name");

        jCheckBox4.setSelected(true);
        jCheckBox4.setText("Last Name");

        jCheckBox5.setSelected(true);
        jCheckBox5.setText("Middle Name");

        jCheckBox6.setSelected(true);
        jCheckBox6.setText("Age");

        jCheckBox7.setSelected(true);
        jCheckBox7.setText("Address");

        jCheckBox8.setSelected(true);
        jCheckBox8.setText("Email");

        jCheckBox9.setSelected(true);
        jCheckBox9.setText("Year");

        jCheckBox10.setSelected(true);
        jCheckBox10.setText("Course");

        jCheckBox11.setSelected(true);
        jCheckBox11.setText("Block");

        jCheckBox12.setSelected(true);
        jCheckBox12.setText("Status");

        javax.swing.GroupLayout editHeadingDialogLayout = new javax.swing.GroupLayout(editHeadingDialog.getContentPane());
        editHeadingDialog.getContentPane().setLayout(editHeadingDialogLayout);
        editHeadingDialogLayout.setHorizontalGroup(
            editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editHeadingDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox3, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addComponent(jCheckBox4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox9, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox10, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox11, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox12, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jCheckBox7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );
        editHeadingDialogLayout.setVerticalGroup(
            editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editHeadingDialogLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editHeadingDialogLayout.createSequentialGroup()
                        .addComponent(jCheckBox7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox12))
                    .addGroup(editHeadingDialogLayout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox6)))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        addStudentsDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addStudentsDialog.setTitle("Add Student");
        addStudentsDialog.setBackground(new java.awt.Color(255, 255, 255));
        addStudentsDialog.setMinimumSize(new java.awt.Dimension(431, 446));
        addStudentsDialog.setSize(new java.awt.Dimension(431, 446));

        jLabel3.setText("RFID ID:");

        jLabel4.setText("ID No. :");

        addRFIDStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_on_20px.png"))); // NOI18N

        jLabel5.setText("RFID");

        jLabel6.setText("First Name:");

        jLabel7.setText("Middle Name:");

        jLabel8.setText("Last Name:");

        jLabel9.setText("Age:");

        jLabel10.setText("Address:");

        jLabel11.setText("Email:");

        jLabel12.setText("Year:");

        jLabel13.setText("Course:");

        jLabel14.setText("Block:");

        jLabel15.setText("Status");

        addStudentRecordBtn.setText("Add");
        addStudentRecordBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudentRecordBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addStudentsDialogLayout = new javax.swing.GroupLayout(addStudentsDialog.getContentPane());
        addStudentsDialog.getContentPane().setLayout(addStudentsDialogLayout);
        addStudentsDialogLayout.setHorizontalGroup(
            addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentsDialogLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addStudentRecordBtn)
                    .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel15)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addStatusTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel14)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addBlockTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel13)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addCourseTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel12)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addYearTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addEmailTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel10)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addAddressTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel9)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addAgeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel8)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addLNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addMNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStudentsDialogLayout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addFNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStudentsDialogLayout.createSequentialGroup()
                            .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(22, 22, 22)
                            .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(addStudentsDialogLayout.createSequentialGroup()
                                    .addComponent(addRFIDTF)
                                    .addGap(18, 18, 18)
                                    .addComponent(jLabel5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(addRFIDStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(addIDNoTF, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        addStudentsDialogLayout.setVerticalGroup(
            addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentsDialogLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addIDNoTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addRFIDStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addRFIDTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addFNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addMNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addLNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addAgeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addAddressTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addEmailTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addYearTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addCourseTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addBlockTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addStatusTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addComponent(addStudentRecordBtn)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        addEvent.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addEvent.setTitle("Add Event");
        addEvent.setBackground(new java.awt.Color(255, 255, 255));
        addEvent.setMinimumSize(new java.awt.Dimension(482, 255));
        addEvent.setModalExclusionType(null);
        addEvent.setModalityType(null);
        addEvent.setSize(new java.awt.Dimension(482, 255));

        jLabel17.setText("Course:");

        jLabel18.setText("Event Name:");

        jLabel20.setText("Students:");

        jLabel21.setText("Time-in:");

        jLabel22.setText("Time-out");

        addStudentRecordBtn1.setText("Add");
        addStudentRecordBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudentRecordBtn1ActionPerformed(evt);
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

        javax.swing.GroupLayout addEventLayout = new javax.swing.GroupLayout(addEvent.getContentPane());
        addEvent.getContentPane().setLayout(addEventLayout);
        addEventLayout.setHorizontalGroup(
            addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addEventLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEventLayout.createSequentialGroup()
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
                                .addComponent(timeOutEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(addStudentRecordBtn1)
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
                        .addComponent(jLabel19))
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel21)
                        .addComponent(timeInStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeOutEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel23))
                    .addGroup(addEventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeOutStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel22)))
                .addGap(15, 15, 15)
                .addComponent(addStudentRecordBtn1)
                .addGap(28, 28, 28))
        );

        studentsList.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        studentsList.setBackground(new java.awt.Color(255, 255, 255));
        studentsList.setIconImage(null);
        studentsList.setMinimumSize(new java.awt.Dimension(264, 156));
        studentsList.setSize(new java.awt.Dimension(264, 156));

        bscsCB.setSelected(true);
        bscsCB.setText("BSCS");
        bscsCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bscsCBMouseClicked(evt);
            }
        });

        bsitFoodTechCB.setSelected(true);
        bsitFoodTechCB.setText("BSIT-Food Tech");
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

        javax.swing.GroupLayout studentsListLayout = new javax.swing.GroupLayout(studentsList.getContentPane());
        studentsList.getContentPane().setLayout(studentsListLayout);
        studentsListLayout.setHorizontalGroup(
            studentsListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentsListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(studentsListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bsfCB)
                    .addComponent(bsitFoodTechCB, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addComponent(bsitElectCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bscsCB, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(studentsListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(studentsListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(bsedEnglish, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                        .addComponent(beedCB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(bsedMathCB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(bsm))
                .addGap(14, 14, 14))
        );
        studentsListLayout.setVerticalGroup(
            studentsListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentsListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(studentsListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(studentsListLayout.createSequentialGroup()
                        .addComponent(bscsCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsitElectCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsitFoodTechCB))
                    .addGroup(studentsListLayout.createSequentialGroup()
                        .addComponent(beedCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsedMathCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bsedEnglish)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(studentsListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bsfCB)
                    .addComponent(bsm))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        manageEventDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        manageEventDialog.setTitle("Manage Events");
        manageEventDialog.setMinimumSize(new java.awt.Dimension(981, 570));
        manageEventDialog.setModalExclusionType(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);

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
        importEventCSVBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_new_copy_40px.png"))); // NOI18N
        importEventCSVBtn.setText("Import CSV");
        importEventCSVBtn.setToolTipText("Add CSV for an event");
        importEventCSVBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        importEventCSVBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        importEventCSVBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        importEventCSVBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        updateEventBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        updateEventBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_edit_property_40px.png"))); // NOI18N
        updateEventBtn.setText("Update Event");
        updateEventBtn.setToolTipText("Update Event");
        updateEventBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        updateEventBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateEventBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        updateEventBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        deleteEventBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        deleteEventBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_delete_document_40px.png"))); // NOI18N
        deleteEventBtn.setText("Delete Event");
        deleteEventBtn.setToolTipText("Delete Event");
        deleteEventBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        deleteEventBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteEventBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteEventBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addEventBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importEventCSVBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateEventBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteEventBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resumeEventBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(resumeEventBtn)
                    .addComponent(deleteEventBtn)
                    .addComponent(updateEventBtn)
                    .addComponent(importEventCSVBtn)
                    .addComponent(addEventBtn1))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jTextField2.setForeground(new java.awt.Color(51, 51, 51));
        jTextField2.setText("Search");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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
        manageEventTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                manageEventTableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(manageEventTable);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 931, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(644, 644, 644)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
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

        javax.swing.GroupLayout manageEventDialogLayout = new javax.swing.GroupLayout(manageEventDialog.getContentPane());
        manageEventDialog.getContentPane().setLayout(manageEventDialogLayout);
        manageEventDialogLayout.setHorizontalGroup(
            manageEventDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageEventDialogLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        manageEventDialogLayout.setVerticalGroup(
            manageEventDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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
        recentEventsBtn.setToolTipText("View Recent Events");
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

        classScheduleBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        classScheduleBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_classroom_50px.png"))); // NOI18N
        classScheduleBtn.setText("Class Schedules");
        classScheduleBtn.setToolTipText("View Class Schedules");
        classScheduleBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        classScheduleBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        classScheduleBtn.setMargin(new java.awt.Insets(2, 0, 2, 0));
        classScheduleBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

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
                .addComponent(classScheduleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(classScheduleBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        recordsPanel.setBackground(new java.awt.Color(255, 255, 255));
        recordsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        eventLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        eventLabel.setText("Event: ");

        dateTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        dateTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dateTimeLabel.setText("March 20, 2023 | 9:23 AM");

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
        jScrollPane1.setViewportView(recentRecordsTable);

        searchTF.setForeground(new java.awt.Color(204, 204, 204));
        searchTF.setText("Search");
        searchTF.setToolTipText("");

        sortCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout recordsTablePanelLayout = new javax.swing.GroupLayout(recordsTablePanel);
        recordsTablePanel.setLayout(recordsTablePanelLayout);
        recordsTablePanelLayout.setHorizontalGroup(
            recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsTablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(recordsTablePanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(sortCB, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(searchTF, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        recordsTablePanelLayout.setVerticalGroup(
            recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsTablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordsTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(searchTF, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortCB, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                        .addComponent(dateTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(recordsPanelLayout.createSequentialGroup()
                        .addComponent(arduinoStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(arduinoStatus))
                .addContainerGap())
        );

        statisticsPanel.setBackground(new java.awt.Color(255, 255, 255));
        statisticsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        totalPresent.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        totalPresent.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        totalPresent.setText("/1290");
        totalPresent.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        totalPresent.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

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

        totalPresent1.setFont(new java.awt.Font("Century Gothic", 1, 48)); // NOI18N
        totalPresent1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalPresent1.setText("0");
        totalPresent1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        totalPresent1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

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
                                .addComponent(line, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(statisticsPanelLayout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(totalPresent1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalPresent, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        statisticsPanelLayout.setVerticalGroup(
            statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statisticsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPresent1, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalPresent))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(recordsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        manageStudentDialog.setVisible(true);
        manageStudentDialog.setLocationRelativeTo(null);
    }//GEN-LAST:event_manageStudentsBtnActionPerformed

    private void settingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsBtnActionPerformed
        // TODO add your handling code here:
  
    }//GEN-LAST:event_settingsBtnActionPerformed

    private void addStudentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudentBtnActionPerformed
        String rfidId = RFIDATTENDANCE.rfidId;
        addStudentsDialog.setVisible(true);
        addStudentsDialog.setLocationRelativeTo(null);
        addRFIDTF.setText(rfidId);
        
        
    }//GEN-LAST:event_addStudentBtnActionPerformed

    private void addEventBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventBtnActionPerformed
        // TODO add your handling code here:
        addEvent.setVisible(true);
        addEvent.setLocationRelativeTo(null);
    }//GEN-LAST:event_addEventBtnActionPerformed

    private void addStudentRecordBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudentRecordBtnActionPerformed
        if(!addIDNoTF.equals("") && !addRFIDTF.equals("") && !addFNameTF.equals("") && !addMNameTF.equals("") && !addLNameTF.equals("")
               && !addAgeTF.equals("") && !addAddressTF.equals("") && !addEmailTF.equals("") && !addYearTF.equals("") && !addCourseTF.equals("")
               && !addBlockTF.equals("") && !addStatusTF.equals("")){
            addStudent();
        }
    }//GEN-LAST:event_addStudentRecordBtnActionPerformed

    private void enableAttendanceBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_enableAttendanceBtnMouseClicked
        // TODO add your handling code here:
        if(!enableAttendanceBtn.isSelected()){
            executeArduinoWrite("msg0   Attendance        Paused        ");
            System.out.println("select");
        }
        else{
            executeArduinoWrite("msg1   Attendance       Resumed        ");
        }
    }//GEN-LAST:event_enableAttendanceBtnMouseClicked

    private void addStudentRecordBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudentRecordBtn1ActionPerformed
        // TODO add your handling code here:
        addStudentDialogConditions();
    }//GEN-LAST:event_addStudentRecordBtn1ActionPerformed

    private void bsedMathCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsedMathCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bsedMathCBActionPerformed

    private void firstYearCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstYearCbActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_firstYearCbActionPerformed

    private void seconYearCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seconYearCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_seconYearCBActionPerformed

    private void thirdYearCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thirdYearCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_thirdYearCBActionPerformed

    private void fourthYearCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fourthYearCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fourthYearCBActionPerformed

    private void customStudentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customStudentsActionPerformed
        // TODO add your handling code here:
        studentsList.setVisible(rootPaneCheckingEnabled);
        studentsList.setLocationRelativeTo(null);
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

    private void timeInStartSpinnerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_timeInStartSpinnerMouseClicked
        // TODO add your handling code here:
        //System.out.println("time in"+ timeInStart.toString());
    }//GEN-LAST:event_timeInStartSpinnerMouseClicked

    private void timeInStartSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_timeInStartSpinnerStateChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_timeInStartSpinnerStateChanged

    private void addEventBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventBtn1ActionPerformed
        // TODO add your handling code here:
        addEventBtnActionPerformed(evt);
    }//GEN-LAST:event_addEventBtn1ActionPerformed

    private void editColumnsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editColumnsBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editColumnsBtnActionPerformed

    private void recentEventsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentEventsBtnActionPerformed
        // TODO add your handling code here:
        manageEventDialog.setVisible(true);
        manageEventDialog.setLocationRelativeTo(null);
        displayEvents();
    }//GEN-LAST:event_recentEventsBtnActionPerformed

    private void manageEventTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manageEventTableMouseClicked
        // TODO add your handling code here:
        if(getRowData(manageEventTable, eventModel,2).equals(date) && !event.equals(getRowData(manageEventTable, eventModel,1)) ){
            resumeEventBtn.setEnabled(true);
        }
        else{
            resumeEventBtn.setEnabled(false);
        }
    }//GEN-LAST:event_manageEventTableMouseClicked

    private void resumeEventBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeEventBtnActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_resumeEventBtnActionPerformed

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
    private javax.swing.JTextField addBlockTF;
    private javax.swing.JTextField addCourseTF;
    private javax.swing.JTextField addEmailTF;
    private javax.swing.JDialog addEvent;
    private javax.swing.JButton addEventBtn;
    private javax.swing.JButton addEventBtn1;
    private javax.swing.JTextField addFNameTF;
    private javax.swing.JTextField addIDNoTF;
    private javax.swing.JTextField addLNameTF;
    private javax.swing.JTextField addMNameTF;
    private javax.swing.JToggleButton addRFIDStatus;
    public javax.swing.JTextField addRFIDTF;
    private javax.swing.JTextField addStatusTF;
    private javax.swing.JButton addStudentBtn;
    private javax.swing.JButton addStudentRecordBtn;
    private javax.swing.JButton addStudentRecordBtn1;
    private javax.swing.JDialog addStudentsDialog;
    private javax.swing.JTextField addYearTF;
    private javax.swing.JCheckBox allCoursesCB;
    public javax.swing.JLabel arduinoStatus;
    private javax.swing.JCheckBox beedCB;
    private javax.swing.JCheckBox bscsCB;
    private javax.swing.JCheckBox bsedEnglish;
    private javax.swing.JCheckBox bsedMathCB;
    private javax.swing.JCheckBox bsfCB;
    private javax.swing.JCheckBox bsitElectCB;
    private javax.swing.JCheckBox bsitFoodTechCB;
    private javax.swing.JCheckBox bsm;
    private javax.swing.JButton classScheduleBtn;
    private javax.swing.JButton customStudents;
    private javax.swing.JLabel dateTimeLabel;
    private javax.swing.JButton deleteEventBtn;
    private javax.swing.JButton deleteStudentBtn;
    private javax.swing.JButton editColumnsBtn;
    private javax.swing.JDialog editHeadingDialog;
    public javax.swing.JToggleButton enableAttendanceBtn;
    private javax.swing.JLabel eventLabel;
    private javax.swing.JTextField eventNameTF;
    private javax.swing.JCheckBox firstYearCb;
    private javax.swing.JCheckBox fourthYearCB;
    private javax.swing.JButton importCSVBtn;
    private javax.swing.JButton importEventCSVBtn;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
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
    private javax.swing.JLabel jLabel3;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JPanel line;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JDialog manageEventDialog;
    private javax.swing.JTable manageEventTable;
    private javax.swing.JDialog manageStudentDialog;
    private javax.swing.JTable manageStudentTable;
    private javax.swing.JButton manageStudentsBtn;
    private javax.swing.JButton recentEventsBtn;
    private javax.swing.JTable recentRecordsTable;
    private javax.swing.JPanel recordsPanel;
    private javax.swing.JPanel recordsTablePanel;
    private javax.swing.JButton resumeEventBtn;
    private javax.swing.JPanel ribbonPanel;
    private javax.swing.JTextField searchTF;
    private javax.swing.JCheckBox seconYearCB;
    private javax.swing.JButton settingsBtn;
    private javax.swing.JComboBox<String> sortCB;
    private javax.swing.JPanel statisticsPanel;
    private javax.swing.JTable studentPerCourseTable;
    private javax.swing.JDialog studentsList;
    private javax.swing.JCheckBox thirdYearCB;
    private javax.swing.JSpinner timeInEndSpinner;
    private javax.swing.JSpinner timeInStartSpinner;
    private javax.swing.JSpinner timeOutEndSpinner;
    private javax.swing.JSpinner timeOutStartSpinner;
    private javax.swing.JLabel timedIn;
    private javax.swing.JLabel timedOut;
    private javax.swing.JLabel totalPresent;
    private javax.swing.JLabel totalPresent1;
    private javax.swing.JButton updateEventBtn;
    private javax.swing.JButton updateStudentBtn;
    // End of variables declaration//GEN-END:variables
}
