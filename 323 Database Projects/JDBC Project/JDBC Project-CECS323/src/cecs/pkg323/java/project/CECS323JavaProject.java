package cecs.pkg323.java.project;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class connects to a database JDBC and manipulate the data inside it. It can update, add,
 * remove using prepared statements. The class connects to db first, then runs its prompts from the user.
 * It is then manipulated by the user choices. 
 * @author Greg Violan and Sarah Han
 */
public class CECS323JavaProject {
    //  Database credentials & private variables
    private static String DBNAME;
    private static boolean exit = false;
    private static Connection conn = null; //initialize the connection
    private static Statement stmt = null;  //initialize the statement that we're using
    private static ResultSet rs = null;
    
    //This is the specification for the printout that I'm doing:
    //each % denotes the start of a new field.
    //The - denotes left justification.
    //The number indicates how wide to make the field.
    //The "s" denotes that it's a string.  All of our output in this test are 
    //strings, but that won't always be the case.
    private static final String displayFormat_1="%-20s%-25s%-20s%-20s\n";
    private static final String displayFormat_2="%-20s%-30s%-20s%-20s%-20s\n";
    
    
// JDBC driver name and database URL
    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver"; // the location of the database
    private static String DB_URL = "jdbc:derby://localhost:1527/"; // accessing the network of the database
//            + "testdb;user=";

    /*
    * main class connects to the database and it prompts the user for
    * input to manipulate the database. 
    */
    public static void main(String[] args) {
        try {
            // Connecting to DB
            connectToDB();
            
            // Workflow
            while(!exit){
                int menu_input = menu();
                input(menu_input);
                if(!exit){
                    display();
                }
            }
            
            // Clean up
            if(rs != null || stmt != null){
                rs.close();
                stmt.close();
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(CECS323JavaProject.class.getName()).log(Level.SEVERE, null, ex);
        }  finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }//end finally try
        }//end try
        
        System.out.println("\nGoodbye!");
    }//end main
    /*
    * connects to the database
    */
    public static void connectToDB() throws SQLException, ClassNotFoundException{
        //Prompt the user for the database name, and the credentials.
        //If your database has no credentials, you can update this code to
        //remove that from the connection string.
        Scanner in = new Scanner(System.in);
        System.out.print("Name of the database (not the user account): ");
        DBNAME = in.nextLine();

        //Constructing the database URL connection string
        DB_URL = DB_URL + DBNAME;

        //STEP 2: Register JDBC driver
        Class.forName("org.apache.derby.jdbc.ClientDriver");

        //STEP 3: Open a connection
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(DB_URL);
    }    
    /*
    * menu choices for prompting the user
    */
    public static int menu(){
        Scanner scnr = new Scanner(System.in);
        System.out.println("\n1. List all writing groups \n2. List all the data for a group specified by the user"
                + "\n3. List all publishers \n4. List all the data for a publisher specified by the user"
                + "\n5. List all books titles \n6. List all the data for a book specified by the user"
                + "\n7. Insert a new book \n8. Insert a new publisher and update all book published by "
                + "one publisher to be published by the new publisher \n9. Remove a book specified by the user"
                + "\n10. Exit");
        System.out.println("Please choose a command:");
        int input = checkInt(1,10);
        return input;
    }
    /*
    * validated input that the user has made and do that business
    */
    public static void input(int i) throws SQLException{
        Scanner scnr = new Scanner(System.in);
        String input = null, input2 = "";
        stmt = conn.createStatement();
        
        switch(i){
            case 1: 
                rs = stmt.executeQuery("select * from writing_groups");        
                break;
            case 2: 
                boolean check = true;
                System.out.println("Please enter the writing group name:"); // need to check input
                input = scnr.nextLine();
                check = checking("writing_groups", 1, input);
                if(!check){
                    System.out.println("**invalid group name: does not exist**");
                    check = true;
                }
                else{
                    check = false;
                }
                select("writing_groups", "group_name", input);
                break;
            case 3: 
                rs = stmt.executeQuery("select * from publishers");
                break;
            case 4:
                System.out.println("Please enter the publisher name:"); // need to check input
                input = scnr.nextLine();
                select("publishers", "pub_name", input);
                break;
            case 5:
                rs = stmt.executeQuery("select * from books");
                break;
            case 6:
                check = true;
                while(check){
                    System.out.println("Please enter the group name:");
                    input = scnr.nextLine();
                    check = checking("writing_groups", 1, input);
                    if(!check){
                        System.out.println("**invalid group name: does not exist**");
                        check = true;
                    }
                    else{
                        check = false;
                    }
                }
                
                check = true;
                System.out.println("Please enter the book title:"); // need to check input
                input2 = scnr.nextLine();
                check = checking("books", 2, input2);
                if(!check){
                    System.out.println("**invalid book title: does not exist**");
                    check = true;
                }
                else{
                    check = false;
                }
                selectM("books", "group_name", "book_title", input, input2);
                break;
            case 7: // add book
                addBook();
                break;
            case 8: // add publisher
                addPublisher();
                break;
            case 9: // remove book
                removeBook();
                break;
            case 10: 
                exit = true;
                break;
            default: 
                System.out.println("default..");
                break;
        }
    }   
    /*
    * displays the results of the table
    */
    public static void display() throws SQLException{
        ResultSetMetaData rsmd = null;
        String attribute_1, attribute_2, attribute_3, attribute_4, attribute_5 = "";
       
        System.out.println("\nCreating statement...");
        
        rsmd = rs.getMetaData();
        
        // Retrieve by column name
        attribute_1 = rsmd.getColumnName(1);
        attribute_2 = rsmd.getColumnName(2);
        attribute_3 = rsmd.getColumnName(3);
        attribute_4 = rsmd.getColumnName(4);
        if(rsmd.getColumnCount() == 5){
            attribute_5 = rsmd.getColumnName(5);
        }

        if(rsmd.getColumnCount() < 5)
            System.out.printf(displayFormat_1, attribute_1, attribute_2, attribute_3, attribute_4);
        else
            System.out.printf(displayFormat_2, attribute_1, attribute_2, attribute_3, attribute_4, attribute_5);

        // Extracting data from result set
        // Display values        
        while(rs.next()){
            if(rsmd.getColumnCount() < 5){
                System.out.printf(displayFormat_1, 
                dispNull(rs.getString(attribute_1)), dispNull(rs.getString(attribute_2)), 
                dispNull(rs.getString(attribute_3)), dispNull(rs.getString(attribute_4)));
            }
            else{
                System.out.printf(displayFormat_2, 
                dispNull(rs.getString(attribute_1)), dispNull(rs.getString(attribute_2)), 
                dispNull(rs.getString(attribute_3)), dispNull(rs.getString(attribute_4)),
                dispNull(rs.getString(attribute_5)));
            } 
        }// end of while
        
    }
    /*
    * this uses a prepared statement for selecting a table and a certain attribute
    */
    public static void select(String table, String attribute, String input) throws SQLException{
        PreparedStatement pstmt = null;
        String prepare = "";
        
        prepare = "SELECT * FROM " + table + " WHERE " + attribute + " = ?";
        pstmt = conn.prepareStatement(prepare);
        pstmt.setString(1, input);
        
        rs = pstmt.executeQuery();
    } 
    /*
    * this uses a prepared statement for selecting a table and two attributes 
    */
    public static void selectM(String table, String attribute, String attribute2
            , String input, String input2) throws SQLException
    {
        PreparedStatement pstmt = null;
        String prepare = "";
        
        prepare = "SELECT * FROM " + table + " WHERE " + attribute + " = ?"
                + " AND " + attribute2 + " = ?";
        pstmt = conn.prepareStatement(prepare);
        pstmt.setString(1, input);
        pstmt.setString(2, input2);
        
        rs = pstmt.executeQuery();
    }
    /*
    * this adds a book into the database, also handles all the invalid inputs
    */
    public static void addBook() throws SQLException{
        Scanner scnr = new Scanner(System.in);
        String input, gn = null, bt = null, pn = null, yr = "";
        int pg = 0;
        PreparedStatement pstmt = null;
        boolean check = true;
        
        System.out.println("Adding book..");
        while(check){
            System.out.println("Please enter group name:");
            gn = scnr.nextLine();
            check = checking("writing_groups", 1, gn);
            if(!check){
                System.out.println("**invalid writing group name: does not exist**");
                check = true;
            } else {
                check = false;
            }
        }
       
        check = true;
        while(check){
            System.out.println("Please enter book title:");
            bt = scnr.nextLine();
            check = checking("books", 2, bt);
            if(!check){
                check = false;
            } else {
                System.out.println("**invalid book's name: it already exist**");
                check = true;
            }
        }
        
        check = true;
        while(check){
            System.out.println("Please enter publisher's name:");
            pn = scnr.nextLine();
            check = checking("publishers", 1, pn);
            if(!check){
                System.out.println("**invalid publisher's name: does not exist**");
                check = true;
            } else {
                check = false;
            }
        }
        
        check = true;
        while(check){
            System.out.println("Please enter year of publish:");
            yr = scnr.nextLine();
            if(yr.length() > 4 || !isInteger(yr)){
                System.out.println("**invalid year**");
            } else {
                check = false;
            }
        }
        
        System.out.println("Please enter number of pages:");
        pg = checkInt(1, 999999);
        input = "INSERT INTO books"
        + "(group_name, book_title, pub_name, year_published, pages) VALUES"
        + "(?,?,?,?,?)";
        
        pstmt = conn.prepareStatement(input);
        pstmt.setString(1, gn);
        pstmt.setString(2, bt);
        pstmt.setString(3, pn);
        pstmt.setString(4, yr);
        pstmt.setInt(5, pg);
        
        pstmt.executeUpdate();
        rs = stmt.executeQuery("SELECT * FROM books");
    }
    /*
    * removes a book in the database
    */
    public static void removeBook() throws SQLException{
        Scanner scnr = new Scanner(System.in);
        PreparedStatement pstmt = null;
        String temp, input = "";
        boolean check = true;
        
        while(check){
            System.out.println("Please enter the book title ('c' to cancel):");
            input = scnr.nextLine();  
            check = checking("books", 2, input);
            if(input.equalsIgnoreCase("c")){
                check = false;
            }
            else if(!check){
                System.out.println("**invalid book title: does not exist**");
                check = true;
            }
            else{
                check = false;
                temp = "DELETE FROM books WHERE book_title = ?";
                pstmt = conn.prepareStatement(temp);
                pstmt.setString(1, input);

                pstmt.executeUpdate();
                rs = stmt.executeQuery("select * from books");
            }
        }
        

    }
    /**
     * adds a publisher in the database
     * @throws SQLException 
     */
    public static void addPublisher() throws SQLException{
        Scanner scnr = new Scanner(System.in);
        String input, pn = null, pa, pp = null, pe, pu = "";
        PreparedStatement pstmt = null;
        boolean check = true;
        
        while(check){
            System.out.println("Adding publisher..\nPlease enter the publisher's name:");
            pn = scnr.nextLine();
            check = checking("publishers", 1, pn);
            if(!check){
                check = false;
            }
            else{
                System.out.println("**invalid publisher: already exist**");
                check = true;
            }
        }
        
        System.out.println("Please enter the publisher's address title:");
        pa = scnr.nextLine();
        
  
        System.out.println("Please enter the publisher's phone:");
        pp = scnr.nextLine();
        
        System.out.println("Please enter the publisher's email");
        pe = scnr.nextLine();
        
        check = true;
        while(check){
            System.out.println("Please enter which publisher you want to update it on");
            pu = scnr.nextLine();
            check = checking("publishers", 1, pu);
            if(!check){
                System.out.println("**invalid publisher name**");
                check = true;
            }
            else{
                check = false;
            }
        }
        
        input = "INSERT INTO publishers"
        + "(pub_name, pub_address, pub_phone, pub_email) VALUES"
        + "(?,?,?,?)";
        
        pstmt = conn.prepareStatement(input);
        pstmt.setString(1, pn);
        pstmt.setString(2, pa);
        pstmt.setString(3, pp);
        pstmt.setString(4, pe);
        
        pstmt.executeUpdate();
        
        input = "update books"
        + " set pub_name = ?"
        + " where pub_name = ?";
        
        pstmt = conn.prepareStatement(input);
        pstmt.setString(1, pn);
        pstmt.setString(2, pu);
        
        pstmt.executeUpdate();
        
        rs = stmt.executeQuery("SELECT * FROM books");
    }
    /**
     * checks the database's table if a data already exist
     * @param table
     * @param col
     * @param input
     * @return check
     * @throws SQLException 
     */
    public static boolean checking(String table, int col, String input) throws SQLException{
        ArrayList<String> arr = new ArrayList<String>();
        PreparedStatement pstmt = null;
        boolean check = false;
        String prepare, s = "";
        
        prepare = "select * from " + table;
        pstmt = conn.prepareStatement(prepare);
        rs = pstmt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        
        while(rs.next()){
            s = dispNull(rs.getString(rsmd.getColumnName(col)));
            arr.add(s);
        }
        
        if(arr.contains(input)){
            check = true;
        }else{
            check = false;
        }
        
        return check;
    }
    /**
 * Takes the input string and outputs "N/A" if the string is empty or null.
 * @param input The string to be mapped.
 * @return  Either the input string or "N/A" as appropriate.
 */
    public static String dispNull (String input) {
        //because of short circuiting, if it's null, it never checks the length.
        if (input == null || input.length() == 0)
            return "NULL";
        else
            return input;
    }
    /**
     * checks the input of the user
     * @param low, lowest input choice
     * @param high, highest input choice
     * @return the integer
     */
    public static int checkInt( int low, int high ) {
        Scanner in = new Scanner(System.in);
        boolean valid = false;
        int validNum = 0;

        while( !valid ) {
                if(in.hasNextInt()) {
                        validNum = in.nextInt();
                        if( validNum >= low && validNum <= high ){
                                valid = true;
                        } 
                        else{
                                System.out.println("**invalid input**");
                        }
                }
                else{
                        //clear buffer of junk input
                        in.next();
                        System.out.println("**invalid input**");
                }
        }
        return validNum;
    }
    /**
     * checks if the string is an integer
     * @param s, the string
     * @return true or false
     */
    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
}//end FirstExample}
