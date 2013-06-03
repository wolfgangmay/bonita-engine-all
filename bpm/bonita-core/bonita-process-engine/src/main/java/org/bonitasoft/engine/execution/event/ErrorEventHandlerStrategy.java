/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.execution.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateThrowEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.TransactionContainedProcessInstanceInterruptor;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ErrorEventHandlerStrategy extends CoupleEventHandlerStrategy {

    private static final OperationsWithContext EMPTY = new OperationsWithContext(null, null);

    private final ProcessInstanceService processInstanceService;

    private final ContainerRegistry containerRegistry;

    private final LockService lockService;

    private final ProcessDefinitionService processDefinitionService;

    private final EventsHandler eventsHandler;

    private final TechnicalLoggerService logger;

    public ErrorEventHandlerStrategy(final BPMInstanceBuilders instanceBuilders, final EventInstanceService eventInstanceService,
            final ProcessInstanceService processInstanceService, final ContainerRegistry containerRegistry, final LockService lockService,
            final ProcessDefinitionService processDefinitionService, final EventsHandler eventsHandler, final TechnicalLoggerService logger) {
        super(instanceBuilders, eventInstanceService);
        this.processInstanceService = processInstanceService;
        this.containerRegistry = containerRegistry;
        this.lockService = lockService;
        this.processDefinitionService = processDefinitionService;
        this.eventsHandler = eventsHandler;
        this.logger = logger;
    }

    @Override
    public void handleThrowEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SThrowEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Error event is thrown, error code = "
                    + ((SErrorEventTriggerDefinition) sEventTriggerDefinition).getErrorCode() + " process instance = " + eventInstance.getRootContainerId());
        }
        final TransactionContainedProcessInstanceInterruptor processInstanceInterruptor = new TransactionContainedProcessInstanceInterruptor(
                getInstanceBuilders(), processInstanceService, getEventInstanceService(), containerRegistry, lockService, logger);
        updateInterruptorErrorEvent(eventInstance);
        processInstanceInterruptor.interruptChildrenOnly(eventInstance.getParentContainerId(), SStateCategory.ABORTING, -1, eventInstance.getId());
    }

    private void updateInterruptorErrorEvent(final SThrowEventInstance eventInstance) throws SProcessInstanceNotFoundException, SProcessInstanceReadException,
            SProcessInstanceModificationException {
        final SIntermediateThrowEventInstanceBuilder throwEventKeyProvider = getInstanceBuilders().getSIntermediateThrowEventInstanceBuilder();
        final SProcessInstanceUpdateBuilder updateBuilder = getInstanceBuilders().getProcessInstanceUpdateBuilder();
        final long parentProcessInstanceId = eventInstance.getLogicalGroup(throwEventKeyProvider.getParentProcessInstanceIndex());
        updateBuilder.updateInterruptingEventId(eventInstance.getId());
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        processInstanceService.updateProcess(processInstance, updateBuilder.done());

    }

    @Override
    public void handleThrowEvent(final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        // NOT supported. Must to be implemented with errors can be sent via the API
    }

    @Override
    public boolean handlePostThrowEvent(final SProcessDefinition processDefinition, final SEndEventDefinition eventDefinition,
            final SThrowEventInstance eventInstance, final SEventTriggerDefinition trigger, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        boolean hasActionToExecute = false;
        final SFlowNodeInstanceBuilder flowNodeKeyProvider = getInstanceBuilders().getSIntermediateThrowEventInstanceBuilder();
        final long parentProcessInstanceId = eventInstance.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex());
        final SErrorEventTriggerDefinition errorTrigger = (SErrorEventTriggerDefinition) trigger;
        final SWaitingErrorEvent waitingErrorEvent = getWaitingErrorEvent(processDefinition.getProcessContainer(), parentProcessInstanceId, errorTrigger,
                eventInstance, flowNodeInstance);
        if (waitingErrorEvent != null) {
            eventsHandler.triggerCatchEvent(waitingErrorEvent, eventInstance.getId());
            hasActionToExecute = true;
        } else {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No catch error event was defined to handle the error code '");
            stringBuilder.append(errorTrigger.getErrorCode());
            stringBuilder.append("' defined in the process [name: ");
            stringBuilder.append(processDefinition.getName());
            stringBuilder.append(", version: ");
            stringBuilder.append(processDefinition.getVersion());
            stringBuilder.append("]");
            if (eventDefinition != null) {
                stringBuilder.append(", throw event: ");
                stringBuilder.append(eventDefinition.getName());
            }
            stringBuilder.append(". This throw error event will act as a Terminate Event.");
            logger.log(this.getClass(), TechnicalLogSeverity.WARNING, stringBuilder.toString());
        }
        return hasActionToExecute;
    }

    private SWaitingErrorEvent getWaitingErrorEvent(final SFlowElementContainerDefinition container, final long parentProcessInstanceId,
            final SErrorEventTriggerDefinition errorTrigger, final SThrowEventInstance eventInstance, final SFlowNodeInstance flowNodeInstance)
            throws SBonitaException {
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        final String errorCode = errorTrigger.getErrorCode();
        final SFlowNodeInstanceBuilder flowNodeKeyProvider = getInstanceBuilders().getSIntermediateThrowEventInstanceBuilder();
        SWaitingErrorEvent waitingErrorEvent;

        // check on direct boundary
        waitingErrorEvent = getWaitingErrorEventFromBoundary(errorTrigger, flowNodeKeyProvider, processInstance, eventInstance, errorCode, flowNodeInstance);
        // check on event sub-process
        if (waitingErrorEvent == null) {
            waitingErrorEvent = getWaitingErrorEventSubProcess(container, parentProcessInstanceId, errorCode);
        }
        // check on call activities (recursive)
        if (waitingErrorEvent == null && processInstance.getCallerId() != -1 && SFlowNodeType.CALL_ACTIVITY.equals(processInstance.getCallerType())) {
            // check on call activities
            waitingErrorEvent = getWaitingErrorEventFromCallActivity(errorTrigger, flowNodeKeyProvider, processInstance, eventInstance, errorCode,
                    flowNodeInstance);
        }
        return waitingErrorEvent;

    }

    private SWaitingErrorEvent getWaitingErrorEventFromBoundary(final SErrorEventTriggerDefinition errorTrigger,
            final SFlowNodeInstanceBuilder flowNodeKeyProvider, final SProcessInstance processInstance, final SThrowEventInstance eventInstance,
            final String errorCode, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        // get the parent activity of the boundary
        final long logicalGroup = eventInstance.getLogicalGroup(flowNodeKeyProvider.getParentActivityInstanceIndex());
        if (logicalGroup <= 0) {
            // not in an activity = no boundary
            return null;
        }
        final long processDefinitionId = flowNodeInstance.getLogicalGroup(flowNodeKeyProvider.getProcessDefinitionIndex());
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SActivityDefinition flowNode = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                flowNodeInstance.getFlowNodeDefinitionId());
        final List<SBoundaryEventDefinition> boundaryEventDefinitions = flowNode.getBoundaryEventDefinitions();
        return getWaitingErrorEventFromBoundary(errorCode, flowNodeInstance, boundaryEventDefinitions);
    }

    private SWaitingErrorEvent getWaitingErrorEventFromCallActivity(final SErrorEventTriggerDefinition errorTrigger,
            final SFlowNodeInstanceBuilder flowNodeKeyProvider, final SProcessInstance processInstance, final SThrowEventInstance eventInstance,
            final String errorCode, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        final SCallActivityInstance callActivityInstance = (SCallActivityInstance) getEventInstanceService().getFlowNodeInstance(processInstance.getCallerId());
        final long processDefinitionId = callActivityInstance.getLogicalGroup(flowNodeKeyProvider.getProcessDefinitionIndex());
        final SProcessDefinition callActivityContainer = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SCallActivityDefinition callActivityDef = (SCallActivityDefinition) callActivityContainer.getProcessContainer().getFlowNode(
                callActivityInstance.getFlowNodeDefinitionId());
        final List<SBoundaryEventDefinition> boundaryEventDefinitions = callActivityDef.getBoundaryEventDefinitions();
        SWaitingErrorEvent waitingErrorEvent = getWaitingErrorEventFromBoundary(errorCode, callActivityInstance, boundaryEventDefinitions);
        if (waitingErrorEvent == null) {
            final long callActivityParentProcInstId = callActivityInstance.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex());
            waitingErrorEvent = getWaitingErrorEvent(callActivityContainer.getProcessContainer(), callActivityParentProcInstId, errorTrigger, eventInstance,
                    flowNodeInstance);
        }
        return waitingErrorEvent;
    }

    private SWaitingErrorEvent getWaitingErrorEventFromBoundary(final String errorCode, final SFlowNodeInstance flowNodeInstance,
            final List<SBoundaryEventDefinition> boundaryEventDefinitions) throws SWaitingEventReadException {
        boolean canHandleError;
        String catchingErrorCode = errorCode;
        canHandleError = containsHandler(boundaryEventDefinitions, catchingErrorCode);
        if (!canHandleError) {
            catchingErrorCode = null; // catch all errors
            canHandleError = containsHandler(boundaryEventDefinitions, catchingErrorCode); // check for a handler that is able to catch all error codes
        }
        if (canHandleError) {
            return getEventInstanceService().getBoundaryWaitingErrorEvent(flowNodeInstance.getId(), catchingErrorCode);
        } else {
            return null;
        }
    }

    private SWaitingErrorEvent getWaitingErrorEventSubProcess(final SFlowElementContainerDefinition container, final long parentProcessInstanceId,
            final String errorCode) throws SBonitaSearchException, SBPMEventHandlerException {
        String catchingErrorCode = errorCode;
        boolean canHandleError = hasEventSubProcessCatchingError(container, catchingErrorCode);
        if (!canHandleError) {
            catchingErrorCode = null;
            canHandleError = hasEventSubProcessCatchingError(container, catchingErrorCode);
        }
        SWaitingErrorEvent waitingErrorEvent = null;
        if (canHandleError) {
            final SWaitingErrorEventBuilder waitingErrorEventKeyProvider = getInstanceBuilders().getSWaitingErrorEventBuilder();
            final OrderByOption orderByOption = new OrderByOption(SWaitingEvent.class, waitingErrorEventKeyProvider.getFlowNodeNameKey(), OrderByType.ASC);

            final List<FilterOption> filters = new ArrayList<FilterOption>(3);
            filters.add(new FilterOption(SWaitingErrorEvent.class, waitingErrorEventKeyProvider.getErrorCodeKey(), catchingErrorCode));
            filters.add(new FilterOption(SWaitingErrorEvent.class, waitingErrorEventKeyProvider.getEventTypeKey(), SBPMEventType.EVENT_SUB_PROCESS.name()));
            filters.add(new FilterOption(SWaitingErrorEvent.class, waitingErrorEventKeyProvider.getParentProcessInstanceIdKey(), parentProcessInstanceId));
            final QueryOptions queryOptions = new QueryOptions(0, 2, Collections.singletonList(orderByOption), filters, null);
            final List<SWaitingErrorEvent> waitingEvents = getEventInstanceService().searchWaitingEvents(SWaitingErrorEvent.class, queryOptions);
            if (waitingEvents.size() != 1) {
                final StringBuilder stb = new StringBuilder();
                stb.append("One and only one error start event sub-process was expected for the process instance ");
                stb.append(parentProcessInstanceId);
                stb.append(" and error code ");
                stb.append(catchingErrorCode);
                stb.append(", but ");
                stb.append(waitingEvents.size());
                stb.append(" was found.");
                throw new SBPMEventHandlerException(stb.toString());
            }
            waitingErrorEvent = waitingEvents.get(0);
        }
        return waitingErrorEvent;
    }

    private boolean containsHandler(final List<SBoundaryEventDefinition> boundaryEventDefinitions, final String errorCode) {
        boolean found = false;
        final Iterator<SBoundaryEventDefinition> iterator = boundaryEventDefinitions.iterator();
        while (iterator.hasNext() && !found) {
            final SBoundaryEventDefinition boundaryEventDefinition = iterator.next();
            final SCatchErrorEventTriggerDefinition currentErrorTrigger = boundaryEventDefinition.getErrorEventTriggerDefinition(errorCode);
            if (currentErrorTrigger != null) {
                found = true;
            }

        }
        return found;
    }

    private boolean hasEventSubProcessCatchingError(final SFlowElementContainerDefinition container, final String errorCode) {
        boolean found = false;
        final Iterator<SActivityDefinition> iterator = container.getActivities().iterator();
        while (iterator.hasNext() && !found) {
            final SActivityDefinition activity = iterator.next();
            if (SFlowNodeType.SUB_PROCESS.equals(activity.getType()) && ((SSubProcessDefinition) activity).isTriggeredByEvent()) {
                final SSubProcessDefinition eventSubProcess = (SSubProcessDefinition) activity;
                final SStartEventDefinition startEventDefinition = eventSubProcess.getSubProcessContainer().getStartEvents().get(0);
                if (startEventDefinition.getErrorEventTriggerDefinition(errorCode) != null) {
                    found = true;
                }
            }

        }
        return found;
    }

    @Override
    public void handleCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SCatchEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final SWaitingErrorEventBuilder builder = getInstanceBuilders().getSWaitingErrorEventBuilder();
        final SErrorEventTriggerDefinition errorEventTriggerDefinition = (SErrorEventTriggerDefinition) sEventTriggerDefinition;
        final SEventInstanceBuilder eventInstanceKeyProvider = getInstanceBuilders().getSIntermediateCatchEventInstanceBuilder();
        switch (eventDefinition.getType()) {
            case BOUNDARY_EVENT:
                final SBoundaryEventInstance boundary = (SBoundaryEventInstance) eventInstance;
                final long rootProcessInstanceId = eventInstance.getLogicalGroup(eventInstanceKeyProvider.getRootProcessInstanceIndex());
                final long parentProcessInstanceId = eventInstance.getLogicalGroup(eventInstanceKeyProvider.getParentProcessInstanceIndex());
                builder.createNewWaitingErrorBoundaryEventInstance(processDefinition.getId(), rootProcessInstanceId, parentProcessInstanceId,
                        eventInstance.getId(), errorEventTriggerDefinition.getErrorCode(), processDefinition.getName(),
                        eventInstance.getFlowNodeDefinitionId(), eventInstance.getName(), boundary.getActivityInstanceId());
                final SWaitingErrorEvent errorEvent = builder.done();
                getEventInstanceService().createWaitingEvent(errorEvent);

                break;
            case INTERMEDIATE_CATCH_EVENT:
            case START_EVENT:
                throw new SWaitingEventCreationException("Catch error event cannot be put in " + eventDefinition.getType()
                        + ". They must be used as boundary events or start event subprocess.");
            default:
                throw new SWaitingEventCreationException(eventDefinition.getType() + " is not a catch event.");
        }

    }

    @Override
    public OperationsWithContext getOperations(final SWaitingEvent waitingEvent, final Long triggeringElementID) throws SBonitaException {
        return EMPTY;
    }

    @Override
    public void handleEventSubProcess(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId, final SProcessInstance parentProcessInstance)
            throws SBonitaException {
        final SWaitingErrorEventBuilder builder = getInstanceBuilders().getSWaitingErrorEventBuilder();
        final SErrorEventTriggerDefinition trigger = (SErrorEventTriggerDefinition) sEventTriggerDefinition;
        builder.createNewWaitingErrorEventSubProcInstance(processDefinition.getId(), parentProcessInstance.getId(),
                parentProcessInstance.getRootProcessInstanceId(), trigger.getErrorCode(), processDefinition.getName(), eventDefinition.getId(),
                eventDefinition.getName(), subProcessId);

        final SWaitingErrorEvent event = builder.done();
        getEventInstanceService().createWaitingEvent(event);

    }
}
