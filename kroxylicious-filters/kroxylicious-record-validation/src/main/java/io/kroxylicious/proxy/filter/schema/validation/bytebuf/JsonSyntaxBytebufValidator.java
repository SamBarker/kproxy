/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.filter.schema.validation.bytebuf;

import java.nio.ByteBuffer;

import org.apache.kafka.common.record.Record;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kroxylicious.proxy.filter.schema.validation.Result;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Checks if a Record's value is well-formed JSON, optionally checking if
 * Object keys are unique. Object key uniqueness is not a hard requirement
 * in the spec but some consumer implementations may expect them to be unique.
 */
class JsonSyntaxBytebufValidator implements BytebufValidator {
    private final boolean validateObjectKeysUnique;
    private final Logger log = getLogger(JsonSyntaxBytebufValidator.class);
    static final ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    JsonSyntaxBytebufValidator(boolean validateObjectKeysUnique) {
        this.validateObjectKeysUnique = validateObjectKeysUnique;
    }

    @Override
    public Result validate(ByteBuffer buffer, int size, Record kafkaRecord, boolean isKey) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer is null");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size is less than 1");
        }
        final JsonFactory factory = mapper.getFactory();
        try (JsonParser parser = factory.createParser(buffer.array(), buffer.position(), Math.min(size, buffer.limit()))) {
            if (validateObjectKeysUnique) {
                parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
            }

            var ignored = parser.nextToken();
            while (ignored != null) {
                ignored = parser.nextToken();
                //Iterating all tokens to ensure it parses, however we don't care about the tokens themselves.
            }
            return Result.VALID;
        }
        catch (Exception e) {
            String message = "value was not syntactically correct JSON" + (e.getMessage() != null ? ": " + e.getMessage() : "");
            log.warn(message, e);
            return new Result(false, message);
        }
    }

}
