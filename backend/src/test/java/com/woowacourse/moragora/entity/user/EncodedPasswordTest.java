package com.woowacourse.moragora.entity.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.woowacourse.moragora.util.CryptoEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EncodedPasswordTest {

    @DisplayName("인코딩 되지 않은 비밀번호를 암호화하여 EncodedPassword를 생성한다.")
    @Test
    void fromRawValue() {
        // given
        String plainValue = "1234smart!";

        // when
        final EncodedPassword encodedPassword = EncodedPassword.fromRawValue(plainValue);

        // then
        assertThat(encodedPassword.getValue()).isEqualTo(CryptoEncoder.encrypt(plainValue));
    }
}
