
import java.sql.*;
import java.math.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/* author: aseem shrestha */
/* database connectivity with MS-Access is done by creating
DataSourceName(dsn) in this example*/
   /* Steps to use this example:
 * go to ms-access and make a table called "student_base" and give it a
file name student_base.mdb
 * 1.	Go to Control Panel
   2.	Click  on Administrative Tools(windows 2000/xp), Click on
ODBC(win98)
   3.	click on ODBC
   4.	Then , you will see a ODBC dialog box. Click on UserDSn
   5.	Click on Add Button
   6.	Select Microsoft Access Driver(*.mdb) driver and click on finish
   7.	Give a Data Source Name : student_base
   8.	Then Click on Select
   9.	Browse on the database name you have created and click
it:student_base.mdb is a database file where all data
        will be stored
  10.	Click on OK.
 Once the DSN is created, you can do this example*/



public class DatabaseDemo extends JFrame implements ActionListener {

  private JButton btnok,btnexit,btnaddnew; //buttons
  private JTextField tf1,tf2;//textfields
  private JLabel lblname,lbladd,lblmsg; //labels

  private JPanel p1,p2,p3,psouth; //panels


 public DatabaseDemo() //constructor
  {
    //initializing buttons
    btnok = new JButton("OK");
    btnok.addActionListener(this);
    btnexit = new JButton("Exit");
    btnexit.addActionListener(this);
    btnaddnew = new JButton("AddNew");
    btnaddnew.addActionListener(this);

    //initializing textfields
    tf1 = new JTextField(12);
    tf2 = new JTextField(12);
    //initializing labels

    lblname = new JLabel("Name:");
    lbladd = new JLabel("Address:");
    lblmsg = new JLabel("",JLabel.CENTER);

    //initializing panels
    p1 = new JPanel();
    p2 = new JPanel();
    p3 = new JPanel();
    psouth = new JPanel();

    //adding buttons and label to panel p1
    //setting flowlayout
    p1.setLayout(new FlowLayout());

    p1.add(btnok);
    p1.add(btnexit);
    p1.add(btnaddnew);
    //adding lblmsg to panel p3
    p3.add(lblmsg);

    //adding both the panels to new panel,psouth
    //settin layout 2:1
    psouth.setLayout(new GridLayout(2,1));
    psouth.add(p3);
    psouth.add(p1);


    //adding label and textfields to panel p2
    p2.setLayout(new GridLayout(3,1));
    //setting line and titled border for panel p2
    p2.setBorder(BorderFactory.createLineBorder(Color.red));
    p2.setBorder(BorderFactory.createTitledBorder("Enter Your Details"));
    p2.add(lblname);
    p2.add(tf1);
    p2.add(lbladd);
    p2.add(tf2);

    //adding panel to container
    this.getContentPane().add(p2,"Center");
    this.getContentPane().add(psouth,"South");

    this.setSize(300,300);
    this.setLocation(100,200);
    this.show();


   }
   public static void main(String args[])
   {
      DatabaseDemo ad = new DatabaseDemo();
    }

 //event handling
 public void actionPerformed(ActionEvent e)
 {
   if(e.getSource()==btnok)
   {
      PreparedStatement pstm;
      ResultSet rs;
      String sql;
      //if no entries has been made and hit ok button throw an error
      //you can do this step using try clause as well
     if((tf1.getText().equals("")&&(tf2.getText().equals(""))))
      {
        lblmsg.setText("Enter your details ");
        lblmsg.setForeground(Color.magenta);
      }


    else
    {

     try
     {
       //loading the driver
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

        //connection object created using DriverManager class
        //student_base is the name of the database
        Connection connect =
DriverManager.getConnection("jdbc:odbc:pragati");

        //creating prepared statement object pstm so that query can besent to database
pstm=connect.prepareStatement("insert into student_base values(?,?)");
        pstm.setString(1,tf1.getText());
        pstm.setString(2,tf2.getText());
        //execute method to execute the query
        pstm.executeUpdate();
        lblmsg.setText("Details have been added to database");



         //closing the prepared statement  and connection object
         pstm.close();
         connect.close();
       }
       catch(SQLException sqe)
       {
         System.out.println("SQl error");
        }
        catch(ClassNotFoundException cnf)
        {
         System.out.println("Class not found error");
        }
       }
     }
    //upon clickin button addnew , your textfield will be empty to enternext record
    if(e.getSource()==btnaddnew)
    {
      tf1.setText("");
      tf2.setText("");
    }

    if(e.getSource()==btnexit)
    {
      System.exit(1);
     }
   }
 }
