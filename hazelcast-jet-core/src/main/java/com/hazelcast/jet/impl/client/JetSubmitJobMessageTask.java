/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.impl.client;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.JetSubmitJobCodec;
import com.hazelcast.instance.Node;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.impl.operation.SubmitJobOperation;
import com.hazelcast.nio.Connection;
import com.hazelcast.spi.InternalCompletableFuture;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.Operation;

public class JetSubmitJobMessageTask extends AbstractJetMessageTask<JetSubmitJobCodec.RequestParameters> {
    protected JetSubmitJobMessageTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(clientMessage, node, connection, JetSubmitJobCodec::decodeRequest,
                o -> JetSubmitJobCodec.encodeResponse());
    }

    @Override
    protected Operation prepareOperation() {
        JobConfig jobConfig = nodeEngine.getSerializationService().toObject(parameters.jobConfig);
        return new SubmitJobOperation(parameters.jobId, parameters.dag, jobConfig);
    }

    @Override
    protected void processMessage() {
        Operation op = prepareOperation();
        op.setCallerUuid(getEndpoint().getUuid());
        InvocationBuilder builder = getInvocationBuilder(op).setResultDeserialized(false);

        InternalCompletableFuture<Object> invocation = builder.invoke();
        getJetService().getClientInvocationRegistry().register(parameters.jobId, invocation);
        invocation.andThen(this);
    }

    @Override
    public String getMethodName() {
        return "execute";
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{};
    }

    @Override
    public void onResponse(Object response) {
        super.onResponse(response);
    }

    @Override
    public void onFailure(Throwable t) {
        super.onFailure(t);
    }
}