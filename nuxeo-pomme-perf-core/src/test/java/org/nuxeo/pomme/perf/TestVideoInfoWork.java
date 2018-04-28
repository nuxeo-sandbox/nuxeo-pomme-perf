package org.nuxeo.pomme.perf;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.video.service.VideoInfoWork;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-pomme-perf-core",
        "org.nuxeo.ecm.platform.picture.api",
        "org.nuxeo.ecm.platform.picture.core",
        "org.nuxeo.ecm.platform.video.api",
        "org.nuxeo.ecm.platform.video.core",
        "org.nuxeo.ecm.platform.video.convert",
        "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.binary.metadata",
        "org.nuxeo.ecm.actions",
        "nuxeo-pomme-perf-core:disable-metadata-queue-contrib.xml"
})
public class TestVideoInfoWork {

    @Inject
    CoreSession session;

    @Inject
    WorkManager wm;


    @Test
    public void testWorker() {
        File file = new File(getClass().getResource("/files/nuxeo.3gp").getPath());
        DocumentModel video = session.createDocumentModel(session.getRootDocument().getPathAsString(),"video","Video");
        video.setPropertyValue("file:content",new FileBlob(file));
        video = session.createDocument(video);
        TransactionHelper.commitOrRollbackTransaction();
        List<Work> list = wm.listWork("MetadataExtraction",null);
        Assert.assertEquals(1,list.size());
        VideoInfoWork worker = (VideoInfoWork) list.get(0);
        Assert.assertEquals(video.getId(),worker.getDocument().getIdRef().value);
    }

}
