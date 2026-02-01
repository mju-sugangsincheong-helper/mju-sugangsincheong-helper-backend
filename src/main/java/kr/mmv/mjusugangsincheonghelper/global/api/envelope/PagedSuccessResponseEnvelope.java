package kr.mmv.mjusugangsincheonghelper.global.api.envelope;

import lombok.Getter;
import org.springframework.data.domain.Page;

import kr.mmv.mjusugangsincheonghelper.global.api.meta.PageMeta;
import kr.mmv.mjusugangsincheonghelper.global.api.meta.ResponseMeta;
import kr.mmv.mjusugangsincheonghelper.global.api.support.MetaGenerator;

import java.util.List;

/**
 * 페이징 성공 응답 봉투
 * - 페이징된 데이터 리스트와 페이지 메타데이터 포함
 */
@Getter
public class PagedSuccessResponseEnvelope<T> implements ResponseEnvelope {
    private final List<T> data;
    private final PageMeta pageMeta;
    private final ResponseMeta meta;

    private PagedSuccessResponseEnvelope(List<T> data, PageMeta pageMeta) {
        this.data = data;
        this.pageMeta = pageMeta;
        this.meta = MetaGenerator.auto();
    }

    public static <T> PagedSuccessResponseEnvelope<T> from(Page<T> page) {
        PageMeta pageMeta = PageMeta.of(page);
        return new PagedSuccessResponseEnvelope<>(page.getContent(), pageMeta);
    }

    @Override
    public ResponseMeta getMeta() {
        return meta;
    }
}
