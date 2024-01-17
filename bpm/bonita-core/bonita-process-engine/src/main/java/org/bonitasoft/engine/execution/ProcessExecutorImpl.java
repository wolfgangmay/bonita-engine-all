/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.execution;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.validation.ContractValidator;
import org.bonitasoft.engine.bpm.contract.validation.ContractValidatorFactory;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.contract.data.SContractDataCreationException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.api.impl.DocumentHelper;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.*;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.*;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.*;
import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.execution.archive.BPMArchiverService;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Hongwen Zang
 * @author Celine Souchet
 */
@Slf4j
public class ProcessExecutorImpl implements ProcessExecutor {

    protected final ActivityInstanceService activityInstanceService;
    protected final ProcessInstanceService processInstanceService;
    protected final ClassLoaderService classLoaderService;
    protected final ExpressionResolverService expressionResolverService;
    protected final ExpressionService expressionService;
    protected final ConnectorService connectorService;
    protected final BPMInstancesCreator bpmInstancesCreator;
    protected final EventsHandler eventsHandler;
    private final FlowNodeExecutor flowNodeExecutor;
    private final WorkService workService;
    private final ProcessDefinitionService processDefinitionService;
    private final GatewayInstanceService gatewayInstanceService;
    private final OperationService operationService;
    private ProcessResourcesService processResourcesService;
    private final ConnectorInstanceService connectorInstanceService;
    private final TransitionEvaluator transitionEvaluator;
    private final ContractDataService contractDataService;
    private final BusinessDataRepository businessDataRepository;
    private final RefBusinessDataService refBusinessDataService;
    private final DocumentHelper documentHelper;
    private final BPMWorkFactory workFactory;
    private final BPMArchiverService bpmArchiverService;

    public ProcessExecutorImpl(final ActivityInstanceService activityInstanceService,
            final ProcessInstanceService processInstanceService, final FlowNodeExecutor flowNodeExecutor,
            final WorkService workService,
            final ProcessDefinitionService processDefinitionService,
            final GatewayInstanceService gatewayInstanceService,
            final ProcessResourcesService processResourcesService, final ConnectorService connectorService,
            final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService,
            final OperationService operationService,
            final ExpressionResolverService expressionResolverService, final ExpressionService expressionService,
            final EventService eventService,
            final Map<String, SProcessInstanceHandler<SEvent>> handlers, final DocumentService documentService,
            final ContainerRegistry containerRegistry, final BPMInstancesCreator bpmInstancesCreator,
            final EventsHandler eventsHandler, final FlowNodeStateManager flowNodeStateManager,
            final BusinessDataRepository businessDataRepository,
            final RefBusinessDataService refBusinessDataService, final TransitionEvaluator transitionEvaluator,
            final ContractDataService contractDataService, BPMWorkFactory workFactory,
            BPMArchiverService bpmArchiverService) {
        super();
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
        this.processResourcesService = processResourcesService;
        this.connectorInstanceService = connectorInstanceService;
        this.flowNodeExecutor = flowNodeExecutor;
        this.workService = workService;
        this.processDefinitionService = processDefinitionService;
        this.gatewayInstanceService = gatewayInstanceService;
        this.connectorService = connectorService;
        this.classLoaderService = classLoaderService;
        this.operationService = operationService;
        this.expressionResolverService = expressionResolverService;
        this.expressionService = expressionService;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.eventsHandler = eventsHandler;
        this.transitionEvaluator = transitionEvaluator;
        this.businessDataRepository = businessDataRepository;
        this.refBusinessDataService = refBusinessDataService;
        this.contractDataService = contractDataService;
        this.workFactory = workFactory;
        this.bpmArchiverService = bpmArchiverService;
        documentHelper = new DocumentHelper(documentService, processDefinitionService, processInstanceService);
        //FIXME There is responsibility issue the circular dependencies must be fixed next time.
        eventsHandler.setProcessExecutor(this);
        for (final Entry<String, SProcessInstanceHandler<SEvent>> handler : handlers.entrySet()) {
            try {
                eventService.addHandler(handler.getKey(), handler.getValue());
            } catch (final HandlerRegistrationException e) {
                log.warn(e.getMessage());
                log.debug("", e);
            }
        }
        containerRegistry.addContainerExecutor(this);
    }

    @Override
    public FlowNodeState executeFlowNode(SFlowNodeInstance flowNodeInstance, Long executerId, Long executerSubstituteId)
            throws SFlowNodeExecutionException {
        return flowNodeExecutor.stepForward(flowNodeInstance, executerId, executerSubstituteId);
    }

    private SConnectorInstance getNextConnectorInstance(final SProcessInstance processInstance,
            final ConnectorEvent event)
            throws SConnectorInstanceReadException {
        final List<SConnectorInstance> connectorInstances = connectorInstanceService.getConnectorInstances(
                processInstance.getId(),
                SConnectorInstance.PROCESS_TYPE, event, 0, 1, ConnectorService.TO_BE_EXECUTED);
        return connectorInstances.size() == 1 ? connectorInstances.get(0) : null;
    }

    @Override
    public boolean registerConnectorsToExecute(final SProcessDefinition processDefinition,
            final SProcessInstance sProcessInstance, final ConnectorEvent activationEvent,
            final FlowNodeSelector selectorForConnectorOnEnter) throws SBonitaException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final long processDefinitionId = processDefinition.getId();
        final List<SConnectorDefinition> connectors = processContainer.getConnectors(activationEvent);
        if (connectors.size() > 0) {
            SConnectorInstance nextConnectorInstance;
            nextConnectorInstance = getNextConnectorInstance(sProcessInstance, activationEvent);
            if (nextConnectorInstance != null) {
                // TODO: extract this search algorithm in a dedicated method:
                for (final SConnectorDefinition sConnectorDefinition : connectors) {
                    if (sConnectorDefinition.getName().equals(nextConnectorInstance.getName())) {
                        workService.registerWork(workFactory.createExecuteConnectorOfProcessDescriptor(
                                processDefinitionId, sProcessInstance.getId(),
                                sProcessInstance.getRootProcessInstanceId(), nextConnectorInstance.getId(),
                                sConnectorDefinition.getName(), activationEvent,
                                selectorForConnectorOnEnter));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<SFlowNodeInstance> initializeFirstExecutableElements(final SProcessInstance sProcessInstance,
            final FlowNodeSelector selector) {
        try {
            final List<SFlowNodeDefinition> flownNodeDefinitions = selector.getFilteredElements();
            long rootProcessInstanceId = sProcessInstance.getRootProcessInstanceId();
            if (rootProcessInstanceId <= 0) {
                rootProcessInstanceId = sProcessInstance.getId();
            }
            return bpmInstancesCreator.createFlowNodeInstances(selector.getProcessDefinition().getId(),
                    rootProcessInstanceId, sProcessInstance.getId(),
                    flownNodeDefinitions, rootProcessInstanceId, sProcessInstance.getId(), SStateCategory.NORMAL);
        } catch (final SBonitaException e) {
            setExceptionContext(selector.getProcessDefinition(), sProcessInstance, e);
            log.error("", e);
            throw new BonitaRuntimeException(e);
        }
    }

    private SProcessInstance createProcessInstance(final SProcessDefinition sDefinition, final long starterId,
            final long starterSubstituteId,
            final long callerId, final SFlowNodeType callerType, final long rootProcessInstanceId)
            throws SProcessInstanceCreationException {

        SProcessInstance sProcessInstance = SProcessInstance.builder().name(sDefinition.getName())
                .processDefinitionId(sDefinition.getId()).description(sDefinition.getDescription())
                .startedBy(starterId).startedBySubstitute(starterSubstituteId).callerId(callerId).callerType(callerType)
                .rootProcessInstanceId(rootProcessInstanceId).build();
        processInstanceService.createProcessInstance(sProcessInstance);
        return sProcessInstance;
    }

    protected SProcessInstance createProcessInstance(final SProcessDefinition processDefinition, final long starterId,
            final long starterSubstituteId,
            final long callerId) throws SProcessInstanceCreationException {
        SActivityInstance callerInstance;
        try {
            callerInstance = getCaller(callerId);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to get caller.", e);
        }

        if (callerInstance != null) {
            return createProcessInstance(processDefinition, starterId, starterSubstituteId, callerId,
                    callerInstance.getType(),
                    callerInstance.getRootContainerId());
        }
        return createProcessInstance(processDefinition, starterId, starterSubstituteId, callerId, null, -1);
    }

    private SActivityInstance getCaller(final long callerId)
            throws SActivityReadException, SActivityInstanceNotFoundException {
        if (callerId > 0) {
            return activityInstanceService.getActivityInstance(callerId);
        }
        return null;
    }

    /*
     * this method is called when a flow node having a transition that goes to a gateway is finished
     * it get the active gateway pointed by this transition, update the tokens of this gateway and execute it if merged
     */
    private void executeGateway(final SProcessDefinition sProcessDefinition,
            final STransitionDefinition sTransitionDefinition,
            final SFlowNodeInstance flowNodeThatTriggeredTheTransition) throws SBonitaException {
        final long parentProcessInstanceId = flowNodeThatTriggeredTheTransition.getParentProcessInstanceId();
        final long rootProcessInstanceId = flowNodeThatTriggeredTheTransition.getRootProcessInstanceId();
        final SFlowNodeDefinition sFlowNodeDefinition = processDefinitionService.getNextFlowNode(sProcessDefinition,
                String.valueOf(sTransitionDefinition.getId()));
        try {
            List<SGatewayInstance> gatewaysToExecute = new ArrayList<>(1);
            final SProcessInstance parentProcessInstance = processInstanceService
                    .getProcessInstance(parentProcessInstanceId);
            final SStateCategory stateCategory = parentProcessInstance.getStateCategory();
            final SGatewayInstance gatewayInstance = getActiveGatewayOrCreateIt(sProcessDefinition, sFlowNodeDefinition,
                    stateCategory,
                    parentProcessInstanceId,
                    rootProcessInstanceId);
            gatewayInstanceService.hitTransition(gatewayInstance,
                    sFlowNodeDefinition.getTransitionIndex(sTransitionDefinition.getId()));
            if (gatewayInstanceService.checkMergingCondition(sProcessDefinition, gatewayInstance)) {
                gatewaysToExecute.add(gatewayInstance);
                gatewaysToExecute.addAll(gatewayInstanceService
                        .setFinishAndCreateNewGatewayForRemainingToken(sProcessDefinition, gatewayInstance));
            }
            for (final SGatewayInstance gatewayToExecute : gatewaysToExecute) {
                registerExecuteFlowNodeWork(gatewayToExecute);
            }
        } catch (final SBonitaException e) {
            setExceptionContext(sProcessDefinition, flowNodeThatTriggeredTheTransition, e);
            log.error("", e);
            throw e;
        }
    }

    /*
     * try to gate active gateway.
     * if the gateway is already hit by this transition or by the same token, we create a new gateway
     */
    SGatewayInstance getActiveGatewayOrCreateIt(final SProcessDefinition sProcessDefinition,
            final SFlowNodeDefinition flowNodeDefinition,
            final SStateCategory stateCategory, final long parentProcessInstanceId, final long rootProcessInstanceId)
            throws SBonitaException {
        SGatewayInstance gatewayInstance = gatewayInstanceService
                .getActiveGatewayInstanceOfTheProcess(parentProcessInstanceId, flowNodeDefinition.getName());
        if (gatewayInstance == null) {
            // no gateway found we create one
            gatewayInstance = createGateway(sProcessDefinition.getId(), flowNodeDefinition, stateCategory,
                    parentProcessInstanceId, rootProcessInstanceId);
        }
        return gatewayInstance;
    }

    private SGatewayInstance createGateway(final Long processDefinitionId, final SFlowNodeDefinition flowNodeDefinition,
            final SStateCategory stateCategory,
            final long parentProcessInstanceId, final long rootProcessInstanceId) throws SBonitaException {
        return (SGatewayInstance) bpmInstancesCreator
                .createFlowNodeInstance(processDefinitionId, rootProcessInstanceId, parentProcessInstanceId,
                        SFlowElementsContainerType.PROCESS,
                        flowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, false, 0, stateCategory,
                        -1);
    }

    protected void executeOperations(final List<SOperation> operations, final Map<String, Object> context,
            SExpressionContext expressionContext,
            final SExpressionContext expressionContextToEvaluateOperations,
            final SProcessInstance sProcessInstance) throws SBonitaException {
        if (operations != null && !operations.isEmpty()) {
            SExpressionContext currentExpressionContext = expressionContextToEvaluateOperations != null
                    ? expressionContextToEvaluateOperations
                    : expressionContext;
            currentExpressionContext.setInputValues(context);
            if (currentExpressionContext.getContainerId() == null) {
                currentExpressionContext.setContainerId(sProcessInstance.getId());
                currentExpressionContext.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
            }
            operationService.execute(new ArrayList<>(operations), sProcessInstance.getId(),
                    DataInstanceContainer.PROCESS_INSTANCE.name(),
                    currentExpressionContext);
        }
    }

    protected boolean initialize(final long userId, final SProcessDefinition sProcessDefinition,
            final SProcessInstance sProcessInstance,
            SExpressionContext expressionContextToEvaluateOperations, List<SOperation> operations,
            final Map<String, Object> context,
            final SFlowElementContainerDefinition processContainer,
            final List<ConnectorDefinitionWithInputValues> connectors,
            final FlowNodeSelector selectorForConnectorOnEnter, final Map<String, Serializable> processInputs)
            throws SBonitaException {

        SExpressionContext expressionContext = createExpressionsContextForProcessInstance(sProcessDefinition,
                sProcessInstance);
        operations = operations != null ? new ArrayList<>(operations) : Collections.emptyList();

        storeProcessInstantiationInputs(sProcessInstance.getId(), processInputs);

        // Create SDataInstances
        bpmInstancesCreator.createDataInstances(sProcessInstance, processContainer, sProcessDefinition,
                expressionContext, operations, context,
                expressionContextToEvaluateOperations);

        initializeBusinessData(processContainer, sProcessInstance, expressionContext);
        initializeStringIndexes(sProcessInstance, sProcessDefinition, processContainer);

        createDocuments(sProcessDefinition, processContainer, sProcessInstance, userId, expressionContext, context);
        createDocumentLists(processContainer, sProcessInstance, userId, expressionContext, context);
        if (connectors != null) {
            //these are set only when start process through the command ExecuteActionsAndStartInstanceExt
            executeConnectors(sProcessDefinition, sProcessInstance, connectors);
        }
        // operations given to the startProcess method of the API or by command, not operations of the process definition
        executeOperations(operations, context, expressionContext, expressionContextToEvaluateOperations,
                sProcessInstance);

        // Create connectors
        bpmInstancesCreator.createConnectorInstances(sProcessInstance, processContainer.getConnectors(),
                SConnectorInstance.PROCESS_TYPE);

        return registerConnectorsToExecute(sProcessDefinition, sProcessInstance, ConnectorEvent.ON_ENTER,
                selectorForConnectorOnEnter);
    }

    private SExpressionContext createExpressionsContextForProcessInstance(SProcessDefinition sProcessDefinition,
            SProcessInstance sProcessInstance) {
        SExpressionContext expressionContext = new SExpressionContext();
        expressionContext.setProcessDefinitionId(sProcessDefinition.getId());
        expressionContext.setContainerId(sProcessInstance.getId());
        expressionContext.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
        return expressionContext;
    }

    private void storeProcessInstantiationInputs(final long processInstanceId,
            final Map<String, Serializable> processInputs)
            throws SContractDataCreationException {
        contractDataService.addProcessData(processInstanceId, processInputs);
    }

    protected void initializeBusinessData(SFlowElementContainerDefinition processContainer, SProcessInstance sInstance,
            SExpressionContext expressionContext)
            throws SBonitaException {
        final List<SBusinessDataDefinition> businessDataDefinitions = processContainer.getBusinessDataDefinitions();
        for (final SBusinessDataDefinition bdd : businessDataDefinitions) {
            final SExpression expression = bdd.getDefaultValueExpression();
            if (bdd.isMultiple()) {
                final List<Long> dataIds = initializeMultipleBusinessDataIds(expressionContext, expression);
                final SRefBusinessDataInstanceBuilderFactory instanceFactory = BuilderFactory
                        .get(SRefBusinessDataInstanceBuilderFactory.class);
                final SRefBusinessDataInstance instance = instanceFactory
                        .createNewInstance(bdd.getName(), sInstance.getId(), dataIds, bdd.getClassName())
                        .done();
                refBusinessDataService.addRefBusinessDataInstance(instance);
            } else {
                final Long primaryKey = initializeSingleBusinessData(expressionContext, expression);
                final SRefBusinessDataInstanceBuilderFactory instanceFactory = BuilderFactory
                        .get(SRefBusinessDataInstanceBuilderFactory.class);
                final SRefBusinessDataInstance instance = instanceFactory
                        .createNewInstance(bdd.getName(), sInstance.getId(), primaryKey,
                                bdd.getClassName())
                        .done();
                refBusinessDataService.addRefBusinessDataInstance(instance);
            }
        }
    }

    private Long initializeSingleBusinessData(final SExpressionContext expressionContext, final SExpression expression)
            throws SBonitaException {
        Long primaryKey = null;
        if (expression != null) {
            final Entity businessData = (Entity) expressionResolverService.evaluate(expression, expressionContext);
            primaryKey = saveBusinessData(businessData);
        }
        return primaryKey;
    }

    private List<Long> initializeMultipleBusinessDataIds(final SExpressionContext expressionContext,
            final SExpression expression) throws SBonitaException {
        final List<Long> dataIds = new ArrayList<>();
        if (expression != null) {
            final List<Entity> businessData = (List<Entity>) expressionResolverService.evaluate(expression,
                    expressionContext);
            if (businessData != null) {
                for (final Entity entity : businessData) {
                    dataIds.add(saveBusinessData(entity));
                }
            }
        }
        return dataIds;
    }

    private Long saveBusinessData(final Entity entity) throws SObjectCreationException {
        try {
            final Entity mergedBusinessData = businessDataRepository.merge(ServerProxyfier.unProxifyIfNeeded(entity));
            if (mergedBusinessData == null) {
                return null;
            }
            return mergedBusinessData.getPersistenceId();
        } catch (IllegalArgumentException e) {
            throw new SObjectCreationException("Unable to save the business data", e);
        }
    }

    private void createDocuments(final SProcessDefinition sDefinition, SFlowElementContainerDefinition processContainer,
            final SProcessInstance sProcessInstance, final long authorId,
            final SExpressionContext expressionContext, final Map<String, Object> context)
            throws SObjectCreationException, SBonitaReadException, SObjectModificationException,
            SExpressionTypeUnknownException, SExpressionDependencyMissingException, SExpressionEvaluationException,
            SInvalidExpressionException, SOperationExecutionException {
        final List<SDocumentDefinition> documentDefinitions = processContainer.getDocumentDefinitions();
        final Map<SExpression, DocumentValue> evaluatedDocumentValues = evaluateInitialExpressionsOfDocument(
                sProcessInstance, expressionContext, context,
                documentDefinitions);
        if (!documentDefinitions.isEmpty()) {
            for (final SDocumentDefinition document : documentDefinitions) {
                final DocumentValue documentValue = getInitialDocumentValue(sDefinition, evaluatedDocumentValues,
                        document);
                if (documentValue != null) {
                    documentHelper.createOrUpdateDocument(documentValue,
                            document.getName(), sProcessInstance.getId(), authorId, document.getDescription());
                }
            }
        }
    }

    protected DocumentValue getInitialDocumentValue(final SProcessDefinition sDefinition,
            final Map<SExpression, DocumentValue> evaluatedDocumentValues,
            final SDocumentDefinition document) throws SBonitaReadException {
        DocumentValue documentValue = null;
        if (document.getInitialValue() != null) {
            documentValue = evaluatedDocumentValues.get(document.getInitialValue());
        } else if (document.getFile() != null) {
            final byte[] content = getProcessDocumentContent(sDefinition, document);
            documentValue = new DocumentValue(content, document.getMimeType(), document.getFileName());
        } else if (document.getUrl() != null) {
            documentValue = new DocumentValue(document.getUrl());
            documentValue.setFileName(document.getFileName());
            documentValue.setMimeType(document.getMimeType());
        }
        return documentValue;
    }

    byte[] getProcessDocumentContent(final SProcessDefinition sDefinition, final SDocumentDefinition document)
            throws SBonitaReadException {
        final String file = document.getFile();// should always exists...validation on BusinessArchive
        return processResourcesService.get(sDefinition.getId(), BARResourceType.DOCUMENT, file).getContent();
    }

    private Map<SExpression, DocumentValue> evaluateInitialExpressionsOfDocument(final SProcessInstance processInstance,
            final SExpressionContext expressionContext,
            final Map<String, Object> context, final List<SDocumentDefinition> documentDefinitions)
            throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException,
            SOperationExecutionException {
        final List<SExpression> initialValuesExpressions = new ArrayList<>(documentDefinitions.size());
        final Map<SExpression, DocumentValue> evaluatedDocumentValue = new HashMap<>();
        for (final SDocumentDefinition documentDefinition : documentDefinitions) {
            if (documentDefinition.getInitialValue() != null) {
                initialValuesExpressions.add(documentDefinition.getInitialValue());
            }
        }
        final List<Object> evaluate = expressionResolverService.evaluate(initialValuesExpressions,
                getsExpressionContext(processInstance, expressionContext, context));
        for (int i = 0; i < initialValuesExpressions.size(); i++) {
            evaluatedDocumentValue.put(initialValuesExpressions.get(i),
                    documentHelper.toCheckedDocumentValue(evaluate.get(i)));
        }
        return evaluatedDocumentValue;
    }

    private void createDocumentLists(SFlowElementContainerDefinition processContainer,
            final SProcessInstance processInstance, final long authorId,
            final SExpressionContext expressionContext, final Map<String, Object> context)
            throws SBonitaException {
        final List<SDocumentListDefinition> documentListDefinitions = processContainer.getDocumentListDefinitions();
        if (!documentListDefinitions.isEmpty()) {
            final List<Object> initialValues = evaluateInitialExpressionsOfDocumentLists(processInstance,
                    expressionContext, context, documentListDefinitions);
            for (int i = 0; i < documentListDefinitions.size(); i++) {
                final Object newValue = initialValues.get(i);
                if (newValue == null) {
                    continue;
                }
                documentHelper.setDocumentList(
                        documentHelper.toCheckedList(newValue), documentListDefinitions.get(i).getName(),
                        processInstance.getId(), authorId);
            }
        }
    }

    private List<Object> evaluateInitialExpressionsOfDocumentLists(final SProcessInstance processInstance,
            final SExpressionContext expressionContext,
            final Map<String, Object> context, final List<SDocumentListDefinition> documentListDefinitions)
            throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        final List<SExpression> initialValuesExpressions = new ArrayList<>(documentListDefinitions.size());
        for (final SDocumentListDefinition documentList : documentListDefinitions) {
            initialValuesExpressions.add(documentList.getExpression());
        }
        final SExpressionContext currentExpressionContext = getsExpressionContext(processInstance, expressionContext,
                context);
        return expressionResolverService.evaluate(initialValuesExpressions, currentExpressionContext);
    }

    private SExpressionContext getsExpressionContext(final SProcessInstance processInstance,
            final SExpressionContext expressionContext,
            final Map<String, Object> context) {
        SExpressionContext currentExpressionContext;
        if (expressionContext != null) {
            expressionContext.setInputValues(context);
            currentExpressionContext = expressionContext;
        } else {
            currentExpressionContext = new SExpressionContext(processInstance.getId(),
                    DataInstanceContainer.PROCESS_INSTANCE.name(),
                    processInstance.getProcessDefinitionId());
            currentExpressionContext.setInputValues(context);
        }
        return currentExpressionContext;
    }

    @Override
    public void childFinished(long processDefinitionId, long parentId, SFlowNodeInstance childFlowNode)
            throws SBonitaException {
        final SProcessDefinition sProcessDefinition = processDefinitionService
                .getProcessDefinition(processDefinitionId);
        final long processInstanceId = childFlowNode.getParentProcessInstanceId();

        SProcessInstance sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);

        // this also deletes the event (unless the process was interrupted by event)
        final boolean wasTheLastFlowNodeToExecute = executeValidOutgoingTransitionsAndUpdateTokens(sProcessDefinition,
                childFlowNode,
                sProcessInstance);
        log.debug("The flow node <{}> with id<{}> of process instance <{}> finished",
                childFlowNode.getName(), childFlowNode.getId(), processInstanceId);
        if (wasTheLastFlowNodeToExecute) {
            int numberOfFlowNode = activityInstanceService.getNumberOfFlowNodes(sProcessInstance.getId());
            if (sProcessInstance.getInterruptingEventId() > 0) {
                //if it's interrupted by an event (error event), the flow node is kept to be executed last and deleted in triggerErrorEvents()
                numberOfFlowNode -= 1;
            }
            if (numberOfFlowNode > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("The process instance <{}> from definition <{}:{}> executed a branch " +
                            "that is finished but there is still <{}> to execute",
                            processInstanceId, sProcessDefinition.getName(), sProcessDefinition.getVersion(),
                            numberOfFlowNode);
                    log.debug(activityInstanceService.getDirectChildrenOfProcessInstance(processInstanceId,
                            0, numberOfFlowNode).toString());
                }
                return;
            }
            log.debug("The process instance <{}> from definition <{}:{}> finished",
                    processInstanceId, sProcessDefinition.getName(), sProcessDefinition.getVersion());
            boolean hasTriggeredErrorEvents = false;
            // in case of interruption by error event:
            // * the first time the last element (except the error event itself)  goes here, it put the process in aborting
            // * the error event in thrown
            //     * the catching flow node is executed IN THE SAME THREAD (I don't know why...)
            //     * OR the catching event sub process is executed IN THE SAME THREAD and the process having this event sub process is interrupted (not locked!)
            // * the throw error event is deleted
            // * the waiting error event is deleted
            // * the process is put in:
            //     * ABORTING: if the process was in state category ABORTING (I don't know why...)
            //         I'm not sure this case really happens because this would require that a last flow node trigger this method again and it's never the case
            //     * COMPLETED: in 'normal' case
            //         In that case if the process is called by a call activity, the calling activity have its token count decremented but is not executed (hasTriggeredErrorEvents==true)
            //
            if (ProcessInstanceState.ABORTING.getId() != sProcessInstance.getStateId()) {
                if (sProcessInstance.getStateCategory() != SStateCategory.CANCELLING
                        && sProcessInstance.hasBeenInterruptedByEvent()) {
                    // trigger error events only if process instance has been aborted by an event
                    // and no-one cancelled the process instance in the meantime:
                    hasTriggeredErrorEvents = triggerErrorEvents(sProcessDefinition, sProcessInstance,
                            childFlowNode);
                }
                // the process instance has maybe changed
                log.debug("has action to execute");
                if (hasTriggeredErrorEvents) {
                    sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
                }
                eventsHandler.unregisterEventSubProcess(sProcessDefinition, sProcessInstance);
            }
            handleProcessCompletion(sProcessDefinition, sProcessInstance, hasTriggeredErrorEvents);
        }
    }

    @Override
    public void handleProcessCompletion(final SProcessDefinition sProcessDefinition,
            final SProcessInstance sProcessInstance, final boolean hasActionsToExecute)
            throws SBonitaException {
        ProcessInstanceState processInstanceState;
        switch (sProcessInstance.getStateCategory()) {
            case ABORTING:
                if (ProcessInstanceState.ABORTING.getId() == sProcessInstance.getStateId()) {
                    processInstanceState = ProcessInstanceState.ABORTED;
                } else {
                    if (hasActionsToExecute) {
                        processInstanceState = ProcessInstanceState.ABORTING;
                    } else {
                        processInstanceState = ProcessInstanceState.ABORTED;
                    }
                }
                break;
            case CANCELLING:
                processInstanceState = ProcessInstanceState.CANCELLED;
                break;
            default:
                if (ProcessInstanceState.COMPLETING.getId() == sProcessInstance.getStateId()) {
                    processInstanceState = ProcessInstanceState.COMPLETED;
                } else {
                    if (registerConnectorsToExecute(sProcessDefinition, sProcessInstance, ConnectorEvent.ON_FINISH,
                            null)) {
                        // some connectors were trigger
                        processInstanceState = ProcessInstanceState.COMPLETING;
                    } else {
                        processInstanceState = ProcessInstanceState.COMPLETED;
                    }
                }
                break;
        }
        processInstanceService.setState(sProcessInstance, processInstanceState);
        flowNodeExecutor.childReachedState(sProcessInstance, processInstanceState, hasActionsToExecute);

    }

    private boolean triggerErrorEvents(final SProcessDefinition sProcessDefinition,
            final SProcessInstance sProcessInstance,
            final SFlowNodeInstance child) throws SBonitaException {
        final SFlowNodeInstance endEventInstance = activityInstanceService
                .getFlowNodeInstance(sProcessInstance.getInterruptingEventId());
        final SEndEventDefinition endEventDefinition = (SEndEventDefinition) sProcessDefinition
                .getProcessContainer().getFlowNode(
                        endEventInstance.getFlowNodeDefinitionId());
        boolean hasTriggeredErrorEvents = eventsHandler.handlePostThrowEvent(sProcessDefinition, endEventDefinition,
                (SThrowEventInstance) endEventInstance, child);
        bpmArchiverService.archiveAndDeleteFlowNodeInstance(endEventInstance, sProcessDefinition.getId());
        return hasTriggeredErrorEvents;
    }

    /**
     * Evaluate the split of the element
     * The element contains the current token it received
     *
     * @return number of token of the process
     */
    private boolean executeValidOutgoingTransitionsAndUpdateTokens(final SProcessDefinition processDefinition,
            final SFlowNodeInstance child, final SProcessInstance sProcessInstance) throws SBonitaException {
        // token we merged
        final SFlowNodeDefinition sFlowNodeDefinition = processDefinition.getProcessContainer()
                .getFlowNode(child.getFlowNodeDefinitionId());
        final FlowNodeTransitionsWrapper transitionsDescriptor = transitionEvaluator
                .buildTransitionsWrapper(sFlowNodeDefinition, processDefinition, child);

        List<STransitionDefinition> chosenGatewaysTransitions = new ArrayList<>(transitionsDescriptor
                .getValidOutgoingTransitionDefinitions().size());
        final List<SFlowNodeDefinition> chosenFlowNode = new ArrayList<>(
                transitionsDescriptor.getValidOutgoingTransitionDefinitions().size());
        for (final STransitionDefinition sTransitionDefinition : transitionsDescriptor
                .getValidOutgoingTransitionDefinitions()) {
            final SFlowNodeDefinition flowNodeDefinition = processDefinitionService.getNextFlowNode(processDefinition,
                    String.valueOf(sTransitionDefinition.getId()));
            if (flowNodeDefinition instanceof SGatewayDefinition) {
                chosenGatewaysTransitions.add(sTransitionDefinition);
            } else {
                // Shortcut: event or activity, we execute them directly
                chosenFlowNode.add(flowNodeDefinition);
            }
        }

        archiveFlowNodeInstance(processDefinition, child, sProcessInstance);

        final long processInstanceId = sProcessInstance.getId();
        createAndExecuteActivities(processDefinition.getId(), child, processInstanceId, chosenFlowNode,
                child.getRootProcessInstanceId());

        //test to check the particular case of an inclusive gateway receiving transitions from the same flownode.
        //if that's the case only one should be executed.
        removeDuplicatedInclusiveGatewayTransitions(processDefinition, chosenGatewaysTransitions);

        // execute transition/activities
        for (final STransitionDefinition sTransitionDefinition : chosenGatewaysTransitions) {
            executeGateway(processDefinition, sTransitionDefinition, child);
        }

        if (processDefinition.getProcessContainer().containsInclusiveGateway()
                && needToReevaluateInclusiveGateways(transitionsDescriptor)) {
            reevaluateGateways(processDefinition, processInstanceId);
        }
        return transitionsDescriptor.isLastFlowNode();
    }

    private void reevaluateGateways(SProcessDefinition processDefinition, long processInstanceId)
            throws SBonitaException {
        log.debug("some branches died, will check again all inclusive gateways");
        final List<SGatewayInstance> inclusiveGatewaysOfProcessInstance = gatewayInstanceService
                .getInclusiveGatewaysOfProcessInstanceThatShouldFire(
                        processDefinition, processInstanceId);
        List<SGatewayInstance> gatewaysToExecute = new ArrayList<>(inclusiveGatewaysOfProcessInstance);
        for (final SGatewayInstance gatewayInstance : inclusiveGatewaysOfProcessInstance) {
            gatewaysToExecute
                    .addAll(gatewayInstanceService.setFinishAndCreateNewGatewayForRemainingToken(processDefinition,
                            gatewayInstance));
        }
        for (final SGatewayInstance gatewayToExecute : gatewaysToExecute) {
            registerExecuteFlowNodeWork(gatewayToExecute);
        }
    }

    protected void removeDuplicatedInclusiveGatewayTransitions(SProcessDefinition processDefinition,
            List<STransitionDefinition> chosenGatewaysTransitions) {
        List<STransitionDefinition> transitionToRemove = new ArrayList<>();
        Set<SGatewayDefinition> gateways = new HashSet<>();
        for (STransitionDefinition gatewaysTransition : chosenGatewaysTransitions) {
            SGatewayDefinition gateway = getGateway(gatewaysTransition, processDefinition);
            if (isInclusiveGateway(gateway)) {
                boolean alreadyExists = !gateways.add(gateway);
                if (alreadyExists) {
                    transitionToRemove.add(gatewaysTransition);
                }
            }
        }
        chosenGatewaysTransitions.removeAll(transitionToRemove);
    }

    private boolean isInclusiveGateway(SGatewayDefinition gateway) {
        return gateway.getGatewayType() == SGatewayType.INCLUSIVE;
    }

    private SGatewayDefinition getGateway(STransitionDefinition gatewaysTransition,
            SProcessDefinition processDefinition) {
        return (SGatewayDefinition) processDefinition.getProcessContainer().getFlowNode(gatewaysTransition.getTarget());
    }

    private void archiveFlowNodeInstance(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance child,
            final SProcessInstance sProcessInstance)
            throws SArchivingException {
        //FIXME we archive the flow node instance here because it was not archived before because the flow node was interrupting the parent.. we should change that because it's not very easy to see how it works
        // * the flow node is archived only if its not the error event that triggered the interruption (unless if its in a sub process????)
        if (child.getId() != sProcessInstance.getInterruptingEventId()
                || SFlowNodeType.SUB_PROCESS.equals(sProcessInstance.getCallerType())) {
            // Let's archive the final state of the child:
            bpmArchiverService.archiveAndDeleteFlowNodeInstance(child, sProcessDefinition.getId());
        }
    }

    private boolean needToReevaluateInclusiveGateways(final FlowNodeTransitionsWrapper transitionsDescriptor) {
        final int allOutgoingTransitions = transitionsDescriptor.getNonDefaultOutgoingTransitionDefinitions().size()
                + (transitionsDescriptor.getDefaultTransition() != null ? 1 : 0);
        final int takenTransition = transitionsDescriptor.getValidOutgoingTransitionDefinitions().size();
        /*
         * Why this condition?
         * If a gateway was blocked because it was waiting for a token to come it will not be unblock when all
         * transitions
         * are taken but only if some transitions are not.
         * In conclusion if all declared transition are not taken it means that a 'branch died' and that some inclusive
         * gateways might be triggered so the
         * reevaluation is needed
         */
        return takenTransition < allOutgoingTransitions;
    }

    @Override
    public SProcessInstance start(final long starterId, final long starterSubstituteId,
            final List<SOperation> operations, final Map<String, Object> context,
            final List<ConnectorDefinitionWithInputValues> connectorsWithInput, final FlowNodeSelector selector,
            final Map<String, Serializable> processInputs)
            throws SProcessInstanceCreationException, SContractViolationException {
        return start(starterId, starterSubstituteId, null, operations, context, connectorsWithInput, -1, selector,
                processInputs);
    }

    @Override
    /* Started by call activity and events, operations must be evaluated using the given context */
    public SProcessInstance start(final long processDefinitionId, final long targetSFlowNodeDefinitionId,
            final long starterId, final long starterSubstituteId,
            final SExpressionContext expressionContextToEvaluateOperations, final List<SOperation> operations,
            final long callerId, final long subProcessDefinitionId,
            final Map<String, Serializable> processInputs)
            throws SProcessInstanceCreationException, SContractViolationException {
        try {
            final SProcessDefinition sProcessDefinition = processDefinitionService
                    .getProcessDefinition(processDefinitionId);
            final FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition,
                    getFilter(targetSFlowNodeDefinitionId), subProcessDefinitionId);
            return start(starterId, starterSubstituteId, expressionContextToEvaluateOperations, operations, null, null,
                    callerId, selector, processInputs);
        } catch (final SProcessDefinitionNotFoundException | SBonitaReadException e) {
            throw new SProcessInstanceCreationException(e);
        }
    }

    private Filter<SFlowNodeDefinition> getFilter(final long targetSFlowNodeDefinitionId) {
        if (targetSFlowNodeDefinitionId == -1) {
            return new StartFlowNodeFilter();
        }
        return new FlowNodeIdFilter(targetSFlowNodeDefinitionId);
    }

    protected void initializeStringIndexes(final SProcessInstance sInstance, SProcessDefinition sProcessDefinition,
            final SFlowElementContainerDefinition processContainer) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException,
            SProcessInstanceModificationException {
        if (!sProcessDefinition.getProcessContainer().equals(processContainer)) {
            //we are not instantiating the process, we are starting an event subprocess
            return;
        }
        final SExpressionContext contextDependency = new SExpressionContext(sInstance.getId(),
                DataInstanceContainer.PROCESS_INSTANCE.name(),
                sProcessDefinition.getId());

        boolean update = false;
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        for (int i = 1; i <= 5; i++) {
            final SExpression value = sProcessDefinition.getStringIndexValue(i);
            if (value != null) {
                update = true;
                entityUpdateDescriptor.addField(SProcessInstance.STRING_INDEX_KEY + i,
                        String.valueOf(expressionResolverService.evaluate(value, contextDependency)));
            }
        }
        if (update) {
            processInstanceService.updateProcess(sInstance, entityUpdateDescriptor);
        }
    }

    protected SProcessInstance start(final long starterId, final long starterSubstituteId,
            final SExpressionContext expressionContextToEvaluateOperations,
            final List<SOperation> operations, final Map<String, Object> context,
            final List<ConnectorDefinitionWithInputValues> connectors,
            final long callerId, final FlowNodeSelector selector, final Map<String, Serializable> processInputs)
            throws SProcessInstanceCreationException,
            SContractViolationException {

        final SProcessDefinition sProcessDefinition = selector.getProcessDefinition();

        // Validate start process contract inputs:
        if (!selector.isEventSubProcess()) {
            validateContractInputs(processInputs, sProcessDefinition);
        }

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // so that event sub-process can trigger even if containing process definition is disabled:
            if (!selector.isEventSubProcess()) {
                ensureProcessIsEnabled(sProcessDefinition);
            }
            setProcessClassloader(sProcessDefinition);
            final SProcessInstance sProcessInstance = createProcessInstance(sProcessDefinition, starterId,
                    starterSubstituteId, callerId);
            final boolean isInitializing = initialize(starterId, sProcessDefinition, sProcessInstance,
                    expressionContextToEvaluateOperations,
                    operations, context, selector.getContainer(), connectors,
                    selector, processInputs);
            handleEventSubProcess(sProcessDefinition, sProcessInstance, selector.getSubProcessDefinitionId());

            if (isInitializing) {
                // some connectors were trigger
                processInstanceService.setState(sProcessInstance, ProcessInstanceState.INITIALIZING);
                // we stop execution here
                return sProcessInstance;
            }
            return startElements(sProcessInstance, selector);
        } catch (final SProcessInstanceCreationException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void ensureProcessIsEnabled(final SProcessDefinition sProcessDefinition)
            throws SBonitaReadException, SProcessDefinitionException {
        final SProcessDefinitionDeployInfo deployInfo = processDefinitionService
                .getProcessDeploymentInfo(sProcessDefinition.getId());
        if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
            throw new SProcessDefinitionException(
                    "The process " + deployInfo.getName() + " " + deployInfo.getVersion() + " is not enabled.",
                    deployInfo.getProcessId(), deployInfo.getName(), deployInfo.getVersion());
        }
    }

    private void setProcessClassloader(SProcessDefinition sProcessDefinition) throws SClassLoaderException {
        final ClassLoader localClassLoader = classLoaderService.getClassLoader(
                identifier(ScopeType.PROCESS, sProcessDefinition.getId()));
        Thread.currentThread().setContextClassLoader(localClassLoader);
        // initialize the process classloader by getting it one time
        try {
            localClassLoader.loadClass(this.getClass().getName());
        } catch (final ClassNotFoundException e) {
            // ignore, just to load
        }
    }

    protected void validateContractInputs(final Map<String, Serializable> processInputs,
            final SProcessDefinition sProcessDefinition)
            throws SContractViolationException {
        final SContractDefinition contractDefinition = sProcessDefinition.getContract();
        if (contractDefinition != null) {
            final ContractValidator validator = new ContractValidatorFactory()
                    .createContractValidator(expressionService);
            validator.validate(sProcessDefinition.getId(), contractDefinition, processInputs);
        }
    }

    /*
     * Execute connectors then execute output operation and disconnect connectors
     */
    protected void executeConnectors(final SProcessDefinition processDefinition,
            final SProcessInstance sProcessInstance,
            final List<ConnectorDefinitionWithInputValues> connectorsList)
            throws SConnectorException {
        final SExpressionContext expcontext = new SExpressionContext();
        expcontext.setProcessDefinitionId(processDefinition.getId());
        expcontext.setProcessDefinition(processDefinition);
        expcontext.setContainerId(sProcessInstance.getId());
        expcontext.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
        for (final ConnectorDefinitionWithInputValues connectorWithInput : connectorsList) {
            final ConnectorDefinition connectorDefinition = connectorWithInput.getConnectorDefinition();
            final Map<String, Map<String, Serializable>> contextInputValues = connectorWithInput.getInputValues();
            final String connectorId = connectorDefinition.getConnectorId();
            final String version = connectorDefinition.getVersion();
            final Map<String, Expression> inputs = connectorDefinition.getInputs();
            if (contextInputValues.size() != inputs.size()) {
                throw new SConnectorException("Invalid number of input parameters (expected " + inputs.size() + ", got "
                        + contextInputValues.size() + ")");
            }
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(inputs);

            // we use the context classloader because the process classloader is already set
            final ConnectorResult result = connectorService.executeMultipleEvaluation(processDefinition.getId(),
                    connectorId, version, connectorsExps,
                    contextInputValues, Thread.currentThread().getContextClassLoader(), expcontext);
            final List<Operation> outputs = connectorDefinition.getOutputs();
            connectorService.executeOutputOperation(ModelConvertor.convertOperations(outputs), expcontext, result);
        }
    }

    protected void handleEventSubProcess(final SProcessDefinition sProcessDefinition,
            final SProcessInstance sProcessInstance,
            final long subProcessDefinitionId)
            throws SBonitaException {
        if (subProcessDefinitionId == -1) {
            // modify that to support event sub-processes within sub-processes
            try {
                eventsHandler.handleEventSubProcess(sProcessDefinition, sProcessInstance);
            } catch (final SProcessInstanceCreationException e) {
                throw e;
            } catch (final SBonitaException e) {
                setExceptionContext(sProcessDefinition, sProcessInstance, e);
                throw new SProcessInstanceCreationException(
                        "Unable to register events for event sub process in process.", e);
            }
        }
    }

    @Override
    public SProcessInstance startElements(final SProcessInstance sProcessInstance, final FlowNodeSelector selector)
            throws SProcessInstanceCreationException,
            SFlowNodeExecutionException {
        try {
            contractDataService.archiveAndDeleteProcessData(sProcessInstance.getId(), System.currentTimeMillis());
        } catch (final SObjectModificationException e) {
            throw new SProcessInstanceCreationException(e);
        }
        final List<SFlowNodeInstance> flowNodeInstances = initializeFirstExecutableElements(sProcessInstance, selector);
        // process is initialized and now the engine trigger jobs to execute other activities, give the hand back
        ProcessInstanceState state;
        final int size = flowNodeInstances.size();
        if (size == 0) {
            state = ProcessInstanceState.COMPLETED;

        } else {
            state = ProcessInstanceState.STARTED;
        }
        try {
            processInstanceService.setState(sProcessInstance, state);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to set the state on the process.", e);
        }
        for (final SFlowNodeInstance sFlowNodeInstance : flowNodeInstances) {
            try {
                registerExecuteFlowNodeWork(sFlowNodeInstance);
            } catch (final SWorkRegisterException e) {
                setExceptionContext(sProcessInstance, sFlowNodeInstance, e);
                throw new SFlowNodeExecutionException("Unable to trigger execution of the flow node.", e);
            }
        }
        return sProcessInstance;
    }

    @Override
    public String getHandledType() {
        return SFlowElementsContainerType.PROCESS.name();
    }

    private void createAndExecuteActivities(final Long processDefinitionId, final SFlowNodeInstance flowNodeInstance,
            final long parentProcessInstanceId,
            final List<SFlowNodeDefinition> choosenActivityDefinitions, final long rootProcessInstanceId)
            throws SBonitaException {
        final SProcessInstance parentProcessInstance = processInstanceService
                .getProcessInstance(parentProcessInstanceId);
        final SStateCategory stateCategory = parentProcessInstance.getStateCategory();

        // Create Activities
        final List<SFlowNodeInstance> sFlowNodeInstances = bpmInstancesCreator.createFlowNodeInstances(
                processDefinitionId,
                flowNodeInstance.getRootContainerId(), flowNodeInstance.getParentContainerId(),
                choosenActivityDefinitions, rootProcessInstanceId,
                parentProcessInstanceId, stateCategory);

        // Execute Activities
        for (final SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
            registerExecuteFlowNodeWork(sFlowNodeInstance);
        }
    }

    private void registerExecuteFlowNodeWork(SFlowNodeInstance sFlowNodeInstance) throws SWorkRegisterException {
        workService
                .registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(sFlowNodeInstance));
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition,
            final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        setExceptionContext(sProcessDefinition, e);
        e.setProcessInstanceIdOnContext(sFlowNodeInstance.getParentProcessInstanceId());
        e.setRootProcessInstanceIdOnContext(sFlowNodeInstance.getRootProcessInstanceId());
        setExceptionContext(sFlowNodeInstance, e);
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition,
            final SProcessInstance sProcessInstance, final SBonitaException e) {
        setExceptionContext(sProcessDefinition, e);
        setExceptionContext(sProcessInstance, e);
    }

    private void setExceptionContext(final SProcessInstance sProcessInstance, final SFlowNodeInstance sFlowNodeInstance,
            final SBonitaException e) {
        e.setProcessDefinitionIdOnContext(sProcessInstance.getProcessDefinitionId());
        e.setProcessDefinitionNameOnContext(sProcessInstance.getName());
        setExceptionContext(sProcessInstance, e);
        setExceptionContext(sFlowNodeInstance, e);
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition, final SBonitaException e) {
        e.setProcessDefinitionIdOnContext(sProcessDefinition.getId());
        e.setProcessDefinitionNameOnContext(sProcessDefinition.getName());
        e.setProcessDefinitionVersionOnContext(sProcessDefinition.getVersion());
    }

    private void setExceptionContext(final SProcessInstance sProcessInstance, final SBonitaException e) {
        e.setProcessInstanceIdOnContext(sProcessInstance.getId());
        e.setRootProcessInstanceIdOnContext(sProcessInstance.getRootProcessInstanceId());
    }

    private void setExceptionContext(final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        e.setFlowNodeDefinitionIdOnContext(sFlowNodeInstance.getFlowNodeDefinitionId());
        e.setFlowNodeInstanceIdOnContext(sFlowNodeInstance.getId());
        e.setFlowNodeNameOnContext(sFlowNodeInstance.getName());
    }
}
