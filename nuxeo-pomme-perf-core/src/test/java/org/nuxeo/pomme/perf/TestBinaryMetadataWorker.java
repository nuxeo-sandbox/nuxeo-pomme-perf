package org.nuxeo.pomme.perf;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;
import java.io.File;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-pomme-perf-core",
        "org.nuxeo.ecm.platform.picture.api",
        "org.nuxeo.ecm.platform.picture.core",
        "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.binary.metadata",
        "org.nuxeo.ecm.actions"
})
public class TestBinaryMetadataWorker {

    @Inject
    CoreSession session;

    @Test
    public void testWorker() {
        File file = new File(getClass().getResource("/files/small.jpg").getPath());
        DocumentModel picture = session.createDocumentModel(session.getRootDocument().getPathAsString(),"picture","Picture");
        picture.setPropertyValue("file:content",new FileBlob(file));
        picture = session.createDocument(picture);
        BinaryMetadataWorker worker = new BinaryMetadataWorker(picture.getRepositoryName(),picture.getId());
        worker.work();

        TransactionHelper.startTransaction();

        picture = session.getDocument(picture.getRef());
        Assert.assertEquals("Uncalibrated",picture.getPropertyValue("imd:color_space"));
    }

}