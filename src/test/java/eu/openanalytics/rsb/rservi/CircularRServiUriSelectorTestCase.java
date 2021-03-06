/*
 *   R Service Bus
 *
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2015
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

package eu.openanalytics.rsb.rservi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.openanalytics.rsb.config.Configuration;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class CircularRServiUriSelectorTestCase
{
    private static final String TEST_APPLICATION_NAME = "appname";

    @Mock
    private Configuration configuration;

    private CircularRServiUriSelector selector;
    private URI defaultPoolUri;

    @Before
    public void setUp() throws URISyntaxException
    {
        selector = new CircularRServiUriSelector();
        selector.setConfiguration(configuration);

        defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getUriForApplicationDefaultOnly() throws URISyntaxException
    {
        when(configuration.getApplicationSpecificRserviPoolUris()).thenReturn(null);
        selector.initialize();
        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(defaultPoolUri));

        when(configuration.getApplicationSpecificRserviPoolUris()).thenReturn(Collections.EMPTY_MAP);
        selector.initialize();
        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(defaultPoolUri));
    }

    @Test
    public void getUriForApplicationSingleSpecificUri() throws URISyntaxException
    {
        final URI appSpecificPoolUri = new URI("fake://pool");
        when(configuration.getApplicationSpecificRserviPoolUris()).thenReturn(
            Collections.singletonMap(TEST_APPLICATION_NAME, Collections.singleton(appSpecificPoolUri)));

        selector.initialize();

        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(appSpecificPoolUri));
        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(appSpecificPoolUri));
    }

    @Test
    public void getUriForApplicationSeveralSpecificUris() throws URISyntaxException
    {
        final URI appSpecificPoolUri1 = new URI("fake://pool1");
        final URI appSpecificPoolUri2 = new URI("fake://pool2");
        when(configuration.getApplicationSpecificRserviPoolUris()).thenReturn(
            Collections.singletonMap(TEST_APPLICATION_NAME,
                (Set<URI>) new TreeSet<URI>(Arrays.asList(appSpecificPoolUri1, appSpecificPoolUri2))));

        selector.initialize();

        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(appSpecificPoolUri1));
        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(appSpecificPoolUri2));
        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(appSpecificPoolUri1));
        assertThat(selector.getUriForApplication(TEST_APPLICATION_NAME), is(appSpecificPoolUri2));
    }
}
