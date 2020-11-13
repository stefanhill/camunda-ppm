package uni_ko.JHPOFramework.DB_Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public abstract class DefaultStringSerialization {

	public String object2String() throws IOException {
	     ByteArrayOutputStream bo = new ByteArrayOutputStream();
	     ObjectOutputStream so = new ObjectOutputStream(bo);
	     so.writeObject(this);
	     so.flush();
	     return Base64.getEncoder().encodeToString(bo.toByteArray());
	}
	public static Object String2Object(String serializedObject) throws IOException, ClassNotFoundException {
		 byte b[] = Base64.getDecoder().decode(serializedObject);
	     ByteArrayInputStream bi = new ByteArrayInputStream(b);
	     ObjectInputStream si = new ObjectInputStream(bi);
	     return si.readObject();
	}
}
