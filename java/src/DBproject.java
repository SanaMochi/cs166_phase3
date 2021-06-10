/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor and date range");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. List available appointments of a given department");
				System.out.println("10. List active appointments of a given department");
				System.out.println("11. List patients with appointments given date");
				System.out.println("12. List patient details");
				System.out.println("13. List patients with active or past appointments given department");
				System.out.println("14. List addressed requests by maintenance staff");
				System.out.println("15. List patients with active or past appointments given date");
				System.out.println("16. List maintenance requests given doctors");
				System.out.println("17. Update appointment status with maintenance request");
				System.out.println("18. List departments of hospital");
				System.out.println("19. List available doctor appointments of the week given hospital name and department name");
				System.out.println("20. List appointment details");
				System.out.println("21. Make maintenance requests given list of appointments");
				System.out.println("22. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: ListAvailableAppointmentsOfDepartmentID(esql); break;
					case 10: ListActiveAppointmentsOfDepartment(esql); break;
					case 11: ListPatientsWithAppointmentsOnDate(esql); break;
					case 12: ListPatientDetails(esql); break;
					case 13: ListPatientsWithActivePastAppointmentsInDepartment(esql); break;
					case 14: ListAddressedRequestsByMaintenanceStaff(esql); break;
					case 15: ListPatientsWithActivePastAppointmentsOnDate(esql); break;
					case 16: ListMaintenanceRequestsOfDoctors(esql); break;
					case 17: UpdateAppointmentStatusWithMaintenanceRequest(esql); break;
					case 18: DepartmentsOfHospital(esql); break;
					case 19: DoctorAppointmentsOfWeek(esql); break;
					case 20: AppointmentDetails(esql); break;
					case 21: MakeMaintenanceRequests(esql); break;
					case 22: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		try {
			String query = "INSERT INTO Doctor (doctor_ID, name, specialty, did) VALUES (\'";
			System.out.print("\tEnter new doctor's id: ");
			String input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new doctor's name: ");
			input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new doctor's specialty: ");
			input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new doctor's department id: ");
			input = in.readLine();
			query += (input + "\');");

			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
// need to test
	public static void AddPatient(DBproject esql) {//2
		try {
			String query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (\'";
			System.out.print("\tEnter new patient's id: ");
			String input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new patient's name: ");
			String input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new patient's gender: ");
			String input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new patient's number of appointments: ");
			String input = in.readLine();
			query += (input + "\');");
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
// need to test
	public static void AddAppointment(DBproject esql) {//3
		try {
			String query = "INSERT INTO Appointment (appnt_ID , adate, time_slot, status) VALUES (\'";
			System.out.print("\tEnter new appointment's id: ");
			String input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new appointment's date (MM/DD/YYYY): ");
			String input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new appointment's time slot (HH:MM-HH:MM): ");
			String input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new appointment's status (AV, AC, PA, WA): ");
			String input = in.readLine();
			query += (input + "\');");
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		// First, we have to find out the status from appointment table. If the status is available/active, insert/update tuples 
		// in patient, has_appointment tables, and change the status (available -> active, active -> waitlisted) in appointment 
		// table. If the status is waitlisted, update/insert tuples in patient and had_appointment tables. For the past status, we have nothing to do.
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		try {
			String query = "SELECT * FROM Appointment, has_appointment WHERE appnt_ID = appt_id AND (status = \'AC\' OR status = \'AV\') AND doctor_id = \'";
			System.out.print("\tEnter doctor id: ");
			String input = in.readLine();
			query += input;
			query += "\' AND (adate BETWEEN \'"; 
			System.out.print("\tEnter first date of date range of the appt (MM/DD/YYYY): ");
			input = in.readLine();
			query += (input + "\' AND \'");
			System.out.print("\tEnter second date of date range of the appt (MM/DD/YYYY): ");
			input = in.readLine();
			query += (input + "\');");
						
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println ("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
//I'll finish later lmao
	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		try {
			String query = "SELECT  FROM Doctor, has_appointment, Appointment WHERE doctor_ID = doctor_id AND appnt_ID = appt_id AND GROUP BY  ORDER BY Desc count";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	//Start of EC
	// Hospital Management Queries
	public static void ListAvailableAppointmentsOfDepartmentID(DBproject esql) {//9
		// For a department ID, find the list of available appointments of the department
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListActiveAppointmentsOfDepartment(DBproject esql) {//10
		// For a department ID and a specific date, find the list of available appointments of the department
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListPatientsWithAppointmentsOnDate(DBproject esql) {//11
		// For a department ID and a specific date, find the list patients who made appointments on the day
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListPatientDetails(DBproject esql) {//12
		// For a patient ID, find the details of the patient
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListPatientsWithActivePastAppointmentsInDepartment(DBproject esql) {//13
		// For a department ID and a patient ID, find the list of active or past appointments made by the patient in the department
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListAddressedRequestsByMaintenanceStaff(DBproject esql) {//14
		// For a maintenance staff ID, find the list of requests addressed by the staff
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	// Hospital Maintenance Staff Queries
	public static void ListPatientsWithActivePastAppointmentsOnDate(DBproject esql) {//15
		// For a patient ID and date range, find the list of active or past appointments made by the patient
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListMaintenanceRequestsOfDoctors(DBproject esql) {//16
		// For a doctor name, find the list of all maintenance requests made by the doctor
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void UpdateAppointmentStatusWithMaintenanceRequest(DBproject esql) {//17
		// After a maintenance request is addressed, make necessary entries showing the available appointments for that doctor of the department
		// how would I know when to make the updates to the appts tho? with the function #4
		// is it just whenever the user calls the function, I get the 2 foreign keys and find the appt with the dept name and timeslot and
		// update the corresponding appt to active? yes
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	// Patients Queries
	public static void DepartmentsOfHospital(DBproject esql) {//18
		// For a hospital name, find the list of the specialized departments in the hospital
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void DoctorAppointmentsOfWeek(DBproject esql) {//19
		// For a hospital name and department name, find the list of all the doctors whose appointments are available on the week
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void AppointmentDetails(DBproject esql) {//20
		// For appt number, find the appt details
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	// Doctors Queries
	public static void MakeMaintenanceRequests(DBproject esql) {//21
		// For a list of available appts, make a maintenance request
		try {
			String query = "";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
