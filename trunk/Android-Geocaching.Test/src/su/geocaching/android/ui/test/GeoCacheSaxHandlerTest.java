package su.geocaching.android.ui.test;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import su.geocaching.android.controller.apimanager.GeoCacheSaxHandler;
import android.test.AndroidTestCase;
import android.util.Log;


/**
 * 
 * @author Nikita Bumakov
 *
 */
public class GeoCacheSaxHandlerTest extends AndroidTestCase {

	private GeoCacheSaxHandler handler = null;
	private static final String ENCODING = "windows-1251";
	private InputSource geoCacheXml;
	private SAXParser parser;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testParse() {

		URL url;
		try {
			url = new URL("http://dl.dropbox.com/u/10802739/parseTest.xml");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			geoCacheXml = new InputSource(new InputStreamReader(connection.getInputStream(), ENCODING));

			SAXParserFactory factory = SAXParserFactory.newInstance();
			handler = new GeoCacheSaxHandler();

			parser = factory.newSAXParser();
			parser.parse(geoCacheXml, handler);
		} catch (Exception e) {
			Log.e(CompasHelperTest.LOG_TAG, "error", e);
			e.printStackTrace();
		}

		int expectedid = 9360;
		int actualId = handler.getGeoCaches().get(0).getId();
		assertEquals(expectedid, actualId);

		expectedid = 62;
		actualId = handler.getGeoCaches().get(66).getId();
		assertEquals(expectedid, actualId);
	}
}
