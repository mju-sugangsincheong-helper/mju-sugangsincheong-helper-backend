package kr.mmv.mjusugangsincheonghelper.global.api.envelope;

import kr.mmv.mjusugangsincheonghelper.global.api.meta.ResponseMeta;

/**
 * 모든 응답은 메타데이터 필수 포함
 */
public interface ResponseEnvelope {
    ResponseMeta getMeta();
}
