package uni_ko.JHPOFramework.DB_Utils;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Config_Utils;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Executor;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveOption;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.OptimizationAlgorithm;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Reflections.ReflectionReads;

public class Table_Read implements Serializable{
	
	public static ResultSet readNextUnfinishedTask(Connection db_con, Integer optID) throws SQLException {
		String sql = "";
		sql += "SELECT * \n";
		sql += "FROM " + Table_Information.task_table + " \n";
		sql += "WHERE is_finished == 0 AND \n";
		sql += "opt_id == " + optID + " \n";
		sql	+= "ORDER BY run_id ASC;";
        return Table_Read.exec(db_con, sql);
	}
	public static ResultSet readSameDataID(Connection db_con, int data_id) throws SQLException {
    	String sql = "";
    	sql += "SELECT * \n";
    	sql += "FROM Data_Table \n";
    	sql += "WHERE id == " + data_id + ";";
    	return Table_Read.exec(db_con, sql);
	}
	public static ResultSet readClassifierCounts(Connection db_con) throws SQLException {
		String sql = "";
		sql += "SELECT simple_name, count(configuration) \n";
		sql += "FROM " + Table_Information.task_table + " \n";
		sql += "GROUP BY simple_name;";
		return Table_Read.exec(db_con, sql);
	}
	public static ResultSet readDataSetNames(Connection db_con) throws SQLException {
		String sql = "";
		sql += "SELECT name \n";
		sql += "FROM " + Table_Information.data_table + ";";
		return Table_Read.exec(db_con, sql);
	}
	public static ResultSet readDataSetIDs(Connection db_con) throws SQLException {
		String sql = "";
		sql += "SELECT id \n";
		sql += "FROM " + Table_Information.data_table + ";";
		return Table_Read.exec(db_con, sql);
	}
	
	public static ResultSet readNextUnfinishedOptimizer(Connection db_con) throws SQLException {
		String sql = "";
		sql += "SELECT * \n";
		sql += "FROM " + Table_Information.opt_table + " \n";
		sql += "WHERE is_finished == 0 ORDER BY id ASC;";
		return Table_Read.exec(db_con, sql);
	}
	public static ResultSet readAllOptimizer(Connection db_con) throws SQLException {
		String sql = "";
		sql += "SELECT * \n";
		sql += "FROM " + Table_Information.opt_table + ";";
		return Table_Read.exec(db_con, sql);
	}

	public static ResultSet readConfigurationAndRuntime(Connection db_con) throws SQLException {
		String sql = "";
		sql += "SELECT h.run_id, t.configuration, h.time_in_ms \n";
		sql += "FROM " + Table_Information.head_table + " AS h \n";
		sql += "JOIN " + Table_Information.task_table + " AS t \n";
		sql += "ON h.run_id == t.run_id;";
		return Table_Read.exec(db_con, sql);
	}
	
	public static List<HashMap<String, String>> readTopResults(Connection db_con, int topX, String metric) throws SQLException {
		String sql = "";
		sql += "SELECT * \n";
		sql += "FROM " + Table_Information.head_table + " \n";
		sql += "ORDER BY " + metric + " DESC \n";
		sql += "LIMIT " + topX + "; \n";

		List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		Statement stmt = db_con.createStatement();
		ResultSet headResult = stmt.executeQuery(sql);
		stmt.close();
		
		ResultSetMetaData headResultMeta = headResult.getMetaData();
		while(headResult.next()) {
			HashMap<String, String> re = new HashMap<String, String>();
			for(int i = 1; i <= headResultMeta.getColumnCount(); i++){
				re.put(headResultMeta.getCatalogName(i), headResult.getString(i));
			}
			
			String sq2 = "";
			sq2 += "SELECT * \n";
			sq2 += "FROM " + headResult.getString(3) + " \n";
			sq2 += "WHERE configuration_fk == " + headResult.getInt(1) + ";\n";
			
			stmt = db_con.createStatement();
			ResultSet classifierResult = stmt.executeQuery(sq2);
			stmt.close();
			ResultSetMetaData classifierResultMeta = classifierResult.getMetaData();
			while(classifierResult.next()) {
				for(int i = 1; i <= classifierResultMeta.getColumnCount(); i++){
					if(!classifierResultMeta.getCatalogName(i).equals("id")) {
						re.put(classifierResultMeta.getCatalogName(i), classifierResult.getString(i));
					}
				}
			}
			result.add(re);
		}
		return result;
	}
	public static Pair<String, HashMap<Integer, Object>> getClassifierDataByID(Connection db_con, Integer id) throws Exception {
		String sql = "";
		sql += "SELECT h.classifier , t.configuration\n";
		sql += "FROM " + Table_Information.head_table + " AS h \n";
		sql += "JOIN "+ Table_Information.task_table + " AS t \n";
		sql += "ON h.id == " + id + " AND h.run_id == t.run_id ;";
		ResultSet res = Table_Read.exec(db_con, sql);
		while(res.next()) {
			return new Pair<String, HashMap<Integer,Object>>(res.getString(1), Config_Utils.from_json(res.getString(2)));
		}
		return null;
	}
	public static Classifier getClassifierByID(Executor executor, Integer id, String givenName, String author) throws Exception {
		String sql = "";
		sql += "SELECT o.configuration, h.classifier  \n";
		sql += "FROM " + Table_Information.head_table + " AS h \n";
		sql += "JOIN "+ Table_Information.task_table + " AS t \n";
		sql += "     JOIN "+ Table_Information.opt_table + " AS o \n";
		sql += "     ON t.opt_id == o.id \n";
		sql += "AND h.id == " + id + " AND h.run_id == t.run_id ;";
		
		Statement stmt = executor.getDBCon().createStatement();
		ResultSet res = stmt.executeQuery(sql);
		// why was this block so early?
		//stmt.close();
		
		while(res.next()) {
			for(SaveOption so :	executor.getSaveDefinition().getSaveOptions()) {
				Pair<File, Double> cp = so.savedClassifier.get(id);
				if(!cp.equals(null)) {
					Classifier classifier = ReflectionReads.deserializeClassifier(cp.getFirst(), (cp.getFirst().getName().split("\\$"))[0]);
					classifier = classifier.importSER(cp.getKey().getAbsolutePath());
					
					ClassifierMeta classifier_meta = new ClassifierMeta(
							givenName,
							1,
							classifier.get_prediction_type(),
							new Date(),
							new Date(),
							author,
							false,
							classifier
							);
					classifier_meta.set_metric(classifier.last_calculated_metric);
					classifier.set_Meta_Data(classifier_meta);
					return classifier;
				}
			}
		}
		stmt.close();
		return null;
	}
	
	private static ResultSet exec(Connection db_con, String sql) throws SQLException {
        Statement stmt = db_con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs;
	}

}
