/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gravitee.custom;

import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.BufferedReadWriteStream;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.SimpleReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequestContent;
import org.gravitee.custom.configuration.TestPolicyConfiguration;
import org.gravitee.custom.db.DatabaseHandler;
import org.json.JSONObject;
import org.json.XML;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@SuppressWarnings("unused")
public class TestPolicy {

    private final TestPolicyConfiguration testPolicyConfiguration;

    public TestPolicy(TestPolicyConfiguration testPolicyConfiguration) {
        this.testPolicyConfiguration = testPolicyConfiguration;
    }

    @OnRequestContent
    public ReadWriteStream onRequestContent(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) throws Exception {
        TestPolicy instance = this;

        return new BufferedReadWriteStream() {
            private Buffer buffer;

            @Override
            public SimpleReadWriteStream<Buffer> write(Buffer content) {
                if (buffer == null) {
                    buffer = Buffer.buffer();
                }

                buffer.appendBuffer(content);
                return this;
            }

            @Override
            public void end() {
                if (buffer != null) {
                    String body = buffer.toString();
                    JSONObject json = calculateResult(body);
                    encoded(json);
                    getAndPutText(json);
                    body = TestPolicy.jsonToXml(json);
                    super.write(Buffer.buffer(body));
                }
                super.end();
            }
        };
    }

    private void getAndPutText(JSONObject object) {
        int id = object.getInt("Id");
        String text = DatabaseHandler.getById(id);
        object.put("Text from DB", text);
    }

    private static JSONObject calculateResult(String json) {
        JSONObject object = new JSONObject(json);
        double a = object.getDouble("A");
        double b = object.getDouble("B");

        Double result = Math.pow(a, b);
        object.put("Result", result);

        return object;
    }

    private void encoded(JSONObject jsonObject) {
        String str = jsonObject.getString("Text_ru");
        String newStr = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(512);
            KeyPair keyPair = generator.generateKeyPair();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] data = cipher.doFinal(str.getBytes());
            newStr = Base64.getEncoder().encodeToString(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonObject.put("Text_ru", newStr);
    }

    private static String jsonToXml(JSONObject jsonObject) {
        return XML.toString(jsonObject);
    }
}
