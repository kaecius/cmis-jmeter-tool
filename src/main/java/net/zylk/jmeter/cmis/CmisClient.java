package net.zylk.jmeter.cmis;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CmisClient {

    private String username;

    private String password;

    private String baseUrl;

    private Session session;

    private static Logger log = Logger.getLogger(CmisClient.class);

    public CmisClient(String username, String password, String baseUrl) {
        if (log.isDebugEnabled()) {
            log.debug("Create client");
        }
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
        log.debug("username=" + username);
        log.debug("pass=" + password);
        log.debug("baseUrl=" + baseUrl);
        // create a session
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.USER, username);
        parameter.put(SessionParameter.PASSWORD, password);
        parameter.put(SessionParameter.ATOMPUB_URL, baseUrl);
        parameter.put(SessionParameter.BINDING_TYPE,
                BindingType.ATOMPUB.value());
        // Use the first repository
        List<Repository> repositories = factory.getRepositories(parameter);
        Session session = repositories.get(0).createSession();
        session.getRootFolder();
        this.session = session;
        log.debug("session" + session);
        session.getDefaultContext().setCacheEnabled(false);
    }

    public String createDocument(String parentPath, String name, byte[] content) {
        Folder parent = (Folder) session.getObjectByPath(parentPath);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        log.debug("File name=" + name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        InputStream stream = new ByteArrayInputStream(content);
        ContentStream contentStream = new ContentStreamImpl(name,
                BigInteger.valueOf(content.length), "text/plain", stream);
        Document ret = parent.createDocument(properties, contentStream, null);
        log.info("File created : " + ret.getName() );
        return ret.getPaths().get(0);
    }

    public int getChildren(String parentPath) {
        Folder parent = (Folder) session.getObjectByPath(parentPath);
        ItemIterable<CmisObject> children = parent.getChildren();
        int ret = 0;
        for (CmisObject child : children){
            ret++;
        }
        return ret;
    }

    public String createFolder(String parentPath,String name){
        Folder parent = (Folder) session.getObjectByPath(parentPath);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        Folder f = parent.createFolder(properties);
        log.info("Folder created: " + f.getPath());
        return f.getPath();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Session getSession() {
        return session;
    }


}
