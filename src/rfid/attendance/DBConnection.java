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
import static rfid.attendance.QueryProcessor.host;
import static rfid.attendance.QueryProcessor.database;
import static rfid.attendance.QueryProcessor.username;
import static rfid.attendance.QueryProcessor.password;
import static rfid.attendance.QueryProcessor.isConnected;


public class DBConnection {
      static Connection connect;
       static String status;
       public static String databaseConnection ="Disconnected | MySQL";
       
   
  public static boolean setConnection()
  {
      try
      {
          System.out.println("host is"+host);
         //Class.forName("com.mysql.jdbc.Driver");
          connect = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database+"", ""+username+"", ""+password+"");
       //JOptionPane.showMessageDialog(null, "Succesfully Connected! "+database);
       databaseConnection ="Connected | MySQL";
       return true;
       
      }catch(Exception e)
     {
        // JOptionPane.showMessageDialog(null,"-No connection from database. \nPlease read the instructions from ReadMe file.","Connection Failed",JOptionPane.ERROR_MESSAGE);
         e.printStackTrace();
        
         return false;
          
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
