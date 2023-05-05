
package rfid.attendance;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

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
    String manageStudentTablecol []= {"ID No.","RFID ID","Last Name","First Name","M.I.","Age","Email","Year","Course","Block","Status"};
    
  
    // </editor-fold>    
    
   

    QueryProcessor qp;
    public HomePanel() {
        qp = new QueryProcessor();
        initComponents();
        displayRecentScans();
        displayManageStudentTable();
    }
    
    public void displayRecentScans(){
        model = new DefaultTableModel(tablerow,tablecol);
        recentRecordsTable.setModel(model);
    }
    public void displayManageStudentTable(){
        String searchQuery = "SELECT * FROM `student_info`"; 
        
                //System.out.println(searchQuery);
        manageStudentTablerow = qp.getAllRecord(searchQuery);
        manageStudentModel = new DefaultTableModel(manageStudentTablerow,manageStudentTablecol);
        manageStudentTable.setModel(manageStudentModel);
    }
    
    public boolean addStudent(){
        qp.executeUpdate("Insert into `student_info` values('"+addRFIDTF.getText()+"','"+addIDNoTF.getText()+"','"+addFNameTF.getText()
            +"','"+addMNameTF.getText()+"','"+addLNameTF.getText()+"','"+addAgeTF.getText()+"','"+addAddressTF.getText()
            +"','"+addEmailTF.getText()+"','"+addYearTF.getText()+"','"+addCourseTF.getText()+"'"
                    + ",'"+addBlockTF.getText()+ "','"+addStatusTF.getText()+"') ");
        return true;
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
        jToggleButton1 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        statisticsPanel = new javax.swing.JPanel();
        totalPresent = new javax.swing.JLabel();
        line = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        totalPresent1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        studentPerCourseTable = new javax.swing.JTable();

        manageStudentDialog.setMinimumSize(new java.awt.Dimension(981, 570));
        manageStudentDialog.setPreferredSize(new java.awt.Dimension(981, 570));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        addStudentBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        addStudentBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_add_user_male_40px.png"))); // NOI18N
        addStudentBtn.setText("Add");
        addStudentBtn.setToolTipText("Manually Add Students");
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
        importCSVBtn.setToolTipText("Add CSV for multiple students");
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
                .addContainerGap(10, Short.MAX_VALUE))
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

        editHeadingDialog.setBackground(new java.awt.Color(255, 255, 255));
        editHeadingDialog.setIconImage(null);
        editHeadingDialog.setMinimumSize(new java.awt.Dimension(215, 300));

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
                .addGroup(editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox7, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox8, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox9, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox10, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox11, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox12, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        editHeadingDialogLayout.setVerticalGroup(
            editHeadingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editHeadingDialogLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addGap(20, 20, 20)
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addComponent(jCheckBox12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        addStudentsDialog.setTitle("Add Student");
        addStudentsDialog.setBackground(new java.awt.Color(255, 255, 255));
        addStudentsDialog.setMinimumSize(new java.awt.Dimension(421, 407));

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
                .addGap(36, 36, 36))
        );
        addStudentsDialogLayout.setVerticalGroup(
            addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentsDialogLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addIDNoTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addStudentsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addRFIDTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(addRFIDStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
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
                .addContainerGap(9, Short.MAX_VALUE))
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
        addEventBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addEventBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEventBtnActionPerformed(evt);
            }
        });

        recentEventsBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        recentEventsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_schedule_50px.png"))); // NOI18N
        recentEventsBtn.setText("Recent Events");
        recentEventsBtn.setToolTipText("View Recent Events");
        recentEventsBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        recentEventsBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        recentEventsBtn.setMargin(new java.awt.Insets(2, 4, 2, 4));
        recentEventsBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

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

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rfid/attendance/images/icons8_switch_off_20px.png"))); // NOI18N
        jToggleButton1.setText("ON");

        jLabel1.setText("Scan Status:");

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
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jToggleButton1)
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                .addGap(7, 7, 7)
                .addGroup(recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton1)))
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
        totalPresent1.setText("790");
        totalPresent1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        totalPresent1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        studentPerCourseTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(studentPerCourseTable);

        javax.swing.GroupLayout statisticsPanelLayout = new javax.swing.GroupLayout(statisticsPanel);
        statisticsPanel.setLayout(statisticsPanelLayout);
        statisticsPanelLayout.setHorizontalGroup(
            statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statisticsPanelLayout.createSequentialGroup()
                .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statisticsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(totalPresent1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalPresent, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(statisticsPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(statisticsPanelLayout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(line, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(278, 278, 278))
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
                    .addComponent(statisticsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
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
        addStudentsDialog.setVisible(true);
        addStudentsDialog.setLocationRelativeTo(null);
    }//GEN-LAST:event_addStudentBtnActionPerformed

    private void addEventBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addEventBtnActionPerformed

    private void editColumnsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editColumnsBtnActionPerformed
        // TODO add your handling code here:
        editHeadingDialog.setVisible(true);
        editHeadingDialog.setLocationRelativeTo(null);
    }//GEN-LAST:event_editColumnsBtnActionPerformed

    private void addStudentRecordBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudentRecordBtnActionPerformed
        if(!addIDNoTF.equals("") && !addRFIDTF.equals("") && !addFNameTF.equals("") && !addMNameTF.equals("") && !addLNameTF.equals("")
               && !addAgeTF.equals("") && !addAddressTF.equals("") && !addEmailTF.equals("") && !addYearTF.equals("") && !addCourseTF.equals("")
               && !addBlockTF.equals("") && !addStatusTF.equals("")){
            if(addStudent()){
                JOptionPane.showMessageDialog(null, "Student Added Successfully");
                displayRecentScans();
            }
        }
    }//GEN-LAST:event_addStudentRecordBtnActionPerformed

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
    private javax.swing.JButton addEventBtn;
    private javax.swing.JTextField addFNameTF;
    private javax.swing.JTextField addIDNoTF;
    private javax.swing.JTextField addLNameTF;
    private javax.swing.JTextField addMNameTF;
    private javax.swing.JToggleButton addRFIDStatus;
    private javax.swing.JTextField addRFIDTF;
    private javax.swing.JTextField addStatusTF;
    private javax.swing.JButton addStudentBtn;
    private javax.swing.JButton addStudentRecordBtn;
    private javax.swing.JDialog addStudentsDialog;
    private javax.swing.JTextField addYearTF;
    private javax.swing.JButton classScheduleBtn;
    private javax.swing.JLabel dateTimeLabel;
    private javax.swing.JButton deleteStudentBtn;
    private javax.swing.JButton editColumnsBtn;
    private javax.swing.JDialog editHeadingDialog;
    private javax.swing.JLabel eventLabel;
    private javax.swing.JButton importCSVBtn;
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JPanel line;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JDialog manageStudentDialog;
    private javax.swing.JTable manageStudentTable;
    private javax.swing.JButton manageStudentsBtn;
    private javax.swing.JButton recentEventsBtn;
    private javax.swing.JTable recentRecordsTable;
    private javax.swing.JPanel recordsPanel;
    private javax.swing.JPanel recordsTablePanel;
    private javax.swing.JPanel ribbonPanel;
    private javax.swing.JTextField searchTF;
    private javax.swing.JButton settingsBtn;
    private javax.swing.JComboBox<String> sortCB;
    private javax.swing.JPanel statisticsPanel;
    private javax.swing.JTable studentPerCourseTable;
    private javax.swing.JLabel totalPresent;
    private javax.swing.JLabel totalPresent1;
    private javax.swing.JButton updateStudentBtn;
    // End of variables declaration//GEN-END:variables
}
