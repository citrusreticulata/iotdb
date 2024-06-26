/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.pipe.api.customizer.parameter;

import org.apache.iotdb.pipe.api.exception.PipeAttributeNotProvidedException;
import org.apache.iotdb.pipe.api.exception.PipeParameterNotValidException;

import java.util.Arrays;

public class PipeParameterValidator {

  private final PipeParameters parameters;

  public PipeParameterValidator(final PipeParameters parameters) {
    this.parameters = parameters;
  }

  public PipeParameters getParameters() {
    return parameters;
  }

  /**
   * Validates whether the attributes entered by the user contain an attribute whose key is
   * attributeKey.
   *
   * @param key key of the attribute
   * @throws PipeAttributeNotProvidedException if the attribute is not provided
   */
  public PipeParameterValidator validateRequiredAttribute(final String key)
      throws PipeAttributeNotProvidedException {
    if (!parameters.hasAttribute(key)) {
      throw new PipeAttributeNotProvidedException(key);
    }
    return this;
  }

  public PipeParameterValidator validateAttributeValueRange(
      final String key, final boolean canBeOptional, final String... optionalValues)
      throws PipeAttributeNotProvidedException {
    if (!parameters.hasAttribute(key)) {
      if (!canBeOptional) {
        throw new PipeParameterNotValidException(String.format("Parameter %s should be set.", key));
      }
      return this;
    }

    final String actualValue = parameters.getStringByKeys(key);
    for (String optionalValue : optionalValues) {
      if (actualValue.equals(optionalValue)) {
        return this;
      }
    }

    throw new PipeParameterNotValidException(
        String.format("The value of %s should be one of %s", key, Arrays.toString(optionalValues)));
  }

  /**
   * Validates the input parameters according to the validation rule given by the user.
   *
   * @param validationRule the validation rule, which can be a lambda expression
   * @param messageToThrow the message to throw when the given argument is not valid
   * @param argument the given argument
   * @throws PipeParameterNotValidException if the given argument is not valid
   */
  public PipeParameterValidator validate(
      final PipeParameterValidator.SingleObjectValidationRule validationRule,
      final String messageToThrow,
      final Object argument)
      throws PipeParameterNotValidException {
    if (!validationRule.validate(argument)) {
      throw new PipeParameterNotValidException(messageToThrow);
    }
    return this;
  }

  public interface SingleObjectValidationRule {

    boolean validate(final Object arg);
  }

  /**
   * Validates the input parameters according to the validation rule given by the user.
   *
   * @param validationRule the validation rule, which can be a lambda expression
   * @param messageToThrow the message to throw when the given arguments are not valid
   * @param arguments the given arguments
   * @throws PipeParameterNotValidException if the given arguments are not valid
   */
  public PipeParameterValidator validate(
      final PipeParameterValidator.MultipleObjectsValidationRule validationRule,
      final String messageToThrow,
      final Object... arguments)
      throws PipeParameterNotValidException {
    if (!validationRule.validate(arguments)) {
      throw new PipeParameterNotValidException(messageToThrow);
    }
    return this;
  }

  public interface MultipleObjectsValidationRule {

    boolean validate(final Object... args);
  }
}
