/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.impl.consumer;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.common.ServiceThread;
import org.slf4j.Logger;

/**
 * 【入口】五、Message 拉取与消费（下 - Consumer） - 5、PushConsumer 消息队列分配
 *
 * 均衡消息队列服务，负责分配当前 Consumer 可消费的消息队列( MessageQueue )。
 * Rebalance Service
 * consumer负载均衡线程服务
 */
public class RebalanceService extends ServiceThread {

    /**
     * 等待间隔，单位：毫秒
     */
    private static long waitInterval =
        Long.parseLong(System.getProperty(
            "rocketmq.client.rebalance.waitInterval", "20000"));

    private final Logger log = ClientLogger.getLog();
    /**
     * MQClient对象
     */
    private final MQClientInstance mqClientFactory;

    public RebalanceService(MQClientInstance mqClientFactory) {
        this.mqClientFactory = mqClientFactory;
    }

    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            this.waitForRunning(waitInterval);
            // 调用 MQClientInstance#doRebalance(...) 分配消息队列。 <iii>
            /*
            目前有三种情况情况下触发：
            如果上一行 等待超时，每 20s 调用一次。
            PushConsumer 启动时，调用 rebalanceService#wakeup(...) 触发。
            Broker 通知 Consumer 加入 或 移除时，Consumer 响应通知，调用 rebalanceService#wakeup(...) 触发。
             */
            this.mqClientFactory.doRebalance();
        }

        log.info(this.getServiceName() + " service end");
    }

    @Override
    public String getServiceName() {
        return RebalanceService.class.getSimpleName();
    }
}
