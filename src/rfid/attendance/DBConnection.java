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
import java.sql.*;
import javax.swing.*;

public class DBConnection {
      static Connection connect;
       static String status;
       public static String databaseConnection ="Disconnected | MySQL";
   
  public static void setConnection()
  {
      try
      {
         //Class.forName("com.mysql.jdbc.Driver");
          connect = DriverManager.getConnection("jdbc:mysql://localhost/rfid_attendance", "root", "pass");
       //JOptionPane.showMessageDialog(null, "Succesfully Connected!");
       databaseConnection ="Connected | MySQL";
       
      }catch(Exception e)
     {
         JOptionPane.showMessageDialog(null,"-No connection from database. \nPlease read the instructions from ReadMe file.","Connection Failed",JOptionPane.ERROR);
         e.printStackTrace();
        
         
          
      }
  }
    public static Connection getConnection()
    {
      
        return connect;
    }
    
     public static String getStatusConnection(){
        if(connect!=null){
           status = "Database Connectivity : Successfully Connected";
        }
        else{
             status = "Database Connectivity: Connection Failed";
        }
        return status;
        
    }

    
}
