package co.com.crediya.r2dbc.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@UtilityClass
public class PaginationUtils {

    public static <T> PageImpl<T> buildPageImpl(List<T> content, int pageNumber, int pageSize, long total) {
        return new PageImpl<>(content, PageRequest.of(pageNumber - 1, pageSize), total);
    }

}
