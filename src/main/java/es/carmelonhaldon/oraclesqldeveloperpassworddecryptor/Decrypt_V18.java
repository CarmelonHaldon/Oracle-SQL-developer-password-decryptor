package es.carmelonhaldon.oraclesqldeveloperpassworddecryptor;

import java.io.File;
import java.io.IOException;

import javax.naming.StringRefAddr;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import oracle.jdevimpl.db.adapter.ReferenceWorker;

import java.util.List;
import java.util.Map;

public class Decrypt_V18 {

  private static String decrypt_v18(
      String encrypted,
      String db_system_id)
  {

    if (encrypted == null) {
      return "";
    }

    ReferenceWorker referenceWorker = ReferenceWorker.createDefaultWorker(db_system_id);
    char[] password = referenceWorker.decrypt(new StringRefAddr(null, encrypted), null);
    return new String(password);
  }

  public static void main(String[] argv)
      throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

    String dbSystemId = getDbSystemId(argv[0]);
		List<Map<String, Object>> connections = getConnections(argv[0]);

		System.out.println("name,user,password");

		for (Map<String, Object> connection: connections) {

      Map<String, Object> info = (Map<String, Object>) connection.get("info");

      String name = (String) connection.get("name");
      String user = (String) info.get("user");
      String password = (String) info.get("password");

      String passwordDecrypt = decrypt_v18(password, dbSystemId);

			StringBuffer stringBuffer = new StringBuffer();

			stringBuffer.append(name);
			stringBuffer.append(",");
			stringBuffer.append(user);
			stringBuffer.append(",");
			stringBuffer.append(passwordDecrypt);

			System.out.println(stringBuffer.toString());
		}
  }

  private static String getDbSystemId(String systemPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(systemPath + "/o.sqldeveloper/product-preferences.xml"));

		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "/preferences/value[@n='db.system.id']/@v";
		return (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
	}

	private static List<Map<String, Object>> getConnections(String systemPath) throws IOException {

    DocumentContext documentContext = JsonPath.parse(new File(systemPath + "/o.jdeveloper.db.connection/connections.json"));
    return documentContext.read("$.connections");
  }
}
