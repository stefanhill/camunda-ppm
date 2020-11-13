package uni_ko.JHPOFramework.DB_Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import uni_ko.JHPOFramework.SimulationEnvironment.Data.Data;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.JHPOFramework.Structures.Triple;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;

public class Table_Management implements Serializable{

	public static List<Integer> createDB(
			List<Class<? extends Classifier>> classifier_types,
			HashMap<String, Method> relevant_metric_methods,
			Data data,
			Connection db_con,
			String path
			) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, SQLException, Data_Exception, IOException {
		List<Integer> data_ids = Table_Management.create_tables(db_con, classifier_types, relevant_metric_methods, data, path);

		DatabaseMetaData md = db_con.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		while (rs.next()) {
			String table_name = rs.getString(3);
			ResultSet rset = md.getColumns(null, null, table_name, null);
	
			List<Pair<String, String>> names = new ArrayList<Pair<String, String>>();
			while (rset.next())
			{
				names.add(new Pair<>(rset.getString(4), rset.getString(6)));
			}
			Table_Information.table_field_names.put(table_name, names);
		}
		return data_ids;
	}
	
	public static List<Integer> create_tables(
			Connection db_con,
			List<Class<? extends Classifier>> classifier_types, 
			HashMap<String, Method> relevant_metric_methods,
			Data data,
			String path
			) throws NoSuchMethodException, SecurityException, SQLException, InstantiationException, IllegalAccessException, Data_Exception, IOException {
		List<Integer> data_ids = Table_Management.create_data_table(db_con, data, path, new ArrayList<Integer>());
		Table_Management.create_head_table(db_con, relevant_metric_methods);
		for(Class<? extends Classifier> classifier_type : classifier_types) {
			Table_Management.create_class_table(db_con, classifier_type);
		}
		Table_Management.create_task_table(db_con);
		Table_Management.create_optimizer_table(db_con);
		return data_ids;
	}
//	public List<Integer> data_ids = new ArrayList<Integer>();
	private static List<Integer> create_data_table(Connection db_con, Data data, String path, List<Integer> data_ids) throws SQLException, Data_Exception, IOException {
        String sql = "";
        sql += "CREATE TABLE IF NOT EXISTS Data_Table (\n";
        sql += "id integer PRIMARY KEY, \n";
        sql += "train_path text NOT NULL, \n";
        sql += "test_path text NOT NULL, \n";
        sql += "name text NOT NULL, \n";
        sql += "UNIQUE(name) \n";
        sql += ");";
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        
        
        for(Triple<Data_Set, Data_Set, String> tri : data.get_data()) {
        	Data_Set train = tri.getFirst();
        	Data_Set test = tri.getSecond();
        	String name = tri.getThird();
        	
        	train.export_SER(path+"DataSet"+File.separator+name+"_train.ser");
        	test.export_SER (path+"DataSet"+File.separator+name+"_test.ser");
        	
        	String in = "";
        	in += "INSERT INTO Data_Table ( train_path, test_path, name ) \n";
        	in += "VALUES(";
        	in += "'" + path+"DataSet"+File.separator+name+"_train.ser',";
        	in += "'" + path+"DataSet"+File.separator+name+"_test.ser',";
        	in += "'" + name + "');";
            stmt = db_con.createStatement();
            stmt.execute(in);
            ResultSet genKeys = stmt.getGeneratedKeys();
            stmt.close();
            while(genKeys.next()) {
            	data_ids.add(genKeys.getInt(1));
            }
        } 
        stmt.close();
        return data_ids;
	}
	private static void create_head_table(Connection db_con, HashMap<String, Method> relevant_metric_methods) throws SQLException {
        String sql = "";
        sql += "CREATE TABLE IF NOT EXISTS " + Table_Information.head_table + " (\n";
        sql += "id integer PRIMARY KEY, \n";
        sql += "run_id integer NOT NULL, \n";
        sql += "classifier text NOT NULL, \n";
        sql += "time_in_ms integer NOT NULL, \n";
       
        
        for(Entry<String, Method> rmm : relevant_metric_methods.entrySet()) {
        	sql += rmm.getKey() + " REAL NOT NULL, \n";
        }
        
        sql += "FOREIGN KEY(run_id) REFERENCES " + Table_Information.task_table + "(run_id)\n";
        sql += ");";
        sql = sql.replace("-", "_");
        
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        stmt.close();
	}
	private static void create_class_table(Connection db_con, Class<? extends Classifier> classifier_type) throws SQLException, InstantiationException, IllegalAccessException {
        String sql = "";
        sql += "CREATE TABLE IF NOT EXISTS " + classifier_type.getSimpleName() + " (\n";
        sql += "id integer PRIMARY KEY,\n";
        sql += "configuration_fk integer,\n";

        for(Parameter_Communication_Wrapper pcw : classifier_type.newInstance().configurational_parameters()) {
        	if(pcw.getClass() == Parameter_Communication_Wrapper_Lists.class) {
        		sql += pcw.text.replace(" ", "_") + " " + Table_Management.get_data_type((Parameter_Communication_Wrapper_Lists)pcw)  + " NOT NULL, \n";
        	}else if(pcw.getClass() == Parameter_Communication_Wrapper_Single.class) {
        		sql += pcw.text.replace(" ", "_") + " " + Table_Management.get_data_type((Parameter_Communication_Wrapper_Single)pcw)  + " NOT NULL, \n";
        	}
        }
        sql += "FOREIGN KEY(configuration_fk) REFERENCES " + Table_Information.head_table + "(id)\n";
        sql += ");";
        sql = sql.replace("-", "_");
        
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        stmt.close();
	}
	private static void create_task_table(Connection db_con) throws SQLException {
        String sql = "";
        sql += "CREATE TABLE IF NOT EXISTS " + Table_Information.task_table + " (\n";
        sql += "run_id integer PRIMARY KEY, \n";
        sql += "data_id integer NOT NULL, \n";
        sql += "opt_id integer NOT NULL, \n";
        sql += "configuration text NOT NULL, \n";
        sql += "simple_name text NOT NULL, \n";
        sql += "is_finished integer DEFAULT 0 NOT NULL CHECK(is_finished == 0 OR is_finished == 1), \n";
        sql += "FOREIGN KEY(data_id) REFERENCES " + Table_Information.data_table + " (id), \n";
        sql += "FOREIGN KEY(opt_id) REFERENCES " + Table_Information.opt_table + " (id) \n";
        sql += ");";
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        stmt.close();
	}
	private static void create_optimizer_table(Connection db_con) throws SQLException {
        String sql = "";
        sql += "CREATE TABLE IF NOT EXISTS " + Table_Information.opt_table + " (\n";
        sql += "id integer PRIMARY KEY, \n";
        sql += "configuration text NOT NULL, \n";
        sql += "is_finished integer DEFAULT 0 NOT NULL CHECK(is_finished == 0 OR is_finished == 1) \n";
        sql += ");";
        Statement stmt = db_con.createStatement();
        stmt.execute(sql);
        stmt.close();
	}
	
    private static String get_data_type(Parameter_Communication_Wrapper_Single pcwl) {
    	if(pcwl.data_type == Integer.class || pcwl.data_type == Long.class) {
    		return "INTEGER";
    	}else if(pcwl.data_type == Float.class || pcwl.data_type == Double.class) {
    		return "REAL";
    	}else {
    		return "TEXT";
    	}
    }
    private static String get_data_type(Parameter_Communication_Wrapper_Lists pcwl) {
    	return "TEXT";
    }

	
	public static void clear_Result_Tables(Connection db_con) throws SQLException {
		ResultSet tables = db_con.getMetaData().getTables(null, null, null, null);
		while(tables.next()) {
			String table = tables.getString(3);
			if(table.equals(Table_Information.task_table)) {
				String sql = "";
				sql += "UPDATE " + table + "\n";
				sql += "SET is_finished = 0;";
		        Statement stmt = db_con.createStatement();
		        stmt.execute(sql);
		        stmt.close();
			}else if(!table.equals(Table_Information.data_table)) {
				Table_Management.clear_table(db_con, table);
			}		
		}
	}
	public static void clear_table(Connection db_con, String table_name) throws SQLException {
        Statement stmt = db_con.createStatement();
        stmt.execute("DELETE FROM " + table_name + ";");
        stmt.close();
	}





}
