/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iu.uis.eden.actions;

import edu.iu.uis.eden.ActionTakenEvent;
import edu.iu.uis.eden.DocumentRouteLevelChange;
import edu.iu.uis.eden.DocumentRouteStatusChange;
import edu.iu.uis.eden.clientapp.DeleteEvent;
import edu.iu.uis.eden.clientapp.WorkflowDocument;
import edu.iu.uis.eden.clientapp.vo.NetworkIdVO;
import edu.iu.uis.eden.exception.WorkflowException;
import edu.iu.uis.eden.exception.WorkflowRuntimeException;
import edu.iu.uis.eden.postprocessor.PostProcessor;
import edu.iu.uis.eden.postprocessor.ProcessDocReport;

/**
 * This is a post processor class used for a Super User Test 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class SuperUserActionInvalidPostProcessor implements PostProcessor {

    /**
     * THIS METHOD WILL THROW AN EXCEPTION IF OLD ROUTE NODE IS 'WorkflowTemplate'
     */
    public ProcessDocReport doActionTaken(ActionTakenEvent event) throws Exception {
        if (isDocumentPostProcessable(new WorkflowDocument(new NetworkIdVO("rkirkend"), event.getRouteHeaderId()))) {
            return new ProcessDocReport(true, "");
        }
//        return new ProcessDocReport(true, "");
        throw new WorkflowRuntimeException("Post Processor should never be called in this instance");
    }

    /**
     * THIS METHOD WILL THROW AN EXCEPTION IF OLD ROUTE NODE IS 'WorkflowTemplate'
     */
    public ProcessDocReport doDeleteRouteHeader(DeleteEvent event) throws Exception {
        if (isDocumentPostProcessable(new WorkflowDocument(new NetworkIdVO("rkirkend"), event.getRouteHeaderId()))) {
            return new ProcessDocReport(true, "");
        }
//        return new ProcessDocReport(true, "");
        throw new WorkflowRuntimeException("Post Processor should never be called in this instance");
    }

    /**
     * THIS METHOD WILL THROW AN EXCEPTION IF OLD ROUTE NODE IS 'WorkflowTemplate'
     */
    public ProcessDocReport doRouteLevelChange(DocumentRouteLevelChange levelChangeEvent) throws Exception {
        if (isDocumentPostProcessable(new WorkflowDocument(new NetworkIdVO("rkirkend"), levelChangeEvent.getRouteHeaderId()))) {
            return new ProcessDocReport(true, "");
        }
        if ("WorkflowDocument2".equals(levelChangeEvent.getNewNodeName())) {
            return new ProcessDocReport(true, "");
        }
//        return new ProcessDocReport(true, "");
        throw new WorkflowRuntimeException("Post Processor should never be called in this instance");
    }

    /**
     * THIS METHOD WILL THROW AN EXCEPTION IF OLD ROUTE NODE IS 'WorkflowTemplate'
     */
    public ProcessDocReport doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) throws Exception {
        if (isDocumentPostProcessable(new WorkflowDocument(new NetworkIdVO("rkirkend"), statusChangeEvent.getRouteHeaderId()))) {
            return new ProcessDocReport(true, "");
        }
//        return new ProcessDocReport(true, "");
        throw new WorkflowRuntimeException("Post Processor should never be called in this instance");
    }
    
    private boolean isDocumentPostProcessable(WorkflowDocument doc) throws WorkflowException {
        String[] nodeNames = doc.getNodeNames();
        if (nodeNames != null && nodeNames.length > 0) {
            return ((doc.getNodeNames()[0].equals("AdHoc")) || (doc.getNodeNames()[0].equals("WorkflowDocument")));
        }
        return false;
    }

}
