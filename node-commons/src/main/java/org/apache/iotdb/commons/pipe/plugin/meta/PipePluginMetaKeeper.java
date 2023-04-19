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

package org.apache.iotdb.commons.pipe.plugin.meta;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PipePluginMetaKeeper {

  protected final Map<String, PipePluginMeta> pipeNameToPipeMetaMap;

  public PipePluginMetaKeeper() {
    pipeNameToPipeMetaMap = new ConcurrentHashMap<>();
  }

  public void addPipePluginMeta(String pluginName, PipePluginMeta pipePluginMeta) {
    pipeNameToPipeMetaMap.put(pluginName.toUpperCase(), pipePluginMeta);
  }

  public void removePipePluginMeta(String pluginName) {
    pipeNameToPipeMetaMap.remove(pluginName.toUpperCase());
  }

  public PipePluginMeta getPipePluginMeta(String pluginName) {
    return pipeNameToPipeMetaMap.get(pluginName.toUpperCase());
  }

  public PipePluginMeta[] getAllPipePluginMeta() {
    return pipeNameToPipeMetaMap.values().toArray(new PipePluginMeta[0]);
  }

  public boolean containsPipePlugin(String pluginName) {
    return pipeNameToPipeMetaMap.containsKey(pluginName.toUpperCase());
  }

  public void clear() {
    pipeNameToPipeMetaMap.clear();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipePluginMetaKeeper that = (PipePluginMetaKeeper) o;
    return pipeNameToPipeMetaMap.equals(that.pipeNameToPipeMetaMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipeNameToPipeMetaMap);
  }
}