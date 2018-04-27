package org.nuxeo.pomme.perf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;

public class CustomBinaryMetadataListener implements EventListener {

    private static final Log log = LogFactory.getLog(CustomBinaryMetadataListener.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        if (doc.isProxy() || doc.isVersion()) {
            return;
        }

        if (!doc.hasSchema("file") || doc.hasFacet("Video")) {
            return;
        }

        BlobProperty blobProperty = (BlobProperty) doc.getProperty("file:content");

        // nothing to do if no blob
        if (blobProperty.getValue() == null) {
            return;
        }

        // do mapping if blob is dirty
        if (DOCUMENT_CREATED.equals(event.getName()) || blobProperty.isDirty()) {
            BinaryMetadataWorker work = new BinaryMetadataWorker(doc.getRepositoryName(), doc.getId());
            WorkManager workManager = Framework.getService(WorkManager.class);
            workManager.schedule(work, WorkManager.Scheduling.IF_NOT_SCHEDULED, true);
        }
    }

}