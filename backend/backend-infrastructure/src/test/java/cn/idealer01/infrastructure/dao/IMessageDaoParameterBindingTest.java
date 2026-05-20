package cn.idealer01.infrastructure.dao;

import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class IMessageDaoParameterBindingTest {

    @Test
    public void queryPublishedPage_shouldExposeOffsetAndPageSizeParameterNames() throws Exception {
        Method method = IMessageDao.class.getMethod("queryPublishedPage", int.class, int.class);

        Map<?, ?> namedParams = (Map<?, ?>) new ParamNameResolver(new Configuration(), method)
                .getNamedParams(new Object[]{0, 10});

        assertTrue(namedParams.containsKey("offset"));
        assertTrue(namedParams.containsKey("pageSize"));
    }

    @Test
    public void queryAdminPage_shouldExposeStatusOffsetAndPageSizeParameterNames() throws Exception {
        Method method = IMessageDao.class.getMethod("queryAdminPage", String.class, int.class, int.class);

        Map<?, ?> namedParams = (Map<?, ?>) new ParamNameResolver(new Configuration(), method)
                .getNamedParams(new Object[]{"PUBLISHED", 0, 10});

        assertTrue(namedParams.containsKey("status"));
        assertTrue(namedParams.containsKey("offset"));
        assertTrue(namedParams.containsKey("pageSize"));
    }
}
