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

	public static void main(String[] args) throws IOException,
			FileNotFoundException, UserException, RepositoryException,
			SubsystemNotAvailableException {
		File sipExampleFile = new File(SIP_EXAMPLE_PATH);
		File sipIngestDir = new File(INGEST_AREA_SUBDIR);
		FileUtils.copyFileToDirectory(sipExampleFile, sipIngestDir);
		App app = new App();
		app.start();
	}

	private void start() throws IOException, FileNotFoundException,
			UserException, RepositoryException, SubsystemNotAvailableException {

		cleanDb();		

		try {
			
			try {
				HibernateUtil
						.init("/home/shs/workspace/sip2aiptest/conf/hibernateCentralDB.cfg.xml");
			} catch (Exception e) {
				System.out.println("Hibernate initialization error");
			}
			
			AbstractApplicationContext ctx =
					new FileSystemXmlApplicationContext("conf/spring-config.xml");
			ctx.registerShutdownHook();
			
			ActionFactory af = (ActionFactory) ctx.getBean("actionFactory");
			AbstractAction action = af.buildNextAction();
			while(action != null) {
				action.run();
				action = af.buildNextAction();
			}
			
			

			// ActionFactory af = (ActionFactory) ctx.getBean("actionFactory");

			// ActionFactory af = (ActionFactory) ctx.getBean("actionFactory");
			// af.buildNextAction();
			// AbstractAction action = af.buildNextAction();

			// ActionRegistry ar = (ActionRegistry)
			// ctx.getBean("actionRegistry");

			// ////////////////////
			// unpack action
			// ////////////////////
			// UnpackAction ua = (UnpackAction)
			// ctx.getBean("IngestUnpackAction");
			// ua.setJob(job);
			// // ua.setIngestGate(ig);
			// ua.setPSystem(ps);
			// ua.setObject(object);
			// ua.setLocalNode(localNode);
			// object.setTransientNodeRef(localNode);
			// ar.registerAction(ua);
			// //ua.setActionMap(ar);
			// ua.run();

			// ////////////////////
			// restructure action
			// ////////////////////
			// RestructureAction ra = new RestructureAction();
			// FileFormatFacade fileFormatFacade = new
			// StandardFileFormatFacade();
			// GridFacade gf = new FakeGridFacade();
			// ra.setName("restructuring");
			// ua.setStartStatus("110");
			// ua.setEndStatus("120");
			// ra.setFileFormatFacade(fileFormatFacade);
			// ra.setGridRoot(gf);
			// ra.setIngestGate(ig);
			// ra.setObject(object);
			// ra.setLocalNode(localNode);
			// ra.setJob(job);
			// object.setTransientNodeRef(localNode);
			// ra.setPSystem(ps);
			// ar.registerAction(ra);
			// ra.setActionMap(ar);
			// ra.run();
			//
			// // ////////////////////
			// // validate metadata action
			// // ////////////////////
			// ValidateMetadataAction vma = new ValidateMetadataAction();
			// MetadataStructureFactory msf = new MetadataStructureFactory();
			// vma.setName("validation");
			// vma.setStartStatus("120");
			// vma.setEndStatus("130");
			// vma.setObject(object);
			// vma.setLocalNode(localNode);
			// vma.setMsf(msf);
			// vma.setJob(job);
			// vma.setPSystem(ps);
			// ar.registerAction(vma);
			// vma.setActionMap(ar);
			// vma.run();
			//
			// // ////////////////////
			// // scan action
			// // ////////////////////
			// ScanAction sca = new ScanAction();
			// DistributedConversionAdapter dca = new
			// FakeDistributedConversionAdapter();
			// sca.setDistributedConversionAdapter(dca);
			// sca.setName("scanning");
			// sca.setStartStatus("130");
			// sca.setEndStatus("140");
			// sca.setObject(object);
			// sca.setLocalNode(localNode);
			// sca.setJob(job);
			// sca.setPSystem(ps);
			// ar.registerAction(sca);
			// sca.setActionMap(ar);
			// sca.run();
			//
			// // ////////////////////
			// // register urn action
			// // ////////////////////
			// RegisterURNAction rua = new RegisterURNAction();
			// rua.setName("scanning");
			// rua.setStartStatus("140");
			// rua.setEndStatus("150");
			// rua.setObject(object);
			// rua.setLocalNode(localNode);
			// rua.setJob(job);
			// rua.setPSystem(ps);
			// ar.registerAction(rua);
			// rua.setActionMap(ar);
			// rua.run();
			//
			// // ////////////////////
			// // convert action
			// // ////////////////////
			// ConvertAction conva = new ConvertAction();
			// conva.setDistributedConversionAdapter(dca);
			// conva.setName("converting");
			// conva.setStartStatus("150");
			// conva.setEndStatus("160");
			// conva.setObject(object);
			// conva.setLocalNode(localNode);
			// conva.setJob(job);
			// conva.setPSystem(ps);
			// ar.registerAction(conva);
			// conva.setActionMap(ar);
			// conva.run();
			//
			// // ////////////////////
			// // update metadata action
			// // ////////////////////
			// UpdateMetadataAction uma = new UpdateMetadataAction();
			// uma.setName("updatingmetadata");
			// uma.setStartStatus("160");
			// uma.setEndStatus("170");
			// uma.setObject(object);
			// uma.setLocalNode(localNode);
			// uma.setJob(job);
			// uma.setPSystem(ps);
			// ar.registerAction(uma);
			// uma.setActionMap(ar);
			// uma.run();
			//
			// // ////////////////////
			// // check formats action
			// // ////////////////////
			// CheckFormatsAction chkfmtsa = new CheckFormatsAction();
			// chkfmtsa.setFileFormatFacade(fileFormatFacade);
			// chkfmtsa.setName("checkingformats");
			// chkfmtsa.setStartStatus("170");
			// chkfmtsa.setEndStatus("180");
			// chkfmtsa.setObject(object);
			// chkfmtsa.setLocalNode(localNode);
			// chkfmtsa.setJob(job);
			// chkfmtsa.setPSystem(ps);
			// ar.registerAction(chkfmtsa);
			// chkfmtsa.setActionMap(ar);
			// chkfmtsa.run();
			//
			// // ////////////////////
			// // create premis action
			// // ////////////////////
			// CreatePremisAction cpa = new CreatePremisAction();
			// cpa.setFileFormatFacade(fileFormatFacade);
			// cpa.setName("creatingpremis");
			// cpa.setStartStatus("188");
			// cpa.setEndStatus("190");
			// cpa.setObject(object);
			// cpa.setLocalNode(localNode);
			// cpa.setJob(job);
			// cpa.setPSystem(ps);
			// ar.registerAction(cpa);
			// cpa.setActionMap(ar);
			// cpa.run();
			//
			// // ////////////////////
			// // scan for presentation action
			// // ////////////////////
			// ScanForPresentationAction scfpa = new
			// ScanForPresentationAction();
			// scfpa.setFormatScanService(fileFormatFacade);
			// scfpa.setDistributedConversionAdapter(dca);
			// scfpa.setName("scanningforpresentation");
			// scfpa.setStartStatus("180");
			// scfpa.setEndStatus("186");
			// scfpa.setObject(object);
			// scfpa.setLocalNode(localNode);
			// scfpa.setJob(job);
			// scfpa.setPSystem(ps);
			// ar.registerAction(scfpa);
			// scfpa.setActionMap(ar);
			// scfpa.run();
			//
			// // ////////////////////
			// // convert for presentation action
			// // ////////////////////
			// ConvertAction convfpa = new ConvertAction();
			// convfpa.setDistributedConversionAdapter(dca);
			// convfpa.setName("convertingforpresentation");
			// convfpa.setStartStatus("186");
			// convfpa.setEndStatus("188");
			// convfpa.setObject(object);
			// convfpa.setLocalNode(localNode);
			// convfpa.setJob(job);
			// convfpa.setPSystem(ps);
			// ar.registerAction(convfpa);
			// convfpa.setActionMap(ar);
			// convfpa.run();
			//
			// // ////////////////////
			// // build aip action
			// // ////////////////////
			// BuildAIPAction baipa = new BuildAIPAction();
			// baipa.setName("buildingaip");
			// baipa.setStartStatus("190");
			// baipa.setEndStatus("200");
			// baipa.setObject(object);
			// baipa.setLocalNode(localNode);
			// baipa.setJob(job);
			// baipa.setPSystem(ps);
			// ar.registerAction(baipa);
			// baipa.setActionMap(ar);
			// baipa.run();
			//
			// // ////////////////////
			// // tar action
			// // ////////////////////
			// TarAction tara = new TarAction();
			// tara.setDistributedConversionAdapter(dca);
			// tara.setName("tarpackaging");
			// tara.setStartStatus("200");
			// tara.setEndStatus("210");
			// tara.setObject(object);
			// tara.setLocalNode(localNode);
			// tara.setJob(job);
			// tara.setPSystem(ps);
			// ar.registerAction(tara);
			// tara.setActionMap(ar);
			// tara.run();

		} finally {
//			FileUtils.deleteDirectory(new File(
//					"/home/shs/workspace/sip2aiptest/storage/WorkArea/work"));
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
			connection = DriverManager
					.getConnection(
							"jdbc:hsqldb:file:/home/shs/workspace/DNSCore/ContentBroker/mydb",
							"sa", "");
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
