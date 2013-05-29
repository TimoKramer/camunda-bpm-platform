package org.camunda.bpm.cockpit.plugin.base.resources;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessInstanceDto;

public class ProcessInstanceQueryParameter extends QueryParameters<ProcessInstanceDto> {
  
  private String processDefinitionId;
  
  public ProcessInstanceQueryParameter(int firstResult, int offset) {
    this.firstResult = firstResult;
    this.maxResults = offset;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

}
