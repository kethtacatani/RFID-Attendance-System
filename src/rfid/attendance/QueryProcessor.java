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
import javax.swing.JOptionPane;


public class QueryProcessor {
        Statement stmt;
    ResultSet rs; //represents the result set of a database query .  refers to the row and column data contained in a ResultSet object
    Connection con;
    ResultSetMetaData metadata;
    
        public QueryProcessor()
    {
        try
        {
            DBConnection.setConnection();
            con = DBConnection.getConnection();
            stmt= con.createStatement();
            
        }catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage()+"\n", "MySQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    public String[][] getAllRecord(String query)
    {
               String row[][]=null;
        try
        {
            rs = stmt.executeQuery(query);
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
            JOptionPane.showMessageDialog(null,"<html><body style='width: 750px;'>"+ e.getMessage()+"<br>"+query+"</body></html>", "MySQL Error", JOptionPane.ERROR_MESSAGE);
            
            
            
        }
        return row;

    }
    
    public boolean executeUpdate(String query) {
               try{
	       stmt.executeUpdate(query);
               return true;
               }
               catch(Exception e)
               {
                   e.printStackTrace();
                    System.out.println(query);
                   JOptionPane.showMessageDialog(null,"<html><body style='width: 750px;'>"+ e.getMessage()+"<br>"+query+"</body></html>", "MySQL Error", JOptionPane.ERROR_MESSAGE);
                   System.out.println(query);

                   //System.out.println("e is "+e.getMessage());
                   
               }
               return false;
	   }
    
    	   public String[] getSpecificRow(String query)
	   {
	       String record[]=null;
	       try
	       {
	           rs=stmt.executeQuery(query);
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
	    	   JOptionPane.showMessageDialog(null,"<html><body style='width: 750px;'>"+ e.getMessage()+"<br>"+query+"</body></html>", "MySQL Error", JOptionPane.ERROR_MESSAGE);
	       }
	       return record;
	   }
           
          
           
}
