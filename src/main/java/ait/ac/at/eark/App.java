package ait.ac.at.eark;

import de.uzk.hki.da.cb.RestructureAction;
import de.uzk.hki.da.cb.UnpackAction;
import de.uzk.hki.da.core.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.core.IngestGate;
import de.uzk.hki.da.core.Path;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.model.Agent;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Node;

import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.SecondStageScanPolicy;
import de.uzk.hki.da.repository.RepositoryException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class App {

    public static final String INGEST_AREA = "/home/shs/Development/EARK/data/TestRepo/IngestArea";
    public static final String INGEST_AREA_SUBDIR = INGEST_AREA+"/ait";
    public static final String WORK_AREA = "/home/shs/Development/EARK/data/TestRepo/WorkArea";
    public static final String SIP_EXAMPLE_PATH = "/home/shs/Development/EARK/data/sipmaterial.tar";
    
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public static void main(String[] args) throws IOException, FileNotFoundException, UserException, RepositoryException {
        File sipExampleFile = new File(SIP_EXAMPLE_PATH);
        File sipIngestDir = new File(INGEST_AREA_SUBDIR);
        FileUtils.copyFileToDirectory(sipExampleFile, sipIngestDir);
        App app = new App();
        app.start();
    }

    private void start() throws IOException, FileNotFoundException, UserException, RepositoryException {
        logger.info("Setting up HibernateUtil ..");
        try {
            HibernateUtil.init("conf/hibernateCentralDB.cfg.xml");
        } catch (Exception e) {
            logger.error("Exception in main!", e);
        }
        User user = new User();
        user.setId(1);
        user.setShort_name("ait");
        user.setUsername("ait");

        Agent a = new Agent();
        a.setLongName("AIT Austrian Institute of Technology");
        a.setName("AIT");
        a.setType("CONTRACTOR");
        Set<Agent> agents = new HashSet<Agent>();
        agents.add(a);
        
        Object object = new Object();

        Package p = new Package("1");
        p.setContainerName("sipmaterial.tar");
        p.setName("1");
        p.setTransientBackRefToObject(object);
        List<Package> packages = new ArrayList<Package>();
        packages.add(p);

        
        object.setDate_created("1414580482723");
        object.setDate_created("1414580482723");
        object.setDdbExclusion(false);
        object.setIdentifier("1-2014102906");
        object.setInitial_node("localnode");
        object.setLast_checked(new Date());
        object.setObject_state(40);
        object.setOrig_name("sipmaterial");
        object.setPublished_flag(0);
        object.setUrn("urn:nbn:at:aittest-1-2014102906");
        object.setContractor(user);
        object.setAgents(agents);
        object.setPackages(packages);

        Path ingestAreaRootPath = Path.make(INGEST_AREA);
        Node localNode = new Node();
        localNode.setIngestAreaRootPath(ingestAreaRootPath);
        Path workAreaRootPath = Path.make(WORK_AREA);
        localNode.setWorkAreaRootPath(workAreaRootPath);

        

        IngestGate ig = new IngestGate();
        ig.setWorkAreaRootPath(workAreaRootPath.toString());

        // create unpack action
        UnpackAction ua = new UnpackAction();
        ua.setIngestGate(ig);
        ua.setObject(object);
        ua.setLocalNode(localNode);
        // set transient node ref
        object.setTransientNodeRef(localNode);
        // execute unpack action
        ua.implementation();
        
//        Job job = new Job();
//        job.setId(1);
//        job.setObject(object);
//        job.setRep_name("rep_name");
//        job.setStatus("140");
        
//        PreservationSystem ps = new PreservationSystem();
//        SecondStageScanPolicy sssp = new SecondStageScanPolicy();
//        sssp.setId(1);
//        sssp.setPUID("fmt/110");
//        List<SecondStageScanPolicy> ssspPolicies = new ArrayList<SecondStageScanPolicy>();
//        ssspPolicies.add(sssp);
//        ps.setSubformatIdentificationPolicies(ssspPolicies);
//
//        RestructureAction ra = new RestructureAction();
//        ra.setIngestGate(ig);
//        ra.setObject(object);
//        ra.setLocalNode(localNode);
//        // set transient node ref
//        object.setTransientNodeRef(localNode);
//        ra.setJob(job);
//        ra.setPSystem(ps);
//        // execute restructure action
//        ra.implementation();
        
        logger.info("output directory deleted");
        //FileUtils.deleteDirectory(new File("/home/shs/Development/EARK/data/TestRepo/WorkArea/work"));
    }

}
