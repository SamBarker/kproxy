/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.filter.encryption;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.kroxylicious.filter.encryption.config.RecordField;
import io.kroxylicious.filter.encryption.encrypt.EncryptionScheme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncryptionSchemeTest {

    @Test
    void shouldRejectInvalidConstructorArgs() {
        EnumSet<RecordField> nonEmpty = EnumSet.of(RecordField.RECORD_VALUE);
        var empty = EnumSet.noneOf(RecordField.class);
        assertThrows(NullPointerException.class, () -> new EncryptionScheme<>(null, nonEmpty));
        Object kekId = new Object();
        assertThrows(NullPointerException.class, () -> new EncryptionScheme<>(kekId, null));
        assertThrows(IllegalArgumentException.class, () -> new EncryptionScheme<>(kekId, empty));
    }

    @Test
    void shouldAcceptValidConstructorArgs() {
        EnumSet<RecordField> nonEmpty = EnumSet.of(RecordField.RECORD_VALUE);
        Object kekId = new Object();
        var es = new EncryptionScheme<>(kekId, nonEmpty);
        assertEquals(kekId, es.kekId());
        assertEquals(nonEmpty, es.recordFields());
    }

    @ParameterizedTest
    @EnumSource(RecordField.class)
    void shouldReturnTrueForTargetedRecordField(RecordField targeted) {
        // Given
        Object kekId = new Object();
        final EncryptionScheme<Object> encryptionScheme = new EncryptionScheme<>(kekId, EnumSet.allOf(RecordField.class));

        // When
        final boolean actual = encryptionScheme.targets(targeted);

        // Then
        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @EnumSource(RecordField.class)
    void shouldReturnFalseForNonTargetedRecordField(RecordField targeted) {
        // Given
        Object kekId = new Object();
        final EnumSet<RecordField> supported = EnumSet.allOf(RecordField.class);
        supported.remove(targeted);
        final EncryptionScheme<Object> encryptionScheme = new EncryptionScheme<>(kekId, supported);

        // When
        final boolean actual = encryptionScheme.targets(targeted);

        // Then
        assertThat(actual).isFalse();
    }
}
