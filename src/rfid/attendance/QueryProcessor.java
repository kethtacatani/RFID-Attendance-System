/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rfid.attendance;


/**
 *
 * @author User1
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
//import static rfid.attendance.HomePanel.dataSaved;


public class QueryProcessor {
    boolean isConnected;
        public Statement stmt;
    ResultSet rs; //represents the result set of a database query .  refers to the row and column data contained in a ResultSet object
    public Connection con;
    ResultSetMetaData metadata;
    String JOusername = "";
    char[] JOpasswordChars = null;
    String JOpassword;
    public static String host="localhost";
    public static String database="rfid_attendance";
    public static String username="root";
    public static String password="";
    public static ArrayList<String> ipv4List = new ArrayList<>();
    public static ArrayList<String> dataSaved = new ArrayList<>();
    public static boolean customUser=false;
    
        public QueryProcessor()
    {
        
        try
        {
                System.out.println(username+"asd"+host);
              //  saveData();
                if(!customUser){
                    loadData();
                    System.out.println("cusomte");
                }
                    
                System.out.println("Array is "+dataSaved.toString());
                connectToDatabase();
       
            
        }catch(Exception e) {
            e.printStackTrace();
            if(isConnected)
            JOptionPane.showMessageDialog(null, e.getMessage()+"\n", "MySQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
        
        public static void saveData() {
            deleteFile("data.txt");
        try (   
                
                PrintWriter writer = new PrintWriter(new FileWriter("data.txt"))) {
            writer.write("");
            if(dataSaved.size()>0){
                for (String item : dataSaved) {
                writer.println(item);
            }
            }
            System.out.println("saved data is "+dataSaved.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
        private static void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            boolean hasData=false;
            while ((line = reader.readLine()) != null) {
                hasData=true;
                dataSaved.add(line);
                System.out.println("line "+line);
            }
            if(hasData){
                host = dataSaved.get(0);
                username = dataSaved.get(1);
                password = dataSaved.get(2);
            }
            else{
                System.out.println("Load not working");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
        public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File " + fileName + " deleted successfully.");
            } else {
                System.out.println("Failed to delete " + fileName + " file.");
            }
        } else {
            System.out.println("File " + fileName + " does not exist.");
        }
    }
     
    private void connectToDatabase() throws SQLException{
        
        System.out.println("user "+username);
         System.out.println("pass "+password);
        if(DBConnection.setConnection()){
                    isConnected=true;
                    con = DBConnection.getConnection();
                    stmt= con.createStatement();
                    
                    dataSaved.clear();
                    dataSaved.add(host);
                    dataSaved.add(username);
                    dataSaved.add(password);
                    saveData();
          }else{
            JPanel panel = new JPanel();
                JTextField usernameField = new JTextField(10);
                JPasswordField passwordField = new JPasswordField(10);
                panel.add(new JLabel("Username:"));
                panel.add(usernameField);
                panel.add(new JLabel("Password:"));
                panel.add(passwordField);

                // Show the option dialog with the panel
                int option = JOptionPane.showOptionDialog(null, panel, "Wrong Credentials", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
                //usernameField.setText(username);
                if (option == JOptionPane.OK_OPTION) {
                    username = usernameField.getText();
                    password = passwordField.getText();
                    connectToDatabase();
                    
                    
//                    JOpassword = new String(JOpasswordChars);

//                    System.out.println("Username: " + JOusername);
//                    home.username=JOusername;
//                    System.out.println("Password: " + JOpasswordChars);
//                    home.password=JOpassword;
                } else {
                    System.out.println("Login canceled.");
                  
                }
        }
    }

    
        
    public String[][] getAllRecord(String query)
    {
               String row[][]=null;
        try
        {
            rs = stmt.executeQuery(query);
            //System.out.println("get all records "+query);
           if(rs.last())
           {
               metadata=rs.getMetaData();
               row= new String[rs.getRow()][metadata.getColumnCount()];
               rs.first();
               int r=0;
               do
               {
                 for(int col=0;col<metadata.getColumnCount();col++)
                   {
                       row[r][col]=rs.getString(col+1);
                   }
                   r++;
               }while(rs.next());
           }
        }catch(Exception e)
        {
            e.printStackTrace();
            if(isConnected)
                e.printStackTrace();
            JOptionPane.showMessageDialog(null,"<html><body style='width: 750px;'>"+ e.getMessage()+"<br>"+query+"</body></html>", "MySQL Error", JOptionPane.ERROR_MESSAGE);
            
            
            
        }
        return row;

    }
    
    public boolean executeUpdate(String query) {
               try{
	       stmt.executeUpdate(query);
                  // System.out.println("execute query "+query);
               return true;
               }
               catch(Exception e)
               {
                   e.printStackTrace();
                    System.out.println(query);
                    if(isConnected)
                        e.printStackTrace();
                   JOptionPane.showMessageDialog(null,"<html><body style='width: 750px;'>"+ e.getMessage()+"<br>"+query+"</body></html>", "MySQL Error", JOptionPane.ERROR_MESSAGE);
                   System.out.println(query);

                   System.out.println("e is "+e.getMessage());
                   
               }
               return false;
	   }
    
    	   public String[] getSpecificRow(String query)
	   {
	       String record[]=null;
	       try
	       {
	           rs=stmt.executeQuery(query);
                   //System.out.println("get specific row "+query);
	           if(rs.first())
	           {
	               metadata=rs.getMetaData();
	               record=new String[metadata.getColumnCount()];
	               for(int col=0; col<metadata.getColumnCount(); col++)
	               {
	                   record[col]=rs.getString(col+1);
	               }
	           }

	       }catch(Exception e)
	       {
                   e.printStackTrace();
                   if(isConnected)
                       e.printStackTrace();
	    	   JOptionPane.showMessageDialog(null,"<html><body style='width: 750px;'>"+ e.getMessage()+"<br>"+query+"</body></html>", "MySQL Error", JOptionPane.ERROR_MESSAGE);
	       }
	       return record;
	   }

            public String getSpecificField(String query)
	   {
	       String record=null;
	       try
	       {
	           rs=stmt.executeQuery(query);
	           if(rs.first())
	           {
	               metadata=rs.getMetaData();
	               
	               for(int col=0; col<metadata.getColumnCount(); col++)
	               {
                           
	                   record=rs.getString(col+1);
	               }
	           }

	       }catch(Exception e)
	       {
                   e.printStackTrace();
                   if(isConnected)
                       e.printStackTrace();
	    	   JOptionPane.showMessageDialog(null,"<html><body style='width: 750px;'>"+ e.getMessage()+"<br>"+query+"</body></html>", "MySQL Error", JOptionPane.ERROR_MESSAGE);
	       }
	       return record;
	   }
            
           
          
           
}
