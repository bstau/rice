/**
 * Copyright 2005-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sampleu.admin;

import org.kuali.rice.testtools.common.JiraAwareFailable;
import org.kuali.rice.testtools.selenium.AutomatedFunctionalTestUtils;
import org.kuali.rice.testtools.selenium.WebDriverUtils;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class ConfigParameterTypeLookUpAndCopyAftBase extends AdminTmplMthdAftNavBase {

    /**
     * ITUtil.PORTAL+"?channelTitle=Parameter%20Type&channelUrl="+WebDriverUtils.getBaseUrlString()+
     * "/kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.coreservice.impl.parameter.ParameterTypeBo&docFormKey=88888888&returnLocation="
     * +ITUtil.PORTAL_URL+ ITUtil.HIDE_RETURN_LINK;
     */
    public static final String BOOKMARK_URL = AutomatedFunctionalTestUtils.PORTAL+"?channelTitle=Parameter%20Type&channelUrl="+ WebDriverUtils
            .getBaseUrlString()+"/kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.coreservice.impl.parameter.ParameterTypeBo&docFormKey=88888888&returnLocation="+
            AutomatedFunctionalTestUtils.PORTAL_URL+ AutomatedFunctionalTestUtils.HIDE_RETURN_LINK;

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    /**
     * {@inheritDoc}
     * Parameter Type
     * @return
     */
    @Override
    protected String getLinkLocator() {
        return "Parameter Type";
    }

    public void testConfigParameterTypeLookUpAndCopyBookmark(JiraAwareFailable failable) throws Exception {
        testConfigParameterTypeLookUpAndCopy();
        passed();
    }

    public void testConfigParameterTypeLookUpAndCopyNav(JiraAwareFailable failable) throws Exception {
        testConfigParameterTypeLookUpAndCopy();
        passed();
    }    
    
    public void testConfigParameterTypeLookUpAndCopy() throws Exception
    {
        selectFrameIframePortlet();
        waitAndClickByXpath("(//input[@name='methodToCall.search'])[2]", "Probably KULRICE-10766 Parameter 500 Error Lookup not defined for business object class org.kuali.rice.coreservice.impl.parameter.ParameterBo");
        waitAndClickByLinkText("copy");
        waitAndTypeByName("document.documentHeader.documentDescription","Test description of parameter type copy");
        waitAndTypeByName("document.newMaintainableObject.code","AUTH2");
        waitAndTypeByName("document.newMaintainableObject.name","AuthorizationCopy");
        waitAndClickByName("methodToCall.route");
        waitAndClickByName("methodToCall.close");
        waitAndClickByName("methodToCall.processAnswer.button1");        
    }
}
