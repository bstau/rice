/*
 * Copyright 2006-2007 The Kuali Foundation.
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
package org.kuali.rice.kns.service.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.OptimisticLockException;
import org.kuali.rice.kew.dto.ActionTakenEventDTO;
import org.kuali.rice.kew.dto.AfterProcessEventDTO;
import org.kuali.rice.kew.dto.BeforeProcessEventDTO;
import org.kuali.rice.kew.dto.DeleteEventDTO;
import org.kuali.rice.kew.dto.DocumentRouteLevelChangeDTO;
import org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.PostProcessorService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;


/**
 * This class is the postProcessor for the Kuali application, and it is responsible for plumbing events up to documents using the
 * built into the document methods for handling route status and other routing changes that take place asyncronously and potentially
 * on a different server.
 */
@Transactional
public class PostProcessorServiceImpl implements PostProcessorService {

    private static Logger LOG = Logger.getLogger(PostProcessorServiceImpl.class);

    private DocumentService documentService;
    private DateTimeService dateTimeService;

    /**
     * @see org.kuali.rice.kew.postprocessor.PostProcessorRemote#doRouteStatusChange(org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO)
     */
    public boolean doRouteStatusChange(DocumentRouteStatusChangeDTO statusChangeEvent) throws RemoteException {
        try {
        	if ( LOG.isInfoEnabled() ) {
        		LOG.info(new StringBuffer("started handling route status change from ").append(statusChangeEvent.getOldRouteStatus()).append(" to ").append(statusChangeEvent.getNewRouteStatus()).append(" for document ").append(statusChangeEvent.getRouteHeaderId()));
        	}
            establishGlobalVariables();
            Document document = documentService.getByDocumentHeaderId(statusChangeEvent.getRouteHeaderId().toString());
            if (document == null) {
                if (!KEWConstants.ROUTE_HEADER_CANCEL_CD.equals(statusChangeEvent.getNewRouteStatus())) {
                    throw new RuntimeException("unable to load document " + statusChangeEvent.getRouteHeaderId());
                }
            }
            else {
                // PLEASE READ BEFORE YOU MODIFY:
                // we dont want to update the document on a Save, as this will cause an
                // OptimisticLockException in many cases, because the DB versionNumber will be
                // incremented one higher than the document in the browser, so when the user then
                // hits Submit or Save again, the versionNumbers are out of synch, and the
                // OptimisticLockException is thrown. This is not the optimal solution, and will
                // be a problem anytime where the user can continue to edit the document after a
                // workflow state change, without reloading the form.
                if (!document.getDocumentHeader().getWorkflowDocument().stateIsSaved()) {
                    document.handleRouteStatusChange();
                    documentService.updateDocument(document);
                }
                document.doRouteStatusChange(statusChangeEvent);
            }
            if ( LOG.isInfoEnabled() ) {
            	LOG.info(new StringBuffer("finished handling route status change from ").append(statusChangeEvent.getOldRouteStatus()).append(" to ").append(statusChangeEvent.getNewRouteStatus()).append(" for document ").append(statusChangeEvent.getRouteHeaderId()));
            }
        }
        catch (Exception e) {
            logAndRethrow("route status", e);
        }
        return true;
    }

    /**
     * @see org.kuali.rice.kew.postprocessor.PostProcessorRemote#doRouteLevelChange(org.kuali.rice.kew.dto.DocumentRouteLevelChangeDTO)
     */
    public boolean doRouteLevelChange(DocumentRouteLevelChangeDTO levelChangeEvent) throws RemoteException {
        // on route level change we'll serialize the XML for the document. we
        // are doing this here cause it's a heavy hitter, and we
        // want to avoid the user waiting for this during sync processing
        try {
        	if ( LOG.isDebugEnabled() ) {
        		LOG.debug(new StringBuffer("started handling route level change from ").append(levelChangeEvent.getOldNodeName()).append(" to ").append(levelChangeEvent.getNewNodeName()).append(" for document ").append(levelChangeEvent.getRouteHeaderId()));
        	}
            establishGlobalVariables();
            Document document = documentService.getByDocumentHeaderId(levelChangeEvent.getRouteHeaderId().toString());
            if (document == null) {
                throw new RuntimeException("unable to load document " + levelChangeEvent.getRouteHeaderId());
            }
            document.populateDocumentForRouting();
            document.handleRouteLevelChange(levelChangeEvent);
            document.getDocumentHeader().getWorkflowDocument().saveRoutingData();
            if ( LOG.isDebugEnabled() ) {
            	LOG.debug(new StringBuffer("finished handling route level change from ").append(levelChangeEvent.getOldNodeName()).append(" to ").append(levelChangeEvent.getNewNodeName()).append(" for document ").append(levelChangeEvent.getRouteHeaderId()));
            }
        }
        catch (Exception e) {
            logAndRethrow("route level", e);
        }
        return true;
    }

    /**
     * @see org.kuali.rice.kew.postprocessor.PostProcessorRemote#doDeleteRouteHeader(org.kuali.rice.kew.dto.DeleteEventDTO)
     */
    public boolean doDeleteRouteHeader(DeleteEventDTO event) throws RemoteException {
        return true;
    }

    /**
     * @see org.kuali.rice.kew.postprocessor.PostProcessorRemote#doActionTaken(org.kuali.rice.kew.dto.ActionTakenEventDTO)
     */
    public boolean doActionTaken(ActionTakenEventDTO event) throws RemoteException {
        try {
        	if ( LOG.isDebugEnabled() ) {
        		LOG.debug(new StringBuffer("started doing action taken for action taken code").append(event.getActionTaken().getActionTaken()).append(" for document ").append(event.getRouteHeaderId()));
        	}
            establishGlobalVariables();
            Document document = documentService.getByDocumentHeaderId(event.getRouteHeaderId().toString());
            if (ObjectUtils.isNull(document)) {
                // only throw an exception if we are not cancelling
                if (!KEWConstants.ACTION_TAKEN_CANCELED.equals(event.getActionTaken())) {
                    LOG.warn("doActionTaken() Unable to load document with id " + event.getRouteHeaderId() + 
                            " using action taken code '" + KEWConstants.ACTION_TAKEN_CD.get(event.getActionTaken().getActionTaken()));
//                    throw new RuntimeException("unable to load document " + event.getRouteHeaderId());
                }
            } else {
                document.doActionTaken(event);
                if ( LOG.isDebugEnabled() ) {
                	LOG.debug(new StringBuffer("finished doing action taken for action taken code").append(event.getActionTaken().getActionTaken()).append(" for document ").append(event.getRouteHeaderId()));
                }
            }
        }
        catch (Exception e) {
            logAndRethrow("do action taken", e);
        }
        return true;
    }

    /**
     * This method first checks to see if the document can be retrieved by the {@link DocumentService}. If the document is
     * found the {@link Document#afterWorkflowEngineProcess(boolean)} method will be invoked on it
     * 
     * @see org.kuali.rice.kew.postprocessor.PostProcessorRemote#afterProcess(org.kuali.rice.kew.dto.AfterProcessEventDTO)
     */
    public boolean afterProcess(AfterProcessEventDTO event) throws Exception {
        try {
        	if ( LOG.isDebugEnabled() ) {
        		LOG.debug(new StringBuffer("started after process method for document ").append(event.getRouteHeaderId()));
        	}
            establishGlobalVariables();
            Document document = documentService.getByDocumentHeaderId(event.getRouteHeaderId().toString());
            if (ObjectUtils.isNull(document)) {
                // no way to verify if this is the processing as a result of a cancel so assume null document is ok to process
                LOG.warn("afterProcess() Unable to load document with id " + event.getRouteHeaderId() + "... ignoring post processing");
            } else {
                document.afterWorkflowEngineProcess(event.isSuccessfullyProcessed());
                if ( LOG.isDebugEnabled() ) {
                	LOG.debug(new StringBuffer("finished after process method for document ").append(event.getRouteHeaderId()));
                }
            }
        }
        catch (Exception e) {
            logAndRethrow("after process", e);
        }
        return true;
    }

    /**
     * This method first checks to see if the document can be retrieved by the {@link DocumentService}. If the document is
     * found the {@link Document#beforeWorkflowEngineProcess()} method will be invoked on it
     * 
     * @see org.kuali.rice.kew.postprocessor.PostProcessorRemote#beforeProcess(org.kuali.rice.kew.dto.BeforeProcessEventDTO)
     */
    public boolean beforeProcess(BeforeProcessEventDTO event) throws Exception {
        try {
        	if ( LOG.isDebugEnabled() ) {
        		LOG.debug(new StringBuffer("started before process method for document ").append(event.getRouteHeaderId()));
        	}
            establishGlobalVariables();
            Document document = documentService.getByDocumentHeaderId(event.getRouteHeaderId().toString());
            if (ObjectUtils.isNull(document)) {
                // no way to verify if this is the processing as a result of a cancel so assume null document is ok to process
                LOG.warn("beforeProcess() Unable to load document with id " + event.getRouteHeaderId() + "... ignoring post processing");
            } else {
                document.beforeWorkflowEngineProcess();
                if ( LOG.isDebugEnabled() ) {
                	LOG.debug(new StringBuffer("finished before process method for document ").append(event.getRouteHeaderId()));
                }
            }
        }
        catch (Exception e) {
            logAndRethrow("before process", e);
        }
        return true;
    }

    private void logAndRethrow(String changeType, Exception e) throws RuntimeException {
        LOG.error("caught exception while handling " + changeType + " change", e);
        logOptimisticDetails(5, e);

        throw new RuntimeException("post processor caught exception while handling " + changeType + " change: " + e.getMessage(), e);
    }

    /**
     * Logs further details of OptimisticLockExceptions, using the given depth value to limit recursion Just In Case
     *
     * @param depth
     * @param t
     */
    private void logOptimisticDetails(int depth, Throwable t) {
        if ((depth > 0) && (t != null)) {
            if (t instanceof OptimisticLockException) {
                OptimisticLockException o = (OptimisticLockException) t;

                LOG.error("source of OptimisticLockException = " + o.getSourceObject().getClass().getName() + " ::= " + o.getSourceObject());
            }
            else {
                Throwable cause = t.getCause();
                if (cause != t) {
                    logOptimisticDetails(--depth, cause);
                }
            }
        }
    }

    /**
     * Sets the documentService attribute value.
     * @param documentService The documentService to set.
     */
    public final void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * @param dateTimeService The dateTimeService to set.
     */
    public final void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Establishes the UserSession if one does not already exist.
     */
    protected void establishGlobalVariables() throws WorkflowException {
        if (GlobalVariables.getUserSession() == null) {
            GlobalVariables.setUserSession(new UserSession(KNSConstants.SYSTEM_USER));
        }
        GlobalVariables.clear();
    }

}
