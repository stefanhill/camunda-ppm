package uni_ko.JHPOFramework.DB_Utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import uni_ko.JHPOFramework.Structures.Pair;

public class Table_Information {
	public static String data_table = "Data_Table";
	public static String task_table = "Task_Stack";
	public static String head_table = "Head_Result_Table";
	public static String opt_table  = "Optimizer_Table";
	
//	public static Connection db_con;
	
	public static HashMap<String, List<Pair<String, String>>> table_field_names = new HashMap<String, List<Pair<String, String>>>();
}
