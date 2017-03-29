import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;

public class DBUtil {
	private static Connection getConnection() throws ClassNotFoundException, SQLException
	{
//		ctx = new InitialContext();
//
//		DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/mysql");
//
//		if (ds != null) return ds.getConnection();
		
		Class.forName("com.mysql.jdbc.Driver");
		//return DriverManager.getConnection("jdbc:mysql://db4free.net/dbsameas?" +
        //        "user=firmao&password=sameas");
        return DriverManager.getConnection("jdbc:mysql://127.0.0.1/linklion2?" +
                        "user=root&password=sameas");        
	}
	
	/*
	 * Table Dataset(int index, string Name)
	 * Table URI(int index, string uri, int indDataset)
	 */
	public static void insert(String dataSetName, String subjURI, Integer count){
		Connection conn = null;
		Context ctx;
		try {
			conn = getConnection();
			int indDataSet = getLastIndex(conn, "DATASET") + 1;
			
			PreparedStatement prep = conn
					.prepareStatement("INSERT INTO DATASET (index, name) VALUES (?, ?);");
			prep.setInt(1, indDataSet);
			prep.setString(2, dataSetName.trim());

			prep.executeUpdate();
			
			int indURI = getLastIndex(conn, "URI") + 1;
			PreparedStatement prep2 = conn
					.prepareStatement("INSERT INTO URI (index, uri, indDataset, countDType) VALUES (?, ?, ?);");
			prep2.setInt(1, indURI);
			prep2.setString(2, subjURI.trim());
			prep2.setInt(3, indDataSet);
			prep2.setInt(4, count);

			prep2.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int getLastIndex(Connection conn, String table) {
		int iRet = 0;
		Statement st = null;
		ResultSet rs = null;

		try {
			st = conn.createStatement();
			String sQuery = "Select MAX(index) as ind from DATASET";
			rs = st.executeQuery(sQuery);
			while (rs.next()) {
				iRet = rs.getInt(1);
			}

		} catch (Exception ex) {
			Logger lgr = Logger.getLogger(DBUtil.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				//if (conn != null) {
				//	conn.close();
				//}

			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(DBUtil.class.getName());
				lgr.log(Level.WARNING, ex.getMessage(), ex);
			}
		}

		return iRet;
	}
}
