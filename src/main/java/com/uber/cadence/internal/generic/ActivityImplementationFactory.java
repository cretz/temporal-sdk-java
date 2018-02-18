/*
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package com.uber.cadence.internal.generic;

import com.uber.cadence.ActivityType;
import com.uber.cadence.internal.worker.ActivityExecutionException;

public interface ActivityImplementationFactory {

    ActivityImplementation getActivityImplementation(ActivityType activityType);

    /**
     * @return true if there is at least one activity type that factory can create implementation of.
     */
    boolean isAnyTypeSupported();

    /**
     * Used by a low level worker code that is not aware about DataConverter to serialize unexpected exceptions.
     */
    ActivityExecutionException serializeUnexpectedFailure(Throwable e);
}