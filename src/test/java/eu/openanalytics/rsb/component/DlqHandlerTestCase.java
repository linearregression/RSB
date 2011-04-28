/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.openanalytics.rsb.component;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;

import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.MessageDispatcher;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@RunWith(MockitoJUnitRunner.class)
public class DlqHandlerTestCase {

    private DlqHandler dlqHandler;

    @Mock
    private MessageDispatcher messageDispatcher;
    @Mock
    private MessageSource messageSource;

    @Before
    public void prepareTest() {
        dlqHandler = new DlqHandler();
        dlqHandler.setMessageDispatcher(messageDispatcher);
        dlqHandler.setMessages(messageSource);
    }

    @Test
    public void handleAbstractJob() throws IOException {
        final AbstractJob job = mock(AbstractJob.class);
        when(job.getApplicationName()).thenReturn("app_name");
        @SuppressWarnings("unchecked")
        final AbstractResult<Object> result = mock(AbstractResult.class);
        when(job.buildErrorResult(any(Throwable.class), eq(messageSource))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return result;
            }
        });

        when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class))).thenReturn("fake err msg");

        dlqHandler.handle(job);

        verify(messageDispatcher).dispatch(any(AbstractResult.class));
    }

    @Test
    public void handleAbstractResult() {
        final AbstractResult<?> result = mock(AbstractResult.class);
        dlqHandler.handle(result);
    }
}
