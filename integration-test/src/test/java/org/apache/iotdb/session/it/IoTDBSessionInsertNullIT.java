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
package org.apache.iotdb.session.it;

import org.apache.iotdb.isession.ISession;
import org.apache.iotdb.isession.SessionDataSet;
import org.apache.iotdb.it.env.EnvFactory;
import org.apache.iotdb.it.env.cluster.node.DataNodeWrapper;
import org.apache.iotdb.it.framework.IoTDBTestRunner;
import org.apache.iotdb.itbase.category.ClusterIT;
import org.apache.iotdb.itbase.category.LocalStandaloneIT;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;

import org.apache.tsfile.enums.TSDataType;
import org.apache.tsfile.file.metadata.enums.CompressionType;
import org.apache.tsfile.file.metadata.enums.TSEncoding;
import org.apache.tsfile.read.common.Field;
import org.apache.tsfile.read.common.RowRecord;
import org.apache.tsfile.write.record.Tablet;
import org.apache.tsfile.write.schema.MeasurementSchema;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@RunWith(IoTDBTestRunner.class)
@Category({LocalStandaloneIT.class, ClusterIT.class})
public class IoTDBSessionInsertNullIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(IoTDBSessionInsertNullIT.class);
  private final int retry = 30;

  @Before
  public void setUp() throws Exception {
    EnvFactory.getEnv().initClusterEnvironment();
  }

  @After
  public void tearDown() throws Exception {
    EnvFactory.getEnv().cleanClusterEnvironment();
  }

  private void prepareData(ISession session)
      throws IoTDBConnectionException, StatementExecutionException {
    session.setStorageGroup("root.sg1");
    session.createTimeseries(
        "root.sg1.clsu.d1.s1", TSDataType.BOOLEAN, TSEncoding.PLAIN, CompressionType.SNAPPY);
    session.createTimeseries(
        "root.sg1.clsu.d1.s2", TSDataType.INT32, TSEncoding.PLAIN, CompressionType.SNAPPY);
    session.createTimeseries(
        "root.sg1.clsu.d1.s3", TSDataType.INT64, TSEncoding.PLAIN, CompressionType.SNAPPY);
    session.createTimeseries(
        "root.sg1.clsu.d1.s4", TSDataType.FLOAT, TSEncoding.PLAIN, CompressionType.SNAPPY);
    session.createTimeseries(
        "root.sg1.clsu.d1.s5", TSDataType.DOUBLE, TSEncoding.PLAIN, CompressionType.SNAPPY);
    session.createTimeseries(
        "root.sg1.clsu.d1.s6", TSDataType.TEXT, TSEncoding.PLAIN, CompressionType.SNAPPY);
    session.createTimeseries(
        "root.sg1.clsu.d2.s1", TSDataType.BOOLEAN, TSEncoding.PLAIN, CompressionType.SNAPPY);
  }

  private long queryCountRecords(ISession session, String sql)
      throws StatementExecutionException, IoTDBConnectionException {
    SessionDataSet dataSetWrapper = session.executeQueryStatement(sql, 60_000);
    long count = 0;
    while (dataSetWrapper.hasNext()) {
      RowRecord record = dataSetWrapper.next();
      Field field = record.getFields().get(0);
      switch (field.getDataType()) {
        case INT32:
          count = field.getIntV();
          break;
        case INT64:
          count = field.getLongV();
          break;
      }
    }
    return count;
  }

  @Test
  public void insertRecordNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);

      String deviceId = "root.sg1.clsu.d1";
      session.insertRecord(deviceId, 100, Arrays.asList("s1"), Arrays.asList("true"));
      List<String> t = new ArrayList<>();
      t.add(null);
      session.insertRecord(deviceId, 200, Arrays.asList("s1"), t);
      session.insertRecord(
          deviceId,
          300,
          Arrays.asList("s1", "s2"),
          Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
          Arrays.asList(true, 30));
      session.insertRecord(
          deviceId,
          400,
          Arrays.asList("s1", "s2"),
          Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
          Arrays.asList(true, null));
      session.insertRecord(
          deviceId,
          500,
          Arrays.asList("s1", "s2"),
          Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
          Arrays.asList(null, null));
      long nums = queryCountRecords(session, "select count(s1) from " + deviceId);
      assertEquals(3, nums);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertAlignedRecordNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);

      String deviceId = "root.sg1.clsu.aligned_d1";
      session.insertAlignedRecord(deviceId, 100, Arrays.asList("s1"), Arrays.asList("true"));
      List<String> t = new ArrayList<>();
      t.add(null);
      session.insertAlignedRecord(deviceId, 200, Arrays.asList("s1"), t);
      session.insertAlignedRecord(
          deviceId,
          300,
          Arrays.asList("s1", "s2"),
          Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
          Arrays.asList(true, 30));
      session.insertAlignedRecord(
          deviceId,
          400,
          Arrays.asList("s1", "s2"),
          Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
          Arrays.asList(true, null));
      session.insertAlignedRecord(
          deviceId,
          500,
          Arrays.asList("s1", "s2"),
          Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
          Arrays.asList(null, null));
      for (int i = 0; i < retry; i++) {
        try {
          long nums = queryCountRecords(session, "select count(s1) from " + deviceId);
          assertEquals(3, nums);
          return;
        } catch (Exception e) {
          LOGGER.info("query records failed, retry: " + i);
          Thread.sleep(1000);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertRecordsNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);

      String deviceId1 = "root.sg1.clsu.d2";
      String deviceId2 = "root.sg1.clsu.d3";
      session.insertRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(300L, 300L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, 101), Arrays.asList(false, 201)));
      session.insertRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(200L, 200L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList("false", "101"), Arrays.asList("true", "201")));
      session.insertRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(400L, 400L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList(null, "102"), Arrays.asList("false", "202")));
      session.insertRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(500L, 500L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, null), Arrays.asList(null, null)));

      for (int i = 0; i < retry; i++) {
        try {
          long nums = queryCountRecords(session, "select count(s1) from " + deviceId1);
          assertEquals(3, nums);
          nums = queryCountRecords(session, "select count(s2) from " + deviceId2);
          assertEquals(3, nums);
          return;
        } catch (Exception e) {
          LOGGER.info("read records failed, retry: " + i);
          Thread.sleep(1000);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertAlignedRecordsNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);
      String deviceId1 = "root.sg1.clsu.aligned_d2";
      String deviceId2 = "root.sg1.clsu.aligned_d3";
      session.insertAlignedRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(300L, 300L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, 101), Arrays.asList(false, 201)));
      session.insertAlignedRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(200L, 200L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList("false", "101"), Arrays.asList("true", "201")));
      session.insertAlignedRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(400L, 400L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList(null, "102"), Arrays.asList("false", "202")));
      session.insertAlignedRecords(
          Arrays.asList(deviceId1, deviceId2),
          Arrays.asList(500L, 500L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, null), Arrays.asList(null, null)));
      long nums = queryCountRecords(session, "select count(s1) from " + deviceId1);
      assertEquals(3, nums);
      nums = queryCountRecords(session, "select count(s2) from " + deviceId2);
      assertEquals(3, nums);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertRecordsOfOneDeviceNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);
      String deviceId1 = "root.sg1.clsu.InsertRecordsOfOneDevice";
      session.insertRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(300L, 301L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, 101), Arrays.asList(false, 201)));
      session.insertStringRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(200L, 201L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList("false", "101"), Arrays.asList("true", "201")));
      session.insertStringRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(400L, 401L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList(null, "102"), Arrays.asList("false", "202")));
      session.insertRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(500L, 501L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, null), Arrays.asList(null, null)));
      long nums = queryCountRecords(session, "select count(s1) from " + deviceId1);
      assertEquals(6, nums);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertAlignedRecordsOfOneDeviceNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);
      String deviceId1 = "root.sg1.clsu.InsertAlignedRecordsOfOneDevice";
      session.insertAlignedRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(300L, 301L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, 101), Arrays.asList(false, 201)));
      session.insertAlignedStringRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(200L, 201L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList("false", "101"), Arrays.asList("true", "201")));
      session.insertAlignedStringRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(400L, 401L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(Arrays.asList(null, "102"), Arrays.asList("false", "202")));
      session.insertAlignedRecordsOfOneDevice(
          deviceId1,
          Arrays.asList(500L, 501L),
          Arrays.asList(Arrays.asList("s1", "s2"), Arrays.asList("s1", "s2")),
          Arrays.asList(
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32),
              Arrays.asList(TSDataType.BOOLEAN, TSDataType.INT32)),
          Arrays.asList(Arrays.asList(true, null), Arrays.asList(null, null)));
      long nums = queryCountRecords(session, "select count(s1) from " + deviceId1);
      assertEquals(6, nums);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertTabletNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);

      String deviceId = "root.sg1.clsu.d1";
      Tablet tablet =
          new Tablet(
              deviceId,
              Arrays.asList(
                  new MeasurementSchema("s1", TSDataType.BOOLEAN),
                  new MeasurementSchema("s2", TSDataType.INT32)),
              3);
      tablet.addTimestamp(0, 300);
      tablet.addValue("s1", 0, null);
      tablet.addValue("s2", 0, null);
      tablet.addTimestamp(1, 400);
      tablet.addValue("s1", 1, null);
      tablet.addValue("s2", 1, null);
      tablet.addTimestamp(2, 500);
      tablet.addValue("s1", 2, null);
      tablet.addValue("s2", 2, null);
      session.insertTablet(tablet);
      long nums = queryCountRecords(session, "select count(s1) from " + deviceId);
      assertEquals(0, nums);
      session.executeNonQueryStatement("flush");
      nums = queryCountRecords(session, "select count(s1) from " + deviceId);
      assertEquals(0, nums);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertAlignedTabletNullTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      prepareData(session);

      String deviceId = "root.sg1.clsu.aligned_d1";
      Tablet tablet =
          new Tablet(
              deviceId,
              Arrays.asList(
                  new MeasurementSchema("s1", TSDataType.BOOLEAN),
                  new MeasurementSchema("s2", TSDataType.INT32)),
              3);
      tablet.addTimestamp(0, 300);
      tablet.addValue("s1", 0, null);
      tablet.addValue("s2", 0, null);
      tablet.addTimestamp(1, 400);
      tablet.addValue("s1", 1, null);
      tablet.addValue("s2", 1, null);
      tablet.addTimestamp(2, 500);
      tablet.addValue("s1", 2, null);
      tablet.addValue("s2", 2, null);
      session.insertAlignedTablet(tablet);
      long nums = queryCountRecords(session, "select count(s1) from " + deviceId);
      assertEquals(0, nums);
      session.executeNonQueryStatement("flush");
      nums = queryCountRecords(session, "select count(s1) from " + deviceId);
      assertEquals(0, nums);
      for (DataNodeWrapper dn : EnvFactory.getEnv().getDataNodeWrapperList()) {
        File dir =
            new File(
                dn.getDataDir()
                    + File.separator
                    + "datanode"
                    + File.separator
                    + "data"
                    + File.separator
                    + "sequence"
                    + File.separator
                    + "root.sg1"
                    + File.separator
                    + "1"
                    + File.separator
                    + "0");
        if (dir.exists() && dir.isDirectory()) {
          File[] files = dir.listFiles();
          if (files != null) {
            for (File file : files) {
              assertFalse(file.getName().endsWith("broken"));
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void insertTabletNullMeasurementTest() {
    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      String deviceId = "root.sg1.clsu.aligned_d1";
      Tablet tablet =
          new Tablet(
              deviceId,
              Arrays.asList(
                  new MeasurementSchema("s1", TSDataType.BOOLEAN),
                  new MeasurementSchema(null, TSDataType.INT32)),
              1);
      tablet.addTimestamp(0, 300);
      tablet.addValue("s1", 0, true);
      tablet.addValue(null, 0, 1);
      session.insertAlignedTablet(tablet);
      fail();
    } catch (Exception e) {
      Assert.assertEquals("measurement should be non null value", e.getMessage());
    }

    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      String deviceId = "root.sg1.clsu.aligned_d1";
      Tablet tablet =
          new Tablet(
              deviceId,
              Arrays.asList(
                  new MeasurementSchema("s1", TSDataType.BOOLEAN),
                  new MeasurementSchema(null, TSDataType.INT32)),
              1);
      tablet.addTimestamp(0, 300);
      tablet.addValue(0, 0, true);
      tablet.addValue(0, 1, 1);
      session.insertAlignedTablet(tablet);
      fail();
    } catch (Exception e) {
      Assert.assertEquals("measurement should be non null value", e.getMessage());
    }

    try (ISession session = EnvFactory.getEnv().getSessionConnection()) {
      String deviceId = "root.sg1.clsu.aligned_d1";
      Tablet tablet =
          new Tablet(
              deviceId,
              Arrays.asList(
                  new MeasurementSchema("s1", TSDataType.BOOLEAN),
                  new MeasurementSchema(null, TSDataType.INT32)),
              1);
      tablet.addTimestamp(0, 300);
      tablet.addValue("s1", 0, true);
      // doesn't insert 2nd measurement
      session.insertAlignedTablet(tablet);
      fail();
    } catch (Exception e) {
      Assert.assertEquals("measurement should be non null value", e.getMessage());
    }
  }
}
