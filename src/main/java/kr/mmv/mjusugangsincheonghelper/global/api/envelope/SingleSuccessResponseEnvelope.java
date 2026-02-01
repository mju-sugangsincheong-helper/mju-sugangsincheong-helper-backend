package kr.mmv.mjusugangsincheonghelper.global.api.envelope;

import kr.mmv.mjusugangsincheonghelper.global.api.meta.ResponseMeta;
import kr.mmv.mjusugangsincheonghelper.global.api.support.MetaGenerator;
import lombok.Getter;

/**
 * 단일 성공 응답 봉투
 * - 페이징 정보 없이 데이터와 메타데이터만 포함
 * - T는 단일 객체이거나 List일 수 있음 (단, 페이징 메타가 없는 단순 리스트)
 */
@Getter
public class SingleSuccessResponseEnvelope<T> implements ResponseEnvelope {
    private final T data;
    private final ResponseMeta meta;

    private SingleSuccessResponseEnvelope(T data) {
        this.data = data;
        this.meta = MetaGenerator.auto();
    }

    public static <T> SingleSuccessResponseEnvelope<T> of(T data) {
        return new SingleSuccessResponseEnvelope<>(data);
    }
    
    /**
     * 데이터 없는 성공 응답 (예: 삭제 성공)
     */
    public static SingleSuccessResponseEnvelope<Void> empty() {
        return new SingleSuccessResponseEnvelope<>(null);
    }

    @Override
    public ResponseMeta getMeta() {
        return meta;
    }
}
