package com.aem.practice.core.workflows;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;

@Component(service = WorkflowProcess.class, property = { Constants.SERVICE_DESCRIPTION + "=media publication workflow",
		Constants.SERVICE_VENDOR + "=Adobe", "process.label=" + "media publication workflow", })

public class AssetApprovalService implements WorkflowProcess {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	@Reference
	Replicator replicator;

	@Override
	public void execute(WorkItem itemW, WorkflowSession sessionW, MetaDataMap metaW) throws WorkflowException {
		String damPath = itemW.getWorkflowData().getPayload().toString();
		log.info("DAM Path : " + damPath);
		try {
			Session session = sessionW.adaptTo(Session.class);
			if (session != null && damPath != null) {
				Node damNode = session.getNode(damPath);
				if (damNode.hasNodes()) {
					NodeIterator damChildNode = damNode.getNodes();
					while (damChildNode.hasNext()) {
						Node subChildNode = damChildNode.nextNode();
						String subChildPath = subChildNode.getPath();
						replicator.replicate(session, ReplicationActionType.ACTIVATE, subChildPath);
					}
				}
			}
		} catch (ReplicationException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}