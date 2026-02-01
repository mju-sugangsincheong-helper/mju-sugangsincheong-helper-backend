package kr.mmv.mjusugangsincheonghelper.global.api.envelope;

import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.ErrorDetail;
import kr.mmv.mjusugangsincheonghelper.global.api.meta.ResponseMeta;
import kr.mmv.mjusugangsincheonghelper.global.api.support.MetaGenerator;
import lombok.Getter;

/**
 * 에러 봉투: 에러 상세 정보를 담음
 */
@Getter
public class ErrorResponseEnvelope implements ResponseEnvelope {
    private final ErrorDetail error;
    private final ResponseMeta meta;

    private ErrorResponseEnvelope(ErrorDetail error) {
        this.error = error;
        this.meta = MetaGenerator.auto();
    }

    public static ErrorResponseEnvelope of(ErrorCode code) {
        return new ErrorResponseEnvelope(ErrorDetail.of(code));
    }

    public static ErrorResponseEnvelope of(ErrorCode code, Object details) {
        return new ErrorResponseEnvelope(ErrorDetail.of(code, details));
    }

    @Override
    public ResponseMeta getMeta() {
        return meta;
    }
}
