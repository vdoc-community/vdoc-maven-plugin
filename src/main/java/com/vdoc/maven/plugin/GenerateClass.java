package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.generate.classes.authenticate.jaxb.AuthenticateQuery;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Generate VDoc classes
 */
@Mojo(name = "generate-classes", threadSafe = true)
public class GenerateClass extends AbstractVDocMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateClass.class);

    /**
     * the VDoc url where found data model
     */
    @Parameter(required = true)
    protected String vdocURL;
    /**
     * A VDoc user login
     */
    @Parameter(required = true)
    protected String login;
    /**
     * the user's password
     */
    @Parameter(required = true)
    protected String password;
    /**
     * Base classes must be generated ?
     */
    @Parameter(required = false, defaultValue = "true")
    protected boolean baseClasses;
    /**
     * where classes must be output. You can prefer a specific folder for generated classes but don't forgot to add this folder into compile plugin.
     */
    @Parameter(required = false, defaultValue = "${project.build.sourceDirectory}")
    protected File outputDirectory;
    /**
     * custom classes must be generated. (it's recommended to turn it on only first time)
     */
    @Parameter(required = false, defaultValue = "false")
    protected boolean customClasses;

    /**
     * can be turn to off to avoid error if the server can't be join
     */
    @Parameter(required = false, defaultValue = "true")
    protected boolean failOnError;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // start by checking the url
        if (!this.vdocURL.endsWith("/")) {
            this.vdocURL += '/';
        }

        CloseableHttpClient httpClient = HttpClients.createSystem();
        try {
            // we start by creating authentication key
            AuthenticateQuery response = this.getAuthenticateToken(httpClient);

            // we get the classes
            URIBuilder uriBuilder = new URIBuilder(this.vdocURL + "navigation/classes/generator");
            uriBuilder.addParameter("_AuthenticationKey", response.getBody().getToken().getKey());
            uriBuilder.addParameter("customflag", Boolean.toString(this.customClasses));
            uriBuilder.addParameter("baseflag", Boolean.toString(this.baseClasses));


            HttpGet getClasses = new HttpGet(uriBuilder.build());
            getClasses.setHeader("Content-Type", "application/xml");

            HttpResponse getClassesResponse = httpClient.execute(getClasses);

            if (getClassesResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new MojoFailureException("Can't generate VDoc interfaces!");
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(getClassesResponse.getEntity().getContent())) {
                this.unzip(zipInputStream, this.outputDirectory);
            }

        } catch (JAXBException e) {
            LOGGER.error("Can't init the JAXB context : ", e);
            throw new MojoExecutionException("Can't init the JAXB context : " + e.getMessage());
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Can't join the VDoc server : ", e);
            if (this.failOnError) {
                throw new MojoExecutionException("Can't join the VDoc server : " + e.getMessage());
            }
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.error("Http client can't be closed : ", e);
            }
        }


    }

    protected AuthenticateQuery getAuthenticateToken(CloseableHttpClient httpclient) throws JAXBException, IOException, MojoFailureException {
        JAXBContext jaxbContext = JAXBContext.newInstance(AuthenticateQuery.class);
        Marshaller authenticationMarshaller = jaxbContext.createMarshaller();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        AuthenticateQuery query = new AuthenticateQuery();
        query.getHeader().setLogin(this.login);
        query.getHeader().setPassword(this.password);

        HttpPost tokenPost = new HttpPost(this.vdocURL + "navigation/flow?module=portal&cmd=authenticate");
        tokenPost.setHeader("Content-Type", "application/xml");

        // set the body
        try (StringWriter writer = new StringWriter()) {
            authenticationMarshaller.marshal(query, writer);
            tokenPost.setEntity(new StringEntity(writer.getBuffer().toString()));
        }

        HttpResponse tokenResponse = httpclient.execute(tokenPost);

        if (tokenResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new MojoFailureException("Can't authenticate to VDoc serveur!");
        }

        AuthenticateQuery response = (AuthenticateQuery) unmarshaller.unmarshal(tokenResponse.getEntity().getContent());

        LOGGER.info("token is : " + response.getBody().getToken().getKey());
        return response;
    }

    public void unzip(ZipInputStream stream, File output) throws IOException {
        // create a buffer to improve copy performance later.
        byte[] buffer = new byte[2048];

        // now iterate through each item in the stream. The get next
        // entry call will return a ZipEntry for each file in the
        // stream
        ZipEntry entry;
        while ((entry = stream.getNextEntry()) != null) {
            // Once we get the entry from the stream, the stream is
            // positioned read to read the raw data, and we keep
            // reading until read returns 0 or less.
            File targetFile = new File(this.project.getCompileSourceRoots().iterator().next(), entry.getName());
            targetFile.getParentFile().mkdirs();
            LOGGER.info("Create file : " + targetFile.getPath());
            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                int len;
                while ((len = stream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
            }
        }
    }

    public String getVdocURL() {
        return this.vdocURL;
    }

    public void setVdocURL(String vdocURL) {
        this.vdocURL = vdocURL;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBaseClasses() {
        return this.baseClasses;
    }

    public void setBaseClasses(boolean baseClasses) {
        this.baseClasses = baseClasses;
    }

    public File getOutputDirectory() {
        return this.outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isCustomClasses() {
        return this.customClasses;
    }

    public void setCustomClasses(boolean customClasses) {
        this.customClasses = customClasses;
    }

    public boolean isFailOnError() {
        return this.failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
}
