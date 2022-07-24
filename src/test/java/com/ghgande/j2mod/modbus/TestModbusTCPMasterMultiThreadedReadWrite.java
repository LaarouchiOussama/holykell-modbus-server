/*
 * Copyright 2002-2016 jamod & j2mod development teams
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
package com.ghgande.j2mod.modbus;

import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusTCPMaster;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class tests the TCP master multithreaded read and write features of the library
 */
@SuppressWarnings("ConstantConditions")
public class TestModbusTCPMasterMultiThreadedReadWrite extends AbstractTestModbusTCPMaster {

    private static final Logger logger = LoggerFactory.getLogger(TestModbusTCPMasterMultiThreadedReadWrite.class);

    enum TaskType {
        READ, WRITE, READWRITE
    }

    //    public static void main(String[] args) {
    @Test
    public void testReadWriteMaster() {
        ExecutorService execService = Executors.newFixedThreadPool(100);
        for (int i = 1; i <= 1000; i++) {
            execService.submit(new TestReadTask(TaskType.READ, i));
            execService.submit(new TestReadTask(TaskType.WRITE, i));
        }
        execService.shutdown();
        try {
            execService.awaitTermination(10, TimeUnit.MINUTES);
        }
        catch (Exception e) {
            logger.error("Problem shutting down");
        }
    }

    static class TestReadTask implements Runnable {

        TaskType type;
        int id;

        TestReadTask(TaskType type, int id) {
            this.type = type;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                if (type == TaskType.READ) {
                    InputRegister[] res = master.readInputRegisters(UNIT_ID, 0, 1);
                    if (res == null || res.length == 0) {
                        logger.error("No read response");
                    }
                    else if (res[0].getValue() != 45) {
                        logger.error("Incorrect value for register 0");
                    }
                    else {
                        logger.info("Successful read {}", id);
                    }
                }
                else if (type == TaskType.WRITE) {
                    int res = master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(45));
                    if (res != 45) {
                        logger.error("No write response");
                    }
                    else {
                        logger.info("Successful write {}", id);
                    }
                }
            }
            catch (Throwable e) {
                logger.error(e.getMessage());
            }
        }
    }
}
