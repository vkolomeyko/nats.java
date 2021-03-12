// Copyright 2020 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.examples;

import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.impl.ConsumerConfiguration;
import io.nats.client.impl.ConsumerInfo;
import io.nats.client.impl.StreamConfiguration;

import java.util.List;

import static io.nats.examples.NatsJsUtils.printConsumerInfoList;
import static io.nats.examples.NatsJsUtils.printObject;

/**
 * This example will demonstrate JetStream management (admin) api.
 *
 * Usage: java NatsJsManageConsumers [server]
 *   Use tls:// or opentls:// to require tls, via the Default SSLContext
 *   Set the environment variable NATS_NKEY to use challenge response authentication by setting a file containing your private key.
 *   Set the environment variable NATS_CREDS to use JWT/NKey authentication by setting a file containing your user creds.
 *   Use the URL for user/pass/token authentication.
 */
public class NatsJsManageConsumers {
    private static final String STREAM = "con-stream";
    private static final String SUBJECT = "con-subject";
    private static final String DURABLE1 = "con-durable1";
    private static final String DURABLE2 = "con-durable2";
    private static final String DELIVER = "con-deliver";

    public static void main(String[] args) {

        try (Connection nc = Nats.connect(ExampleUtils.createExampleOptions(args))) {
            // Create a JetStreamManagement context.
            JetStreamManagement jsm = nc.jetStreamManagement();

            // Create (add) a stream with a subject
            System.out.println("\n----------\n1. Configure And Add Stream 1");
            StreamConfiguration streamConfig = StreamConfiguration.builder()
                    .name(STREAM)
                    .subjects(SUBJECT)
                    .storageType(StreamConfiguration.StorageType.Memory)
                    .build();
            jsm.addStream(streamConfig);

            // 1. Add Consumers
            System.out.println("\n----------\n1. Configure And Add Consumers");
            ConsumerConfiguration cc = ConsumerConfiguration.builder()
                    .durable(DURABLE1) // durable name is required when creating consumers
                    .deliverSubject(DELIVER)
                    .build();
            ConsumerInfo ci = jsm.addOrUpdateConsumer(STREAM, cc);
            printObject(ci);

            cc = ConsumerConfiguration.builder()
                    .durable(DURABLE2)
                    .build();
            ci = jsm.addOrUpdateConsumer(STREAM, cc);
            printObject(ci);

            // 2. Get information on consumers
            // 2.1 get a list of all consumers
            // 2.2 get a list of ConsumerInfo's for all consumers
            System.out.println("----------\n2.1 getConsumerNames");
            List<String> consumerNames = jsm.getConsumerNames(STREAM);
            printObject(consumerNames);

            System.out.println("----------\n2.2 getConsumers");
            List<ConsumerInfo> consumers = jsm.getConsumers(STREAM);
            printConsumerInfoList(consumers);

            // 3 Delete consumers
            // Subsequent calls to deleteStream will throw a
            // JetStreamApiException "consumer not found (404)"
            System.out.println("----------\n3. Delete consumers");
            jsm.deleteConsumer(STREAM, DURABLE1);
            consumerNames = jsm.getConsumerNames(STREAM);
            printObject(consumerNames);

            System.out.println("----------\n");
        }
        catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
