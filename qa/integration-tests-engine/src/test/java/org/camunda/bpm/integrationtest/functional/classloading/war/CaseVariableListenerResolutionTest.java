/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.functional.classloading.war;

import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleCaseVariableListener;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roman Smirnov
 *
 */
@RunWith(Arquillian.class)
public class CaseVariableListenerResolutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {
    return initWebArchiveDeployment()
            .addClass(ExampleCaseVariableListener.class)
            .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/CaseVariableListenerResolutionTest.cmmn");
  }

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);

    TestContainer.addContainerSpecificResources(webArchive);

    return webArchive;

  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveCaseExecutionListenerClass() {
    // assert that we cannot load the delegate here:
    try {
      Class.forName("org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleCaseVariableListener");
      Assert.fail("CNFE expected");
    }catch (ClassNotFoundException e) {
      // expected
    }

    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // the listener should execute successfully
    caseService
      .withCaseExecution(humanTaskId)
      .setVariable("variable", "initial")
      .execute();

    VariableInstanceQuery query = runtimeService
      .createVariableInstanceQuery()
      .variableName("variable")
      .caseInstanceIdIn(caseInstanceId);

    Assert.assertNotNull(query.singleResult());
    Assert.assertEquals("listener-notified-1", query.singleResult().getValue());

    caseService
      .withCaseExecution(humanTaskId)
      .setVariable("variable", "manual-start")
      .manualStart();

    Assert.assertNotNull(query.singleResult());
    Assert.assertEquals("listener-notified-2", query.singleResult().getValue());

  }

}
