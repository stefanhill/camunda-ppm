package uni_ko.JHPOFramework.DB_Utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.OptimizationAlgorithm;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;

public class Table_Updates implements Serializable{

	public static void updateFinishedRun(Connection db_con, int runID) throws SQLException {
		String sql = "";
		sql += "UPDATE " + Table_Information.task_table + " \n";
	    sql += "SET is_finished = 1 \n";
	    sql += "WHERE run_id == " + runID + ";";
	    Table_Updates.exec(db_con, sql);	
	}
	public static void setAllUnfinished(Connection db_con) throws SQLException {
		String sql = "";
		sql += "UPDATE " + Table_Information.task_table + " \n";
		sql += "SET is_finished = 0";
        Table_Updates.exec(db_con, sql);
	}
	public static Long registerNewConfiguration(Connection db_con, String configuration, Class<? extends Classifier> classifier, int dataID, int optID) throws Exception {
		String sql = "";
		sql += "INSERT INTO " + Table_Information.task_table + " (data_id, opt_id, configuration, simple_name) \n";
		sql += "Values(" + dataID +", " + optID + ", '" + configuration + "', '" + classifier.getSimpleName() + "' ); \n";
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        return Table_Updates.getKeys(stmt);
	}
	
	public static void add_data(Connection db_con, Pair<Classifier, Metric> data, long time, HashMap<String, Method> relevant_metric_methods, int run_id) throws Exception{
		Long key = Table_Updates.insert_head(db_con, data.getKey().getClass(), time, data.getValue(), relevant_metric_methods, run_id);
		Table_Updates.insert_specific(db_con, data.getKey(), time, data.getValue(), key);
	}
	private static Long insert_head(Connection db_con, Class<? extends Classifier> classifier_type, Long time_in_ns, Metric metric, HashMap<String, Method> relevant_metric_methods, int run_id) throws Exception {
		String sql = "";
		String values = "";
		sql += "INSERT INTO " + Table_Information.head_table + " (";
		sql += "run_id,";
		for( Pair<String,String> field : Table_Information.table_field_names.get(Table_Information.head_table)) {
			if(!field.getKey().equals("id") && !field.getKey().equals("run_id")) {
				sql += field.getKey() + ",";
				if(relevant_metric_methods.containsKey(field.getKey())) {
					values += "'" + relevant_metric_methods.get(field.getKey()).invoke(metric, null)+"',";
				}else if(field.getKey().equals("classifier")) {
					values += "'" + classifier_type.newInstance().get_Classifier_Name()+ "',";
				}else if(field.getKey().equals("time_in_ms")) {
					values += "'"+ time_in_ns+ "',";
				}
			}
		}
		sql = sql.substring(0,sql.lastIndexOf(','));
		values = values.substring(0,values.lastIndexOf(','));
		
		sql += ") \n VALUES (" + run_id + "," + values + ");";

        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        stmt.close();
        return Table_Updates.getKeys(stmt);
	}
	private static void insert_specific(Connection db_con, Classifier classifier, Long time_in_ns, Metric metric, Long key) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		String sql = "";
		String values = "";
		sql += "INSERT INTO " + classifier.get_Classifier_Name() + " (";
		
		List<Parameter_Communication_Wrapper> lpcw = classifier.configurational_parameters();
		
		for( Pair<String,String> field : Table_Information.table_field_names.get(classifier.get_Classifier_Name())) {
			if(!field.getKey().equals("id") && !field.getKey().equals("configuration_fk")) {
				sql += field.getKey() + ",";
				Parameter_Communication_Wrapper pcw = lpcw.stream().filter(p -> p.text.replace("-", "_").replace(" ", "_").equals(field.getKey())).findFirst().get();
				if(pcw.current_value == null) {
					// should not happen 
					values += "'" + "VALUE" + "',";
				}else {
					values += "'" + pcw.current_value.toString() + "',";
				}
			}
		}
		sql += "configuration_fk) \n VALUES (" + values + key +");";
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        stmt.close();
	}

	public static void insert_optimizer(Connection db_con, OptimizationAlgorithm oa) throws IOException, SQLException {
		String sql = "";
		sql += "INSERT INTO " + Table_Information.opt_table + "\n";
		sql += "(configuration) \n";
		sql += "VALUES ('" + oa.object2String() + "');";
        Table_Updates.exec(db_con, sql);
	}
	public static void update_optimizer(Connection db_con, OptimizationAlgorithm oa) throws IOException, SQLException {
		String sql = "";
		sql += "UPDATE " + Table_Information.opt_table + " \n";
	    sql += "SET configuration = '" + oa.object2String() + "' \n";
	    sql += "WHERE id == " + oa.optimizerID + " \n;";
	    Table_Updates.exec(db_con, sql);	
	}
	public static void update_finish_optimizer(Connection db_con, OptimizationAlgorithm oa, int id) throws IOException, SQLException {
		String sql = "";
		sql += "UPDATE " + Table_Information.opt_table + " \n";
	    sql += "SET configuration = '" + oa.object2String() + "', \n";
	    sql += "is_finished = 1 \n";
	    sql += "WHERE id == " + id + " \n;";
	    Table_Updates.exec(db_con, sql);	
	}

	
	private static Long getKeys(Statement stmt) throws Exception {
        List<Long> keys = new ArrayList<Long>();
        ResultSet genKeys = stmt.getGeneratedKeys();
        stmt.close();
        while(genKeys.next()) {
        	keys.add(genKeys.getLong(1));
        }
        if(keys.size() > 1) {
        	throw new Exception("Too many keys were generated!");
        }else {
        	return keys.get(0);
        }
	}
	private static void exec(Connection db_con, String sql) throws SQLException {
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        stmt.close();
	}
}
