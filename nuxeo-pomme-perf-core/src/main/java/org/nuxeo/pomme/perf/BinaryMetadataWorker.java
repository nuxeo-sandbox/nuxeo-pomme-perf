package org.nuxeo.pomme.perf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.binary.metadata.api.BinaryMetadataService;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class BinaryMetadataWorker extends AbstractWork {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(BinaryMetadataWorker.class);

    public BinaryMetadataWorker(String repositoryName, String docId) {
        super(repositoryName + ':' + docId + ":BinaryMetadataMapper");
        setDocument(repositoryName, docId);
    }

    @Override
    public String getCategory() {
        return "binaryMetadataExtraction";
    }

    @Override
    public void work() {

        setProgress(Work.Progress.PROGRESS_INDETERMINATE);
        setStatus("Extracting");

        if (!TransactionHelper.isTransactionActive()) {
            startTransaction();
        }

        openSystemSession();
        if (!session.exists(new IdRef(docId))) {
            setStatus("Nothing to process");
            return;
        }

        DocumentModel doc = session.getDocument(new IdRef(docId));
        Property fileProperty = doc.getProperty("file:content");
        Blob blob = (Blob) fileProperty.getValue();

        if (blob == null) {
            setStatus("Nothing to process");
            return;
        }

        BinaryMetadataService service = Framework.getService(BinaryMetadataService.class);

        service.writeMetadata(doc);
        session.saveDocument(doc);

        TransactionHelper.commitOrRollbackTransaction();
        closeSession();

        setStatus("Done");
    }

    @Override
    public String getTitle() {
        return "Metadata Extraction "+id;
    }

}