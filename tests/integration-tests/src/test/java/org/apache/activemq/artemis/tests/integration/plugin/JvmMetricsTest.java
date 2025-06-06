/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.activemq.artemis.tests.integration.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.Meter;
import org.apache.activemq.artemis.core.config.MetricsConfiguration;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.metrics.plugins.SimpleMetricsPlugin;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JvmMetricsTest extends ActiveMQTestBase {

   @Override
   @BeforeEach
   public void setUp() throws Exception {
      super.setUp();
   }

   @Test
   public void testJvmMemoryMetricsPositive() throws Exception {
      internalTestMetrics(true, true, false, false, "jvm.memory");
   }

   @Test
   public void testJvmMemoryMetricsNegative() throws Exception {
      internalTestMetrics(false, false, false, false, "jvm.memory");
   }

   @Test
   public void testJvmGcMetricsPositive() throws Exception {
      internalTestMetrics(true, false, true, false, "jvm.gc");
   }

   @Test
   public void testJvmGcMetricsNegative() throws Exception {
      internalTestMetrics(false, false, false, false, "jvm.gc");
   }

   @Test
   public void testJvmThreadMetricsPositive() throws Exception {
      internalTestMetrics(true, false, false, true, "jvm.thread");
   }

   @Test
   public void testJvmThreadMetricsNegative() throws Exception {
      internalTestMetrics(false, false, false, false, "jvm.thread");
   }

   private void internalTestMetrics(boolean found, boolean memory, boolean gc, boolean thread, String match) throws Exception {
      ActiveMQServer server = createServer(false, createDefaultInVMConfig()
         .setMetricsConfiguration(new MetricsConfiguration()
                                     .setPlugin(new SimpleMetricsPlugin().init(null))
                                     .setJvmMemory(memory)
                                     .setJvmGc(gc)
                                     .setJvmThread(thread)));
      server.start();
      boolean result = false;
      String brokerTagValue = "";
      for (Meter.Id meterId : MetricsPluginTest.getMetrics(server).keySet()) {
         if (meterId.getName().startsWith(match)) {
            result = true;
            brokerTagValue = meterId.getTag("broker");
            break;
         }
      }

      assertEquals(found, result);
      if (found) {
         assertEquals(brokerTagValue, server.getConfiguration().getName());
      }
   }
}
