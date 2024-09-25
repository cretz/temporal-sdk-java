/*
 * Copyright (C) 2022 Temporal Technologies, Inc. All Rights Reserved.
 *
 * Copyright (C) 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this material except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.temporal.internal.testservice;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.temporal.api.common.v1.WorkflowExecution;
import java.io.*;
import java.util.Objects;
import javax.annotation.Nonnull;

public class NexusTaskToken {

  @Nonnull private final NexusOperationRef ref;
  private final int attempt;
  private final boolean isCancel;

  NexusTaskToken(
      @Nonnull String namespace,
      @Nonnull WorkflowExecution execution,
      long scheduledEventId,
      int attempt,
      boolean isCancel) {
    this(
        new ExecutionId(Objects.requireNonNull(namespace), Objects.requireNonNull(execution)),
        scheduledEventId,
        attempt,
        isCancel);
  }

  NexusTaskToken(
      @Nonnull String namespace,
      @Nonnull String workflowId,
      @Nonnull String runId,
      long scheduledEventId,
      int attempt,
      boolean isCancel) {
    this(
        namespace,
        WorkflowExecution.newBuilder()
            .setWorkflowId(Objects.requireNonNull(workflowId))
            .setRunId(Objects.requireNonNull(runId))
            .build(),
        scheduledEventId,
        attempt,
        isCancel);
  }

  NexusTaskToken(
      @Nonnull ExecutionId executionId, long scheduledEventId, int attempt, boolean isCancel) {
    this(
        new NexusOperationRef(Objects.requireNonNull(executionId), scheduledEventId),
        attempt,
        isCancel);
  }

  public NexusTaskToken(@Nonnull NexusOperationRef ref, int attempt, boolean isCancel) {
    this.ref = Objects.requireNonNull(ref);
    this.attempt = attempt;
    this.isCancel = isCancel;
  }

  public NexusOperationRef getOperationRef() {
    return ref;
  }

  public long getAttempt() {
    return attempt;
  }

  public boolean isCancel() {
    return isCancel;
  }

  /** Used for task tokens. */
  public ByteString toBytes() {
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout)) {
      ExecutionId executionId = ref.getExecutionId();
      out.writeUTF(executionId.getNamespace());
      WorkflowExecution execution = executionId.getExecution();
      out.writeUTF(execution.getWorkflowId());
      out.writeUTF(execution.getRunId());
      out.writeLong(ref.getScheduledEventId());
      out.writeInt(attempt);
      out.writeBoolean(isCancel);
      return ByteString.copyFrom(bout.toByteArray());
    } catch (IOException e) {
      throw Status.INTERNAL.withCause(e).withDescription(e.getMessage()).asRuntimeException();
    }
  }

  public static NexusTaskToken fromBytes(ByteString serialized) {
    ByteArrayInputStream bin = new ByteArrayInputStream(serialized.toByteArray());
    DataInputStream in = new DataInputStream(bin);
    try {
      String namespace = in.readUTF();
      String workflowId = in.readUTF();
      String runId = in.readUTF();
      long scheduledEventId = in.readLong();
      int attempt = in.readInt();
      boolean isCancel = in.readBoolean();
      return new NexusTaskToken(namespace, workflowId, runId, scheduledEventId, attempt, isCancel);
    } catch (IOException e) {
      throw Status.INVALID_ARGUMENT
          .withCause(e)
          .withDescription(e.getMessage())
          .asRuntimeException();
    }
  }
}