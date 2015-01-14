package ait.ac.at.eark;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.action.ActionFactory;
import de.uzk.hki.da.action.ActionRegistry;
import de.uzk.hki.da.cb.BuildAIPAction;
import de.uzk.hki.da.cb.CheckFormatsAction;
import de.uzk.hki.da.cb.ConvertAction;
import de.uzk.hki.da.cb.CreatePremisAction;
import de.uzk.hki.da.cb.RegisterURNAction;
import de.uzk.hki.da.cb.RestructureAction;
import de.uzk.hki.da.cb.ScanAction;
import de.uzk.hki.da.cb.ScanForPresentationAction;
import de.uzk.hki.da.cb.TarAction;
import de.uzk.hki.da.cb.UnpackAction;
import de.uzk.hki.da.cb.UpdateMetadataAction;
import de.uzk.hki.da.cb.ValidateMetadataAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import de.uzk.hki.da.core.IngestGate;
import de.uzk.hki.da.core.RegisterObjectService;
import de.uzk.hki.da.core.SubsystemNotAvailableException;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.format.FileFormatFacade;
import de.uzk.hki.da.format.StandardFileFormatFacade;
import de.uzk.hki.da.grid.FakeGridFacade;
import de.uzk.hki.da.grid.GridFacade;
import de.uzk.hki.da.metadata.MetadataStructureFactory;
import de.uzk.hki.da.model.ConversionInstruction;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import de.uzk.hki.da.repository.RepositoryException;
import de.uzk.hki.da.service.HibernateUtil;
import de.uzk.hki.da.util.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hsqldb.server.Server;
import org.apache.commons.io.FileUtils;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import de.uzk.hki.da.grid.DistributedConversionAdapter;
import de.uzk.hki.da.grid.FakeDistributedConversionAdapter;

public class App {

	public static final String INGEST_AREA = "/home/shs/workspace/sip2aiptest/storage/IngestArea";
	public static final String INGEST_AREA_SUBDIR = INGEST_AREA + "/TEST";
	public static final String SIP_EXAMPLE_PATH = "/home/shs/Development/EARK/data/sipmaterial.tar";

	protected Logger logger = LoggerFactory
			.getLogger(this.getClass().getName());

	public static void main(String[] args) throws IOException, FileNotFoundException, UserException, RepositoryException, SubsystemNotAvailableException {
		File sipExampleFile = new File(SIP_EXAMPLE_PATH);
		File sipIngestDir = new File(INGEST_AREA_SUBDIR);
		FileUtils.copyFileToDirectory(sipExampleFile, sipIngestDir);
		App app = new App();
		app.start();
	}

	private void start() throws IOException, FileNotFoundException,	UserException, RepositoryException, SubsystemNotAvailableException {
		cleanDb();
		try {
			try {
				HibernateUtil.init("/home/shs/workspace/sip2aiptest/conf/hibernateCentralDB.cfg.xml");
			} catch (Exception e) {
				System.out.println("Hibernate initialization error");
			}

			AbstractApplicationContext ctx = new FileSystemXmlApplicationContext(
					"conf/spring-config.xml");
			ctx.registerShutdownHook();

			ActionFactory af = (ActionFactory) ctx.getBean("actionFactory");
			AbstractAction action = af.buildNextAction();
			while (action != null) {
				action.run();
				action = af.buildNextAction();
			}
		} finally {
			// FileUtils.deleteDirectory(new File(
			// "/home/shs/workspace/sip2aiptest/storage/WorkArea/work"));
		}
	}

	private void cleanDb() {
		Server hsqlServer = null;
		Connection connection = null;
		hsqlServer = new Server();
		hsqlServer.setLogWriter(null);
		hsqlServer.setSilent(true);
		hsqlServer.setDatabaseName(0, "mydb");
		hsqlServer.setDatabasePath(0,
				"file:/home/shs/workspace/DNSCore/ContentBroker/mydb");
		hsqlServer.start();
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			connection = DriverManager.getConnection("jdbc:hsqldb:file:/home/shs/workspace/DNSCore/ContentBroker/mydb",	"sa", "");
			connection.prepareStatement("DELETE FROM DOCUMENTS;").execute();
			connection.prepareStatement("DELETE FROM EVENTS;").execute();
			connection.prepareStatement("DELETE FROM CONVERSION_QUEUE;")
					.execute();
			connection.prepareStatement("DELETE FROM DAFILES;").execute();
			connection.prepareStatement("DELETE FROM QUEUE;").execute();
			connection.prepareStatement("INSERT INTO QUEUE (id,rep_name,initial_node,status,objects_id) values (1,'2015_01_14+13_50+','localnode','110', 1);").execute();
		} catch (SQLException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		hsqlServer.stop();
		hsqlServer = null;
	}

}
