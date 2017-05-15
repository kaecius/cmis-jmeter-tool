package net.zylk.jmeter.cmis;

import junit.framework.TestCase;
import net.zylk.jmeter.tools.RandomTextGenerator;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class CmisTest extends TestCase {

    private static Logger log = Logger.getLogger(CmisClient.class);
    private static final String BASE_URL = "http://localhost:8080/alfresco/api/-default-/public/cmis/versions/1.0/atom";

    private static final String USERNAME = "admin";

    private static final String PASSWORD = "admin";

    private CmisClient cmis;

    private String baseFolderName;

    private String threadNum;

    private Integer sizeKB = 8;

    private boolean fromJmeter = false;

    private RandomTextGenerator gen;

    public CmisTest() {
        super();
    }


    /**
     * Jmeter create a test case for each sampler and each thread
     *
     * @throws Exception
     *
     * @params submited by jmeter
     */
    public CmisTest(String params) throws Exception {
        super(params);
        Map<String, String> map = getParamMap(params);
        String username = USERNAME;
        if (map.containsKey("username")) {
            username = map.get("username");
        }
        String password = PASSWORD;
        if (map.containsKey("password")) {
            password = map.get("password");
        }
        String baseUrl = BASE_URL;
        if (map.containsKey("base_url")) {
            baseUrl = map.get("base_url");
        }
        if (map.containsKey("thread_num")) {
            threadNum = map.get("thread_num");
            fromJmeter = true;
        }
        if (map.containsKey("folder_path")) {
            baseFolderName = map.get("folder_path");
        } else {
            baseFolderName = "";
        }
        if (map.containsKey("size_kb")) {
            String value = map.get("size_kb");
            sizeKB = Integer.parseInt(value);
            gen = new RandomTextGenerator();
            gen.prefilCache();
            log.debug("Dictionary loaded");
        }
        log.debug("request_params:" + map.toString());
        log.debug("Create CMIS session for thread: " + threadNum
                + ", baseFolderName: " + baseFolderName);
        cmis = new CmisClient(username, password, baseUrl);
    }


    public static Map<String, String> getParamMap(String params) {
        String[] items = params.split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String item : items) {
            if (item.indexOf('=') == -1) {
                continue;
            }
            String name = item.split("=")[0];
            String value = item.split("=")[1];
            map.put(name, value);
        }
        return map;
    }


    @Override
    public void setUp() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("setUp");
        }
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("tearDown");
        }
        super.tearDown();
    }

    /**
     * Create a document in the specified folder($FOLDER_PATH).
     *
     * Note that the jmeter test runner keep the same test case instance between
     * test, so folderPath is not null.
     * @throws Exception
     */
    public void testCreateDocument() throws Exception {
        if (!fromJmeter) {
            gen = new RandomTextGenerator();
            gen.prefilCache();
        }
        String docName = "doc-" + System.currentTimeMillis() + "-T" +  threadNum;
        String content = gen.getRandomText(sizeKB);
        String docPath = cmis.createDocument(baseFolderName, docName, content.getBytes());
        log.debug("ASSERT: " + (docPath.equals(baseFolderName + (baseFolderName.equals("/") ? "" : "/") + docName)));
        assertEquals(docPath,  baseFolderName + (baseFolderName.equals("/") ? "" : "/") + docName);
    }

    public void testGetChildren() throws Exception {
        if (!fromJmeter) {
            testCreateDocument();
        }
        int count = cmis.getChildren(baseFolderName);
        assertTrue(count > 0);
    }

    public void testCreateFolder() throws Exception{
        String folderName = "folder-" + System.currentTimeMillis() + "-T" +  threadNum;
        String folderResultPath = cmis.createFolder(baseFolderName, folderName);
        log.debug("resultFolderPath=" + folderResultPath);
        log.debug("ASSERT: " + (folderResultPath.equals(baseFolderName + (baseFolderName.equals("/") ? "" : "/") + folderName)));
        assertEquals(folderResultPath, baseFolderName + (baseFolderName.equals("/") ? "" : "/") + folderName);
    }
}
